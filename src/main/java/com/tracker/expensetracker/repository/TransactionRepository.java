package com.tracker.expensetracker.repository;

import com.tracker.expensetracker.model.Transaction;
import com.tracker.expensetracker.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find by type
    List<Transaction> findByType(TransactionType type);

    // Find by type with pagination
    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    // Find all ordered by date descending
    List<Transaction> findAllByOrderByTransactionDateDesc();

    // Find recent transactions
    List<Transaction> findTop10ByOrderByTransactionDateDescCreatedAtDesc();

    // Find by date range
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by type and date range
    List<Transaction> findByTypeAndTransactionDateBetween(
            TransactionType type, LocalDate startDate, LocalDate endDate);

    // Find by category
    List<Transaction> findByCategoryId(Long categoryId);

    // Search by title
    Page<Transaction> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Complex search with filters
    @Query("SELECT t FROM Transaction t WHERE " +
            "(:type IS NULL OR t.type = :type) AND " +
            "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
            "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.transactionDate <= :endDate) AND " +
            "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))" +
            " ORDER BY t.transactionDate DESC, t.createdAt DESC")
    Page<Transaction> findWithFilters(
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search,
            Pageable pageable);

    // Sum of amount by type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    // Sum of amount by type and date range
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateBetween(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Category spending summary for current month
    @Query("SELECT t.category.name, t.category.icon, t.category.color, SUM(t.amount) " +
            "FROM Transaction t WHERE t.type = com.tracker.expensetracker.model.TransactionType.EXPENSE " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category.name, t.category.icon, t.category.color " +
            "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryExpenseSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Monthly income/expense totals for last N months
    @Query("SELECT FUNCTION('MONTH', t.transactionDate), FUNCTION('YEAR', t.transactionDate), " +
            "t.type, SUM(t.amount) FROM Transaction t " +
            "WHERE t.transactionDate >= :since " +
            "GROUP BY FUNCTION('YEAR', t.transactionDate), FUNCTION('MONTH', t.transactionDate), t.type " +
            "ORDER BY FUNCTION('YEAR', t.transactionDate), FUNCTION('MONTH', t.transactionDate)")
    List<Object[]> getMonthlyTotals(@Param("since") LocalDate since);

    // Count transactions by category in date range
    @Query("SELECT t.category.id, COUNT(t) FROM Transaction t " +
            "WHERE t.type = com.tracker.expensetracker.model.TransactionType.EXPENSE AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category.id")
    List<Object[]> countByCategoryInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
