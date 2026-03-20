/* ============================================
   Smart Personal Expense Tracker - API Client
   ============================================ */

const API_BASE = '/api';

const api = {
    // ---- Dashboard ----
    async getDashboard() {
        const res = await fetch(`${API_BASE}/dashboard`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    // ---- Transactions ----
    async createTransaction(data) {
        const res = await fetch(`${API_BASE}/transactions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async updateTransaction(id, data) {
        const res = await fetch(`${API_BASE}/transactions/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async deleteTransaction(id) {
        const res = await fetch(`${API_BASE}/transactions/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok && res.status !== 204) throw await res.json();
        return true;
    },

    async getTransaction(id) {
        const res = await fetch(`${API_BASE}/transactions/${id}`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async getTransactions(page = 0, size = 15) {
        const res = await fetch(`${API_BASE}/transactions?page=${page}&size=${size}`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async getRecentTransactions() {
        const res = await fetch(`${API_BASE}/transactions/recent`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async searchTransactions(params) {
        const query = new URLSearchParams();
        if (params.type) query.append('type', params.type);
        if (params.categoryId) query.append('categoryId', params.categoryId);
        if (params.startDate) query.append('startDate', params.startDate);
        if (params.endDate) query.append('endDate', params.endDate);
        if (params.search) query.append('search', params.search);
        query.append('page', params.page || 0);
        query.append('size', params.size || 15);

        const res = await fetch(`${API_BASE}/transactions/search?${query}`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    // ---- Categories ----
    async getCategories() {
        const res = await fetch(`${API_BASE}/categories`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async getCategoriesByType(type) {
        const res = await fetch(`${API_BASE}/categories/type/${type}`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async createCategory(data) {
        const res = await fetch(`${API_BASE}/categories`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async updateCategory(id, data) {
        const res = await fetch(`${API_BASE}/categories/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async deleteCategory(id) {
        const res = await fetch(`${API_BASE}/categories/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok && res.status !== 204) throw await res.json();
        return true;
    },

    // ---- Reports ----
    async getMonthlyReport(month, year) {
        const res = await fetch(`${API_BASE}/reports/monthly?month=${month}&year=${year}`);
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async getYearlyReport(year) {
        const res = await fetch(`${API_BASE}/reports/yearly?year=${year}`);
        if (!res.ok) throw await res.json();
        return res.json();
    }
};
