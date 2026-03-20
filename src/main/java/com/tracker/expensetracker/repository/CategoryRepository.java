package com.tracker.expensetracker.repository;

import com.tracker.expensetracker.model.Category;
import com.tracker.expensetracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(TransactionType type);

    List<Category> findByTypeOrderByNameAsc(TransactionType type);

    List<Category> findAllByOrderByTypeAscNameAsc();

    boolean existsByNameAndType(String name, TransactionType type);
}
