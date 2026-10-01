package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.HomeworkWithCourse
import com.example.data.model.UserAccount
import com.example.data.repository.HomeworkRepository
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
    private val syncManager: FirebaseSyncManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.cleanupDuplicates()
            repository.seedInitialDataIfNeeded()
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
                list = list.filter {
                    it.homework.dueDateMillis in startOfToday..endOfWeek
                }
            }
            TimeFilter.OVERDUE -> {
                list = list.filter {
                    !it.homework.isCompleted && it.homework.dueDateMillis < startOfToday
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
            !it.homework.isCompleted && it.homework.dueDateMillis < startOfToday
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

        fun getEndOfWeek(millis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                add(Calendar.DAY_OF_YEAR, 7)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return cal.timeInMillis
        }
    }
}

class HomeworkViewModelFactory(
    private val repository: HomeworkRepository,
    private val authManager: AuthManager,
    private val syncManager: FirebaseSyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeworkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeworkViewModel(repository, authManager, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
