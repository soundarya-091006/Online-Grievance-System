/* ============================================================
   authority-shared.js  –  SafeReport Authority Portal
   ============================================================ */

/* ─── HAMBURGER SIDENAV ─── */
function toggleMenu() {
    const d = document.getElementById('hamburgerDropdown');
    if (d) d.classList.toggle('active');
}
function closeMenu() {
    const d = document.getElementById('hamburgerDropdown');
    if (d) d.classList.remove('active');
}

/* ─── USER MENU ─── */
function toggleUserMenu() {
    const d = document.getElementById('userMenuDropdown');
    if (d) d.classList.toggle('active');
}

/* Close on outside click */
document.addEventListener('click', function (e) {
    const hm = document.querySelector('.hamburger-menu');
    const um = document.querySelector('.user-menu');
    const hd = document.getElementById('hamburgerDropdown');
    const ud = document.getElementById('userMenuDropdown');

    if (hm && hd && !hm.contains(e.target)) hd.classList.remove('active');
    if (um && ud && !um.contains(e.target)) ud.classList.remove('active');
});

/* ─── ACTIVE NAV LINK ─── */
(function highlightNav() {
    const page = location.pathname.split('/').pop() || 'dashboard.html';
    document.querySelectorAll('.menu-link').forEach(link => {
        if (link.getAttribute('href') === page) link.classList.add('active');
    });
})();

/* ─── FILTER CHIPS ─── */
document.querySelectorAll('.filter-chip').forEach(btn => {
    btn.addEventListener('click', function () {
        const bar = this.closest('.filters-bar') || this.parentElement;
        bar.querySelectorAll('.filter-chip').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
    });
});

/* ─── TOAST ─── */
function showToast(msg, type = 'success') {
    let t = document.getElementById('globalToast');
    if (!t) {
        t = document.createElement('div');
        t.id = 'globalToast';
        t.className = 'toast';
        t.innerHTML = `<span id="globalToastIcon"></span><span id="globalToastMsg"></span>`;
        document.body.appendChild(t);
    }
    const icons = { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' };
    document.getElementById('globalToastIcon').textContent = icons[type] || '✓';
    document.getElementById('globalToastMsg').textContent = msg;
    t.className = 'toast ' + type + ' show';
    clearTimeout(t._timer);
    t._timer = setTimeout(() => t.classList.remove('show'), 3200);
}

/* ─── MODAL HELPERS ─── */
function openModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.add('active');
}
function closeModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.remove('active');
}
document.querySelectorAll('.modal-overlay').forEach(m => {
    m.addEventListener('click', function (e) {
        if (e.target === this) this.classList.remove('active');
    });
});
document.addEventListener('keydown', e => {
    if (e.key === 'Escape')
        document.querySelectorAll('.modal-overlay.active').forEach(m => m.classList.remove('active'));
});

/* ─── AUTHORITY USER ─── */
function getAuthUser() {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser && currentUser.role === 'authority') {
        return {
            name: currentUser.fullname || 'Authority Officer',
            role: 'Authority Officer',
            dept: 'Investigation Department',
            initials: (currentUser.fullname || 'AU').split(' ').map(n => n[0]).join('').toUpperCase()
        };
    }
    return {
        name: 'Rajan Arora',
        role: 'Senior Investigator',
        dept: 'Dept. of Labour',
        initials: 'RA'
    };
}
function loadAuthUser() {
    const u = getAuthUser();
    document.querySelectorAll('.js-auth-name').forEach(el => el.textContent = u.name);
    document.querySelectorAll('.js-auth-role').forEach(el => el.textContent = u.role);
    document.querySelectorAll('.js-auth-initials').forEach(el => el.textContent = u.initials);
    document.querySelectorAll('.js-auth-dept').forEach(el => el.textContent = u.dept);
}
window.addEventListener('DOMContentLoaded', loadAuthUser);

/* ─── LOGOUT ─── */
function logoutAuth() {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authUser');
    window.location.href = '../login.html';
}

/* ─── UTILS ─── */
function formatDate(d) {
    if (!d) return 'N/A';
    return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}
function formatDateTime(d) {
    if (!d) return 'N/A';
    return new Date(d).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}
function priorityBadge(p) {
    const map = { critical: 'badge-urgent', high: 'badge-high', medium: 'badge-medium', low: 'badge-low' };
    return `<span class="badge ${map[p.toLowerCase()] || 'badge-medium'}">${p}</span>`;
}
function statusBadge(s) {
    const map = {
        'NEW': 'badge-new', 'UNDER REVIEW': 'badge-active', 'ASSIGNED': 'badge-progress',
        'IN PROGRESS': 'badge-pending', 'RESOLVED': 'badge-resolved', 'CLOSED': 'badge-closed',
        'ACTIVE': 'badge-active', 'PENDING': 'badge-pending'
    };
    return `<span class="badge ${map[s] || 'badge-new'}">${s}</span>`;
}

// keep authority pages in sync when complaints change elsewhere
window.addEventListener("storage", function(evt) {
    if (evt.key === "complaints") {
        document.dispatchEvent(new Event("complaintsUpdated"));
    }
});