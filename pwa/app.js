/* ==========================================================================
   TufiDESK PWA - Application Logic & State Management
   ========================================================================== */

// --- Initial Demo Data ---
const DEFAULT_HOMEWORKS = [
  { id: '1', title: 'Türev ve İntegral Uygulamaları Test 4', course: 'Matematik', date: '2026-10-06', priority: 'high', completed: false },
  { id: '2', title: 'Elektromanyetik İndüksiyon Soru Çözümü', course: 'Fizik', date: '2026-10-07', priority: 'normal', completed: false },
  { id: '3', title: 'Organik Kimya Fonksiyonel Gruplar Özeti', course: 'Kimya', date: '2026-10-08', priority: 'normal', completed: true },
  { id: '4', title: 'Divan Edebiyatı Nazım Şekilleri Tablosu', course: 'Edebiyat', date: '2026-10-09', priority: 'low', completed: false }
];

const DEFAULT_NOTES = [
  { id: '1', title: 'Matematik Formülleri', content: 'Türev: (x^n)\' = n*x^(n-1)\nİntegral: ∫x^n dx = (x^(n+1))/(n+1) + C', color: '#DBEAFE' },
  { id: '2', title: 'Haftalık Hedef', content: 'Her gün en az 50 soru ve 2 Pomodoro odak çalışması tamamlanacak.', color: '#FEF3C7' },
  { id: '3', title: 'Fizik İpuçları', content: 'Sağ el kuralı: Başparmak akım yönü, dört parmak manyetik alan.', color: '#D1FAE5' }
];

// --- State Variables ---
let homeworks = JSON.parse(localStorage.getItem('tufidesk_hw')) || DEFAULT_HOMEWORKS;
let notes = JSON.parse(localStorage.getItem('tufidesk_notes')) || DEFAULT_NOTES;
let currentTab = 'home';
let currentFilter = 'all';
let searchQuery = '';
let selectedCalendarDate = new Date();
let deferredPrompt = null;

// Timer State
let timerInterval = null;
let timerTotalSeconds = 25 * 60;
let timerRemainingSeconds = 25 * 60;
let isTimerRunning = false;

// --- DOM References ---
const screenTitle = document.getElementById('screenTitle');
const screenSubtitle = document.getElementById('screenSubtitle');
const homeworkList = document.getElementById('homeworkList');
const emptyHomeworks = document.getElementById('emptyHomeworks');
const searchBarRow = document.getElementById('searchBarRow');
const searchInput = document.getElementById('searchInput');
const fabAddHomework = document.getElementById('fabAddHomework');
const addHwModal = document.getElementById('addHwModal');
const islandNav = document.getElementById('islandNav');
const tufiMorphBar = document.getElementById('tufiMorphBar');
const navTabTufi = document.getElementById('navTabTufi');
const navIconTufi = document.getElementById('navIconTufi');
const navLabelTufi = document.getElementById('navLabelTufi');
const themeToggle = document.getElementById('themeToggle');

// ==========================================================================
// 1. Navigation & Morphing Bottom Bar
// ==========================================================================
const screenTitles = {
  home: { title: 'TufiDESK', subtitle: 'Ödevler & Görevler' },
  calendar: { title: 'Ödev Takvimi', subtitle: 'Haftalık ve Aylık Plan' },
  tufiai: { title: 'Tufi AI Asistan', subtitle: 'Yapay Zeka Çalışma Koçu' },
  stats: { title: 'İstatistikler', subtitle: 'Başarı & Odak Analizi' },
  notes: { title: 'Ders Notları', subtitle: 'Hızlı Hatırlatıcılar' },
  tools: { title: 'Öğrenci Araçları', subtitle: 'Yardımcı Gereçler' },
  settings: { title: 'Ayarlar', subtitle: 'Görünüm & Sistem' }
};

function openTab(tabId) {
  currentTab = tabId;

  // Hide all screens
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  const targetScreen = document.getElementById(`screen-${tabId}`);
  if (targetScreen) targetScreen.classList.add('active');

  // Update Top Bar Titles
  const meta = screenTitles[tabId] || { title: 'TufiDESK', subtitle: '' };
  screenTitle.textContent = meta.title;
  screenSubtitle.textContent = meta.subtitle;

  // Toggle FAB
  fabAddHomework.style.display = (tabId === 'home') ? 'flex' : 'none';

  // Toggle Search Bar Row
  if (tabId !== 'home') searchBarRow.style.display = 'none';

  // Handle Morphing Bottom Bar logic:
  // When inside Tufi AI screen:
  // Center tab changes to "Sohbet" with ChatBubble icon
  if (tabId === 'tufiai') {
    navLabelTufi.textContent = 'Sohbet';
    navIconTufi.innerHTML = '<path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>';
  } else {
    // Other tabs: resets to "Tufi AI" with Star icon
    navLabelTufi.textContent = 'Tufi AI';
    navIconTufi.innerHTML = '<path d="m19 9 1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 20l2.5-5.5L17 12z"/>';
    // Collapse any open input bar
    collapseTufiBar();
  }

  // Highlight active nav item
  document.querySelectorAll('.island-nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.tab === tabId);
  });

  // Haptic feedback
  if (navigator.vibrate) navigator.vibrate(10);

  // Refresh tab-specific views
  if (tabId === 'calendar') renderCalendar();
  if (tabId === 'stats') updateStats();
  if (tabId === 'notes') renderNotes();
}

// Bind Bottom Nav Tabs
document.querySelectorAll('.island-nav-item').forEach(btn => {
  btn.addEventListener('click', () => {
    const target = btn.dataset.tab;
    // If user is already on Tufi AI and clicks "Sohbet", expand the chat input box!
    if (target === 'tufiai' && currentTab === 'tufiai') {
      expandTufiBar();
    } else {
      openTab(target);
    }
  });
});

// Morphing Bar Controls
function expandTufiBar() {
  islandNav.style.display = 'none';
  tufiMorphBar.classList.add('active');
  const input = document.getElementById('tufiTextInput');
  setTimeout(() => input.focus(), 150);
}

function collapseTufiBar() {
  tufiMorphBar.classList.remove('active');
  islandNav.style.display = 'flex';
}

document.getElementById('btnCollapseTufi').addEventListener('click', collapseTufiBar);

// ==========================================================================
// 2. Homeworks Management
// ==========================================================================
function saveHomeworks() {
  localStorage.setItem('tufidesk_hw', JSON.stringify(homeworks));
  renderHomeworks();
}

function renderHomeworks() {
  homeworkList.innerHTML = '';
  
  const filtered = homeworks.filter(hw => {
    const matchesSearch = hw.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          hw.course.toLowerCase().includes(searchQuery.toLowerCase());
    if (!matchesSearch) return false;
    
    if (currentFilter === 'pending') return !hw.completed;
    if (currentFilter === 'completed') return hw.completed;
    if (currentFilter === 'math') return hw.course === 'Matematik';
    if (currentFilter === 'phys') return hw.course === 'Fizik';
    if (currentFilter === 'lit') return hw.course === 'Edebiyat';
    return true;
  });

  // Update counts
  document.getElementById('countAll').textContent = homeworks.length;
  document.getElementById('countPending').textContent = homeworks.filter(h => !h.completed).length;
  document.getElementById('countCompleted').textContent = homeworks.filter(h => h.completed).length;

  if (filtered.length === 0) {
    emptyHomeworks.style.display = 'block';
  } else {
    emptyHomeworks.style.display = 'none';
  }

  filtered.forEach(hw => {
    const card = document.createElement('div');
    card.className = 'card';
    card.innerHTML = `
      <div class="card-header">
        <span class="badge ${hw.priority === 'high' ? 'badge-danger' : (hw.priority === 'low' ? 'badge-warning' : 'badge-primary')}">
          ${hw.course}
        </span>
        <span style="font-size: 0.75rem; color: var(--text-muted);">📅 ${hw.date}</span>
      </div>
      <div style="display: flex; align-items: center; gap: 12px;">
        <div class="checkbox-round ${hw.completed ? 'checked' : ''}" onclick="toggleHwCompletion('${hw.id}')">
          ${hw.completed ? '✓' : ''}
        </div>
        <div style="flex: 1; ${hw.completed ? 'text-decoration: line-through; opacity: 0.6;' : ''}">
          <div style="font-weight: 600; font-size: 0.95rem;">${hw.title}</div>
        </div>
        <button class="icon-btn" onclick="deleteHomework('${hw.id}')" title="Sil" style="color: var(--danger); width: 32px; height: 32px;">
          🗑️
        </button>
      </div>
    `;
    homeworkList.appendChild(card);
  });
}

window.toggleHwCompletion = function(id) {
  const hw = homeworks.find(h => h.id === id);
  if (hw) {
    hw.completed = !hw.completed;
    saveHomeworks();
    if (navigator.vibrate) navigator.vibrate(15);
  }
};

window.deleteHomework = function(id) {
  if (confirm('Bu ödevi silmek istediğinize emin misiniz?')) {
    homeworks = homeworks.filter(h => h.id !== id);
    saveHomeworks();
  }
};

// Filter Buttons
document.querySelectorAll('.filter-pill').forEach(pill => {
  pill.addEventListener('click', (e) => {
    if (!pill.dataset.filter) return;
    document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
    pill.classList.add('active');
    currentFilter = pill.dataset.filter;
    renderHomeworks();
  });
});

// Search
document.getElementById('btnSearch').addEventListener('click', () => {
  searchBarRow.style.display = searchBarRow.style.display === 'none' ? 'block' : 'none';
  if (searchBarRow.style.display === 'block') searchInput.focus();
});
searchInput.addEventListener('input', (e) => {
  searchQuery = e.target.value;
  renderHomeworks();
});

// Modal: Add Homework
fabAddHomework.addEventListener('click', () => {
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('hwDate').value = today;
  document.getElementById('hwTitle').value = '';
  addHwModal.classList.add('active');
});

document.getElementById('btnCancelHw').addEventListener('click', () => {
  addHwModal.classList.remove('active');
});

document.getElementById('btnSaveHw').addEventListener('click', () => {
  const title = document.getElementById('hwTitle').value.trim();
  const course = document.getElementById('hwCourse').value;
  const date = document.getElementById('hwDate').value;
  const priority = document.getElementById('hwPriority').value;

  if (!title) {
    alert('Lütfen bir ödev başlığı girin.');
    return;
  }

  homeworks.unshift({
    id: Date.now().toString(),
    title,
    course,
    date,
    priority,
    completed: false
  });

  saveHomeworks();
  addHwModal.classList.remove('active');
});

// ==========================================================================
// 3. Tufi AI Chatbot
// ==========================================================================
const chatStream = document.getElementById('chatStream');
const tufiTextInput = document.getElementById('tufiTextInput');
const btnSendTufi = document.getElementById('btnSendTufi');

function appendChatMessage(sender, text) {
  const isUser = sender === 'user';
  const msg = document.createElement('div');
  msg.style.display = 'flex';
  msg.style.justifyContent = isUser ? 'flex-end' : 'flex-start';
  
  msg.innerHTML = `
    <div style="max-width: 82%; padding: 12px 16px; border-radius: 18px; font-size: 0.9rem; line-height: 1.5; 
      ${isUser ? 'background: var(--primary); color: white; border-bottom-right-radius: 4px;' : 'background: var(--surface-color); color: var(--text-main); border: 1px solid var(--border-color); border-bottom-left-radius: 4px;'}">
      ${!isUser ? '<div style="font-weight: 700; color: #38BDF8; font-size: 0.78rem; margin-bottom: 4px;">✨ Tufi AI</div>' : ''}
      <div>${text}</div>
    </div>
  `;
  chatStream.appendChild(msg);
  chatStream.scrollTop = chatStream.scrollHeight;
}

function handleUserSend() {
  const text = tufiTextInput.value.trim();
  if (!text) return;
  tufiTextInput.value = '';

  appendChatMessage('user', text);

  // Generate Smart Response based on user homeworks & input
  setTimeout(() => {
    let reply = "Sorunuzu inceledim. ";
    const lower = text.toLowerCase();
    
    if (lower.includes('acil') || lower.includes('ödev')) {
      const urgent = homeworks.filter(h => !h.completed && h.priority === 'high');
      if (urgent.length > 0) {
        reply += `Şu an en acil ödeviniz: **${urgent[0].course} - ${urgent[0].title}** (Teslim: ${urgent[0].date}). Hemen başlamanızı öneririm!`;
      } else {
        const anyPending = homeworks.filter(h => !h.completed);
        if (anyPending.length > 0) {
          reply += `Şu an ${anyPending.length} adet bekleyen ödeviniz var. En yakını: **${anyPending[0].title}**.`;
        } else {
          reply += "Tüm ödevleriniz tamamlanmış görünüyor! Harikasınız 🎉";
        }
      }
    } else if (lower.includes('plan')) {
      reply += "Bugün için önerim:\n1. 25 dk Pomodoro ile en zorlandığınız derse odaklanın.\n2. 5 dk mola verin.\n3. Ödev listenizdeki ilk görevi bitirip işaretleyin!";
    } else if (lower.includes('türev') || lower.includes('matematik')) {
      reply += "Türev formülleri:\n• f(x) = x^n => f'(x) = n*x^(n-1)\n• (u*v)' = u'v + uv'\n• Zincir Kuralı: [f(g(x))]' = f'(g(x)) * g'(x)";
    } else {
      reply += `"${text}" konusunda size yardımcı olmaktan mutluluk duyarım. Pomodoro sayacını başlatıp odaklanarak çalışmaya ne dersiniz?`;
    }

    appendChatMessage('ai', reply);
  }, 600);
}

btnSendTufi.addEventListener('click', handleUserSend);
tufiTextInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') handleUserSend();
});

window.sendSuggestion = function(text) {
  openTab('tufiai');
  tufiTextInput.value = text;
  handleUserSend();
};

// ==========================================================================
// 4. Calendar Logic
// ==========================================================================
function renderCalendar() {
  const calendarGrid = document.getElementById('calendarGrid');
  const monthTitle = document.getElementById('calendarMonthTitle');
  calendarGrid.innerHTML = '';

  const dayNames = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];
  dayNames.forEach(d => {
    const header = document.createElement('div');
    header.style.fontSize = '0.72rem';
    header.style.color = 'var(--text-muted)';
    header.style.fontWeight = '700';
    header.style.padding = '4px 0';
    header.textContent = d;
    calendarGrid.appendChild(header);
  });

  const year = selectedCalendarDate.getFullYear();
  const month = selectedCalendarDate.getMonth();
  const monthNames = ['Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran', 'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'];
  monthTitle.textContent = `${monthNames[month]} ${year}`;

  const firstDay = new Date(year, month, 1).getDay(); // 0 is Sunday
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const shift = (firstDay === 0 ? 6 : firstDay - 1);

  // Empty leading days
  for (let i = 0; i < shift; i++) {
    const empty = document.createElement('div');
    calendarGrid.appendChild(empty);
  }

  // Days of Month
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    const hasHomework = homeworks.some(h => h.date === dateStr);
    
    const dayBtn = document.createElement('div');
    dayBtn.style.padding = '8px 0';
    dayBtn.style.borderRadius = '8px';
    dayBtn.style.cursor = 'pointer';
    dayBtn.style.fontSize = '0.85rem';
    dayBtn.style.position = 'relative';
    dayBtn.style.backgroundColor = (d === selectedCalendarDate.getDate()) ? 'var(--primary)' : 'transparent';
    dayBtn.style.color = (d === selectedCalendarDate.getDate()) ? 'white' : 'var(--text-main)';
    dayBtn.textContent = d;

    if (hasHomework) {
      const dot = document.createElement('div');
      dot.style.width = '4px';
      dot.style.height = '4px';
      dot.style.borderRadius = '50%';
      dot.style.backgroundColor = (d === selectedCalendarDate.getDate()) ? 'white' : '#F59E0B';
      dot.style.position = 'absolute';
      dot.style.bottom = '3px';
      dot.style.left = '50%';
      dot.style.transform = 'translateX(-50%)';
      dayBtn.appendChild(dot);
    }

    dayBtn.addEventListener('click', () => {
      selectedCalendarDate = new Date(year, month, d);
      renderCalendar();
      renderCalendarDateTasks(dateStr);
    });

    calendarGrid.appendChild(dayBtn);
  }

  const currentDateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(selectedCalendarDate.getDate()).padStart(2, '0')}`;
  renderCalendarDateTasks(currentDateStr);
}

function renderCalendarDateTasks(dateStr) {
  const container = document.getElementById('calendarDateTasks');
  const title = document.getElementById('selectedDateTitle');
  title.textContent = `${dateStr} Tarihli Ödevler`;
  container.innerHTML = '';

  const dayTasks = homeworks.filter(h => h.date === dateStr);
  if (dayTasks.length === 0) {
    container.innerHTML = '<span style="font-size: 0.8rem; color: var(--text-muted);">Bu tarihe ait ödev bulunmuyor.</span>';
    return;
  }

  dayTasks.forEach(h => {
    const item = document.createElement('div');
    item.style.padding = '10px 12px';
    item.style.background = 'var(--surface-variant)';
    item.style.borderRadius = '8px';
    item.style.fontSize = '0.85rem';
    item.style.display = 'flex';
    item.style.justifyContent = 'space-between';
    item.innerHTML = `<strong>${h.course}:</strong> <span>${h.title}</span>`;
    container.appendChild(item);
  });
}

document.getElementById('btnPrevMonth').addEventListener('click', () => {
  selectedCalendarDate.setMonth(selectedCalendarDate.getMonth() - 1);
  renderCalendar();
});
document.getElementById('btnNextMonth').addEventListener('click', () => {
  selectedCalendarDate.setMonth(selectedCalendarDate.getMonth() + 1);
  renderCalendar();
});

// ==========================================================================
// 5. Notes Management
// ==========================================================================
function renderNotes() {
  const grid = document.getElementById('notesGrid');
  grid.innerHTML = '';

  notes.forEach(note => {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.backgroundColor = note.color || 'var(--card-bg)';
    card.style.color = '#0F172A'; // readable dark text on pastel notes
    card.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <strong style="font-size: 0.9rem;">${note.title}</strong>
        <button onclick="deleteNote('${note.id}')" style="background: none; border: none; cursor: pointer; color: #DC2626;">✕</button>
      </div>
      <p style="font-size: 0.8rem; line-height: 1.4; white-space: pre-wrap; margin-top: 4px;">${note.content}</p>
    `;
    grid.appendChild(card);
  });
}

window.deleteNote = function(id) {
  notes = notes.filter(n => n.id !== id);
  localStorage.setItem('tufidesk_notes', JSON.stringify(notes));
  renderNotes();
};

document.getElementById('btnNewNote').addEventListener('click', () => {
  const title = prompt('Not Başlığı:');
  if (!title) return;
  const content = prompt('Not İçeriği:');
  if (!content) return;
  const colors = ['#DBEAFE', '#FEF3C7', '#D1FAE5', '#EDE9FE', '#FCE7F3'];
  const randomColor = colors[Math.floor(Math.random() * colors.length)];

  notes.unshift({ id: Date.now().toString(), title, content, color: randomColor });
  localStorage.setItem('tufidesk_notes', JSON.stringify(notes));
  renderNotes();
});

// ==========================================================================
// 6. Pomodoro Focus Timer
// ==========================================================================
const timerDisplay = document.getElementById('timerDisplay');
const btnTimerToggle = document.getElementById('btnTimerToggle');
const btnTimerReset = document.getElementById('btnTimerReset');

function updateTimerDisplay() {
  const mins = Math.floor(timerRemainingSeconds / 60);
  const secs = timerRemainingSeconds % 60;
  timerDisplay.textContent = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

function playChime() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
    osc.frequency.setValueAtTime(880, ctx.currentTime + 0.15); // A5
    gain.gain.setValueAtTime(0.2, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.6);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.6);
  } catch(e) {}
}

btnTimerToggle.addEventListener('click', () => {
  if (isTimerRunning) {
    clearInterval(timerInterval);
    isTimerRunning = false;
    btnTimerToggle.textContent = 'Devam Et';
  } else {
    isTimerRunning = true;
    btnTimerToggle.textContent = 'Durdur';
    timerInterval = setInterval(() => {
      if (timerRemainingSeconds > 0) {
        timerRemainingSeconds--;
        updateTimerDisplay();
      } else {
        clearInterval(timerInterval);
        isTimerRunning = false;
        btnTimerToggle.textContent = 'Başlat';
        playChime();
        if (navigator.vibrate) navigator.vibrate([200, 100, 200]);
        alert('🎉 Tebrikler! Odaklanma süreniz tamamlandı.');
      }
    }, 1000);
  }
});

btnTimerReset.addEventListener('click', () => {
  clearInterval(timerInterval);
  isTimerRunning = false;
  timerRemainingSeconds = timerTotalSeconds;
  btnTimerToggle.textContent = 'Başlat';
  updateTimerDisplay();
});

window.setTimerPreset = function(minutes, label) {
  clearInterval(timerInterval);
  isTimerRunning = false;
  timerTotalSeconds = minutes * 60;
  timerRemainingSeconds = timerTotalSeconds;
  document.getElementById('timerLabel').textContent = label;
  btnTimerToggle.textContent = 'Başlat';
  updateTimerDisplay();
};

// ==========================================================================
// 7. Stats
// ==========================================================================
function updateStats() {
  const total = homeworks.length;
  const completed = homeworks.filter(h => h.completed).length;
  const rate = total > 0 ? Math.round((completed / total) * 100) : 100;
  document.getElementById('statRate').textContent = `%${rate}`;
}

// ==========================================================================
// 8. Theming & Palettes
// ==========================================================================
themeToggle.addEventListener('change', (e) => {
  const theme = e.target.checked ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('tufidesk_theme', theme);
});

window.setPalette = function(palette) {
  document.documentElement.setAttribute('data-palette', palette);
  localStorage.setItem('tufidesk_palette', palette);
};

// Load saved theme
const savedTheme = localStorage.getItem('tufidesk_theme') || 'dark';
document.documentElement.setAttribute('data-theme', savedTheme);
themeToggle.checked = (savedTheme === 'dark');

const savedPalette = localStorage.getItem('tufidesk_palette') || 'blue';
document.documentElement.setAttribute('data-palette', savedPalette);

// ==========================================================================
// 9. Windows BSOD (Blue Screen of Death) Easter Egg
// ==========================================================================
const bsodScreen = document.getElementById('bsodScreen');
const bsodPercent = document.getElementById('bsodPercent');
const btnTriggerBsod = document.getElementById('btnTriggerBsod');
const btnBsodCopy = document.getElementById('btnBsodCopy');
const btnBsodRestart = document.getElementById('btnBsodRestart');

btnTriggerBsod.addEventListener('click', () => {
  bsodScreen.classList.add('active');
  let pct = 1;
  const interval = setInterval(() => {
    if (pct < 100) {
      pct += Math.floor(Math.random() * 8) + 3;
      if (pct > 100) pct = 100;
      bsodPercent.textContent = `${pct}% compelete`;
    } else {
      clearInterval(interval);
    }
  }, 120);
});

btnBsodRestart.addEventListener('click', () => {
  bsodScreen.classList.remove('active');
});

btnBsodCopy.addEventListener('click', () => {
  const dump = `=== TUFIDESK WINDOWS BSOD CRASH DUMP ===
Stop Code: STATUS_SYSTEM_PROCESS_TERMINATED (0xC000021A)
Timestamp: ${new Date().toLocaleString('tr-TR')}
Platform: Progressive Web App (PWA) / Webkit
What Failed: SettingsScreen.ManualCrashTrigger()
========================================`;
  navigator.clipboard.writeText(dump).then(() => {
    alert('Loglar panoya kopyalandı! 📋');
  });
});

// ==========================================================================
// 10. PWA Installation Prompt (beforeinstallprompt)
// ==========================================================================
const btnInstallPwa = document.getElementById('btnInstallPwa');
const pwaInstallCard = document.getElementById('pwaInstallCard');

window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;
  pwaInstallCard.style.display = 'block';
});

btnInstallPwa.addEventListener('click', async () => {
  if (deferredPrompt) {
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    console.log('[PWA] User choice:', outcome);
    deferredPrompt = null;
    pwaInstallCard.style.display = 'none';
  } else {
    alert('PWA yüklemek için tarayıcınızın menüsünden (⋮ veya Paylaş) "Ana Ekrana Ekle" seçeneğine dokunun.');
  }
});

// Quick navigation to Settings from top bar
document.getElementById('btnSettingsTop').addEventListener('click', () => openTab('settings'));
document.getElementById('btnProfile').addEventListener('click', () => openTab('settings'));

// Initialize Home Screen
renderHomeworks();
updateTimerDisplay();
