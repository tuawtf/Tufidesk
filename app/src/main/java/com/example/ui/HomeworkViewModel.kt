package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.HomeworkWithCourse
import com.example.data.model.Note
import com.example.data.model.NoteFolder
import com.example.data.model.UserAccount
import com.example.data.repository.HomeworkRepository
import com.example.data.repository.NotesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

enum class StatusFilter(val title: String) {
    ALL("Tümü"),
    PENDING("Bekleyenler"),
    COMPLETED("Tamamlananlar")
}

enum class TimeFilter(val title: String) {
    TODAY("Bugün"),
    THIS_WEEK("Bu Hafta"),
    ALL("Tüm Zamanlar"),
    OVERDUE("Gecikenler")
}

enum class SortOrder(val title: String) {
    DUE_DATE_ASC("Teslim Tarihi (Önce Yakın)"),
    DUE_DATE_DESC("Teslim Tarihi (Önce Uzak)"),
    PRIORITY_HIGH_FIRST("Önceliğe Göre (Acil)"),
    COURSE_NAME("Derse Göre")
}

data class HomeworkStats(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val dueTodayCount: Int = 0,
    val overdueCount: Int = 0,
    val completionPercentage: Int = 0,
    val totalEstimatedHours: Float = 0f
)

class HomeworkViewModel(
    private val repository: HomeworkRepository,
    private val authManager: AuthManager,
    private val syncManager: FirebaseSyncManager,
    private val notesRepository: NotesRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.cleanupDuplicates()
            repository.seedInitialDataIfNeeded()
            notesRepository.seedInitialNotesIfNeeded()
            syncManager.startRealtimeListeners()
            syncManager.syncWithCloud(silent = true)
            repository.cleanupDuplicates()
        }
    }

    val currentUser: StateFlow<UserAccount?> = authManager.currentUser
    val isOnline: StateFlow<Boolean> = syncManager.isOnline
    val syncState = syncManager.syncState
    val lastSyncTime = syncManager.lastSyncTime
    val syncErrorMessage = syncManager.lastErrorMessage

    val adminAssignedFilter = MutableStateFlow("Elif") // "Elif", "Tuna", "Tümü"

    fun setAdminAssignedFilter(filter: String) {
        adminAssignedFilter.value = filter
    }

    // Notes & Folders State Flows
    val allNotes: StateFlow<List<Note>> = notesRepository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allNoteFolders: StateFlow<List<NoteFolder>> = notesRepository.allFolders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedNoteFolderId = MutableStateFlow<Long?>(null)
    val noteSearchQuery = MutableStateFlow("")

    // Google Notlar (Keep Notes) Preferences & State
    val noteLayoutGrid = MutableStateFlow(true) // true: 2-column staggered grid, false: 1-column list
    val noteShowWordCount = MutableStateFlow(true)
    val noteConfirmDelete = MutableStateFlow(true)
    val keepAddNewToBottom = MutableStateFlow(false)
    val keepMoveCheckedToBottom = MutableStateFlow(true)
    val keepRichLinkPreviews = MutableStateFlow(true)
    val keepMorningTime = MutableStateFlow("08:00")
    val keepAfternoonTime = MutableStateFlow("13:00")
    val keepEveningTime = MutableStateFlow("18:00")
    val keepSharingEnabled = MutableStateFlow(true)
    val noteVaultPassword = MutableStateFlow("1234") // Vault PIN
    val noteVaultUnlocked = MutableStateFlow(false)
    val noteFilterVaultOnly = MutableStateFlow(false)

    // Alt barın arkasındaki siyah çubuğu kaldırarak tamamen havada yüzen ada modunu sağlar
    val floatingBottomBar = MutableStateFlow(true)
    fun setFloatingBottomBar(enabled: Boolean) {
        floatingBottomBar.value = enabled
    }

    // Araçlar Bölümü & Alt Bar Sabitleme / Sıralama
    val pinnedBottomTools = MutableStateFlow<List<String>>(listOf()) // örn: "exams", "timer", "notes"
    val toolsOrder = MutableStateFlow<List<String>>(listOf("notes", "exams", "timer", "schedule"))

    fun togglePinToolToBottom(toolKey: String) {
        val current = pinnedBottomTools.value.toMutableList()
        if (current.contains(toolKey)) {
            current.remove(toolKey)
        } else {
            current.add(toolKey)
        }
        pinnedBottomTools.value = current
    }

    fun isToolPinnedToBottom(toolKey: String): Boolean {
        return pinnedBottomTools.value.contains(toolKey)
    }

    fun setToolsOrder(newOrder: List<String>) {
        toolsOrder.value = newOrder
    }

    fun moveTool(toolKey: String, moveUp: Boolean) {
        val list = toolsOrder.value.toMutableList()
        val index = list.indexOf(toolKey)
        if (index != -1) {
            val targetIndex = if (moveUp) index - 1 else index + 1
            if (targetIndex in 0 until list.size) {
                val item = list.removeAt(index)
                list.add(targetIndex, item)
                toolsOrder.value = list
            }
        }
    }

    val userNotes: StateFlow<List<Note>> = combine(
        allNotes,
        currentUser,
        selectedNoteFolderId,
        noteSearchQuery,
        noteFilterVaultOnly
    ) { notes, user, folderId, query, vaultOnly ->
        var list = notes
        if (user != null) {
            when {
                user.isTuna -> list = list.filter { it.assignedTo.equals("Tuna", ignoreCase = true) }
                user.isElif -> list = list.filter { it.assignedTo.equals("Elif", ignoreCase = true) }
                else -> { /* teacher sees all */ }
            }
        }
        if (vaultOnly) {
            list = list.filter { it.isLocked }
        } else {
            list = list.filter { !it.isLocked }
        }
        if (folderId != null) {
            list = list.filter { it.folderId == folderId }
        }
        if (query.isNotBlank()) {
            val q = query.trim()
            list = list.filter {
                it.title.contains(q, ignoreCase = true) || it.content.contains(q, ignoreCase = true)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setNoteLayoutGrid(isGrid: Boolean) {
        noteLayoutGrid.value = isGrid
    }

    fun setNoteShowWordCount(enabled: Boolean) {
        noteShowWordCount.value = enabled
    }

    fun setNoteConfirmDelete(enabled: Boolean) {
        noteConfirmDelete.value = enabled
    }

    fun setKeepAddNewToBottom(enabled: Boolean) {
        keepAddNewToBottom.value = enabled
    }

    fun setKeepMoveCheckedToBottom(enabled: Boolean) {
        keepMoveCheckedToBottom.value = enabled
    }

    fun setKeepRichLinkPreviews(enabled: Boolean) {
        keepRichLinkPreviews.value = enabled
    }

    fun setKeepMorningTime(time: String) {
        keepMorningTime.value = time
    }

    fun setKeepAfternoonTime(time: String) {
        keepAfternoonTime.value = time
    }

    fun setKeepEveningTime(time: String) {
        keepEveningTime.value = time
    }

    fun setKeepSharingEnabled(enabled: Boolean) {
        keepSharingEnabled.value = enabled
    }

    fun setNoteVaultPassword(pin: String) {
        noteVaultPassword.value = pin
    }

    fun unlockVault(pin: String): Boolean {
        return if (noteVaultPassword.value.isEmpty() || noteVaultPassword.value == pin) {
            noteVaultUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockVault() {
        noteVaultUnlocked.value = false
        noteFilterVaultOnly.value = false
    }

    fun setNoteFilterVaultOnly(vaultOnly: Boolean) {
        noteFilterVaultOnly.value = vaultOnly
    }

    fun toggleNoteLock(note: Note) {
        viewModelScope.launch {
            notesRepository.updateNote(note.copy(isLocked = !note.isLocked))
        }
    }

    fun setSelectedNoteFolder(folderId: Long?) {
        selectedNoteFolderId.value = folderId
    }

    fun setNoteSearchQuery(query: String) {
        noteSearchQuery.value = query
    }

    fun insertNote(note: Note) {
        viewModelScope.launch {
            notesRepository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            notesRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
    }

    fun toggleNotePin(note: Note) {
        viewModelScope.launch {
            notesRepository.togglePin(note.id, !note.isPinned)
        }
    }

    fun insertFolder(name: String, colorHex: String) {
        viewModelScope.launch {
            notesRepository.insertFolder(NoteFolder(name = name, colorHex = colorHex))
        }
    }

    fun updateFolder(folder: NoteFolder) {
        viewModelScope.launch {
            notesRepository.insertFolder(folder)
        }
    }

    fun deleteFolder(folder: NoteFolder) {
        viewModelScope.launch {
            if (selectedNoteFolderId.value == folder.id) {
                selectedNoteFolderId.value = null
            }
            notesRepository.deleteFolder(folder)
        }
    }

    fun selectProfile(profile: UserAccount) {
        authManager.selectProfile(profile)
    }

    fun switchProfile() {
        authManager.switchProfile()
    }

    fun loginAsStudent(name: String, studentNo: String = "") {
        authManager.loginAsStudent(name, studentNo)
    }

    fun loginAsTeacher(name: String, passCode: String): Boolean {
        return authManager.loginAsTeacher(name, passCode)
    }

    fun logout() {
        authManager.logout()
    }

    fun syncCloudNow(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val success = syncManager.syncWithCloud(silent = false)
            onComplete?.invoke(success)
        }
    }

    val allCourses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allHomeworks: StateFlow<List<HomeworkWithCourse>> = repository.allHomeworks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filters and Search
    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow(StatusFilter.ALL)
    val timeFilter = MutableStateFlow(TimeFilter.TODAY)
    val selectedCourseFilter = MutableStateFlow<Long?>(null)
    val selectedPriorityFilter = MutableStateFlow<String?>(null)
    val sortOrder = MutableStateFlow(SortOrder.DUE_DATE_ASC)

    // Calendar selected date (start of day millis)
    val selectedCalendarDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))

    // User-scoped homeworks (Personalized: Tuna only gets Tuna, Elif only gets Elif)
    val userHomeworks: StateFlow<List<HomeworkWithCourse>> = combine(
        allHomeworks,
        currentUser,
        adminAssignedFilter
    ) { homeworks, user, adminFilter ->
        if (user == null) return@combine homeworks
        when {
            user.isTuna -> homeworks.filter { it.homework.assignedTo.equals("Tuna", ignoreCase = true) }
            user.isElif -> homeworks.filter { it.homework.assignedTo.equals("Elif", ignoreCase = true) }
            user.isTeacher -> {
                when (adminFilter) {
                    "Tuna" -> homeworks.filter { it.homework.assignedTo.equals("Tuna", ignoreCase = true) }
                    "Elif" -> homeworks.filter { it.homework.assignedTo.equals("Elif", ignoreCase = true) }
                    else -> homeworks
                }
            }
            else -> homeworks
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private data class FilterBase(
        val query: String,
        val status: StatusFilter,
        val time: TimeFilter,
        val courseId: Long?
    )

    private data class FilterParams(
        val query: String,
        val status: StatusFilter,
        val time: TimeFilter,
        val courseId: Long?,
        val priority: String?,
        val sort: SortOrder
    )

    private val filtersFlow = combine(
        searchQuery,
        statusFilter,
        timeFilter,
        selectedCourseFilter
    ) { query, status, time, courseId ->
        FilterBase(query, status, time, courseId)
    }.combine(
        combine(selectedPriorityFilter, sortOrder) { priority, sort ->
            Pair(priority, sort)
        }
    ) { base, (priority, sort) ->
        FilterParams(base.query, base.status, base.time, base.courseId, priority, sort)
    }

    // Filtered homework list
    val filteredHomeworks: StateFlow<List<HomeworkWithCourse>> = combine(
        allHomeworks,
        filtersFlow,
        currentUser,
        adminAssignedFilter
    ) { homeworks, filters, user, adminFilter ->
        val now = System.currentTimeMillis()
        val startOfToday = getStartOfDay(now)
        val endOfToday = getEndOfDay(now)
        val endOfWeek = getEndOfWeek(now)

        var list = homeworks

        // 0. Profile/Student Filter
        if (user != null) {
            when {
                user.isTuna -> {
                    list = list.filter {
                        it.homework.assignedTo.equals("Tuna", ignoreCase = true)
                    }
                }
                user.isElif -> {
                    list = list.filter {
                        it.homework.assignedTo.equals("Elif", ignoreCase = true)
                    }
                }
                user.isTeacher -> {
                    when (adminFilter) {
                        "Tuna" -> list = list.filter { it.homework.assignedTo.equals("Tuna", ignoreCase = true) }
                        "Elif" -> list = list.filter { it.homework.assignedTo.equals("Elif", ignoreCase = true) }
                        else -> { /* show all */ }
                    }
                }
            }
        }

        // 1. Search Query
        if (filters.query.isNotBlank()) {
            val trimmed = filters.query.trim()
            list = list.filter {
                it.homework.title.contains(trimmed, ignoreCase = true) ||
                        it.homework.description.contains(trimmed, ignoreCase = true) ||
                        (it.course?.name?.contains(trimmed, ignoreCase = true) == true)
            }
        }

        // 2. Status Filter
        when (filters.status) {
            StatusFilter.PENDING -> list = list.filter { !it.homework.isCompleted }
            StatusFilter.COMPLETED -> list = list.filter { it.homework.isCompleted }
            StatusFilter.ALL -> {}
        }

        // 3. Time Filter
        when (filters.time) {
            TimeFilter.TODAY -> {
                list = list.filter {
                    it.homework.dueDateMillis in startOfToday..endOfToday
                }
            }
            TimeFilter.THIS_WEEK -> {
                val startOfWeek = getMondayOfWeek(now)
                val endOfWeek = getSundayOfWeek(now)
                list = list.filter {
                    it.homework.dueDateMillis in startOfWeek..endOfWeek
                }
            }
            TimeFilter.OVERDUE -> {
                list = list.filter {
                    isOverdue(it.homework.dueDateMillis, it.homework.isCompleted, now)
                }
            }
            TimeFilter.ALL -> {}
        }

        // 4. Course Filter
        if (filters.courseId != null) {
            list = list.filter { it.homework.courseId == filters.courseId }
        }

        // 5. Priority Filter
        if (filters.priority != null) {
            list = list.filter { it.homework.priority == filters.priority }
        }

        // 6. Sort
        when (filters.sort) {
            SortOrder.DUE_DATE_ASC -> list.sortedWith(
                compareBy<HomeworkWithCourse> { it.homework.isCompleted }
                    .thenBy { it.homework.dueDateMillis }
            )
            SortOrder.DUE_DATE_DESC -> list.sortedWith(
                compareBy<HomeworkWithCourse> { it.homework.isCompleted }
                    .thenByDescending { it.homework.dueDateMillis }
            )
            SortOrder.PRIORITY_HIGH_FIRST -> list.sortedWith(
                compareBy<HomeworkWithCourse> { it.homework.isCompleted }
                    .thenBy {
                        when (it.homework.priority) {
                            "HIGH" -> 0
                            "MEDIUM" -> 1
                            else -> 2
                        }
                    }
                    .thenBy { it.homework.dueDateMillis }
            )
            SortOrder.COURSE_NAME -> list.sortedWith(
                compareBy<HomeworkWithCourse> { it.homework.isCompleted }
                    .thenBy { it.course?.name ?: "" }
                    .thenBy { it.homework.dueDateMillis }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calculated Statistics (Scoped to Active Profile)
    val stats: StateFlow<HomeworkStats> = combine(
        allHomeworks,
        currentUser,
        adminAssignedFilter
    ) { homeworks, user, adminFilter ->
        val now = System.currentTimeMillis()
        val startOfToday = getStartOfDay(now)
        val endOfToday = getEndOfDay(now)

        var scopedList = homeworks
        if (user != null) {
            when {
                user.isTuna -> {
                    scopedList = scopedList.filter {
                        it.homework.assignedTo.equals("Tuna", ignoreCase = true)
                    }
                }
                user.isElif -> {
                    scopedList = scopedList.filter {
                        it.homework.assignedTo.equals("Elif", ignoreCase = true)
                    }
                }
                user.isTeacher -> {
                    when (adminFilter) {
                        "Tuna" -> scopedList = scopedList.filter { it.homework.assignedTo.equals("Tuna", ignoreCase = true) }
                        "Elif" -> scopedList = scopedList.filter { it.homework.assignedTo.equals("Elif", ignoreCase = true) }
                        else -> { /* show all */ }
                    }
                }
            }
        }

        val total = scopedList.size
        val completed = scopedList.count { it.homework.isCompleted }
        val pending = total - completed
        val dueToday = scopedList.count {
            !it.homework.isCompleted && it.homework.dueDateMillis in startOfToday..endOfToday
        }
        val overdue = scopedList.count {
            isOverdue(it.homework.dueDateMillis, it.homework.isCompleted, now)
        }
        val percentage = if (total > 0) (completed * 100) / total else 0
        val totalMinutes = scopedList.filter { !it.homework.isCompleted }.sumOf { it.homework.estimatedMinutes }
        val hours = totalMinutes / 60f

        HomeworkStats(
            totalCount = total,
            completedCount = completed,
            pendingCount = pending,
            dueTodayCount = dueToday,
            overdueCount = overdue,
            completionPercentage = percentage,
            totalEstimatedHours = hours
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeworkStats()
    )

    // Actions
    fun toggleCompletion(homework: Homework) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newStatus = !homework.isCompleted
            repository.toggleComplete(homework.id, newStatus, now)
            syncManager.updateHomeworkCompletionInCloud(homework.id, newStatus, now)
        }
    }

    fun saveHomework(
        id: Long = 0,
        courseId: Long,
        title: String,
        description: String,
        dueDateMillis: Long,
        dueTimeText: String,
        priority: String,
        estimatedMinutes: Int,
        assignedTo: String = ""
    ) {
        viewModelScope.launch {
            val targetStudent = if (assignedTo.isNotBlank()) {
                assignedTo
            } else {
                currentUser.value?.name ?: "Tuna"
            }
            val now = System.currentTimeMillis()
            val item = Homework(
                id = id,
                courseId = courseId,
                title = title.trim(),
                description = description.trim(),
                dueDateMillis = dueDateMillis,
                dueTimeText = dueTimeText,
                priority = priority,
                estimatedMinutes = estimatedMinutes,
                assignedTo = targetStudent,
                updatedAtMillis = now
            )
            val generatedId = if (id == 0L) {
                repository.insertHomework(item)
            } else {
                repository.updateHomework(item)
                item.id
            }
            val finalItem = if (id == 0L && generatedId > 0) item.copy(id = generatedId) else item
            syncManager.uploadHomeworkToCloud(finalItem)
        }
    }

    fun deleteHomework(homework: Homework) {
        viewModelScope.launch {
            repository.deleteHomework(homework)
            syncManager.deleteHomeworkFromCloud(homework.id)
        }
    }

    fun clearAllFirebaseData(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val msg = syncManager.clearAllFirebaseData()
                onResult(true, msg)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Silme başarısız: ${e.message}")
            }
        }
    }

    fun resetFirebaseWithDemoData(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val msg = syncManager.resetFirebaseWithDemoData()
                onResult(true, msg)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sıfırlama başarısız: ${e.message}")
            }
        }
    }

    fun exportFirebaseDataToJson(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = syncManager.exportAllDataToJson()
                onResult(true, json)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Dışa aktarma başarısız: ${e.message}")
            }
        }
    }

    fun importFirebaseDataFromJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val msg = syncManager.importDataFromJson(jsonString)
                onResult(true, msg)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "İçe aktarma hatası: ${e.message}")
            }
        }
    }

    fun saveCourse(
        id: Long = 0,
        name: String,
        teacher: String,
        colorHex: String,
        iconName: String
    ) {
        viewModelScope.launch {
            val course = Course(
                id = id,
                name = name.trim(),
                teacher = teacher.trim(),
                colorHex = colorHex,
                iconName = iconName
            )
            val generatedId = if (id == 0L) {
                repository.insertCourse(course)
            } else {
                repository.updateCourse(course)
                course.id
            }
            val finalCourse = if (id == 0L && generatedId > 0) course.copy(id = generatedId) else course
            syncManager.uploadCourseToCloud(finalCourse)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            syncManager.deleteCourseFromCloud(course.id)
        }
    }

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun setStatus(filter: StatusFilter) {
        statusFilter.value = filter
    }

    fun setTime(filter: TimeFilter) {
        timeFilter.value = filter
    }

    fun setCourseFilter(courseId: Long?) {
        selectedCourseFilter.value = courseId
    }

    fun setPriorityFilter(priority: String?) {
        selectedPriorityFilter.value = priority
    }

    fun setSort(order: SortOrder) {
        sortOrder.value = order
    }

    val themeMode = MutableStateFlow("SYSTEM") // "SYSTEM", "LIGHT", "DARK"
    val colorPalette = MutableStateFlow("DYNAMIC") // "DYNAMIC", "INDIGO", "TEAL", "VIOLET", "ROSE", "FOREST", "AMBER"
    val hideCompleted = MutableStateFlow(false)

    fun setThemeMode(mode: String) {
        themeMode.value = mode
    }

    fun setColorPalette(palette: String) {
        colorPalette.value = palette
    }

    fun setHideCompleted(hide: Boolean) {
        hideCompleted.value = hide
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            repository.resetToDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun setSelectedCalendarDate(millis: Long) {
        selectedCalendarDate.value = getStartOfDay(millis)
    }

    companion object {
        fun getStartOfDay(millis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun getEndOfDay(millis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return cal.timeInMillis
        }

        fun getMondayOfWeek(millis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                val dayOfWeek = get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else (dayOfWeek - Calendar.MONDAY)
                add(Calendar.DAY_OF_YEAR, -daysFromMonday)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        /**
         * O haftanın Pazar günü 23:59:59.999 zamanını döner.
         * Teslim tarihi her zaman o haftanın Pazar günüdür.
         */
        fun getSundayOfWeek(millis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                val dayOfWeek = get(Calendar.DAY_OF_WEEK)
                val daysUntilSunday = if (dayOfWeek == Calendar.SUNDAY) 0 else (Calendar.SATURDAY - dayOfWeek + 1)
                add(Calendar.DAY_OF_YEAR, daysUntilSunday)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return cal.timeInMillis
        }

        fun getEndOfWeek(millis: Long): Long {
            return getSundayOfWeek(millis)
        }

        /**
         * Bir ödev ancak o haftanın Pazar günü (23:59:59) bittiğinde ve tamamlanmamışsa gecikmiş sayılır.
         * Gün bitti diye (örn. Çarşamba bittiğinde) gecikmiş olmaz.
         */
        fun isOverdue(dueDateMillis: Long, isCompleted: Boolean, now: Long = System.currentTimeMillis()): Boolean {
            if (isCompleted) return false
            val sundayDeadline = getSundayOfWeek(dueDateMillis)
            return now > sundayDeadline
        }
    }
}

class HomeworkViewModelFactory(
    private val repository: HomeworkRepository,
    private val authManager: AuthManager,
    private val syncManager: FirebaseSyncManager,
    private val notesRepository: NotesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeworkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeworkViewModel(repository, authManager, syncManager, notesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
