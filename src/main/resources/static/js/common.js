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

    initGlobalSearch();
});

/* ============================================
   Global Navbar Search
   ============================================ */
function initGlobalSearch() {
    const input = document.getElementById('globalSearch');
    if (!input) return;

    // Inject dropdown container right after the search wrapper
    const wrapper = input.closest('.navbar-search');
    wrapper.style.position = 'relative';

    const dropdown = document.createElement('div');
    dropdown.id = 'globalSearchDropdown';
    dropdown.style.cssText = `
        display: none;
        position: absolute;
        top: calc(100% + 8px);
        left: 0;
        right: 0;
        min-width: 340px;
        background: var(--bg-card);
        border: 1px solid var(--border-color);
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-xl);
        z-index: 9999;
        overflow: hidden;
        max-height: 420px;
        overflow-y: auto;
    `;
    wrapper.appendChild(dropdown);

    let debounceTimer = null;

    input.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        const q = input.value.trim();
        if (q.length < 1) {
            closeSearchDropdown(dropdown);
            return;
        }
        debounceTimer = setTimeout(() => runSearch(q, dropdown), 280);
    });

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeSearchDropdown(dropdown);
        if (e.key === 'Enter') {
            const q = input.value.trim();
            if (q) {
                window.location.href = '/transactions?search=' + encodeURIComponent(q);
            }
        }
    });

    // Close when clicking outside
    document.addEventListener('click', (e) => {
        if (!wrapper.contains(e.target)) closeSearchDropdown(dropdown);
    });
}

async function runSearch(query, dropdown) {
    dropdown.innerHTML = `
        <div style="padding:16px 18px; color:var(--text-muted); font-size:13px; display:flex; align-items:center; gap:8px;">
            <i class="fas fa-spinner fa-spin"></i> Searching...
        </div>`;
    dropdown.style.display = 'block';

    try {
        const res = await fetch(`/api/transactions/search?search=${encodeURIComponent(query)}&page=0&size=8`);
        if (!res.ok) throw new Error('Search failed');
        const data = await res.json();
        renderSearchResults(data.content || [], query, dropdown);
    } catch (err) {
        dropdown.innerHTML = `
            <div style="padding:16px 18px; color:var(--danger); font-size:13px;">
                <i class="fas fa-exclamation-circle"></i> Could not load results.
            </div>`;
    }
}

function renderSearchResults(results, query, dropdown) {
    if (results.length === 0) {
        dropdown.innerHTML = `
            <div style="padding:24px 18px; text-align:center;">
                <div style="font-size:28px; margin-bottom:8px;">🔍</div>
                <div style="font-size:13px; color:var(--text-muted);">No transactions found for <strong style="color:var(--text-primary);">"${escapeHtml(query)}"</strong></div>
            </div>`;
        dropdown.style.display = 'block';
        return;
    }

    const headerHtml = `
        <div style="padding:10px 16px 6px; font-size:11px; font-weight:700; letter-spacing:1px; text-transform:uppercase; color:var(--text-muted); border-bottom:1px solid var(--border-color);">
            ${results.length} result${results.length !== 1 ? 's' : ''} for "${escapeHtml(query)}"
        </div>`;

    const itemsHtml = results.map(tx => {
        const isIncome  = tx.type === 'INCOME';
        const amtColor  = isIncome ? 'var(--accent)' : 'var(--danger)';
        const amtSign   = isIncome ? '+' : '-';
        // BigDecimal comes as a number in JSON
        const amtVal    = tx.amount != null ? parseFloat(tx.amount) : 0;
        const iconColor = isIncome ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)';
        const iconEl    = isIncome ? 'fa-arrow-down' : 'fa-arrow-up';
        const iconClr   = isIncome ? 'var(--accent)' : 'var(--danger)';
        // DTO field is transactionDate, not date
        const dateStr   = tx.transactionDate ? formatDate(tx.transactionDate) : '';
        const cat       = tx.categoryName || 'Uncategorized';
        // DTO field is title, not description
        const desc      = highlight(tx.title || 'No title', query);

        return `
            <a href="/transactions" class="search-result-item" style="
                display:flex; align-items:center; gap:14px;
                padding:12px 16px; text-decoration:none;
                border-bottom:1px solid var(--border-light);
                transition:background 0.12s ease; cursor:pointer;
            " onmouseover="this.style.background='var(--bg-card-hover)'"
               onmouseout="this.style.background='transparent'">
                <div style="
                    width:36px; height:36px; border-radius:10px; flex-shrink:0;
                    background:${iconColor}; color:${iconClr};
                    display:flex; align-items:center; justify-content:center; font-size:14px;">
                    <i class="fas ${iconEl}"></i>
                </div>
                <div style="flex:1; min-width:0;">
                    <div style="font-size:13px; font-weight:600; color:var(--text-primary); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${desc}</div>
                    <div style="font-size:11px; color:var(--text-muted); margin-top:2px;">${escapeHtml(cat)} &bull; ${dateStr}</div>
                </div>
                <div style="font-size:14px; font-weight:700; color:${amtColor}; white-space:nowrap;">
                    ${amtSign}${formatCurrency(amtVal)}
                </div>
            </a>`;
    }).join('');

    const footerHtml = `
        <a href="/transactions?search=${encodeURIComponent(query)}" style="
            display:block; padding:11px 16px; font-size:12px; font-weight:600;
            color:var(--primary-light); text-align:center; text-decoration:none;
            border-top:1px solid var(--border-color);
            transition:background 0.12s ease;
        " onmouseover="this.style.background='rgba(79,70,229,0.08)'"
           onmouseout="this.style.background='transparent'">
            <i class="fas fa-search" style="margin-right:6px;"></i> View all results for "${escapeHtml(query)}"
        </a>`;

    dropdown.innerHTML = headerHtml + itemsHtml + footerHtml;
    dropdown.style.display = 'block';
}

function closeSearchDropdown(dropdown) {
    if (dropdown) dropdown.style.display = 'none';
}

function highlight(text, query) {
    if (!query) return escapeHtml(text);
    const safe  = escapeHtml(text);
    const safeQ = escapeHtml(query).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return safe.replace(new RegExp(`(${safeQ})`, 'gi'),
        '<mark style="background:rgba(79,70,229,0.3);color:var(--text-primary);border-radius:3px;padding:0 2px;">$1</mark>');
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

