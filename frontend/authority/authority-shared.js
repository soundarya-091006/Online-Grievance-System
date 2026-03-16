/* ============================================================
   authority-shared.js  –  SafeReport Authority Portal (API-based)
   ============================================================ */

const API_BASE = "http://localhost:8080/api";

/* ─── AUTH HELPERS ─── */
function getToken()   { return localStorage.getItem("token"); }
function getRole()    { return localStorage.getItem("role"); }
function getUserName(){ return localStorage.getItem("fullName"); }

function requireAuthority() {
    const token = getToken();
    const role  = getRole();
    if (!token || role !== "AUTHORITY") {
        window.location.href = "../login.html";
        return false;
    }
    return true;
}

function doLogout() {
    ["token","role","userId","fullName","email"].forEach(k => localStorage.removeItem(k));
    window.location.href = "../login.html";
}

/* ─── API FETCH ─── */
async function authFetch(path, options = {}) {
    const token = getToken();
    const isFormData = options.body instanceof FormData;
    const headers = isFormData
        ? { ...(options.headers || {}) }
        : { "Content-Type": "application/json", ...(options.headers || {}) };
    if (token) headers["Authorization"] = "Bearer " + token;
    return fetch(API_BASE + path, { ...options, headers });
}

async function authGet(path) {
    const res  = await authFetch(path);
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function authPost(path, body) {
    const res  = await authFetch(path, { method: "POST", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function authPatch(path, body = {}) {
    const res  = await authFetch(path, { method: "PATCH", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

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
    const name = getUserName() || 'Authority Officer';
    return {
        name: name,
        role: 'Authority Officer',
        dept: 'Investigation Department',
        initials: name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2)
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

/* ─── UTILS ─── */
function formatDate(d) {
    if (!d) return 'N/A';
    return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}
function formatDateTime(d) {
    if (!d) return 'N/A';
    return new Date(d).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}
function escHtml(str) {
    return (str || "").replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;");
}