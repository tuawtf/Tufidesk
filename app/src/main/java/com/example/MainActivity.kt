package com.example

import android.app.Activity
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.components.FixedStandardBottomNavigation
import com.example.ui.components.IslandBottomNavigation
import com.example.ui.components.TabletIslandNavigation
import com.example.ui.components.UserFlowerAvatar
import com.example.ui.components.SwitchProfileDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.AuthManager
import com.example.data.database.AppDatabase
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.firebase.SyncState
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.UserRole
import com.example.data.repository.HomeworkRepository
import com.example.data.repository.NotesRepository
import com.example.ui.HomeworkViewModel
import com.example.ui.HomeworkViewModelFactory
import com.example.ui.components.AddCourseDialog
import com.example.ui.components.AddEditHomeworkDialog
import com.example.ui.components.DynamicIslandController
import com.example.ui.components.DynamicIslandOverlay
import com.example.ui.components.IslandNotificationType
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CoursesScreen
import com.example.ui.screens.DenemeExamScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.TufiAIScreen
import com.example.data.model.NoteType
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.AutoAwesome

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Ödevler", Icons.Filled.Assignment, Icons.Outlined.Assignment)
    data object Calendar : Screen("calendar", "Takvim", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    data object TufiAI : Screen("tufi_ai", "Tufi AI", Icons.Filled.AutoAwesome, Icons.Filled.AutoAwesome)
    data object Stats : Screen("stats", "İstatistik", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    data object Notes : Screen("notes", "Notlar", Icons.Filled.Description, Icons.Outlined.Description)
    data object Exams : Screen("exams", "Denemeler", Icons.Filled.Assessment, Icons.Filled.Assessment)
    data object Timer : Screen("timer", "Sayaç", Icons.Filled.HourglassTop, Icons.Filled.HourglassTop)
    data object Tools : Screen("tools", "Araçlar", Icons.Filled.GridView, Icons.Outlined.GridView)
    data object Settings : Screen("settings", "Ayarlar", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: HomeworkViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = HomeworkRepository(database.courseDao(), database.homeworkDao())
        val notesRepository = NotesRepository(database.noteDao())
        val authManager = AuthManager(applicationContext)
        val syncManager = FirebaseSyncManager(
            applicationContext,
            repository,
            CoroutineScope(Dispatchers.IO + SupervisorJob()),
            notesRepository
        )
        HomeworkViewModelFactory(repository, authManager, syncManager, notesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge(
                statusBarStyle = androidx.activity.SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                ),
                navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )
            )
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            setContent {
                val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                val colorPalette by viewModel.colorPalette.collectAsStateWithLifecycle()
                MyApplicationTheme(themeMode = themeMode, colorPalette = colorPalette) {
                    HomeworkApp(viewModel = viewModel)
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Fatal error in onCreate: ${t.message}", t)
            CrashActivity.start(this, t)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkApp(viewModel: HomeworkViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val islandController = remember { DynamicIslandController() }
    val context = LocalContext.current
    val localView = LocalView.current

    val showNotification: (String, IslandNotificationType, String?) -> Unit = { msg, type, title ->
        islandController.show(
            message = msg,
            title = title,
            type = type,
            durationMillis = 2200L,
            scope = coroutineScope,
            context = context,
            view = localView
        )
    }

    val filteredHomeworks by viewModel.filteredHomeworks.collectAsStateWithLifecycle()
    val allHomeworks by viewModel.allHomeworks.collectAsStateWithLifecycle()
    val userHomeworks by viewModel.userHomeworks.collectAsStateWithLifecycle()
    val courses by viewModel.allCourses.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val selectedCourseFilter by viewModel.selectedCourseFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val colorPalette by viewModel.colorPalette.collectAsStateWithLifecycle()
    val hideCompleted by viewModel.hideCompleted.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val syncErrorMessage by viewModel.syncErrorMessage.collectAsStateWithLifecycle()
    val adminAssignedFilter by viewModel.adminAssignedFilter.collectAsStateWithLifecycle()

    val userNotes by viewModel.userNotes.collectAsStateWithLifecycle()
    val noteFolders by viewModel.allNoteFolders.collectAsStateWithLifecycle()
    val selectedNoteFolderId by viewModel.selectedNoteFolderId.collectAsStateWithLifecycle()
    val noteSearchQuery by viewModel.noteSearchQuery.collectAsStateWithLifecycle()

    val noteLayoutGrid by viewModel.noteLayoutGrid.collectAsStateWithLifecycle()
    val noteShowWordCount by viewModel.noteShowWordCount.collectAsStateWithLifecycle()
    val noteConfirmDelete by viewModel.noteConfirmDelete.collectAsStateWithLifecycle()
    val keepAddNewToBottom by viewModel.keepAddNewToBottom.collectAsStateWithLifecycle()
    val keepMoveCheckedToBottom by viewModel.keepMoveCheckedToBottom.collectAsStateWithLifecycle()
    val keepRichLinkPreviews by viewModel.keepRichLinkPreviews.collectAsStateWithLifecycle()
    val keepMorningTime by viewModel.keepMorningTime.collectAsStateWithLifecycle()
    val keepAfternoonTime by viewModel.keepAfternoonTime.collectAsStateWithLifecycle()
    val keepEveningTime by viewModel.keepEveningTime.collectAsStateWithLifecycle()
    val keepSharingEnabled by viewModel.keepSharingEnabled.collectAsStateWithLifecycle()
    val noteVaultPassword by viewModel.noteVaultPassword.collectAsStateWithLifecycle()
    val noteFilterVaultOnly by viewModel.noteFilterVaultOnly.collectAsStateWithLifecycle()
    val floatingBottomBar by viewModel.floatingBottomBar.collectAsStateWithLifecycle()

    if (currentUser == null) {
        LoginScreen(
            onSelectProfile = { profile ->
                viewModel.selectProfile(profile)
            },
            isOnline = isOnline,
            syncState = syncState
        )
        return
    }

    val canManage = currentUser?.canManage ?: false

    var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }

    // Dialog states
    var showAddEditHomeworkDialog by remember { mutableStateOf(false) }
    var homeworkToEdit by remember { mutableStateOf<Homework?>(null) }
    var newHomeworkInitialDate by remember { mutableStateOf<Long?>(null) }

    var showAddEditCourseDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }

    var showSwitchProfileDialog by remember { mutableStateOf(false) }

    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var activeTool by rememberSaveable { mutableStateOf<String?>(null) }
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    var isSelectionActive by rememberSaveable { mutableStateOf(false) }

    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    // Tam Ekran modu kontrolü: Durum ve gezinme çubuklarını gizle
    DisposableEffect(isFullScreen, activity) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullScreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Yüzen bar ayarı: Açıkken Android sistem çubuğu kontrast zorlamasını (siyah dikdörtgeni) devre dışı bırak
    DisposableEffect(floatingBottomBar, activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            activity?.window?.isNavigationBarContrastEnforced = !floatingBottomBar
        }
        onDispose {}
    }

    val pinnedBottomTools by viewModel.pinnedBottomTools.collectAsStateWithLifecycle()
    val toolsOrder by viewModel.toolsOrder.collectAsStateWithLifecycle()

    val navItems = remember(pinnedBottomTools) {
        buildList {
            add(Screen.Home)
            add(Screen.Calendar)
            add(Screen.TufiAI)
            add(Screen.Stats)
            if (pinnedBottomTools.contains("notes")) add(Screen.Notes)
            if (pinnedBottomTools.contains("exams")) add(Screen.Exams)
            if (pinnedBottomTools.contains("timer")) add(Screen.Timer)
            add(Screen.Tools)
        }
    }

    val currentTab = remember(currentRoute, navItems) {
        val index = navItems.indexOfFirst { it.route == currentRoute }
        if (index != -1) index else 0
    }

    val selectedScreen = remember(currentRoute, navItems) {
        navItems.find { it.route == currentRoute } ?: Screen.Home
    }

    val isTimerActive = (selectedScreen == Screen.Timer) || (selectedScreen == Screen.Tools && activeTool == "timer")

    // Handle back button on sub-tabs
    BackHandler(enabled = isSettingsOpen) {
        isSettingsOpen = false
    }
    BackHandler(enabled = !isSettingsOpen && activeTool != null) {
        activeTool = null
    }
    BackHandler(enabled = !isSettingsOpen && activeTool == null && currentRoute != Screen.Home.route) {
        currentRoute = Screen.Home.route
    }

    val screenTitle = when {
        isSettingsOpen -> "Ayarlar"
        selectedScreen == Screen.Home -> {
            if (selectedCourseFilter != null) {
                val courseName = courses.find { it.id == selectedCourseFilter }?.name ?: "Filtrelenmiş"
                "Ödevler • $courseName"
            } else {
                "TufiDESK"
            }
        }
        selectedScreen == Screen.Calendar -> "Ödev Takvimi"
        selectedScreen == Screen.TufiAI -> "Tufi AI"
        selectedScreen == Screen.Stats -> "İstatistikler"
        selectedScreen == Screen.Notes -> "Notlar"
        selectedScreen == Screen.Exams -> "Deneme Sınavları"
        selectedScreen == Screen.Timer -> "Sayaç & Odak"
        selectedScreen == Screen.Tools -> when (activeTool) {
            "notes" -> "Notlar"
            "exams" -> "Deneme Sınavları"
            "timer" -> "Sayaç & Odak"
            else -> "Araçlar"
        }
        else -> "TufiDESK"
    }

    val renderSettings: @Composable () -> Unit = {
        SettingsScreen(
            currentThemeMode = themeMode,
            onThemeModeChange = { viewModel.setThemeMode(it) },
            currentColorPalette = colorPalette,
            onColorPaletteChange = { viewModel.setColorPalette(it) },
            hideCompleted = hideCompleted,
            onHideCompletedChange = { viewModel.setHideCompleted(it) },
            isFullScreen = isFullScreen,
            onFullScreenChange = { isFullScreen = it },
            floatingBottomBar = floatingBottomBar,
            onFloatingBottomBarChange = { viewModel.setFloatingBottomBar(it) },
            onResetDemoData = {
                viewModel.resetToDemoData()
                showNotification("Örnek veriler başarıyla yüklendi", IslandNotificationType.SUCCESS, "Veriler")
            },
            onClearAllData = {
                viewModel.clearAllData()
                showNotification("Tüm veriler temizlendi", IslandNotificationType.WARNING, "Veriler")
            },
            currentUser = currentUser,
            isOnline = isOnline,
            syncState = syncState,
            syncErrorMessage = syncErrorMessage,
            onManualSync = {
                viewModel.syncCloudNow()
                if (!isOnline) {
                    showNotification("İnternet bağlantısı yok. Veriler cihazda güvende!", IslandNotificationType.WARNING, "Bağlantı")
                } else if (syncState == SyncState.ERROR && syncErrorMessage != null) {
                    showNotification("Hata: $syncErrorMessage", IslandNotificationType.ERROR, "Senkronizasyon")
                } else {
                    showNotification("Bulut senkronizasyonu başlatıldı...", IslandNotificationType.INFO, "Senkronizasyon")
                }
            },
            onLogout = {
                viewModel.logout()
            },
            onSwitchProfile = {
                showSwitchProfileDialog = true
            },
            onSelectProfile = { profile ->
                viewModel.selectProfile(profile)
            },
            canManage = canManage,
            isNoteGrid = noteLayoutGrid,
            onNoteGridChange = { viewModel.setNoteLayoutGrid(it) },
            moveCheckedToBottom = keepMoveCheckedToBottom,
            onMoveCheckedToBottomChange = { viewModel.setKeepMoveCheckedToBottom(it) },
            onClearAllFirebaseData = { onResult ->
                viewModel.clearAllFirebaseData(onResult)
            },
            onResetFirebaseData = { onResult ->
                viewModel.resetFirebaseWithDemoData(onResult)
            },
            onExportFirebaseJson = { onResult ->
                viewModel.exportFirebaseDataToJson(onResult)
            },
            onImportFirebaseJson = { jsonStr, onResult ->
                viewModel.importFirebaseDataFromJson(jsonStr, onResult)
            }
        )
    }

    val screenContent: @Composable () -> Unit = {
        if (isSettingsOpen) {
            renderSettings()
        } else {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(90),
                label = "tab_crossfade"
            ) { tab ->
                val targetScreen = navItems.getOrNull(tab) ?: Screen.Home
                when (targetScreen) {
                    Screen.Home -> HomeScreen(
                        homeworks = filteredHomeworks,
                        stats = stats,
                        statusFilter = statusFilter,
                        onStatusChange = { viewModel.setStatus(it) },
                        timeFilter = timeFilter,
                        onTimeChange = { viewModel.setTime(it) },
                        sortOrder = sortOrder,
                        onSortChange = { viewModel.setSort(it) },
                        onToggleComplete = { hw ->
                            viewModel.toggleCompletion(hw)
                            val msg = if (!hw.isCompleted) "Tebrikler! Ödev tamamlandı 🎉" else "Ödev bekleyenlere alındı"
                            val type = if (!hw.isCompleted) IslandNotificationType.SUCCESS else IslandNotificationType.INFO
                            showNotification(msg, type, "Ödev")
                        },
                        onEditHomework = { hw ->
                            homeworkToEdit = hw
                            showAddEditHomeworkDialog = true
                        },
                        onDeleteHomework = { hw ->
                            viewModel.deleteHomework(hw)
                            showNotification("Ödev silindi", IslandNotificationType.WARNING, "Silindi")
                        },
                        canManage = canManage,
                        hideCompletedInSettings = hideCompleted,
                        currentUser = currentUser,
                        adminAssignedFilter = adminAssignedFilter,
                        onAdminAssignedFilterChange = { viewModel.setAdminAssignedFilter(it) },
                        courses = courses,
                        onNavigateToTufiAI = { currentRoute = Screen.TufiAI.route }
                    )

                    Screen.Calendar -> CalendarScreen(
                        homeworks = userHomeworks,
                        selectedDateMillis = selectedCalendarDate,
                        onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                        onToggleComplete = { hw ->
                            viewModel.toggleCompletion(hw)
                            val msg = if (!hw.isCompleted) "Tebrikler! Ödev tamamlandı 🎉" else "Ödev bekleyenlere alındı"
                            val type = if (!hw.isCompleted) IslandNotificationType.SUCCESS else IslandNotificationType.INFO
                            showNotification(msg, type, "Ödev")
                        },
                        onEditHomework = { hw ->
                            homeworkToEdit = hw
                            showAddEditHomeworkDialog = true
                        },
                        onDeleteHomework = { hw ->
                            viewModel.deleteHomework(hw)
                            showNotification("Ödev silindi", IslandNotificationType.WARNING, "Silindi")
                        },
                        onAddNewHomeworkForDate = { dateMillis ->
                            homeworkToEdit = null
                            newHomeworkInitialDate = dateMillis
                            viewModel.setSelectedCalendarDate(dateMillis)
                            showAddEditHomeworkDialog = true
                        },
                        canManage = canManage
                    )

                    Screen.TufiAI -> TufiAIScreen(
                        courses = courses,
                        homeworks = filteredHomeworks,
                        onBack = { currentRoute = Screen.Home.route }
                    )

                    Screen.Stats -> StatsScreen(
                        stats = stats,
                        courses = courses,
                        homeworks = userHomeworks,
                        notes = userNotes,
                        onToggleComplete = { hw ->
                            viewModel.toggleCompletion(hw)
                            val msg = if (!hw.isCompleted) "Tebrikler! Ödev tamamlandı 🎉" else "Ödev bekleyenlere alındı"
                            val type = if (!hw.isCompleted) IslandNotificationType.SUCCESS else IslandNotificationType.INFO
                            showNotification(msg, type, "Ödev")
                        }
                    )

                    Screen.Notes -> {
                        NotesScreen(
                            notes = userNotes,
                            folders = noteFolders,
                            selectedFolderId = selectedNoteFolderId,
                            searchQuery = noteSearchQuery,
                            onSelectFolder = { viewModel.setSelectedNoteFolder(it) },
                            onSearchQueryChange = { viewModel.setNoteSearchQuery(it) },
                            onInsertNote = { note ->
                                viewModel.insertNote(note)
                                showNotification("Not kaydedildi 📝", IslandNotificationType.SUCCESS, "Google Notlar")
                            },
                            onUpdateNote = { note ->
                                viewModel.updateNote(note)
                                showNotification("Not güncellendi", IslandNotificationType.SUCCESS, "Google Notlar")
                            },
                            onDeleteNote = { note ->
                                viewModel.deleteNote(note)
                                showNotification("Not silindi", IslandNotificationType.WARNING, "Google Notlar")
                            },
                            onTogglePin = { note ->
                                viewModel.toggleNotePin(note)
                            },
                            onCreateFolder = { name, colorHex ->
                                viewModel.insertFolder(name, colorHex)
                                showNotification("'$name' klasörü oluşturuldu 📁", IslandNotificationType.SUCCESS, "Klasör")
                            },
                            onUpdateFolder = { folder ->
                                viewModel.updateFolder(folder)
                                showNotification("Klasör güncellendi 📁", IslandNotificationType.SUCCESS, "Klasör")
                            },
                            onDeleteFolder = { folder ->
                                viewModel.deleteFolder(folder)
                                showNotification("Klasör silindi", IslandNotificationType.WARNING, "Klasör")
                            },
                            canManage = canManage,
                            currentUser = currentUser,
                            currentUserAssigned = currentUser?.name ?: "Tuna",
                            isGridLayout = noteLayoutGrid,
                            onToggleLayoutGrid = { viewModel.setNoteLayoutGrid(it) },
                            showWordCount = noteShowWordCount,
                            confirmDelete = noteConfirmDelete,
                            moveCheckedToBottom = keepMoveCheckedToBottom,
                            onSwitchProfile = { showSwitchProfileDialog = true },
                            onSelectionModeChange = { isSelectionActive = it }
                        )
                    }

                    Screen.Exams -> {
                        DenemeExamScreen(
                            notes = userNotes,
                            currentUserAssigned = currentUser?.name ?: "Tuna",
                            onInsertExam = { note ->
                                viewModel.insertNote(note)
                                showNotification("Deneme sınavı kaydedildi 🎯", IslandNotificationType.SUCCESS, "Denemeler")
                            },
                            onUpdateExam = { note ->
                                viewModel.updateNote(note)
                                showNotification("Deneme güncellendi 🎯", IslandNotificationType.SUCCESS, "Denemeler")
                            },
                            onDeleteExam = { note ->
                                viewModel.deleteNote(note)
                                showNotification("Deneme silindi", IslandNotificationType.WARNING, "Denemeler")
                            },
                            onNotify = { msg ->
                                showNotification(msg, IslandNotificationType.INFO, "Denemeler")
                            }
                        )
                    }

                    Screen.Timer -> {
                        FocusTimerScreen(
                            onBackToTools = {
                                currentRoute = Screen.Tools.route
                                activeTool = null
                            },
                            onNotify = { msg ->
                                showNotification(msg, IslandNotificationType.SUCCESS, "Sayaç & Odak")
                            }
                        )
                    }

                    Screen.Tools -> {
                        when (activeTool) {
                            "notes" -> {
                                NotesScreen(
                                    notes = userNotes,
                                    folders = noteFolders,
                                    selectedFolderId = selectedNoteFolderId,
                                    searchQuery = noteSearchQuery,
                                    onSelectFolder = { viewModel.setSelectedNoteFolder(it) },
                                    onSearchQueryChange = { viewModel.setNoteSearchQuery(it) },
                                    onInsertNote = { note ->
                                        viewModel.insertNote(note)
                                        showNotification("Not kaydedildi 📝", IslandNotificationType.SUCCESS, "Google Notlar")
                                    },
                                    onUpdateNote = { note ->
                                        viewModel.updateNote(note)
                                        showNotification("Not güncellendi", IslandNotificationType.SUCCESS, "Google Notlar")
                                    },
                                    onDeleteNote = { note ->
                                        viewModel.deleteNote(note)
                                        showNotification("Not silindi", IslandNotificationType.WARNING, "Google Notlar")
                                    },
                                    onTogglePin = { note ->
                                        viewModel.toggleNotePin(note)
                                    },
                                    onCreateFolder = { name, colorHex ->
                                        viewModel.insertFolder(name, colorHex)
                                        showNotification("'$name' klasörü oluşturuldu 📁", IslandNotificationType.SUCCESS, "Klasör")
                                    },
                                    onUpdateFolder = { folder ->
                                        viewModel.updateFolder(folder)
                                        showNotification("Klasör güncellendi 📁", IslandNotificationType.SUCCESS, "Klasör")
                                    },
                                    onDeleteFolder = { folder ->
                                        viewModel.deleteFolder(folder)
                                        showNotification("Klasör silindi", IslandNotificationType.WARNING, "Klasör")
                                    },
                                    canManage = canManage,
                                    currentUser = currentUser,
                                    currentUserAssigned = currentUser?.name ?: "Tuna",
                                    isGridLayout = noteLayoutGrid,
                                    onToggleLayoutGrid = { viewModel.setNoteLayoutGrid(it) },
                                    showWordCount = noteShowWordCount,
                                    confirmDelete = noteConfirmDelete,
                                    moveCheckedToBottom = keepMoveCheckedToBottom,
                                    onSwitchProfile = { showSwitchProfileDialog = true },
                                    onSelectionModeChange = { isSelectionActive = it }
                                )
                            }
                            "exams" -> {
                                DenemeExamScreen(
                                    notes = userNotes,
                                    currentUserAssigned = currentUser?.name ?: "Tuna",
                                    onInsertExam = { note ->
                                        viewModel.insertNote(note)
                                        showNotification("Deneme sınavı kaydedildi 🎯", IslandNotificationType.SUCCESS, "Denemeler")
                                    },
                                    onUpdateExam = { note ->
                                        viewModel.updateNote(note)
                                        showNotification("Deneme güncellendi 🎯", IslandNotificationType.SUCCESS, "Denemeler")
                                    },
                                    onDeleteExam = { note ->
                                        viewModel.deleteNote(note)
                                        showNotification("Deneme silindi", IslandNotificationType.WARNING, "Denemeler")
                                    },
                                    onNotify = { msg ->
                                        showNotification(msg, IslandNotificationType.INFO, "Denemeler")
                                    }
                                )
                            }
                            "timer" -> {
                                FocusTimerScreen(
                                    onBackToTools = {
                                        activeTool = null
                                    },
                                    onNotify = { msg ->
                                        showNotification(msg, IslandNotificationType.SUCCESS, "Sayaç & Odak")
                                    }
                                )
                            }
                            else -> {
                                ToolsScreen(
                                    noteCount = userNotes.count { it.type != NoteType.REPORT },
                                    examCount = userNotes.count { it.type == NoteType.REPORT },
                                    pinnedBottomTools = pinnedBottomTools,
                                    toolsOrder = toolsOrder,
                                    onTogglePinToBottom = { viewModel.togglePinToolToBottom(it) },
                                    onMoveTool = { key, up -> viewModel.moveTool(key, up) },
                                    onResetOrder = { viewModel.setToolsOrder(listOf("notes", "exams", "timer", "schedule")) },
                                    onOpenNotes = { activeTool = "notes" },
                                    onOpenExams = { activeTool = "exams" },
                                    onOpenTimer = { activeTool = "timer" },
                                    onSelectionActiveChange = { isSelectionActive = it },
                                    onNotify = { msg -> showNotification(msg, IslandNotificationType.INFO, "Araçlar") }
                                )
                            }
                        }
                    }

                    else -> HomeScreen(
                        homeworks = filteredHomeworks,
                        stats = stats,
                        statusFilter = statusFilter,
                        onStatusChange = { viewModel.setStatus(it) },
                        timeFilter = timeFilter,
                        onTimeChange = { viewModel.setTime(it) },
                        sortOrder = sortOrder,
                        onSortChange = { viewModel.setSort(it) },
                        onToggleComplete = { hw ->
                            viewModel.toggleCompletion(hw)
                            val msg = if (!hw.isCompleted) "Tebrikler! Ödev tamamlandı 🎉" else "Ödev bekleyenlere alındı"
                            val type = if (!hw.isCompleted) IslandNotificationType.SUCCESS else IslandNotificationType.INFO
                            showNotification(msg, type, "Ödev")
                        },
                        onEditHomework = { hw ->
                            homeworkToEdit = hw
                            showAddEditHomeworkDialog = true
                        },
                        onDeleteHomework = { hw ->
                            viewModel.deleteHomework(hw)
                            showNotification("Ödev silindi", IslandNotificationType.WARNING, "Silindi")
                        },
                        canManage = canManage,
                        hideCompletedInSettings = hideCompleted,
                        currentUser = currentUser,
                        adminAssignedFilter = adminAssignedFilter,
                        onAdminAssignedFilterChange = { viewModel.setAdminAssignedFilter(it) },
                        courses = courses,
                        onNavigateToTufiAI = { currentRoute = Screen.TufiAI.route }
                    )
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // TABLET MODE: Left Floating Island Dock + Right Content Area
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Island Menu
                    TabletIslandNavigation(
                        currentTab = currentTab,
                        onTabSelected = { index ->
                            navItems.getOrNull(index)?.let { currentRoute = it.route }
                        },
                        onAddHomeworkClick = {
                            homeworkToEdit = null
                            showAddEditHomeworkDialog = true
                        },
                        navItems = navItems,
                        showAddAction = canManage,
                        currentUser = currentUser,
                        isOnline = isOnline,
                        syncState = syncState,
                        onSyncClick = {
                            viewModel.syncCloudNow()
                            if (isOnline) {
                                showNotification("Bulut senkronizasyonu başlatıldı...", IslandNotificationType.INFO, "Senkronizasyon")
                            } else {
                                showNotification("Çevrimdışı mod: İnternet bağlantısı bekleniyor", IslandNotificationType.WARNING, "Bağlantı")
                            }
                        },
                        onLogoutClick = {
                            viewModel.switchProfile()
                        },
                        onSwitchProfile = {
                            showSwitchProfileDialog = true
                        }
                    )

                    // Right Content Scaffold
                    Scaffold(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        snackbarHost = {},
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = screenTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                navigationIcon = {
                                    if (isSettingsOpen) {
                                        IconButton(onClick = { isSettingsOpen = false }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Geri"
                                            )
                                        }
                                    } else if (currentTab == 3 && activeTool != null) {
                                        IconButton(onClick = { activeTool = null }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Araçlara Dön"
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (!isSettingsOpen && currentTab == 0 && selectedCourseFilter != null) {
                                        IconButton(onClick = { viewModel.setCourseFilter(null) }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Filtreyi Kaldır"
                                            )
                                        }
                                    }

                                    IconButton(onClick = {
                                        viewModel.syncCloudNow()
                                        if (!isOnline) {
                                            showNotification("Çevrimdışı mod: İnternet bağlantısı bekleniyor", IslandNotificationType.WARNING, "Bağlantı")
                                        } else if (syncState == SyncState.ERROR && syncErrorMessage != null) {
                                            showNotification("Hata: $syncErrorMessage", IslandNotificationType.ERROR, "Senkronizasyon")
                                        } else {
                                            showNotification("Bulut senkronizasyonu başlatıldı...", IslandNotificationType.INFO, "Senkronizasyon")
                                        }
                                    }) {
                                        val syncIcon = when {
                                            !isOnline -> Icons.Default.CloudOff
                                            syncState == SyncState.SYNCED -> Icons.Default.CloudDone
                                            syncState == SyncState.ERROR -> Icons.Default.CloudOff
                                            else -> Icons.Default.Sync
                                        }
                                        val syncColor = when {
                                            !isOnline || syncState == SyncState.ERROR -> MaterialTheme.colorScheme.error
                                            syncState == SyncState.SYNCED -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.tertiary
                                        }
                                        Icon(imageVector = syncIcon, contentDescription = "Eşitle", tint = syncColor)
                                    }

                                    // Ayarlar butonu (Bulut simgesinin sağı)
                                    IconButton(
                                        onClick = { isSettingsOpen = !isSettingsOpen },
                                        modifier = Modifier.testTag("tablet_top_bar_settings_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isSettingsOpen) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Ayarlar",
                                            tint = if (isSettingsOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            screenContent()
                        }
                    }
                }
            }
        } else {
            // PHONE MODE: Standard Top Bar + Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = {},
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = screenTitle,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            if (isSettingsOpen) {
                                IconButton(onClick = { isSettingsOpen = false }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Geri"
                                    )
                                }
                            } else if (currentTab == 3 && activeTool != null) {
                                IconButton(onClick = { activeTool = null }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Araçlara Dön"
                                    )
                                }
                            }
                        },
                        actions = {
                            if (!isSettingsOpen && currentTab == 0 && selectedCourseFilter != null) {
                                IconButton(onClick = { viewModel.setCourseFilter(null) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Filtreyi Kaldır"
                                    )
                                }
                            }

                            // Cloud Sync status icon button
                            IconButton(onClick = {
                                if (!isOnline) {
                                    showNotification("İnternet bağlantısı yok! Eşitleme başarısız ❌", IslandNotificationType.ERROR, "Bağlantı")
                                } else {
                                    showNotification("Bulut senkronizasyonu başlatıldı...", IslandNotificationType.INFO, "Senkronizasyon")
                                    viewModel.syncCloudNow { success ->
                                        if (success) {
                                            showNotification("Bulut senkronizasyonu başarıyla tamamlandı ✅", IslandNotificationType.SUCCESS, "Senkronizasyon")
                                        } else {
                                            showNotification("Bulut senkronizasyonu başarısız ❌", IslandNotificationType.ERROR, "Senkronizasyon")
                                        }
                                    }
                                }
                            }) {
                                val syncIcon = when {
                                    !isOnline || syncState == SyncState.OFFLINE -> Icons.Default.CloudOff
                                    syncState == SyncState.ERROR -> Icons.Default.CloudOff
                                    syncState == SyncState.SYNCED -> Icons.Default.CloudDone
                                    syncState == SyncState.SYNCING -> Icons.Default.Sync
                                    else -> Icons.Default.CloudOff
                                }
                                val syncColor = when {
                                    !isOnline || syncState == SyncState.OFFLINE || syncState == SyncState.ERROR -> MaterialTheme.colorScheme.error
                                    syncState == SyncState.SYNCED -> MaterialTheme.colorScheme.primary
                                    syncState == SyncState.SYNCING -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                }
                                Icon(imageVector = syncIcon, contentDescription = "Eşitle", tint = syncColor)
                            }

                            // Mobil için bulut simgesinin SAĞINDA Ayarlar butonu
                            IconButton(
                                onClick = { isSettingsOpen = !isSettingsOpen },
                                modifier = Modifier.testTag("top_bar_settings_button")
                            ) {
                                Icon(
                                    imageVector = if (isSettingsOpen) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Ayarlar",
                                    tint = if (isSettingsOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!floatingBottomBar && !isSettingsOpen && !isSelectionActive && !isTimerActive) {
                        FixedStandardBottomNavigation(
                            currentTab = currentTab,
                            onTabSelected = { index ->
                                navItems.getOrNull(index)?.let { currentRoute = it.route }
                            },
                            navItems = navItems
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    if (!isSettingsOpen && canManage && (currentTab == 0 || currentTab == 1)) {
                        FloatingActionButton(
                            onClick = {
                                homeworkToEdit = null
                                showAddEditHomeworkDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .offset(y = if (floatingBottomBar) (-74).dp else (-2).dp)
                                .testTag("add_homework_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Yeni Ödev Ekle"
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = if (!floatingBottomBar) innerPadding.calculateBottomPadding() else 0.dp
                        )
                ) {
                    screenContent()

                    // Tamamen havada yüzen ada menüsü (Arkada hiçbir siyahlık veya şerit yok)
                    if (floatingBottomBar && !isSettingsOpen && !isSelectionActive && !isTimerActive) {
                        IslandBottomNavigation(
                            currentTab = currentTab,
                            onTabSelected = { index ->
                                navItems.getOrNull(index)?.let { currentRoute = it.route }
                            },
                            navItems = navItems,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }

        // Dynamic Island Overlay at top
        DynamicIslandOverlay(
            controller = islandController,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    // Add/Edit Homework Dialog
    if (showAddEditHomeworkDialog) {
        AddEditHomeworkDialog(
            initialHomework = homeworkToEdit,
            initialDateMillis = newHomeworkInitialDate,
            courses = courses,
            activeUser = currentUser,
            onDismiss = {
                showAddEditHomeworkDialog = false
                homeworkToEdit = null
                newHomeworkInitialDate = null
            },
            onSave = { id, courseId, title, description, dueDateMillis, dueTimeText, priority, estimatedMinutes, assignedTo ->
                viewModel.saveHomework(
                    id = id,
                    courseId = courseId,
                    title = title,
                    description = description,
                    dueDateMillis = dueDateMillis,
                    dueTimeText = dueTimeText,
                    priority = priority,
                    estimatedMinutes = estimatedMinutes,
                    assignedTo = assignedTo
                )
                showAddEditHomeworkDialog = false
                homeworkToEdit = null
                newHomeworkInitialDate = null
                showNotification(
                    if (id == 0L) "Ödev başarıyla eklendi 📋" else "Ödev güncellendi",
                    IslandNotificationType.SUCCESS,
                    "Ödev"
                )
            },
            onAddNewCourseRequest = {
                courseToEdit = null
                showAddEditCourseDialog = true
            }
        )
    }

    // Add/Edit Course Dialog
    if (showAddEditCourseDialog) {
        AddCourseDialog(
            initialCourse = courseToEdit,
            onDismiss = {
                showAddEditCourseDialog = false
                courseToEdit = null
            },
            onSave = { id, name, teacher, colorHex, iconName ->
                viewModel.saveCourse(id, name, teacher, colorHex, iconName)
                showAddEditCourseDialog = false
                courseToEdit = null
                showNotification(
                    if (id == 0L) "$name dersi eklendi 📚" else "$name dersi güncellendi",
                    IslandNotificationType.SUCCESS,
                    "Ders"
                )
            }
        )
    }

    // Switch Profile Dialog
    if (showSwitchProfileDialog) {
        SwitchProfileDialog(
            currentProfile = currentUser,
            onSelectProfile = { profile ->
                viewModel.selectProfile(profile)
                showSwitchProfileDialog = false
                showNotification("Profil değiştirildi: ${profile.name}", IslandNotificationType.INFO, "Profil")
            },
            onDismiss = {
                showSwitchProfileDialog = false
            }
        )
    }
}
