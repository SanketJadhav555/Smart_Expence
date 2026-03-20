/* ============================================
   Dashboard Page Logic
   ============================================ */

let trendChart = null;
let categoryChart = null;

document.addEventListener('DOMContentLoaded', loadDashboard);

async function loadDashboard() {
    try {
        const data = await api.getDashboard();
        renderSummaryCards(data);
        renderTrendChart(data.monthlyTrends);
        renderCategoryChart(data.expenseByCategory);
        renderRecentTransactions(data.recentTransactions);
        renderInsights(data.insights);
    } catch (err) {
        console.error('Dashboard load error:', err);
        showToast('Failed to load dashboard data', 'error');
    }
}

function renderSummaryCards(data) {
    document.getElementById('totalBalance').textContent = formatCurrency(data.totalBalance);
    document.getElementById('totalIncome').textContent = formatCurrency(data.totalIncome);
    document.getElementById('totalExpense').textContent = formatCurrency(data.totalExpense);
    document.getElementById('savings').textContent = formatCurrency(data.savings);

    // Update trends
    const balanceTrend = document.getElementById('balanceTrend');
    if (data.totalBalance >= 0) {
        balanceTrend.className = 'card-change positive';
        balanceTrend.innerHTML = '<i class="fas fa-arrow-up"></i> Positive';
    } else {
        balanceTrend.className = 'card-change negative';
        balanceTrend.innerHTML = '<i class="fas fa-arrow-down"></i> Negative';
    }

    const incomeTrend = document.getElementById('incomeTrend');
    incomeTrend.innerHTML = `<i class="fas fa-calendar"></i> ${formatCurrency(data.currentMonthIncome)} this month`;
    incomeTrend.className = 'card-change positive';

    const expenseTrend = document.getElementById('expenseTrend');
    expenseTrend.innerHTML = `<i class="fas fa-calendar"></i> ${formatCurrency(data.currentMonthExpense)} this month`;
    expenseTrend.className = 'card-change negative';

    const savingsTrend = document.getElementById('savingsTrend');
    if (data.savings >= 0) {
        savingsTrend.className = 'card-change positive';
        savingsTrend.innerHTML = '<i class="fas fa-arrow-up"></i> You\'re saving!';
    } else {
        savingsTrend.className = 'card-change negative';
        savingsTrend.innerHTML = '<i class="fas fa-arrow-down"></i> Overspending!';
    }
}

function renderTrendChart(monthlyTrends) {
    const ctx = document.getElementById('trendChart').getContext('2d');

    if (trendChart) trendChart.destroy();

    const labels = monthlyTrends.map(t => t.month);
    const incomeData = monthlyTrends.map(t => t.income);
    const expenseData = monthlyTrends.map(t => t.expense);

    trendChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomeData,
                    backgroundColor: 'rgba(34, 197, 94, 0.7)',
                    borderColor: '#22c55e',
                    borderWidth: 2,
                    borderRadius: 6,
                    barPercentage: 0.4,
                    categoryPercentage: 0.7
                },
                {
                    label: 'Expense',
                    data: expenseData,
                    backgroundColor: 'rgba(239, 68, 68, 0.7)',
                    borderColor: '#ef4444',
                    borderWidth: 2,
                    borderRadius: 6,
                    barPercentage: 0.4,
                    categoryPercentage: 0.7
                }
            ]
        },
        options: {
            ...chartDefaultOptions,
            plugins: {
                ...chartDefaultOptions.plugins,
                legend: {
                    ...chartDefaultOptions.plugins.legend,
                    position: 'top'
                }
            }
        }
    });
}

function renderCategoryChart(categories) {
    const ctx = document.getElementById('categoryChart').getContext('2d');

    if (categoryChart) categoryChart.destroy();

    if (!categories || categories.length === 0) {
        ctx.font = '14px Inter';
        ctx.fillStyle = '#64748b';
        ctx.textAlign = 'center';
        ctx.fillText('No expense data for this month', ctx.canvas.width / 2, ctx.canvas.height / 2);
        return;
    }

    const labels = categories.map(c => c.categoryName);
    const data = categories.map(c => c.totalAmount);
    const colors = categories.map(c => c.categoryColor || '#6366f1');

    categoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors.map(c => c + 'cc'),
                borderColor: colors,
                borderWidth: 2,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#94a3b8',
                        font: { family: 'Inter', size: 11 },
                        padding: 12,
                        usePointStyle: true,
                        pointStyleWidth: 8
                    }
                },
                tooltip: {
                    ...chartDefaultOptions.plugins.tooltip,
                    callbacks: {
                        label: function (ctx) {
                            const pct = ((ctx.parsed / ctx.dataset.data.reduce((a, b) => a + b, 0)) * 100).toFixed(1);
                            return ` ${ctx.label}: ${formatCurrency(ctx.parsed)} (${pct}%)`;
                        }
                    }
                }
            }
        }
    });
}

function renderRecentTransactions(transactions) {
    const container = document.getElementById('recentTransactions');

    if (!transactions || transactions.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-receipt"></i>
                <h3>No transactions yet</h3>
                <p>Start by adding your first expense or income</p>
                <a href="/add-expense" class="btn btn-primary btn-sm">
                    <i class="fas fa-plus"></i> Add Expense
                </a>
            </div>`;
        return;
    }

    container.innerHTML = transactions.map(tx => `
        <li class="transaction-item">
            <div class="tx-icon" style="background: ${tx.categoryColor || '#6366f1'}20; color: ${tx.categoryColor || '#6366f1'}">
                <i class="${tx.categoryIcon || 'fas fa-tag'}"></i>
            </div>
            <div class="tx-details">
                <div class="tx-title">${tx.title}</div>
                <div class="tx-category">${tx.categoryName || 'Uncategorized'} · ${tx.paymentMethod || ''}</div>
            </div>
            <div>
                <div class="tx-amount ${tx.type === 'INCOME' ? 'income' : 'expense'}">
                    ${tx.type === 'INCOME' ? '+' : '-'}${formatCurrency(tx.amount)}
                </div>
                <div class="tx-date">${formatDate(tx.transactionDate)}</div>
            </div>
        </li>
    `).join('');
}

function renderInsights(insights) {
    const container = document.getElementById('insightsList');

    if (!insights || insights.length === 0) {
        container.innerHTML = '<li class="insight-item">📊 Start adding transactions to see personalized insights!</li>';
        return;
    }

    container.innerHTML = insights.map(text =>
        `<li class="insight-item">${text}</li>`
    ).join('');
}
