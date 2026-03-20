package com.tracker.expensetracker.config;

import com.tracker.expensetracker.model.Category;
import com.tracker.expensetracker.model.Transaction;
import com.tracker.expensetracker.model.TransactionType;
import com.tracker.expensetracker.repository.CategoryRepository;
import com.tracker.expensetracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void run(String... args) {
        // Seed categories if empty
        if (categoryRepository.count() == 0) {
            seedCategories();
        }

        // Seed sample transactions if empty
        if (transactionRepository.count() == 0) {
            seedTransactions();
        }
    }

    private void seedCategories() {
        // Income categories
        categoryRepository.save(new Category("Salary", TransactionType.INCOME, "fas fa-briefcase", "#22c55e"));
        categoryRepository.save(new Category("Freelance", TransactionType.INCOME, "fas fa-laptop-code", "#3b82f6"));
        categoryRepository.save(new Category("Business", TransactionType.INCOME, "fas fa-store", "#8b5cf6"));
        categoryRepository.save(new Category("Investment", TransactionType.INCOME, "fas fa-chart-line", "#f59e0b"));
        categoryRepository.save(new Category("Other Income", TransactionType.INCOME, "fas fa-plus-circle", "#6366f1"));

        // Expense categories
        categoryRepository.save(new Category("Food", TransactionType.EXPENSE, "fas fa-utensils", "#ef4444"));
        categoryRepository.save(new Category("Travel", TransactionType.EXPENSE, "fas fa-plane", "#f97316"));
        categoryRepository.save(new Category("Shopping", TransactionType.EXPENSE, "fas fa-shopping-bag", "#ec4899"));
        categoryRepository.save(new Category("Bills", TransactionType.EXPENSE, "fas fa-file-invoice", "#14b8a6"));
        categoryRepository.save(new Category("Entertainment", TransactionType.EXPENSE, "fas fa-film", "#a855f7"));
        categoryRepository.save(new Category("Health", TransactionType.EXPENSE, "fas fa-heartbeat", "#f43f5e"));
        categoryRepository.save(new Category("Education", TransactionType.EXPENSE, "fas fa-graduation-cap", "#0ea5e9"));
        categoryRepository.save(new Category("Other Expense", TransactionType.EXPENSE, "fas fa-tag", "#64748b"));
    }

    private void seedTransactions() {
        List<Category> incomeCategories = categoryRepository.findByType(TransactionType.INCOME);
        List<Category> expenseCategories = categoryRepository.findByType(TransactionType.EXPENSE);

        if (incomeCategories.isEmpty() || expenseCategories.isEmpty()) return;

        Random random = new Random(42);
        LocalDate today = LocalDate.now();
        String[] paymentMethods = {"Cash", "UPI", "Credit Card", "Debit Card", "Net Banking"};

        // --- Income: Last 3 months ---
        for (int m = 0; m < 3; m++) {
            LocalDate salaryDate = today.minusMonths(m).withDayOfMonth(1);
            // Salary
            Transaction salary = new Transaction(
                    "Monthly Salary", new BigDecimal("45000"),
                    TransactionType.INCOME, incomeCategories.get(0),
                    salaryDate, "Net Banking", "Monthly salary credited");
            transactionRepository.save(salary);

            // Optional freelance
            if (random.nextBoolean()) {
                Transaction freelance = new Transaction(
                        "Freelance Project", new BigDecimal(5000 + random.nextInt(10000)),
                        TransactionType.INCOME, incomeCategories.get(1),
                        salaryDate.plusDays(10 + random.nextInt(10)),
                        "UPI", "Freelance web development");
                transactionRepository.save(freelance);
            }
        }

        // --- Expenses: Last 3 months ---
        String[][] expenseData = {
                {"Groceries", "2500", "Food", "UPI"},
                {"Restaurant Dinner", "1200", "Food", "Credit Card"},
                {"Uber Rides", "800", "Travel", "UPI"},
                {"Train Ticket", "450", "Travel", "Debit Card"},
                {"Amazon Shopping", "3500", "Shopping", "Credit Card"},
                {"Electricity Bill", "1800", "Bills", "Net Banking"},
                {"Mobile Recharge", "599", "Bills", "UPI"},
                {"Netflix Subscription", "649", "Entertainment", "Credit Card"},
                {"Movie Tickets", "500", "Entertainment", "UPI"},
                {"Pharmacy", "350", "Health", "Cash"},
                {"Online Course", "2999", "Education", "Credit Card"},
                {"Street Food", "200", "Food", "Cash"},
                {"Petrol", "1500", "Travel", "Debit Card"},
                {"Clothing", "2200", "Shopping", "Credit Card"},
                {"Water Bill", "300", "Bills", "Net Banking"},
                {"Gym Membership", "1500", "Health", "UPI"},
                {"Books", "800", "Education", "UPI"},
                {"Coffee Shop", "350", "Food", "Cash"},
                {"Bus Pass", "600", "Travel", "Cash"},
                {"Home Decor", "1800", "Shopping", "Credit Card"},
        };

        for (int m = 0; m < 3; m++) {
            // Pick a subset of expenses for each month
            int count = 10 + random.nextInt(8);
            for (int i = 0; i < count && i < expenseData.length; i++) {
                String[] data = expenseData[i];
                Category cat = expenseCategories.stream()
                        .filter(c -> c.getName().equals(data[2]))
                        .findFirst().orElse(expenseCategories.get(expenseCategories.size() - 1));

                int day = 1 + random.nextInt(Math.min(28, today.minusMonths(m).lengthOfMonth()));
                LocalDate txDate = today.minusMonths(m).withDayOfMonth(day);

                // Vary amount slightly
                double baseAmount = Double.parseDouble(data[1]);
                BigDecimal amount = BigDecimal.valueOf(baseAmount * (0.8 + random.nextDouble() * 0.4))
                        .setScale(0, java.math.RoundingMode.HALF_UP);

                Transaction tx = new Transaction(
                        data[0], amount, TransactionType.EXPENSE,
                        cat, txDate, data[3], "");
                transactionRepository.save(tx);
            }
        }
    }
}
