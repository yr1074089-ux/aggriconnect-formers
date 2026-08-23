/* AgriConnect — shared client helpers
   Same-origin API (this static frontend is served by the Spring Boot app),
   so requests just target relative /api/... paths. */

const API_BASE = '';

const Session = {
  save(auth) {
    localStorage.setItem('ac_token', auth.token);
    localStorage.setItem('ac_userId', auth.userId);
    localStorage.setItem('ac_name', auth.name);
    localStorage.setItem('ac_email', auth.email);
    localStorage.setItem('ac_role', auth.role);
  },
  token() { return localStorage.getItem('ac_token'); },
  role() { return localStorage.getItem('ac_role'); },
  name() { return localStorage.getItem('ac_name'); },
  email() { return localStorage.getItem('ac_email'); },
  userId() { return localStorage.getItem('ac_userId'); },
  isLoggedIn() { return !!this.token(); },
  clear() {
    ['ac_token', 'ac_userId', 'ac_name', 'ac_email', 'ac_role'].forEach(k => localStorage.removeItem(k));
  },
  dashboardFor(role) {
    switch (role) {
      case 'FARMER': return 'farmer-dashboard.html';
      case 'CUSTOMER': return 'customer-dashboard.html';
      case 'DELIVERY_PARTNER': return 'delivery-dashboard.html';
      case 'ADMIN': return 'admin-dashboard.html';
      default: return 'login.html';
    }
  }
};

/** Guards a dashboard page: redirects to login if not authenticated / wrong role. */
function requireRole(expectedRole) {
  if (!Session.isLoggedIn()) {
    window.location.href = 'login.html';
    return false;
  }
  if (expectedRole && Session.role() !== expectedRole) {
    window.location.href = Session.dashboardFor(Session.role());
    return false;
  }
  return true;
}

function logout() {
  Session.clear();
  window.location.href = 'index.html';
}

/** Fetch wrapper that attaches the JWT and normalizes error handling. */
async function authFetch(path, options = {}) {
  const headers = Object.assign(
    { 'Content-Type': 'application/json' },
    options.headers || {}
  );
  const token = Session.token();
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const res = await fetch(API_BASE + path, Object.assign({}, options, { headers }));

  if (res.status === 401) {
    Session.clear();
    window.location.href = 'login.html';
    throw new Error('Session expired');
  }

  let body = null;
  const text = await res.text();
  try { body = text ? JSON.parse(text) : null; } catch (e) { body = text; }

  if (!res.ok) {
    const message = (body && body.message) ? body.message : ('Request failed (' + res.status + ')');
    throw new Error(message);
  }
  return body;
}

function money(amount) {
  const n = Number(amount || 0);
  return '₹' + n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function timeAgo(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) +
    ' · ' + d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
}

function toast(message, type = 'info') {
  let wrap = document.querySelector('.toast-wrap');
  if (!wrap) {
    wrap = document.createElement('div');
    wrap.className = 'toast-wrap';
    document.body.appendChild(wrap);
  }
  const el = document.createElement('div');
  el.className = 'toast' + (type === 'error' ? ' error' : type === 'success' ? ' success' : '');
  el.textContent = message;
  wrap.appendChild(el);
  setTimeout(() => el.remove(), 4200);
}

function statusBadgeClass(status) {
  const map = {
    PLACED: 'badge-muted', CONFIRMED: 'badge-canopy', OUT_FOR_DELIVERY: 'badge-sundried',
    DELIVERED: 'badge-sprout', CANCELLED: 'badge-clay',
    UNASSIGNED: 'badge-muted', ASSIGNED: 'badge-canopy', PICKED_UP: 'badge-sundried', IN_TRANSIT: 'badge-sundried',
    PENDING: 'badge-muted', COMPLETED: 'badge-sprout', FAILED: 'badge-clay'
  };
  return map[status] || 'badge-muted';
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str == null ? '' : String(str);
  return div.innerHTML;
}

/** Renders the shared dashboard nav bar into #dashNav (role, name, logout). */
function renderDashNav(activeLabel) {
  const el = document.getElementById('dashNav');
  if (!el) return;
  el.innerHTML = `
    <div class="nav-inner">
      <a class="brand" href="index.html">
        <svg class="brand-mark" viewBox="0 0 32 32" fill="none"><path d="M16 4C10 8 8 14 8 19a8 8 0 0 0 16 0c0-5-2-11-8-15Z" stroke="#24402F" stroke-width="1.6"/><path d="M16 30V16" stroke="#24402F" stroke-width="1.6"/></svg>
        AgriConnect
      </a>
      <div class="nav-user">
        <span class="badge badge-canopy">${escapeHtml(activeLabel)}</span>
        <span class="muted">${escapeHtml(Session.name() || '')}</span>
        <button class="btn btn-ghost btn-sm" onclick="logout()">Log out</button>
      </div>
    </div>`;
}
