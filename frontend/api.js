/* ============================================================
   api.js  –  SafeReport central API helper
   Backend: http://localhost:8080/api
   ============================================================ */

const API_BASE = "http://localhost:8080/api";

/* ── Auth helpers ─────────────────────────────────────────── */

function getToken() { return localStorage.getItem("token"); }
function getRole() { return localStorage.getItem("role"); }
function getUserId() { return localStorage.getItem("userId"); }
function getUserName() { return localStorage.getItem("fullName"); }

function saveAuth(data) {
    localStorage.setItem("token", data.accessToken);
    localStorage.setItem("role", data.role);
    localStorage.setItem("userId", data.userId);
    localStorage.setItem("fullName", data.fullName);
    localStorage.setItem("email", data.email);
}

function clearAuth() {
    ["token", "role", "userId", "fullName", "email"].forEach(k => localStorage.removeItem(k));
}

/* ── Token expiry check (call on every page load) ─────────── */
function checkTokenExpiry() {
    const token = localStorage.getItem("token");
    if (!token) return;
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        if (payload.exp * 1000 < Date.now()) {
            console.warn("Token expired, logging out.");
            clearAuth();
            redirectToLogin();
        }
    } catch (e) {
        clearAuth();
        redirectToLogin();
    }
}

/* ── Redirect to login (works from any subfolder) ─────────── */
function redirectToLogin() {
    window.location.href = "../login.html";
}

function logout() {
    clearAuth();
    redirectToLogin();
}

/* Alias used by several user-facing pages */
function logoutUser() { logout(); }

/* ── Redirect to login if not authenticated ───────────────── */
function requireAuth(allowedRoles = []) {
    checkTokenExpiry();
    const token = getToken();
    const role = getRole();
    if (!token) { redirectToLogin(); return false; }
    if (allowedRoles.length && !allowedRoles.includes(role)) {
        alert("Access denied.");
        redirectToLogin();
        return false;
    }
    return true;
}

/* ── Core fetch wrapper ───────────────────────────────────── */

async function apiFetch(path, options = {}) {
    const token = getToken();
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    if (token) headers["Authorization"] = "Bearer " + token;

    const res = await fetch(API_BASE + path, { ...options, headers });

    if (res.status === 401) {
        clearAuth();
        redirectToLogin();
        return res;
    }

    return res; // ✅ always return res
}

/* ── Convenience methods ──────────────────────────────────── */

async function apiGet(path) {
    const res = await apiFetch(path);
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiPost(path, body) {
    const res = await apiFetch(path, { method: "POST", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiPut(path, body) {
    const res = await apiFetch(path, { method: "PUT", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiPatch(path, body = {}) {
    const res = await apiFetch(path, { method: "PATCH", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiDelete(path) {
    const res = await apiFetch(path, { method: "DELETE" });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

/* ── Populate topbar with user info ──────────────────────── */

function populateTopbar() {
    const name = getUserName() || "User";
    const role = getRole() || "User";
    const initials = name.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);

    // Support both id="topbarName" and class="user-name"
    const nameElId = document.getElementById("topbarName");
    if (nameElId) nameElId.textContent = name;
    document.querySelectorAll(".user-name").forEach(el => el.textContent = name);

    // Support both id="topbarRole" and class="user-role"
    const roleElId = document.getElementById("topbarRole");
    if (roleElId) roleElId.textContent = role;
    document.querySelectorAll(".user-role").forEach(el => el.textContent = role);

    // Support both id="userAvatar" and class="user-avatar"
    const avatarElId = document.getElementById("userAvatar");
    if (avatarElId) avatarElId.textContent = initials;
    document.querySelectorAll(".user-avatar").forEach(el => {
        // Only set text if no img inside
        if (!el.querySelector("img")) el.textContent = initials;
    });

    // Welcome name
    const welcomeEl = document.getElementById("welcomeName");
    if (welcomeEl) welcomeEl.textContent = name.split(" ")[0]; // first name only
}

/* ── Init sidebar menus (highlight active link) ──────────── */

function initMenus() {
    const current = window.location.pathname.split("/").pop();
    document.querySelectorAll(".sidebar a, .nav-link, .menu-link").forEach(a => {
        if (a.getAttribute("href") === current) a.classList.add("active");
    });
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) logoutBtn.addEventListener("click", logout);
}

/* ── Show API error banner ───────────────────────────────── */

function showApiError(msg) {
    const el = document.getElementById("apiError");
    if (el) { el.textContent = "⚠️ " + msg; el.style.display = "block"; }
    else console.error("API Error:", msg);
}

/* ── Format date ─────────────────────────────────────────── */

function fmtDate(iso) {
    if (!iso) return "—";
    return new Date(iso).toLocaleDateString("en-IN", {
        day: "2-digit", month: "short", year: "numeric"
    });
}

function fmtDateTime(iso) {
    if (!iso) return "—";
    return new Date(iso).toLocaleString("en-IN", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

/* ── Status badge ────────────────────────────────────────── */

function statusBadge(status) {
    const colors = {
        SUBMITTED: "badge-warning",
        UNDER_REVIEW: "badge-info",
        VERIFIED: "badge-primary",
        ASSIGNED: "badge-secondary",
        INVESTIGATING: "badge-primary",
        RESOLVED: "badge-success",
        CLOSED: "badge-success",
        REJECTED: "badge-danger",
        DRAFT: "badge-light"
    };
    return `<span class="badge ${colors[status] || 'badge-secondary'}">${status}</span>`;
}

function priorityBadge(priority) {
    const colors = {
        LOW: "badge-success", MEDIUM: "badge-warning",
        HIGH: "badge-danger", CRITICAL: "badge-dark"
    };
    return `<span class="badge ${colors[priority] || 'badge-secondary'}">${priority}</span>`;
}

/* ── Escape HTML ─────────────────────────────────────────── */

function escHtml(str) {
    return (str || "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

/* ── Menu toggle functions ───────────────────────────────── */

function toggleMenu() {
    const d = document.getElementById("hamburgerDropdown");
    if (d) d.classList.toggle("active");
}

function closeMenu() {
    const d = document.getElementById("hamburgerDropdown");
    if (d) d.classList.remove("active");
}

function toggleUserMenu() {
    const d = document.getElementById("userMenuDropdown");
    if (d) d.classList.toggle("active");
}

// Close dropdowns when clicking outside
document.addEventListener("click", function (e) {
    if (!e.target.closest(".hamburger-menu")) closeMenu();
    if (!e.target.closest(".user-menu")) {
        const d = document.getElementById("userMenuDropdown");
        if (d) d.classList.remove("active");
    }
});

/* ── Welcome name ────────────────────────────────────────── */

function setWelcomeName() {
    const name = getUserName() || "User";
    const el = document.getElementById("welcomeName");
    if (el) el.textContent = name;
    const av = document.getElementById("userAvatar");
    if (av) av.textContent = name.charAt(0).toUpperCase();
}

/* ── Status CSS class ────────────────────────────────────── */

function statusClass(status) {
    const map = {
        SUBMITTED: "status-submitted",
        UNDER_REVIEW: "status-review",
        ASSIGNED: "status-assigned",
        INVESTIGATING: "status-progress",
        RESOLVED: "status-resolved",
        CLOSED: "status-closed",
        REJECTED: "status-danger",
        DRAFT: "status-draft"
    };
    return map[status] || "status-draft";
}

/* ── Priority CSS class ──────────────────────────────────── */

function priorityClass(priority) {
    const map = {
        CRITICAL: "priority-CRITICAL",
        HIGH: "priority-HIGH",
        MEDIUM: "priority-MEDIUM",
        LOW: "priority-LOW"
    };
    return map[priority] || "priority-LOW";
}

/* ── Format date ─────────────────────────────────────────── */

function formatDate(iso) {
    if (!iso) return "—";
    return new Date(iso).toLocaleDateString("en-IN", {
        day: "2-digit", month: "short", year: "numeric"
    });
}

function formatDateTime(iso) {
    if (!iso) return "—";
    return new Date(iso).toLocaleString("en-IN", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

/* ── Toast notification ──────────────────────────────────── */

function showToast(msg, type = "success") {
    let toast = document.getElementById("globalToast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "globalToast";
        toast.style.cssText = `
            position:fixed;bottom:24px;right:24px;padding:14px 20px;
            border-radius:12px;font-size:14px;font-weight:600;
            z-index:9999;display:none;max-width:320px;
            box-shadow:0 8px 25px rgba(0,0,0,0.15);
            animation:slideInRight 0.3s ease;
        `;
        document.body.appendChild(toast);
    }
    const colors = {
        success: "background:#10b981;color:white",
        error: "background:#ef4444;color:white",
        warning: "background:#f59e0b;color:white",
        info: "background:#6366f1;color:white"
    };
    toast.style.cssText += ";" + (colors[type] || colors.success);
    toast.textContent = msg;
    toast.style.display = "block";
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => { toast.style.display = "none"; }, 3000);
}

/* ── Notification bell badge (call on any page) ──────────── */

async function loadNotifBadge() {
    try {
        const res = await apiFetch("/notifications/unread-count");
        if (!res) return;
        const json = await res.json();
        const count = json.data || 0;
        // Update any badge element on the page
        document.querySelectorAll(".notif-badge, #notifBadge").forEach(el => {
            el.textContent = count > 9 ? "9+" : count;
            el.style.display = count > 0 ? "flex" : "none";
        });
    } catch (e) { /* silent fail */ }
}

// Call on every page load automatically
window.addEventListener("load", () => {
    if (getToken()) {
        loadNotifBadge();
        // Refresh badge every 60 seconds
        setInterval(loadNotifBadge, 60000);
    }
});