/* ============================================
   Smart Personal Expense Tracker - Common Utilities
   ============================================ */

// Format currency
function formatCurrency(amount, symbol = '₹') {
    if (amount == null) return `${symbol}0`;
    const num = parseFloat(amount);
    if (isNaN(num)) return `${symbol}0`;
    return `${symbol}${num.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`;
}

// Format date
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

// Format date for input
function formatDateInput(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toISOString().split('T')[0];
}

// Toast notification
function showToast(message, type = 'success') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const icons = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle'
    };

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="${icons[type] || icons.success}"></i>
        <span class="toast-message">${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
        if (container.children.length === 0) container.remove();
    }, 3000);
}

// Sidebar toggle
function initSidebar() {
    const toggle = document.querySelector('.menu-toggle');
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.querySelector('.sidebar-overlay');

    if (toggle) {
        toggle.addEventListener('click', () => {
            sidebar.classList.toggle('open');
            overlay.classList.toggle('active');
        });
    }

    if (overlay) {
        overlay.addEventListener('click', () => {
            sidebar.classList.remove('open');
            overlay.classList.remove('active');
        });
    }

    // Highlight active nav item
    const path = window.location.pathname;
    document.querySelectorAll('.nav-item').forEach(item => {
        const href = item.getAttribute('href');
        if (href === path || (href === '/dashboard' && path === '/')) {
            item.classList.add('active');
        }
    });
}

// Modal helpers
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('active');
}

// Pagination renderer
function renderPagination(containerId, currentPage, totalPages, onPageChange) {
    const container = document.getElementById(containerId);
    if (!container || totalPages <= 1) {
        if (container) container.innerHTML = '';
        return;
    }

    let html = '';

    // Previous
    html += `<button class="page-btn" onclick="${onPageChange}(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>
        <i class="fas fa-chevron-left"></i>
    </button>`;

    // Page numbers
    const start = Math.max(0, currentPage - 2);
    const end = Math.min(totalPages, start + 5);

    for (let i = start; i < end; i++) {
        html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" 
                  onclick="${onPageChange}(${i})">${i + 1}</button>`;
    }

    // Next
    html += `<button class="page-btn" onclick="${onPageChange}(${currentPage + 1})" 
              ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
        <i class="fas fa-chevron-right"></i>
    </button>`;

    html += `<span class="page-info">Page ${currentPage + 1} of ${totalPages}</span>`;

    container.innerHTML = html;
}

// Chart colors
const chartColors = [
    '#4F46E5', '#22C55E', '#ef4444', '#f59e0b', '#ec4899',
    '#14b8a6', '#a855f7', '#f97316', '#0ea5e9', '#64748b',
    '#f43f5e', '#8b5cf6', '#3b82f6'
];

// Chart default options
const chartDefaultOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            labels: {
                color: '#94a3b8',
                font: { family: 'Inter', size: 12 },
                padding: 16,
                usePointStyle: true,
                pointStyleWidth: 10
            }
        },
        tooltip: {
            backgroundColor: '#1e293b',
            titleColor: '#f1f5f9',
            bodyColor: '#94a3b8',
            borderColor: '#334155',
            borderWidth: 1,
            cornerRadius: 8,
            padding: 12,
            titleFont: { family: 'Inter', weight: '600' },
            bodyFont: { family: 'Inter' },
            callbacks: {
                label: function (ctx) {
                    return ` ${ctx.dataset.label || ctx.label}: ${formatCurrency(ctx.parsed.y || ctx.parsed)}`;
                }
            }
        }
    },
    scales: {
        x: {
            grid: { color: 'rgba(51, 65, 85, 0.5)', drawBorder: false },
            ticks: { color: '#64748b', font: { family: 'Inter', size: 11 } }
        },
        y: {
            grid: { color: 'rgba(51, 65, 85, 0.5)', drawBorder: false },
            ticks: {
                color: '#64748b',
                font: { family: 'Inter', size: 11 },
                callback: val => formatCurrency(val)
            }
        }
    }
};

// Theme management
function toggleTheme() {
    const html = document.documentElement;
    const currentTheme = html.getAttribute('data-theme') || 'dark';
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    html.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
    
    // Dispatch event for components that might need theme update
    window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme: newTheme } }));
}

function updateThemeIcon(theme) {
    const icons = document.querySelectorAll('.theme-toggle i');
    icons.forEach(icon => {
        if (theme === 'dark') {
            icon.className = 'fas fa-moon';
        } else {
            icon.className = 'fas fa-sun';
        }
    });
}

function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);
}

// Init on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    initSidebar();
    initTheme();
    
    // Add click listener to theme toggle button if it exists
    const toggleBtn = document.querySelector('.theme-toggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', (e) => {
            e.preventDefault();
            toggleTheme();
        });
    }
});
