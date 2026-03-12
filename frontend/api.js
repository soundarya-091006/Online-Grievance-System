/* ============================================================
   api.js  –  SafeReport central API helper
   Backend: http://localhost:8080/api
   ============================================================ */

const API_BASE = "http://localhost:8080/api";

/* ── Auth helpers ─────────────────────────────────────────── */

function getToken()   { return localStorage.getItem("token"); }
function getRole()    { return localStorage.getItem("role"); }
function getUserId()  { return localStorage.getItem("userId"); }
function getUserName(){ return localStorage.getItem("fullName"); }

function saveAuth(data) {
    localStorage.setItem("token",    data.accessToken);
    localStorage.setItem("role",     data.role);
    localStorage.setItem("userId",   data.userId);
    localStorage.setItem("fullName", data.fullName);
    localStorage.setItem("email",    data.email);
}

function clearAuth() {
    ["token","role","userId","fullName","email"].forEach(k => localStorage.removeItem(k));
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
    } catch(e) {
        clearAuth();
        redirectToLogin();
    }
}

/* ── Redirect to login (works from any subfolder) ─────────── */
function redirectToLogin() {
    const depth = window.location.pathname.split("/").length - 2;
    const prefix = depth > 1 ? "../".repeat(depth - 1) : "";
    window.location.href = prefix + "login.html";
}

function logout() {
    clearAuth();
    redirectToLogin();
}

/* ── Redirect to login if not authenticated ───────────────── */
function requireAuth(allowedRoles = []) {
    checkTokenExpiry();
    const token = getToken();
    const role  = getRole();
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
    const res  = await apiFetch(path);
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiPost(path, body) {
    const res  = await apiFetch(path, { method: "POST", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiPut(path, body) {
    const res  = await apiFetch(path, { method: "PUT", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiPatch(path, body = {}) {
    const res  = await apiFetch(path, { method: "PATCH", body: JSON.stringify(body) });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

async function apiDelete(path) {
    const res  = await apiFetch(path, { method: "DELETE" });
    const json = await res.json();
    if (!json.success) throw new Error(json.message || "Request failed");
    return json.data;
}

/* ── Populate topbar with user info ──────────────────────── */

function populateTopbar() {
    const name = getUserName() || "User";
    const role = getRole()     || "";
    const nameEl = document.getElementById("topbarName");
    const roleEl = document.getElementById("topbarRole");
    if (nameEl) nameEl.textContent = name;
    if (roleEl) roleEl.textContent = role;
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
        SUBMITTED:     "badge-warning",
        UNDER_REVIEW:  "badge-info",
        VERIFIED:      "badge-primary",
        ASSIGNED:      "badge-secondary",
        INVESTIGATING: "badge-primary",
        RESOLVED:      "badge-success",
        CLOSED:        "badge-success",
        REJECTED:      "badge-danger",
        DRAFT:         "badge-light"
    };
    return `<span class="badge ${colors[status] || 'badge-secondary'}">${status}</span>`;
}

function priorityBadge(priority) {
    const colors = {
        LOW: "badge-success", MEDIUM: "badge-warning",
        HIGH: "badge-danger",  CRITICAL: "badge-dark"
    };
    return `<span class="badge ${colors[priority] || 'badge-secondary'}">${priority}</span>`;
}

/* ── Escape HTML ─────────────────────────────────────────── */

function escHtml(str) {
    return (str || "").replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;");
}