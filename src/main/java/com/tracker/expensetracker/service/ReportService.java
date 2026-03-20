package com.tracker.expensetracker.service;

import com.tracker.expensetracker.model.TransactionType;
import com.tracker.expensetracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Generate monthly report data
     */
    public Map<String, Object> getMonthlyReport(int month, int year) {
        Map<String, Object> report = new LinkedHashMap<>();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        // Totals
        BigDecimal income = transactionRepository.sumAmountByTypeAndDateBetween(
                TransactionType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumAmountByTypeAndDateBetween(
                TransactionType.EXPENSE, start, end);
        BigDecimal savings = income.subtract(expense);

        report.put("month", Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        report.put("year", year);
        report.put("totalIncome", income);
        report.put("totalExpense", expense);
        report.put("savings", savings);
        report.put("savingsRate", income.compareTo(BigDecimal.ZERO) > 0
                ? savings.multiply(BigDecimal.valueOf(100)).divide(income, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        // Category breakdown
        List<Object[]> categorySummary = transactionRepository.getCategoryExpenseSummary(start, end);
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Object[] row : categorySummary) {
            Map<String, Object> cat = new LinkedHashMap<>();
            cat.put("name", row[0]);
            cat.put("icon", row[1]);
            cat.put("color", row[2]);
            cat.put("amount", row[3]);
            cat.put("percentage", expense.compareTo(BigDecimal.ZERO) > 0
                    ? ((BigDecimal) row[3]).multiply(BigDecimal.valueOf(100))
                    .divide(expense, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            categories.add(cat);
        }
        report.put("categoryBreakdown", categories);

        return report;
    }

    /**
     * Get yearly summary (12 months)
     */
    public Map<String, Object> getYearlySummary(int year) {
        Map<String, Object> summary = new LinkedHashMap<>();

        List<Map<String, Object>> monthlyData = new ArrayList<>();
        BigDecimal yearIncome = BigDecimal.ZERO;
        BigDecimal yearExpense = BigDecimal.ZERO;

        for (int m = 1; m <= 12; m++) {
            LocalDate start = LocalDate.of(year, m, 1);
            LocalDate end = YearMonth.of(year, m).atEndOfMonth();

            BigDecimal income = transactionRepository.sumAmountByTypeAndDateBetween(
                    TransactionType.INCOME, start, end);
            BigDecimal expense = transactionRepository.sumAmountByTypeAndDateBetween(
                    TransactionType.EXPENSE, start, end);

            yearIncome = yearIncome.add(income);
            yearExpense = yearExpense.add(expense);

            Map<String, Object> month = new LinkedHashMap<>();
            month.put("month", Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            month.put("income", income);
            month.put("expense", expense);
            month.put("savings", income.subtract(expense));
            monthlyData.add(month);
        }

        summary.put("year", year);
        summary.put("totalIncome", yearIncome);
        summary.put("totalExpense", yearExpense);
        summary.put("totalSavings", yearIncome.subtract(yearExpense));
        summary.put("monthlyData", monthlyData);

        return summary;
    }
}
