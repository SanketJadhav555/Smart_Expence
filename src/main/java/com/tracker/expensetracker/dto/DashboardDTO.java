package com.tracker.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private BigDecimal totalBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal currentMonthExpense;
    private BigDecimal currentMonthIncome;
    private BigDecimal savings;

    private List<CategorySummary> expenseByCategory;
    private List<TransactionDTO> recentTransactions;
    private List<MonthlyTrend> monthlyTrends;
    private List<String> insights;

    // Inner class for category summary
    public static class CategorySummary {
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;
        private BigDecimal totalAmount;
        private double percentage;

        public CategorySummary() {}

        public CategorySummary(String categoryName, String categoryIcon, String categoryColor,
                               BigDecimal totalAmount, double percentage) {
            this.categoryName = categoryName;
            this.categoryIcon = categoryIcon;
            this.categoryColor = categoryColor;
            this.totalAmount = totalAmount;
            this.percentage = percentage;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getCategoryIcon() { return categoryIcon; }
        public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
        public String getCategoryColor() { return categoryColor; }
        public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    // Inner class for monthly trend
    public static class MonthlyTrend {
        private String month;
        private BigDecimal income;
        private BigDecimal expense;

        public MonthlyTrend() {}

        public MonthlyTrend(String month, BigDecimal income, BigDecimal expense) {
            this.month = month;
            this.income = income;
            this.expense = expense;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public BigDecimal getIncome() { return income; }
        public void setIncome(BigDecimal income) { this.income = income; }
        public BigDecimal getExpense() { return expense; }
        public void setExpense(BigDecimal expense) { this.expense = expense; }
    }

    // Constructors
    public DashboardDTO() {}

    // Getters and Setters
    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getCurrentMonthExpense() { return currentMonthExpense; }
    public void setCurrentMonthExpense(BigDecimal currentMonthExpense) { this.currentMonthExpense = currentMonthExpense; }

    public BigDecimal getCurrentMonthIncome() { return currentMonthIncome; }
    public void setCurrentMonthIncome(BigDecimal currentMonthIncome) { this.currentMonthIncome = currentMonthIncome; }

    public BigDecimal getSavings() { return savings; }
    public void setSavings(BigDecimal savings) { this.savings = savings; }

    public List<CategorySummary> getExpenseByCategory() { return expenseByCategory; }
    public void setExpenseByCategory(List<CategorySummary> expenseByCategory) { this.expenseByCategory = expenseByCategory; }

    public List<TransactionDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionDTO> recentTransactions) { this.recentTransactions = recentTransactions; }

    public List<MonthlyTrend> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrend> monthlyTrends) { this.monthlyTrends = monthlyTrends; }

    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }
}
