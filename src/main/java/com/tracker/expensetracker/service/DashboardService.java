package com.tracker.expensetracker.service;

import com.tracker.expensetracker.dto.DashboardDTO;
import com.tracker.expensetracker.dto.TransactionDTO;
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
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();

        // ---- Totals ----
        BigDecimal totalIncome = transactionService.getTotalIncome();
        BigDecimal totalExpense = transactionService.getTotalExpense();
        BigDecimal balance = totalIncome.subtract(totalExpense);

        dashboard.setTotalIncome(totalIncome);
        dashboard.setTotalExpense(totalExpense);
        dashboard.setTotalBalance(balance);

        // ---- Current Month ----
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal currentMonthExpense = transactionService.getAmountByTypeAndDateRange(
                TransactionType.EXPENSE, monthStart, monthEnd);
        BigDecimal currentMonthIncome = transactionService.getAmountByTypeAndDateRange(
                TransactionType.INCOME, monthStart, monthEnd);

        dashboard.setCurrentMonthExpense(currentMonthExpense);
        dashboard.setCurrentMonthIncome(currentMonthIncome);
        dashboard.setSavings(currentMonthIncome.subtract(currentMonthExpense));

        // ---- Expense by Category (Current Month) ----
        List<Object[]> categorySummary = transactionRepository.getCategoryExpenseSummary(monthStart, monthEnd);
        List<DashboardDTO.CategorySummary> categoryList = new ArrayList<>();

        for (Object[] row : categorySummary) {
            String categoryName = row[0].toString();
            String categoryIcon = row[1].toString();
            String categoryColor = row[2].toString();
            BigDecimal total = (BigDecimal) row[3];

            double percentage = currentMonthExpense.compareTo(BigDecimal.ZERO) > 0
                    ? total.multiply(BigDecimal.valueOf(100))
                    .divide(currentMonthExpense, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            categoryList.add(new DashboardDTO.CategorySummary(
                    categoryName, categoryIcon, categoryColor, total, percentage));
        }
        dashboard.setExpenseByCategory(categoryList);

        // ---- Recent Transactions ----
        List<TransactionDTO> recentTransactions = transactionService.getRecentTransactions();
        dashboard.setRecentTransactions(recentTransactions);

        // ---- Monthly Trends (Last 6 months) ----
        LocalDate sixMonthsAgo = now.minusMonths(5).withDayOfMonth(1);
        List<Object[]> monthlyTotals = transactionRepository.getMonthlyTotals(sixMonthsAgo);

        // Build a map: "YYYY-MM" -> {income, expense}
        Map<String, BigDecimal[]> monthMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now.minusMonths(i));
            String key = ym.getYear() + "-" + String.format("%02d", ym.getMonthValue());
            monthMap.put(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (Object[] row : monthlyTotals) {
            int m = ((Number) row[0]).intValue();
            int y = ((Number) row[1]).intValue();
            TransactionType type = parseTransactionType(row[2]);
            BigDecimal amount = (BigDecimal) row[3];
            String key = y + "-" + String.format("%02d", m);

            if (monthMap.containsKey(key)) {
                if (type == TransactionType.INCOME) {
                    monthMap.get(key)[0] = amount;
                } else {
                    monthMap.get(key)[1] = amount;
                }
            }
        }

        List<DashboardDTO.MonthlyTrend> trends = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> entry : monthMap.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int monthNum = Integer.parseInt(parts[1]);
            String monthName = Month.of(monthNum).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            trends.add(new DashboardDTO.MonthlyTrend(
                    monthName, entry.getValue()[0], entry.getValue()[1]));
        }
        dashboard.setMonthlyTrends(trends);

        // ---- Smart Insights ----
        dashboard.setInsights(generateInsights(now, currentMonthExpense, currentMonthIncome));

        return dashboard;
    }

    private List<String> generateInsights(LocalDate now, BigDecimal currentMonthExpense, BigDecimal currentMonthIncome) {
        List<String> insights = new ArrayList<>();

        // Savings insight
        BigDecimal savings = currentMonthIncome.subtract(currentMonthExpense);
        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            insights.add("💰 Great job! You saved ₹" + savings.setScale(0, RoundingMode.HALF_UP) + " this month.");
        } else if (savings.compareTo(BigDecimal.ZERO) < 0) {
            insights.add("⚠️ Alert! You overspent by ₹" + savings.abs().setScale(0, RoundingMode.HALF_UP) + " this month.");
        }

        // Compare with last month
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = YearMonth.from(now.minusMonths(1)).atEndOfMonth();

        BigDecimal lastMonthExpense = transactionService.getAmountByTypeAndDateRange(
                TransactionType.EXPENSE, lastMonthStart, lastMonthEnd);

        if (lastMonthExpense.compareTo(BigDecimal.ZERO) > 0 && currentMonthExpense.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = currentMonthExpense.subtract(lastMonthExpense);
            BigDecimal pct = diff.multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthExpense, 0, RoundingMode.HALF_UP);

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                insights.add("📈 Your expenses increased by " + pct.abs() + "% compared to last month.");
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                insights.add("📉 Your expenses decreased by " + pct.abs() + "% compared to last month. Keep it up!");
            }
        }

        // Top spending category
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        List<Object[]> categorySummary = transactionRepository.getCategoryExpenseSummary(monthStart, monthEnd);

        if (!categorySummary.isEmpty()) {
            String topCategory = categorySummary.get(0)[0].toString();
            BigDecimal topAmount = (BigDecimal) categorySummary.get(0)[3];
            insights.add("🏷️ Your highest spending category is " + topCategory + " with ₹" +
                    topAmount.setScale(0, RoundingMode.HALF_UP) + " this month.");
        }

        // Category comparison with last month
        List<Object[]> lastMonthCategories = transactionRepository.getCategoryExpenseSummary(lastMonthStart, lastMonthEnd);
        Map<String, BigDecimal> lastMonthCatMap = new HashMap<>();
        for (Object[] row : lastMonthCategories) {
            lastMonthCatMap.put(row[0].toString(), (BigDecimal) row[3]);
        }

        for (Object[] row : categorySummary) {
            String catName = row[0].toString();
            BigDecimal catAmount = (BigDecimal) row[3];
            BigDecimal lastAmount = lastMonthCatMap.getOrDefault(catName, BigDecimal.ZERO);

            if (lastAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal catDiff = catAmount.subtract(lastAmount);
                BigDecimal catPct = catDiff.multiply(BigDecimal.valueOf(100))
                        .divide(lastAmount, 0, RoundingMode.HALF_UP);

                if (catPct.compareTo(BigDecimal.valueOf(20)) > 0) {
                    insights.add("🔺 You spent " + catPct + "% more on " + catName + " compared to last month.");
                }
            }
        }

        // Income insight
        if (currentMonthIncome.compareTo(BigDecimal.ZERO) == 0) {
            insights.add("💡 No income recorded this month. Don't forget to add your income!");
        }

        if (insights.isEmpty()) {
            insights.add("📊 Start adding transactions to see personalized insights!");
        }

        return insights;
    }
    private TransactionType parseTransactionType(Object obj) {
        if (obj == null) return null;
        if (obj instanceof TransactionType) {
            return (TransactionType) obj;
        }
        return TransactionType.valueOf(obj.toString());
    }
}
