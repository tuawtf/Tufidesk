/* ==========================================================================
   TufiDESK PWA - Application Logic (1:1 Android Jetpack Compose Replica)
   ========================================================================== */

// --- Initial Realistic Homeworks ---
const INITIAL_HOMEWORKS = [
  {
    id: 'hw-1',
    title: 'Türev ve İntegral Uygulamaları Test 4',
    course: 'Matematik',
    date: '2026-10-05', // Today
    priority: 'high',
    desc: 'Sayfa 142-146 arasındaki tüm sorular bitirilecek.',
    completed: false
  },
  {
    id: 'hw-2',
    title: 'Elektromanyetik İndüksiyon Soru Çözümü',
    course: 'Fizik',
    date: '2026-10-06', // Tomorrow
    priority: 'normal',
    desc: 'Manyetik akı ve Faraday yasası örnekleri.',
    completed: false
  },
  {
    id: 'hw-3',
    title: 'Organik Kimya Fonksiyonel Gruplar Tablosu',
    course: 'Kimya',
    date: '2026-10-09',
    priority: 'normal',
    desc: 'Alkoller ve Eterler reaksiyon mekanizmaları.',
    completed: false
  },
  {
    id: 'hw-4',
    title: 'Divan Edebiyatı Nazım Şekilleri Özeti',
    course: 'Edebiyat',
    date: '2026-10-04', // Overdue
    priority: 'high',
    desc: 'Gazel, kaside ve mesnevi özellikleri.',
    completed: true
  }
];

const INITIAL_NOTES = [
  { id: '1', title: 'Matematik Formülleri', content: 'Türev: (x^n)\' = n*x^(n-1)\nİntegral: ∫x^n dx = (x^(n+1))/(n+1) + C', color: '#DBEAFE' },
  { id: '2', title: 'Haftalık Çalışma Hedefi', content: 'Her gün en az 60 soru ve 2 Pomodoro odak çalışması.', color: '#FEF3C7' },
  { id: '3', title: 'Fizik Sağ El Kuralı', content: 'Başparmak: Akım (I)\nDört parmak: Manyetik Alan (B)\nAvuç içi: Manyetik Kuvvet (F)', color: '#D1FAE5' }
];

// --- Course Metadata (Color & Icon matching Android CourseIconHelper.kt) ---
const COURSE_META = {
  'Matematik': { color: '#2563EB', icon: '📐' },
  'Fizik':     { color: '#0284C7', icon: '⚡' },
  'Kimya':     { color: '#7C3AED', icon: '🧪' },
  'Biyoloji':  { color: '#059669', icon: '🧬' },
  'Edebiyat':  { color: '#E11D48', icon: '📖' },
  'Tarih':     { color: '#D97706', icon: '🏛️' },
  'Coğrafya':  { color: '#0D9488', icon: '🌍' },
  'İngilizce': { color: '#4F46E5', icon: '🇬🇧' },
  'Genel':     { color: '#2563EB', icon: '📚' }
};

// --- State ---
let homeworks = JSON.parse(localStorage.getItem('tufidesk_hw')) || INITIAL_HOMEWORKS;
let notes = JSON.parse(localStorage.getItem('tufidesk_notes')) || INITIAL_NOTES;
let currentTab = 'home';
let currentTimeFilter = 'all';
let currentSearchQuery = '';
let isCompletedExpanded = true;
let selectedDate = new Date();
let deferredInstallPrompt = null;
let respectCount = 0;

// Timer State
let timerTotalSec = 25 * 60;
let timerRemainSec = 25 * 60;
let timerActive = false;
let timerHandle = null;

// ==========================================================================
// 1. Dynamic Island Top Notch Overlay (Exact spring pill notification)
// ==========================================================================
const dynamicIslandNotch = document.getElementById('dynamicIslandNotch');
const islandIcon = document.getElementById('islandIcon');
const islandTitle = document.getElementById('islandTitle');
const islandMsg = document.getElementById('islandMsg');
let islandTimeout = null;

function triggerDynamicIsland(icon, title, message, durationMs = 2800) {
  if (islandTimeout) clearTimeout(islandTimeout);
  
  islandIcon.textContent = icon;
  islandTitle.textContent = title;
  islandMsg.textContent = message;
  
  dynamicIslandNotch.classList.add('expanded');
  if (navigator.vibrate) navigator.vibrate([20, 40, 20]);

  islandTimeout = setTimeout(() => {
    dynamicIslandNotch.classList.remove('expanded');
  }, durationMs);
}

// Live Status Bar Clock
function updateClock() {
  const now = new Date();
  const timeStr = now.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });
  const timeEl = document.getElementById('deviceTime');
  if (timeEl) timeEl.textContent = timeStr;
}
setInterval(updateClock, 1000);
updateClock();

// ==========================================================================
// 2. Navigation & Morphing Island Bottom Bar (1:1 Android)
// ==========================================================================
const screenTitlesMap = {
  home:     { title: 'TufiDESK', subtitle: 'Ödevler & Görevler' },
  calendar: { title: 'Ödev Takvimi', subtitle: 'Aylık & Haftalık Görünüm' },
  tufiai:   { title: 'Tufi AI Asistan', subtitle: 'Yapay Zeka Çalışma Koçu' },
  stats:    { title: 'İstatistikler', subtitle: 'Başarı & Odak Analizi' },
  notes:    { title: 'Ders Notları', subtitle: 'Hızlı Hatırlatıcılar' },
  tools:    { title: 'Öğrenci Araçları', subtitle: 'Yardımcı Gereçler' },
  settings: { title: 'Ayarlar', subtitle: 'Görünüm & Sistem' }
};

const dockNormalIsland = document.getElementById('dockNormalIsland');
const dockExpandedTextbox = document.getElementById('dockExpandedTextbox');
const dockTufiTab = document.getElementById('dockTufiTab');
const dockTufiIcon = document.getElementById('dockTufiIcon');
const dockTufiLabel = document.getElementById('dockTufiLabel');
const fabAddHwBtn = document.getElementById('fabAddHwBtn');

function openScreenTab(tabId) {
  currentTab = tabId;

  // Switch active screen
  document.querySelectorAll('.app-screen').forEach(s => s.classList.remove('active'));
  const target = document.getElementById(`screen-${tabId}`);
  if (target) target.classList.add('active');

  // Update Top Bar Titles
  const meta = screenTitlesMap[tabId] || { title: 'TufiDESK', subtitle: '' };
  document.getElementById('screenMainTitle').textContent = meta.title;
  document.getElementById('screenSubTitle').textContent = meta.subtitle;

  // Toggle FAB (only on Home)
  fabAddHwBtn.style.display = (tabId === 'home') ? 'flex' : 'none';

  // Morphing Bottom Bar:
  // When inside Tufi AI: middle button says "Sohbet" with ChatBubble icon
  if (tabId === 'tufiai') {
    dockTufiLabel.textContent = 'Sohbet';
    dockTufiIcon.innerHTML = '<path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>';
  } else {
    // When on any other menu: resets to "Tufi AI" with star icon
    dockTufiLabel.textContent = 'Tufi AI';
    dockTufiIcon.innerHTML = '<path d="m19 9 1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 20l2.5-5.5L17 12z"/>';
    // Collapse any open textbox
    collapseToIslandDock();
  }

  // Active tab pill highlighting
  document.querySelectorAll('.dock-tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tabId === tabId);
  });

  if (navigator.vibrate) navigator.vibrate(8);

  // Tab refreshers
  if (tabId === 'calendar') renderCalendar();
  if (tabId === 'stats') updateStatsUI();
  if (tabId === 'notes') renderNotes();
}

// Bind Dock Buttons
document.querySelectorAll('.dock-tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    const target = btn.dataset.tabId;
    if (target === 'tufiai' && currentTab === 'tufiai') {
      // If already in Tufi AI and user taps "Sohbet", expand to the text input bar!
      expandToTufiTextbox();
    } else {
      openScreenTab(target);
    }
  });
});

function expandToTufiTextbox() {
  dockNormalIsland.style.display = 'none';
  dockExpandedTextbox.classList.add('active');
  const input = document.getElementById('tufiScreenInput');
  setTimeout(() => input.focus(), 120);
}

function collapseToIslandDock() {
  dockExpandedTextbox.classList.remove('active');
  dockNormalIsland.style.display = 'flex';
}

document.getElementById('btnCollapseToIsland').addEventListener('click', collapseToIslandDock);

// ==========================================================================
// 3. Homeworks Engine & UI (1:1 Android HomeworkItemCard.kt)
// ==========================================================================
function saveHomeworks() {
  localStorage.setItem('tufidesk_hw', JSON.stringify(homeworks));
  renderHomeworks();
}

function getRelativeDateLabel(dateStr) {
  const today = new Date();
  today.setHours(0,0,0,0);
  const target = new Date(dateStr);
  target.setHours(0,0,0,0);
  
  const diffDays = Math.round((target - today) / (1000 * 60 * 60 * 24));
  if (diffDays < 0) return { text: 'Gecikti 🔥', class: 'due-urgent' };
  if (diffDays === 0) return { text: 'Bugün Teslim', class: 'due-urgent' };
  if (diffDays === 1) return { text: 'Yarın Teslim', class: 'due-normal' };
  return { text: `${diffDays} gün kaldı`, class: 'due-normal' };
}

function renderHomeworks() {
  const activeContainer = document.getElementById('activeHomeworkList');
  const completedContainer = document.getElementById('completedHomeworkList');
  activeContainer.innerHTML = '';
  completedContainer.innerHTML = '';

  const active = homeworks.filter(h => !h.completed);
  const completed = homeworks.filter(h => h.completed);

  // Filter Active
  const filteredActive = active.filter(h => {
    const matchesSearch = h.title.toLowerCase().includes(currentSearchQuery.toLowerCase()) || 
                          h.course.toLowerCase().includes(currentSearchQuery.toLowerCase());
    if (!matchesSearch) return false;

    if (currentTimeFilter === 'today') {
      const todayStr = new Date().toISOString().split('T')[0];
      return h.date === todayStr;
    }
    if (currentTimeFilter === 'overdue') {
      const todayStr = new Date().toISOString().split('T')[0];
      return h.date < todayStr;
    }
    return true;
  });

  // Update Counters
  document.getElementById('statAllCount').textContent = homeworks.length;
  document.getElementById('statPendingCount').textContent = active.length;
  document.getElementById('statUrgentCount').textContent = active.filter(h => h.priority === 'high').length;
  document.getElementById('statDoneCount').textContent = completed.length;
  document.getElementById('activeCountBadge').textContent = `${filteredActive.length} ödev`;
  document.getElementById('completedCountBadge').textContent = completed.length;

  // Hero Progress Ring
  const total = homeworks.length;
  const pct = total > 0 ? Math.round((completed.length / total) * 100) : 100;
  document.getElementById('heroProgressText').textContent = `%${pct}`;
  const strokeOffset = 150.7 - (150.7 * (pct / 100));
  document.getElementById('heroProgressCircle').style.strokeDashoffset = strokeOffset;

  // Render Active Cards
  if (filteredActive.length === 0) {
    activeContainer.innerHTML = `
      <div class="homework-card" style="text-align: center; padding: 28px 16px;">
        <span style="font-size: 2.5rem;">🎉</span>
        <strong style="margin-top: 6px;">Harika! Bekleyen Ödev Yok</strong>
        <p style="font-size: 0.78rem; color: var(--text-muted); margin-top: 2px;">Tüm ödevlerinizi tamamladınız veya filtreye uygun ödev bulunamadı.</p>
      </div>
    `;
  } else {
    filteredActive.forEach(hw => {
      activeContainer.appendChild(createHomeworkCard(hw));
    });
  }

  // Render Completed Cards
  completed.forEach(hw => {
    completedContainer.appendChild(createHomeworkCard(hw));
  });
}

function createHomeworkCard(hw) {
  const meta = COURSE_META[hw.course] || COURSE_META['Genel'];
  const due = getRelativeDateLabel(hw.date);

  const card = document.createElement('div');
  card.className = `homework-card ${hw.completed ? 'completed' : ''}`;
  card.innerHTML = `
    <div class="homework-card-header">
      <div class="course-badge-box" style="background-color: ${meta.color};">
        <span>${meta.icon}</span>
        <span>${hw.course}</span>
      </div>
      <span class="due-date-pill ${due.class}">📅 ${due.text}</span>
    </div>

    <div class="homework-card-body">
      <div class="m3-round-checkbox ${hw.completed ? 'checked' : ''}" onclick="toggleHomeworkStatus('${hw.id}')">
        ${hw.completed ? '✓' : ''}
      </div>
      <div style="flex: 1;">
        <div class="homework-title-text" style="${hw.completed ? 'text-decoration: line-through;' : ''}">${hw.title}</div>
        ${hw.desc ? `<div class="homework-desc-text">${hw.desc}</div>` : ''}
      </div>
    </div>

    <div class="homework-card-footer">
      <div class="homework-tags-group">
        <span class="tag-badge" style="background: ${hw.priority === 'high' ? 'rgba(239, 68, 68, 0.2)' : 'var(--surface-variant)'}; color: ${hw.priority === 'high' ? '#EF4444' : 'var(--text-muted)'};">
          ${hw.priority === 'high' ? '🔥 Acil' : 'Normal'}
        </span>
        <span class="tag-badge">⏱️ 45 dk</span>
      </div>
      <button class="delete-hw-btn" onclick="deleteHomeworkItem('${hw.id}')" title="Ödevi Sil">🗑️</button>
    </div>
  `;
  return card;
}

window.toggleHomeworkStatus = function(id) {
  const hw = homeworks.find(h => h.id === id);
  if (hw) {
    hw.completed = !hw.completed;
    saveHomeworks();
    if (hw.completed) {
      triggerDynamicIsland('🎉', 'Tebrikler!', `"${hw.title}" tamamlandı!`);
    } else {
      triggerDynamicIsland('⏳', 'Geri Alındı', `"${hw.title}" yapılacaklara alındı.`);
    }
  }
};

window.deleteHomeworkItem = function(id) {
  if (confirm('Bu ödevi silmek istediğinize emin misiniz?')) {
    homeworks = homeworks.filter(h => h.id !== id);
    saveHomeworks();
    triggerDynamicIsland('🗑️', 'Silindi', 'Ödev listeden kaldırıldı.');
  }
};

// Filter Bar
document.querySelectorAll('.m3-filter-pill[data-time-filter]').forEach(pill => {
  pill.addEventListener('click', () => {
    document.querySelectorAll('.m3-filter-pill[data-time-filter]').forEach(p => p.classList.remove('active'));
    pill.classList.add('active');
    currentTimeFilter = pill.dataset.timeFilter;
    renderHomeworks();
  });
});

window.setQuickFilter = function(type) {
  if (type === 'all') currentTimeFilter = 'all';
  if (type === 'pending') currentTimeFilter = 'all';
  if (type === 'urgent') currentTimeFilter = 'all';
  if (type === 'completed') {
    isCompletedExpanded = true;
    document.getElementById('completedHomeworkList').style.display = 'flex';
  }
  renderHomeworks();
};

// Toggle Completed Accordion
document.getElementById('btnToggleCompleted').addEventListener('click', () => {
  isCompletedExpanded = !isCompletedExpanded;
  document.getElementById('completedHomeworkList').style.display = isCompletedExpanded ? 'flex' : 'none';
  document.getElementById('completedArrow').textContent = isCompletedExpanded ? '▼' : '▶';
});

// Search
document.getElementById('btnToggleSearch').addEventListener('click', () => {
  const box = document.getElementById('searchBoxContainer');
  box.style.display = (box.style.display === 'none') ? 'flex' : 'none';
  if (box.style.display === 'flex') document.getElementById('homeSearchInput').focus();
});
document.getElementById('homeSearchInput').addEventListener('input', (e) => {
  currentSearchQuery = e.target.value;
  renderHomeworks();
});

// Add Homework Modal
const addHomeworkModal = document.getElementById('addHomeworkModal');
fabAddHwBtn.addEventListener('click', () => {
  document.getElementById('inputHwDate').value = new Date().toISOString().split('T')[0];
  document.getElementById('inputHwTitle').value = '';
  addHomeworkModal.classList.add('active');
});
document.getElementById('btnCancelHomework').addEventListener('click', () => addHomeworkModal.classList.remove('active'));

document.getElementById('btnSaveHomework').addEventListener('click', () => {
  const title = document.getElementById('inputHwTitle').value.trim();
  const course = document.getElementById('selectHwCourse').value;
  const date = document.getElementById('inputHwDate').value;
  const priority = document.getElementById('selectHwPriority').value;

  if (!title) {
    alert('Lütfen bir ödev başlığı girin.');
    return;
  }

  homeworks.unshift({
    id: `hw-${Date.now()}`,
    title,
    course,
    date,
    priority,
    desc: '',
    completed: false
  });

  saveHomeworks();
  addHomeworkModal.classList.remove('active');
  triggerDynamicIsland('✨', 'Yeni Ödev Eklendi', `"${title}" kaydedildi!`);
});

// ==========================================================================
// 4. Tufi AI Engine (1:1 Android TufiAIScreen.kt)
// ==========================================================================
const tufiChatStream = document.getElementById('tufiChatStream');
const tufiScreenInput = document.getElementById('tufiScreenInput');
const btnSendAiMsg = document.getElementById('btnSendAiMsg');

function appendTufiMsg(role, text) {
  const isUser = (role === 'user');
  const bubble = document.createElement('div');
  bubble.className = `tufi-msg-bubble ${isUser ? 'tufi-msg-user' : 'tufi-msg-ai'}`;
  
  if (!isUser) {
    bubble.innerHTML = `
      <div class="tufi-ai-header-badge">
        <span>✨</span> Tufi AI • Öğrenci Koçu
      </div>
      <div>${text}</div>
    `;
  } else {
    bubble.textContent = text;
  }
  
  tufiChatStream.appendChild(bubble);
  tufiChatStream.scrollTop = tufiChatStream.scrollHeight;
}

function handleAiSend() {
  const text = tufiScreenInput.value.trim();
  if (!text) return;
  tufiScreenInput.value = '';

  appendTufiMsg('user', text);

  // Smart Response
  setTimeout(() => {
    let reply = "";
    const lower = text.toLowerCase();
    if (lower.includes('acil') || lower.includes('hangi')) {
      const urgentHw = homeworks.filter(h => !h.completed && h.priority === 'high');
      if (urgentHw.length > 0) {
        reply = `Şu an en acil ödeviniz: **${urgentHw[0].course} - ${urgentHw[0].title}** (Teslim: ${urgentHw[0].date}). Hemen başlamanızı tavsiye ederim!`;
      } else {
        reply = "Harika! Yüksek öncelikli acil bir ödeviniz bulunmuyor. Normal sıradaki görevlerinizle devam edebilirsiniz.";
      }
    } else if (lower.includes('plan')) {
      reply = "İşte bugünkü çalışma planınız:\n1. 25 dakika: Matematik testine odaklanın.\n2. 5 dakika: Mola verin.\n3. 25 dakika: Fizik sorularını çözün.\n4. Gün sonunda tamamlananları işaretleyin!";
    } else if (lower.includes('türev')) {
      reply = "Türev Alma Kuralları:\n• [x^n]' = n * x^(n-1)\n• [c]' = 0 (Sabit sayı)\n• [u * v]' = u'v + uv' (Çarpım)\n• [u / v]' = (u'v - uv') / v^2 (Bölüm)";
    } else {
      reply = `"${text}" sorunuzu not aldım. Bu konuyu verimli çalışmak için 25 dakikalık Pomodoro sayacını başlatabilir ve odaklanabilirsiniz!`;
    }
    appendTufiMsg('ai', reply);
    triggerDynamicIsland('✨', 'Tufi AI Yanıtladı', 'Yeni bir tavsiye aldınız.');
  }, 600);
}

btnSendAiMsg.addEventListener('click', handleAiSend);
tufiScreenInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') handleAiSend();
});

window.sendAiPrompt = function(promptText) {
  openScreenTab('tufiai');
  tufiScreenInput.value = promptText;
  handleAiSend();
};

// ==========================================================================
// 5. Calendar Engine (1:1 Android CalendarScreen.kt)
// ==========================================================================
function renderCalendar() {
  const grid = document.getElementById('calendarDaysGrid');
  const header = document.getElementById('calendarMonthHeader');
  grid.innerHTML = '';

  const dayHeaders = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];
  dayHeaders.forEach(d => {
    const el = document.createElement('div');
    el.className = 'calendar-day-header';
    el.textContent = d;
    grid.appendChild(el);
  });

  const year = selectedDate.getFullYear();
  const month = selectedDate.getMonth();
  const monthNames = ['Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran', 'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'];
  header.textContent = `${monthNames[month]} ${year}`;

  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const shift = (firstDay === 0 ? 6 : firstDay - 1);

  for (let i = 0; i < shift; i++) {
    const empty = document.createElement('div');
    grid.appendChild(empty);
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    const hasHw = homeworks.some(h => h.date === dateStr);
    const isSelected = (d === selectedDate.getDate());

    const cell = document.createElement('div');
    cell.className = `calendar-day-cell ${isSelected ? 'selected' : ''}`;
    cell.textContent = d;

    if (hasHw && !isSelected) {
      const dot = document.createElement('div');
      dot.className = 'dot-indicator';
      cell.appendChild(dot);
    }

    cell.addEventListener('click', () => {
      selectedDate = new Date(year, month, d);
      renderCalendar();
    });

    grid.appendChild(cell);
  }

  // Render Selected Date Tasks
  const currentDateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(selectedDate.getDate()).padStart(2, '0')}`;
  document.getElementById('selectedDateHeader').textContent = `${currentDateStr} Tarihli Ödevler`;
  const container = document.getElementById('selectedDateTaskList');
  container.innerHTML = '';

  const dayTasks = homeworks.filter(h => h.date === currentDateStr);
  if (dayTasks.length === 0) {
    container.innerHTML = '<span style="font-size: 0.8rem; color: var(--text-muted);">Bu tarihte teslim edilecek ödev yok.</span>';
  } else {
    dayTasks.forEach(h => {
      const meta = COURSE_META[h.course] || COURSE_META['Genel'];
      const item = document.createElement('div');
      item.className = 'homework-card';
      item.style.padding = '10px 14px';
      item.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span>${meta.icon}</span>
            <strong>${h.course}: ${h.title}</strong>
          </div>
          <span style="font-size: 0.72rem; color: ${h.completed ? 'var(--success)' : '#F59E0B'}; font-weight: 700;">
            ${h.completed ? 'Tamamlandı' : 'Bekliyor'}
          </span>
        </div>
      `;
      container.appendChild(item);
    });
  }
}

document.getElementById('btnPrevMonth').addEventListener('click', () => {
  selectedDate.setMonth(selectedDate.getMonth() - 1);
  renderCalendar();
});
document.getElementById('btnNextMonth').addEventListener('click', () => {
  selectedDate.setMonth(selectedDate.getMonth() + 1);
  renderCalendar();
});
document.getElementById('btnAddHwToSelectedDate').addEventListener('click', () => {
  const dateStr = `${selectedDate.getFullYear()}-${String(selectedDate.getMonth() + 1).padStart(2, '0')}-${String(selectedDate.getDate()).padStart(2, '0')}`;
  document.getElementById('inputHwDate').value = dateStr;
  document.getElementById('inputHwTitle').value = '';
  addHomeworkModal.classList.add('active');
});

// ==========================================================================
// 6. Notes Engine (1:1 Android NotesScreen.kt)
// ==========================================================================
function renderNotes() {
  const grid = document.getElementById('notesCardsGrid');
  grid.innerHTML = '';

  notes.forEach(note => {
    const card = document.createElement('div');
    card.className = 'homework-card';
    card.style.backgroundColor = note.color || 'var(--surface-color)';
    card.style.color = '#0F172A';
    card.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <strong style="font-size: 0.88rem;">${note.title}</strong>
        <button onclick="deleteNoteItem('${note.id}')" style="background: none; border: none; cursor: pointer; color: #DC2626; font-size: 0.85rem;">✕</button>
      </div>
      <p style="font-size: 0.78rem; line-height: 1.4; white-space: pre-wrap; margin-top: 6px;">${note.content}</p>
    `;
    grid.appendChild(card);
  });
}

window.deleteNoteItem = function(id) {
  notes = notes.filter(n => n.id !== id);
  localStorage.setItem('tufidesk_notes', JSON.stringify(notes));
  renderNotes();
  triggerDynamicIsland('🗑️', 'Not Silindi', 'Seçilen ders notu kaldırıldı.');
};

document.getElementById('btnCreateNote').addEventListener('click', () => {
  const title = prompt('Not Başlığı:');
  if (!title) return;
  const content = prompt('Not İçeriği:');
  if (!content) return;
  const colors = ['#DBEAFE', '#FEF3C7', '#D1FAE5', '#EDE9FE', '#FCE7F3'];
  const color = colors[Math.floor(Math.random() * colors.length)];

  notes.unshift({ id: `note-${Date.now()}`, title, content, color });
  localStorage.setItem('tufidesk_notes', JSON.stringify(notes));
  renderNotes();
  triggerDynamicIsland('📝', 'Not Eklendi', `"${title}" kaydedildi.`);
});

// ==========================================================================
// 7. Pomodoro Focus Timer (1:1 FocusTimerScreen.kt)
// ==========================================================================
const timerDisplayNumber = document.getElementById('timerDisplayNumber');
const timerProgressCircle = document.getElementById('timerProgressCircle');
const btnTimerStartStop = document.getElementById('btnTimerStartStop');
const btnTimerReset = document.getElementById('btnTimerReset');

function renderTimer() {
  const m = Math.floor(timerRemainSec / 60);
  const s = timerRemainSec % 60;
  timerDisplayNumber.textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;

  const pct = (timerTotalSec - timerRemainSec) / timerTotalSec;
  const offset = 552.9 * (1 - pct);
  timerProgressCircle.style.strokeDashoffset = offset;
}

btnTimerStartStop.addEventListener('click', () => {
  if (timerActive) {
    clearInterval(timerHandle);
    timerActive = false;
    btnTimerStartStop.textContent = 'Devam Et';
    triggerDynamicIsland('⏸️', 'Sayaç Duraklatıldı', 'Odak sayacı bekletiliyor.');
  } else {
    timerActive = true;
    btnTimerStartStop.textContent = 'Durdur';
    triggerDynamicIsland('⏱️', 'Sayaç Başladı', 'İyi çalışmalar dileriz!');
    timerHandle = setInterval(() => {
      if (timerRemainSec > 0) {
        timerRemainSec--;
        renderTimer();
      } else {
        clearInterval(timerHandle);
        timerActive = false;
        btnTimerStartStop.textContent = 'Başlat';
        playAlertTone();
        triggerDynamicIsland('🎉', 'Tebrikler!', 'Odaklanma seansınız bitti!');
        alert('🎉 Odaklanma süreniz tamamlandı! Harika çalıştınız.');
      }
    }, 1000);
  }
});

btnTimerReset.addEventListener('click', () => {
  clearInterval(timerHandle);
  timerActive = false;
  timerRemainSec = timerTotalSec;
  btnTimerStartStop.textContent = 'Başlat';
  renderTimer();
});

window.setTimerDuration = function(mins, label) {
  clearInterval(timerHandle);
  timerActive = false;
  timerTotalSec = mins * 60;
  timerRemainSec = timerTotalSec;
  document.getElementById('timerModeLabel').textContent = `${label} Modu`;
  btnTimerStartStop.textContent = 'Başlat';
  renderTimer();
};

function playAlertTone() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const g = ctx.createGain();
    osc.frequency.setValueAtTime(587.33, ctx.currentTime);
    osc.frequency.setValueAtTime(880, ctx.currentTime + 0.15);
    g.gain.setValueAtTime(0.2, ctx.currentTime);
    g.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.6);
    osc.connect(g);
    g.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.6);
  } catch(e) {}
}

// ==========================================================================
// 8. Stats Calculation (1:1 StatsScreen.kt)
// ==========================================================================
function updateStatsUI() {
  const total = homeworks.length;
  const done = homeworks.filter(h => h.completed).length;
  const pct = total > 0 ? Math.round((done / total) * 100) : 100;
  document.getElementById('statCompletionPct').textContent = `%${pct}`;
}

// ==========================================================================
// 9. Theming & Palettes
// ==========================================================================
const themeCheckbox = document.getElementById('themeCheckbox');
themeCheckbox.addEventListener('change', (e) => {
  const t = e.target.checked ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', t);
  localStorage.setItem('tufidesk_theme', t);
  triggerDynamicIsland('🎨', 'Tema Güncellendi', `${t === 'dark' ? 'Karanlık' : 'Aydınlık'} mod devrede.`);
});

window.changePalette = function(p) {
  document.documentElement.setAttribute('data-palette', p);
  localStorage.setItem('tufidesk_palette', p);
  triggerDynamicIsland('🎨', 'Renk Paleti', `${p} teması seçildi.`);
};

// ==========================================================================
// 10. Windows BSOD (Blue Screen of Death) Easter Egg (1:1 CrashActivity.kt)
// ==========================================================================
const bsodScreenOverlay = document.getElementById('bsodScreenOverlay');
const bsodCounterText = document.getElementById('bsodCounterText');
const btnTriggerBsodEasterEgg = document.getElementById('btnTriggerBsodEasterEgg');
const btnCopyBsodLog = document.getElementById('btnCopyBsodLog');
const btnRestartFromBsod = document.getElementById('btnRestartFromBsod');
const btnFRespect = document.getElementById('btnFRespect');
const respectsBadge = document.getElementById('respectsBadge');

btnTriggerBsodEasterEgg.addEventListener('click', () => {
  bsodScreenOverlay.classList.add('active');
  let pct = 1;
  const intv = setInterval(() => {
    if (pct < 100) {
      pct += Math.floor(Math.random() * 9) + 4;
      if (pct > 100) pct = 100;
      bsodCounterText.textContent = `${pct}% compelete`;
    } else {
      clearInterval(intv);
    }
  }, 130);
});

btnRestartFromBsod.addEventListener('click', () => {
  bsodScreenOverlay.classList.remove('active');
  triggerDynamicIsland('🔄', 'Sistem Yeniden Başlatıldı', 'TufiDESK normal çalışıyor.');
});

btnCopyBsodLog.addEventListener('click', () => {
  const dump = `=== TUFIDESK WINDOWS BSOD CRASH DUMP ===
Stop Code: STATUS_SYSTEM_PROCESS_TERMINATED (0xC000021A)
Timestamp: ${new Date().toLocaleString('tr-TR')}
Platform: Progressive Web App (PWA) / HTML5
What Failed: SettingsScreen.DeveloperBsodTrigger()
========================================`;
  navigator.clipboard.writeText(dump).then(() => {
    alert('Loglar panoya kopyalandı! 📋');
  });
});

btnFRespect.addEventListener('click', () => {
  respectCount++;
  respectsBadge.textContent = `+${respectCount} 🫡`;
  if (navigator.vibrate) navigator.vibrate(15);
});

// ==========================================================================
// 11. PWA Installation Handler
// ==========================================================================
const btnInstallPwaDirect = document.getElementById('btnInstallPwaDirect');
const pwaBannerCard = document.getElementById('pwaBannerCard');

window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredInstallPrompt = e;
  pwaBannerCard.style.display = 'flex';
});

btnInstallPwaDirect.addEventListener('click', async () => {
  if (deferredInstallPrompt) {
    deferredInstallPrompt.prompt();
    const { outcome } = await deferredInstallPrompt.userChoice;
    deferredInstallPrompt = null;
    pwaBannerCard.style.display = 'none';
  } else {
    alert('TufiDESK uygulamasını yüklemek için tarayıcınızın menüsünden "Ana Ekrana Ekle" veya "Uygulamayı Yükle" seçeneğine dokunun.');
  }
});

// Shortcuts from header
document.getElementById('btnSettingsShortcut').addEventListener('click', () => openScreenTab('settings'));
document.getElementById('btnProfileBadge').addEventListener('click', () => openScreenTab('settings'));

// Initialize
renderHomeworks();
renderTimer();
