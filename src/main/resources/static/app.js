const $ = (s, r = document) => r.querySelector(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g,
  c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

const state = {
  token: localStorage.getItem('token'),
  user: null,        // the logged-in student (includes email)
  students: [],      // everyone else's public profiles
  tab: 'matches',
  authMode: 'login',
};
const me = () => state.user;

async function api(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (state.token) headers.Authorization = 'Bearer ' + state.token;
  const res = await fetch('/api' + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  if (res.status === 401 && !path.startsWith('/auth/') && state.token) {
    logout();
    throw new Error('Your session has expired. Log in again.');
  }
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || 'Something went wrong. Try again.');
  return data;
}

let toastTimer;
// A popover sits in the top layer, so toasts stay visible above open dialogs.
$('#toast').showPopover();
function toast(msg, isError = false) {
  const t = $('#toast');
  t.textContent = msg;
  t.className = 'show' + (isError ? ' err' : '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (t.className = ''), 3200);
}
const run = fn => async (...a) => { try { await fn(...a); } catch (e) { toast(e.message, true); } };

const chips = (set, kind, removable = false) => [...set].sort().map(s =>
  `<span class="chip ${kind}">${esc(s)}${removable
    ? `<button data-action="remove-skill" data-skill="${esc(s)}" data-teaching="${kind === 'teach'}" aria-label="Remove ${esc(s)}">×</button>` : ''}</span>`).join('')
  || `<span class="sub">None yet</span>`;

/* ---------- session ---------- */
function logout() {
  state.token = null; state.user = null; state.students = []; state.tab = 'matches';
  localStorage.removeItem('token');
  showShell(); render();
}

function showShell() {
  const on = !!state.user;
  $('#who').hidden = !on; $('#tabs').hidden = !on;
  if (on) $('#whoName').textContent = state.user.name;
}

async function boot() {
  if (!state.token) { showShell(); return render(); }
  try {
    const [user, students] = await Promise.all([api('/auth/me'), api('/students')]);
    state.user = user;
    state.students = students.filter(s => s.id !== user.id);
  } catch (e) { logout(); throw e; }
  showShell();
  await refreshBadge();
  await render();
}

async function refreshBadge() {
  const badge = $('#badge');
  const swaps = await api(`/students/${state.user.id}/swaps`);
  const n = swaps.filter(s => s.receiverId === state.user.id && s.status === 'PENDING').length;
  badge.textContent = n; badge.hidden = n === 0;
}

async function render() {
  if (!state.user) return renderAuth();
  document.querySelectorAll('#tabs button').forEach(b => {
    if (b.dataset.tab === state.tab) b.setAttribute('aria-current', 'page'); else b.removeAttribute('aria-current');
  });
  await ({ matches: renderMatches, browse: renderBrowse, requests: renderRequests, profile: renderProfile })[state.tab]();
}

/* ---------- views ---------- */
function renderAuth() {
  const reg = state.authMode === 'register';
  $('#view').innerHTML = `
    <section class="card auth">
      <h2>${reg ? 'Create your account' : 'Log in'}</h2>
      <p class="sub">${reg ? 'List what you can teach and what you want to learn, then find people to swap with.'
                           : 'Log in to see your matches and swap requests.'}</p>
      <form id="authForm" class="stack">
        ${reg ? '<label>Name <input name="name" required maxlength="80" autocomplete="name"></label>' : ''}
        <label>Email <input name="email" type="email" required autocomplete="email"></label>
        ${reg ? '<label>Short bio <textarea name="bio" rows="2" maxlength="300" placeholder="Course, year, what you\'re after"></textarea></label>' : ''}
        <label>Password <input name="password" type="password" required minlength="${reg ? 8 : 1}" maxlength="72"
          autocomplete="${reg ? 'new-password' : 'current-password'}">${reg ? '<span class="sub">At least 8 characters</span>' : ''}</label>
        <div class="row">
          <button class="btn" type="submit">${reg ? 'Create account' : 'Log in'}</button>
          <button class="btn ghost" type="button" data-action="switch-auth">${reg ? 'I already have an account' : 'Create an account'}</button>
        </div>
      </form>
    </section>`;
  $('#authForm').addEventListener('submit', run(async e => {
    e.preventDefault();
    const data = await api(reg ? '/auth/register' : '/auth/login', { method: 'POST', body: Object.fromEntries(new FormData(e.target)) });
    state.token = data.token; localStorage.setItem('token', data.token);
    state.tab = reg ? 'profile' : 'matches';
    if (reg) toast('Account created. Add your skills below.');
    await boot();
  }));
}

async function renderMatches() {
  const matches = await api(`/students/${state.user.id}/matches`);
  $('#view').innerHTML = `<p class="lead">People who teach something you want to learn <em>and</em> want to learn something you teach. Best matches first.</p>` +
    (matches.length ? matches.map(m => `
      <article class="card">
        <header><div><h3>${esc(m.student.name)}</h3><p class="sub">${esc(m.student.bio || 'No bio yet')}</p></div>
          <span class="score">${m.score} skill${m.score === 1 ? '' : 's'} line up</span></header>
        <div class="trade">
          <div><h4>${esc(m.student.name.split(' ')[0])} can teach you</h4><div class="chips">${chips(m.theyCanTeachYou, 'teach')}</div></div>
          <span class="arrow" aria-hidden="true">⇄</span>
          <div><h4>You can teach ${esc(m.student.name.split(' ')[0])}</h4><div class="chips">${chips(m.youCanTeachThem, 'learn')}</div></div>
        </div>
        <div class="row"><button class="btn" data-action="swap" data-id="${m.student.id}">Request a swap</button></div>
      </article>`).join('')
      : `<div class="empty"><strong>No matches yet</strong>Add skills you teach and skills you want to learn in “My profile”. A match appears when someone covers both sides.</div>`);
}

async function renderBrowse() {
  $('#view').innerHTML = `<input class="search" id="q" type="search" placeholder="Search by skill, for example “python”" aria-label="Search by skill"><div id="list"></div>`;
  const list = $('#list');
  const load = run(async () => {
    const q = $('#q').value.trim();
    const students = (await api('/students' + (q ? `?skill=${encodeURIComponent(q)}` : ''))).filter(s => s.id !== state.user.id);
    list.innerHTML = students.length ? students.map(s => `
      <article class="card" style="margin-bottom:16px">
        <header><div><h3>${esc(s.name)}</h3><p class="sub">${esc(s.bio || 'No bio yet')}</p></div>
          <button class="btn small" data-action="swap" data-id="${s.id}">Request a swap</button></header>
        <div class="chips">${chips(s.skillsToTeach, 'teach')}</div>
        <div class="chips">${chips(s.skillsToLearn, 'learn')}</div>
      </article>`).join('') : `<div class="empty"><strong>No students found</strong>Try a different skill, or clear the search.</div>`;
  });
  let timer; $('#q').addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(load, 250); });
  await load();
}

async function renderRequests() {
  const swaps = await api(`/students/${state.user.id}/swaps`);
  const card = s => {
    const incoming = s.receiverId === state.user.id;
    const line = incoming
      ? `<strong>${esc(s.requesterName)}</strong> offers <b>${esc(s.skillOffered)}</b> in exchange for your <b>${esc(s.skillRequested)}</b>`
      : `You offered <b>${esc(s.skillOffered)}</b> to <strong>${esc(s.receiverName)}</strong> in exchange for <b>${esc(s.skillRequested)}</b>`;
    const actions = incoming && s.status === 'PENDING'
      ? `<button class="btn small" data-action="status" data-id="${s.id}" data-status="ACCEPTED">Accept</button>
         <button class="btn small ghost" data-action="status" data-id="${s.id}" data-status="REJECTED">Decline</button>`
      : s.status === 'ACCEPTED'
        ? `<button class="btn small" data-action="status" data-id="${s.id}" data-status="COMPLETED">Mark as completed</button>` : '';
    return `<article class="card"><p>${line}</p>
      <div class="row"><span class="status ${s.status}">${s.status[0] + s.status.slice(1).toLowerCase()}</span>${actions}</div></article>`;
  };
  const inbox = swaps.filter(s => s.receiverId === state.user.id), sent = swaps.filter(s => s.requesterId === state.user.id);
  $('#view').innerHTML = `
    <h2 class="section">Received</h2>${inbox.map(card).join('') || '<div class="empty">Nothing received yet. Requests from other students show up here.</div>'}
    <h2 class="section">Sent</h2>${sent.map(card).join('') || '<div class="empty">You haven’t sent a request. Open “Matches” to start one.</div>'}`;
}

function renderProfile() {
  const u = state.user;
  $('#view').innerHTML = `
    <form class="card" id="profileForm">
      <h3>Your details</h3>
      <label>Name <input name="name" value="${esc(u.name)}" required maxlength="80"></label>
      <label>Email <input value="${esc(u.email)}" disabled></label>
      <label>Short bio <textarea name="bio" rows="3" maxlength="300">${esc(u.bio || '')}</textarea></label>
      <div class="row"><button class="btn" type="submit">Save changes</button></div>
    </form>
    <section class="card">
      <h3>Your skills</h3>
      <div><h4 class="sub">I can teach</h4><div class="chips">${chips(u.skillsToTeach, 'teach', true)}</div></div>
      <div><h4 class="sub">I want to learn</h4><div class="chips">${chips(u.skillsToLearn, 'learn', true)}</div></div>
      <form class="row" id="skillForm">
        <input name="skill" placeholder="e.g. photoshop" required maxlength="40" aria-label="Skill name">
        <select name="type" aria-label="Skill type"><option value="true">I can teach it</option><option value="false">I want to learn it</option></select>
        <button class="btn" type="submit">Add skill</button>
      </form>
    </section>
    <section class="card"><h3>Delete account</h3><p class="sub">Removes your profile and every swap request you’re part of.</p>
      <div class="row"><button class="btn danger" data-action="delete-me">Delete my account</button></div></section>`;

  $('#profileForm').addEventListener('submit', run(async e => {
    e.preventDefault();
    const f = new FormData(e.target);
    await api(`/students/${state.user.id}`, { method: 'PUT', body: { name: f.get('name'), bio: f.get('bio') } });
    toast('Changes saved'); await boot();
  }));
  $('#skillForm').addEventListener('submit', run(async e => {
    e.preventDefault();
    const f = new FormData(e.target);
    await api(`/students/${state.user.id}/skills`, { method: 'POST', body: { skill: f.get('skill'), teaching: f.get('type') === 'true' } });
    toast('Skill added'); await boot();
  }));
}

/* ---------- events ---------- */
document.addEventListener('click', run(async e => {
  const t = e.target.closest('[data-action], [data-close], [data-tab]');
  if (!t) return;
  if (t.dataset.close !== undefined) return t.closest('dialog').close();
  if (t.dataset.tab) { state.tab = t.dataset.tab; return render(); }

  switch (t.dataset.action) {
    case 'switch-auth': state.authMode = state.authMode === 'login' ? 'register' : 'login'; return render();
    case 'swap': return openSwap(Number(t.dataset.id));
    case 'status':
      await api(`/swaps/${t.dataset.id}`, { method: 'PATCH', body: { status: t.dataset.status } });
      toast('Request updated'); await refreshBadge(); return render();
    case 'remove-skill':
      await api(`/students/${state.user.id}/skills?skill=${encodeURIComponent(t.dataset.skill)}&teaching=${t.dataset.teaching}`, { method: 'DELETE' });
      return boot();
    case 'delete-me':
      if (!confirm('Delete your account and all your swap requests? This can’t be undone.')) return;
      await api(`/students/${state.user.id}`, { method: 'DELETE' });
      logout(); return toast('Account deleted');
  }
}));

$('#logoutBtn').addEventListener('click', () => { logout(); toast('Logged out'); });

async function openSwap(otherId) {
  let other = state.students.find(s => s.id === otherId);
  if (!other) other = await api(`/students/${otherId}`);
  if (!state.user.skillsToTeach.length) return toast('Add a skill you can teach first (My profile).', true);
  if (!other.skillsToTeach.length) return toast(`${other.name} hasn’t listed a skill to teach yet.`, true);
  const opts = set => [...set].sort().map(s => `<option>${esc(s)}</option>`).join('');
  const f = $('#swapForm');
  f.offered.innerHTML = opts(state.user.skillsToTeach);
  f.requested.innerHTML = opts(other.skillsToTeach);
  $('#swapTitle').textContent = `Request a swap with ${other.name}`;
  f.dataset.receiver = otherId;
  $('#swapDialog').showModal();
}

$('#swapForm').addEventListener('submit', run(async e => {
  e.preventDefault();
  const f = e.target;
  await api('/swaps', { method: 'POST', body: {
    receiverId: Number(f.dataset.receiver), skillOffered: f.offered.value, skillRequested: f.requested.value } });
  $('#swapDialog').close(); toast('Request sent'); state.tab = 'requests'; await refreshBadge(); render();
}));

run(boot)();
