// ══════════════════════════════════════════════════════
//  Testify — Shared JS Utilities (Final Version)
// ══════════════════════════════════════════════════════

const API_BASE = 'http://localhost:8080/api';

// ── Theme Management ─────────────────────────────────
const Theme = {
  KEY: 'ep_theme',
  get() {
    return localStorage.getItem(this.KEY) || 'dark';
  },
  set(theme) {
    localStorage.setItem(this.KEY, theme);
    document.documentElement.setAttribute('data-theme', theme);
    this._updateToggles(theme);
  },
  toggle() {
    this.set(this.get() === 'dark' ? 'light' : 'dark');
  },
  init() {
    const t = this.get();
    document.documentElement.setAttribute('data-theme', t);
  },
  _updateToggles(theme) {
    document.querySelectorAll('.theme-toggle__icon').forEach(el => {
      el.textContent = theme === 'dark' ? '☀️' : '🌙';
    });
    document.querySelectorAll('.theme-toggle__label').forEach(el => {
      el.textContent = theme === 'dark' ? 'Light' : 'Dark';
    });
  }
};

// Init theme immediately to avoid flash
Theme.init();

// ── Session helpers ──────────────────────────────────
const Session = {
  set(user) { sessionStorage.setItem('ep_user', JSON.stringify(user)); },
  get()     { try { return JSON.parse(sessionStorage.getItem('ep_user')); } catch { return null; } },
  clear()   { sessionStorage.removeItem('ep_user'); },
  require(adminOnly = false) {
    const user = Session.get();
    if (!user) { location.href = '../index.html'; return null; }
    if (adminOnly && user.role !== 'ADMIN') { location.href = 'dashboard.html'; return null; }
    return user;
  }
};

// ── API wrapper ──────────────────────────────────────
const Api = {
  async _fetch(method, path, body, isForm = false) {
    const opts = { method, headers: {} };
    if (body && !isForm) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    } else if (isForm) {
      opts.body = body;
    }
    const res = await fetch(API_BASE + path, opts);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  },
  get:    (path)        => Api._fetch('GET',    path),
  post:   (path, body)  => Api._fetch('POST',   path, body),
  put:    (path, body)  => Api._fetch('PUT',    path, body),
  del:    (path)        => Api._fetch('DELETE', path),
  upload: (path, form)  => Api._fetch('POST',   path, form, true),
};

// ── Alert helper ─────────────────────────────────────
function showAlert(containerId, message, type = 'error') {
  const el = document.getElementById(containerId);
  if (!el) return;
  const icons = { error: '✕', success: '✓', info: 'ℹ', warning: '⚠' };
  el.innerHTML = `
    <div class="alert alert--${type}">
      <span>${icons[type] || ''}</span>
      <span>${message}</span>
    </div>`;
  el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function clearAlert(containerId) {
  const el = document.getElementById(containerId);
  if (el) el.innerHTML = '';
}

// ── Navbar renderer ──────────────────────────────────
function renderNavbar(user) {
  const nav = document.getElementById('navbar');
  if (!nav) return;
  const theme = Theme.get();
  nav.innerHTML = `
    <div class="navbar__inner">
      <a class="navbar__brand" href="dashboard.html">
        <span>Testify</span> - An Exam Portal
      </a>
      <div class="navbar__actions">
        <span class="navbar__user mono">${user.name}</span>
        ${user.role === 'ADMIN' ? `<a class="btn btn--sm btn--secondary" href="admin.html">Admin</a>` : ''}
        <button class="theme-toggle" onclick="Theme.toggle()" title="Toggle theme">
          <span class="theme-toggle__icon">${theme === 'dark' ? '☀️' : '🌙'}</span>
          <span class="theme-toggle__label">${theme === 'dark' ? 'Light' : 'Dark'}</span>
        </button>
        <button class="btn btn--sm btn--ghost" onclick="logout()">Logout</button>
      </div>
    </div>`;
}

function logout() {
  Session.clear();
  location.href = '../index.html';
}

// ── Format seconds ────────────────────────────────────
function formatTime(sec) {
  const m = Math.floor(sec / 60).toString().padStart(2, '0');
  const s = (sec % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

// ── Debounce ─────────────────────────────────────────
function debounce(fn, ms = 300) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); }
}

// ── Auto-save badge helper ────────────────────────────
const AutoSave = {
  _badge: null,
  _timer: null,
  _hideTimer: null,

  show(saving = false) {
    if (!this._badge) {
      this._badge = document.createElement('div');
      this._badge.className = 'autosave-badge';
      this._badge.innerHTML = `<span class="dot"></span><span class="label">Saved</span>`;
      document.body.appendChild(this._badge);
    }
    clearTimeout(this._hideTimer);
    this._badge.classList.toggle('saving', saving);
    this._badge.querySelector('.label').textContent = saving ? 'Saving…' : 'Saved';
    this._badge.classList.add('visible');
    if (!saving) {
      this._hideTimer = setTimeout(() => {
        if (this._badge) this._badge.classList.remove('visible');
      }, 2000);
    }
  },

  hide() {
    if (this._badge) this._badge.classList.remove('visible');
  }
};
