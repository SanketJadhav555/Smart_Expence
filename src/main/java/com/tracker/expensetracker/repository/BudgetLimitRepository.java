package com.tracker.expensetracker.repository;

import com.tracker.expensetracker.model.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {

    Optional<BudgetLimit> findByCategoryIdAndMonthAndYear(Long categoryId, Integer month, Integer year);

    List<BudgetLimit> findByMonthAndYear(Integer month, Integer year);
}
