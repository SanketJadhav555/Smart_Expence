-- ============================================
-- Smart Personal Expense Tracker - MySQL Schema
-- ============================================
-- Run this script on MySQL to create the database

CREATE DATABASE IF NOT EXISTS expense_tracker_db;
USE expense_tracker_db;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    icon VARCHAR(50) DEFAULT 'fas fa-tag',
    color VARCHAR(20) DEFAULT '#6366f1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    category_id BIGINT,
    transaction_date DATE NOT NULL,
    payment_method VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Budget Limits Table
CREATE TABLE IF NOT EXISTS budget_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT,
    monthly_limit DECIMAL(12,2) NOT NULL,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Insert default categories
INSERT INTO categories (name, type, icon, color) VALUES
('Salary', 'INCOME', 'fas fa-briefcase', '#22c55e'),
('Freelance', 'INCOME', 'fas fa-laptop-code', '#3b82f6'),
('Business', 'INCOME', 'fas fa-store', '#8b5cf6'),
('Investment', 'INCOME', 'fas fa-chart-line', '#f59e0b'),
('Other Income', 'INCOME', 'fas fa-plus-circle', '#6366f1'),
('Food', 'EXPENSE', 'fas fa-utensils', '#ef4444'),
('Travel', 'EXPENSE', 'fas fa-plane', '#f97316'),
('Shopping', 'EXPENSE', 'fas fa-shopping-bag', '#ec4899'),
('Bills', 'EXPENSE', 'fas fa-file-invoice', '#14b8a6'),
('Entertainment', 'EXPENSE', 'fas fa-film', '#a855f7'),
('Health', 'EXPENSE', 'fas fa-heartbeat', '#f43f5e'),
('Education', 'EXPENSE', 'fas fa-graduation-cap', '#0ea5e9'),
('Other Expense', 'EXPENSE', 'fas fa-tag', '#64748b');
