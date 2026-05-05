package com.tracker.expensetracker.service;

import com.tracker.expensetracker.dto.TransactionDTO;
import com.tracker.expensetracker.model.Category;
import com.tracker.expensetracker.model.Transaction;
import com.tracker.expensetracker.model.TransactionType;
import com.tracker.expensetracker.repository.CategoryRepository;
import com.tracker.expensetracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Convert Entity to DTO
    public TransactionDTO toDTO(Transaction t) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(t.getId());
        dto.setTitle(t.getTitle());
        dto.setAmount(t.getAmount());
        dto.setType(t.getType());
        dto.setTransactionDate(t.getTransactionDate());
        dto.setPaymentMethod(t.getPaymentMethod());
        dto.setNotes(t.getNotes());
        if (t.getCategory() != null) {
            dto.setCategoryId(t.getCategory().getId());
            dto.setCategoryName(t.getCategory().getName());
            dto.setCategoryIcon(t.getCategory().getIcon());
            dto.setCategoryColor(t.getCategory().getColor());
        }
        return dto;
    }

    // Public alias used by ExportService
    public TransactionDTO toPublicDTO(com.tracker.expensetracker.model.Transaction t) {
        return toDTO(t);
    }

    // Convert DTO to Entity
    private Transaction toEntity(TransactionDTO dto) {
        Transaction t = new Transaction();
        t.setTitle(dto.getTitle());
        t.setAmount(dto.getAmount());
        t.setType(dto.getType());
        t.setTransactionDate(dto.getTransactionDate());
        t.setPaymentMethod(dto.getPaymentMethod());
        t.setNotes(dto.getNotes());
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId()).ifPresent(t::setCategory);
        }
        return t;
    }

    // Create transaction
    public TransactionDTO createTransaction(TransactionDTO dto) {
        Transaction transaction = toEntity(dto);
        Transaction saved = transactionRepository.save(transaction);
        return toDTO(saved);
    }

    // Update transaction
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        transaction.setTitle(dto.getTitle());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setNotes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId()).ifPresent(transaction::setCategory);
        } else {
            transaction.setCategory(null);
        }

        Transaction updated = transactionRepository.save(transaction);
        return toDTO(updated);
    }

    // Delete transaction
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    // Get by ID
    public TransactionDTO getTransactionById(Long id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return toDTO(t);
    }

    // Get all transactions (paginated)
    public Page<TransactionDTO> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending()
                .and(Sort.by("createdAt").descending()));
        return transactionRepository.findAll(pageable).map(this::toDTO);
    }

    // Get recent transactions
    public List<TransactionDTO> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByTransactionDateDescCreatedAtDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Get transactions by type
    public Page<TransactionDTO> getTransactionsByType(TransactionType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findByType(type, pageable).map(this::toDTO);
    }

    // Search/filter transactions
    public Page<TransactionDTO> searchTransactions(TransactionType type, Long categoryId,
                                                    LocalDate startDate, LocalDate endDate,
                                                    String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending().and(Sort.by("createdAt").descending()));
        
        org.springframework.data.jpa.domain.Specification<Transaction> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return transactionRepository.findAll(spec, pageable).map(this::toDTO);
    }

    // Get total income
    public BigDecimal getTotalIncome() {
        return transactionRepository.sumAmountByType(TransactionType.INCOME);
    }

    // Get total expense
    public BigDecimal getTotalExpense() {
        return transactionRepository.sumAmountByType(TransactionType.EXPENSE);
    }

    // Get balance
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    // Get income/expense for date range
    public BigDecimal getAmountByTypeAndDateRange(TransactionType type, LocalDate start, LocalDate end) {
        return transactionRepository.sumAmountByTypeAndDateBetween(type, start, end);
    }

    // Get all transactions in date range
    public List<TransactionDTO> getTransactionsInRange(LocalDate start, LocalDate end) {
        return transactionRepository.findByTransactionDateBetween(start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
}
