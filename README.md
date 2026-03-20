# 💳 Smart Personal Expense Tracker

A modern, full-stack Spring Boot application designed to help you track expenses, manage budgets, and gain smart insights into your financial health. Featuring a sleek, responsive UI and detailed analytics.

---

## 🚀 Quick Start (Windows)

To run this application locally on your machine:

1.  **Open PowerShell** in the project directory.
2.  **Stop any existing sessions** (to avoid port/file locks):
    ```powershell
    Stop-Process -Name java -ErrorAction SilentlyContinue
    ```
3.  **Run the application**:
    ```powershell
    .\run_app.ps1
    ```
4.  **Access the Dashboard**:
    Open your browser and navigate to: `http://localhost:8080`

---

## ✨ Features

- 📊 **Dynamic Dashboard**: Real-time overview of your balance, total income, and total expenses.
- 💸 **Transaction Management**: Easily add, edit, or delete income and expense records.
- 📂 **Smart Categorization**: Organise your spending with customizable categories (Food, Travel, Bills, etc.).
- 📈 **Graphical Reports**: Visualize your spending habits with interactive charts.
- 📱 **Mobile Ready**: Fully responsive design that works perfectly on Android and iOS browsers.
- 🔐 **Demo Auth System**: Pre-built Login, Registration, and Forgot Password pages.

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA
- **Frontend**: HTML5, CSS3 (Modern Glassmorphism Design), Thymeleaf
- **Database**: H2 (Embedded for Development) / MySQL (Compatible)
- **Security**: Demo Authentication system (Expandable to Spring Security)
- **Utilities**: Lombok, Jackson, Maven

---

## 📱 Using on Android

You can use the Expense Tracker as a mobile app on your Android device:

1.  **Same Network**: Connect your PC and Phone to the same Wi-Fi.
2.  **Find PC IP**: Run `ipconfig` in PowerShell to find your local IP (e.g., `192.168.1.5`).
3.  **Browse**: Open Chrome on your Android phone and go to `http://192.168.1.5:8080`.
4.  **Install**: Select **"Add to Home Screen"** from the Chrome menu for a true app-like experience.

---

## ⚙️ Configuration

The application settings can be found in `src/main/resources/application.properties`:
- **Server Port**: `8080`
- **Database Type**: H2 (In-memory file-based)
- **H2 Console**: Accessible at `/h2-console` (JDBC URL: `jdbc:h2:file:./data/expense_tracker_db`)

---

## 📝 Future Roadmap

- [ ] Email notifications for budget limits.
- [ ] Multi-currency support.
- [ ] PDF/Excel export functionality (using Apache POI/iText).
- [ ] OAuth2 (Google/GitHub) social login integration.

---

**Built with ❤️ for Financial Clarity.**
