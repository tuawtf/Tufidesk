package com.example.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.repository.HomeworkRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class SyncState {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

class FirebaseSyncManager(
    private val context: Context,
    private val repository: HomeworkRepository,
    private val scope: CoroutineScope
) {
    private val TAG = "FirebaseSyncManager"

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialNetwork())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    private var homeworksListener: ListenerRegistration? = null
    private var coursesListener: ListenerRegistration? = null

    init {
        initFirebaseIfAvailable()
        startRealtimeListeners()
        registerNetworkCallback()
    }

    private fun checkInitialNetwork(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork ?: return true
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    private fun initFirebaseIfAvailable() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                try {
                    val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                    db.firestoreSettings = settings
                } catch (ignored: Exception) {
                    // Settings might already be locked
                }
                firestore = db
                Log.d(TAG, "Firebase Firestore successfully initialized")
            } else {
                Log.w(TAG, "FirebaseApp is not initialized (google-services.json not yet placed)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Firestore: ${e.message}", e)
            _lastErrorMessage.value = "Firebase Başlatma Bilgisi: Yerel veritabanı aktif."
        }
    }

    private fun registerNetworkCallback() {
        try {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                    Log.d(TAG, "Internet connection available - triggering realtime sync")
                    startRealtimeListeners()
                    scope.launch(Dispatchers.IO) {
                        syncWithCloud(silent = true)
                    }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkInitialNetwork()
                    if (!_isOnline.value) {
                        _syncState.value = SyncState.OFFLINE
                        Log.d(TAG, "Internet connection lost - switched to offline mode")
                    }
                }
            })
        } catch (e: Exception) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                    startRealtimeListeners()
                    scope.launch(Dispatchers.IO) {
                        syncWithCloud(silent = true)
                    }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkInitialNetwork()
                    if (!_isOnline.value) {
                        _syncState.value = SyncState.OFFLINE
                    }
                }
            })
        }
    }

    private suspend fun resolveValidCourseId(rawCourseId: Long, courseName: String): Long {
        return try {
            val localCourses = repository.allCourses.first()
            if (localCourses.any { it.id == rawCourseId }) {
                return rawCourseId
            }
            if (courseName.isNotBlank()) {
                val byName = localCourses.find { it.name.trim().equals(courseName.trim(), ignoreCase = true) }
                if (byName != null) return byName.id
            }
            if (localCourses.isNotEmpty()) {
                return localCourses.first().id
            }
            repository.insertCourse(
                Course(
                    name = if (courseName.isNotBlank()) courseName else "Genel",
                    teacher = "",
                    colorHex = "#1B65D4",
                    iconName = "MenuBook"
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "Fallback course ID resolution: ${e.message}")
            1L
        }
    }

    /**
     * Real-time listeners for sub-second, instant synchronization across devices.
     * When any user checks a checkbox or updates an assignment on another device,
     * Firestore fires a snapshot callback and the local database is updated in real time.
     */
    fun startRealtimeListeners() {
        if (firestore == null) {
            initFirebaseIfAvailable()
        }
        val db = firestore ?: return

        // 1. Homeworks Realtime Listener
        homeworksListener?.remove()
        homeworksListener = db.collection("homeworks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Homeworks realtime listener error: ${error.message}", error)
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        _lastErrorMessage.value = "Bulut Firestore kuralları kısıtlı. Verileriniz yerel cihazınızda (Room) güvenle saklanmaktadır."
                    } else {
                        _lastErrorMessage.value = "Senkronizasyon hatası: ${error.localizedMessage ?: error.message}"
                    }
                    _syncState.value = SyncState.ERROR
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                scope.launch(Dispatchers.IO) {
                    try {
                        val localHomeworks = repository.allHomeworks.first()

                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val id = doc.getLong("id") ?: continue

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val rawCourseId = doc.getLong("courseId") ?: 1L
                                    val courseName = doc.getString("courseName") ?: ""
                                    val courseId = resolveValidCourseId(rawCourseId, courseName)
                                    val title = doc.getString("title") ?: ""
                                    val description = doc.getString("description") ?: ""
                                    val dueDateMillis = doc.getLong("dueDateMillis") ?: System.currentTimeMillis()
                                    val dueTimeText = doc.getString("dueTimeText") ?: "23:59"
                                    val priority = doc.getString("priority") ?: "MEDIUM"
                                    val estimatedMinutes = doc.getLong("estimatedMinutes")?.toInt() ?: 0
                                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                                    val createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                                    val completedAtMillis = doc.getLong("completedAtMillis")?.takeIf { it > 0 }
                                    val updatedAtMillis = doc.getLong("updatedAtMillis")
                                        ?: completedAtMillis
                                        ?: createdAtMillis
                                    val assignedTo = doc.getString("assignedTo") ?: "Tuna"

                                    val existing = localHomeworks.find { it.homework.id == id }

                                    // "Clickbox'lara veri yazan öncelikli olacak":
                                    // If new or if remote is newer or has updated completion status, update local immediately
                                    try {
                                        if (existing == null) {
                                            repository.insertHomework(
                                                Homework(
                                                    id = id,
                                                    courseId = courseId,
                                                    title = title,
                                                    description = description,
                                                    dueDateMillis = dueDateMillis,
                                                    dueTimeText = dueTimeText,
                                                    priority = priority,
                                                    estimatedMinutes = estimatedMinutes,
                                                    isCompleted = isCompleted,
                                                    createdAtMillis = createdAtMillis,
                                                    completedAtMillis = completedAtMillis,
                                                    assignedTo = assignedTo,
                                                    updatedAtMillis = updatedAtMillis
                                                )
                                            )
                                        } else {
                                            val localHw = existing.homework
                                            // Update if remote is newer or completion state was modified
                                            if (updatedAtMillis >= localHw.updatedAtMillis || localHw.isCompleted != isCompleted) {
                                                repository.insertHomework(
                                                    Homework(
                                                        id = id,
                                                        courseId = courseId,
                                                        title = title,
                                                        description = description,
                                                        dueDateMillis = dueDateMillis,
                                                        dueTimeText = dueTimeText,
                                                        priority = priority,
                                                        estimatedMinutes = estimatedMinutes,
                                                        isCompleted = isCompleted,
                                                        createdAtMillis = createdAtMillis,
                                                        completedAtMillis = completedAtMillis,
                                                        assignedTo = assignedTo,
                                                        updatedAtMillis = updatedAtMillis
                                                    )
                                                )
                                            }
                                        }
                                    } catch (insertEx: Exception) {
                                        Log.e(TAG, "Error inserting realtime homework $id: ${insertEx.message}", insertEx)
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    val existing = localHomeworks.find { it.homework.id == id }
                                    if (existing != null) {
                                        repository.deleteHomework(existing.homework)
                                    }
                                }
                            }
                        }
                        _syncState.value = SyncState.SYNCED
                        _lastSyncTime.value = System.currentTimeMillis()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing realtime homework update: ${e.message}", e)
                    }
                }
            }

        // 2. Courses Realtime Listener
        coursesListener?.remove()
        coursesListener = db.collection("courses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Courses realtime listener error: ${error.message}", error)
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        _lastErrorMessage.value = "Firestore Kuralları Engeli: Lütfen Firebase Console'da kuralları 'allow read, write: if true;' olarak güncelleyin."
                    } else {
                        _lastErrorMessage.value = "Senkronizasyon hatası: ${error.localizedMessage ?: error.message}"
                    }
                    _syncState.value = SyncState.ERROR
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                scope.launch(Dispatchers.IO) {
                    try {
                        val localCourses = repository.allCourses.first()
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val id = doc.getLong("id") ?: continue

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val name = doc.getString("name") ?: ""
                                    val teacher = doc.getString("teacher") ?: ""
                                    val colorHex = doc.getString("colorHex") ?: "#1B65D4"
                                    val iconName = doc.getString("iconName") ?: "menu_book"

                                    val existing = localCourses.find { it.id == id || it.name.trim().equals(name.trim(), ignoreCase = true) }
                                    if (existing == null || existing.name != name || existing.teacher != teacher || existing.colorHex != colorHex || existing.iconName != iconName) {
                                        repository.insertCourse(
                                            Course(id = existing?.id ?: id, name = name, teacher = teacher, colorHex = colorHex, iconName = iconName)
                                        )
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    val existing = localCourses.find { it.id == id }
                                    if (existing != null) {
                                        repository.deleteCourse(existing)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing realtime course update: ${e.message}", e)
                    }
                }
            }
    }

    /**
     * Checkbox'a basıldığında ANINDA Firestore'a yazar.
     * Veriyi yazan oturum önceliklidir (yeni zaman damgasıyla).
     * Gerçek zamanlı dinleyici sayesinde diğer cihazlar milisaniyeler içinde güncellenir.
     */
    suspend fun updateHomeworkCompletionInCloud(
        homeworkId: Long,
        isCompleted: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (!_isOnline.value) return
        val db = firestore ?: return
        try {
            val completedAt = if (isCompleted) timestamp else null
            db.collection("homeworks").document(homeworkId.toString())
                .set(
                    mapOf(
                        "isCompleted" to isCompleted,
                        "completedAtMillis" to (completedAt ?: 0L),
                        "updatedAtMillis" to timestamp
                    ),
                    SetOptions.merge()
                )
                .await()
            _lastSyncTime.value = timestamp
            _syncState.value = SyncState.SYNCED
            Log.d(TAG, "Checkbox anında Firestore'a iletildi: hw=$homeworkId, isCompleted=$isCompleted, time=$timestamp")
        } catch (e: Exception) {
            Log.e(TAG, "Checkbox durumu buluta yazılamadı: ${e.message}", e)
        }
    }

    suspend fun uploadHomeworkToCloud(hw: Homework) {
        if (!_isOnline.value) return
        val db = firestore ?: return
        try {
            val localCourses = repository.allCourses.first()
            val courseName = localCourses.find { it.id == hw.courseId }?.name ?: ""
            val data = mapOf(
                "id" to hw.id,
                "courseId" to hw.courseId,
                "courseName" to courseName,
                "title" to hw.title,
                "description" to hw.description,
                "dueDateMillis" to hw.dueDateMillis,
                "dueTimeText" to hw.dueTimeText,
                "priority" to hw.priority,
                "estimatedMinutes" to hw.estimatedMinutes,
                "isCompleted" to hw.isCompleted,
                "createdAtMillis" to hw.createdAtMillis,
                "completedAtMillis" to (hw.completedAtMillis ?: 0L),
                "assignedTo" to hw.assignedTo,
                "updatedAtMillis" to hw.updatedAtMillis
            )
            db.collection("homeworks").document(hw.id.toString())
                .set(data, SetOptions.merge())
                .await()
            _lastSyncTime.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Ödev buluta yüklenemedi: ${e.message}", e)
        }
    }

    suspend fun deleteHomeworkFromCloud(id: Long) {
        if (!_isOnline.value) return
        val db = firestore ?: return
        try {
            db.collection("homeworks").document(id.toString()).delete().await()
            _lastSyncTime.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "Ödev buluttan silinemedi: ${e.message}", e)
        }
    }

    suspend fun uploadCourseToCloud(c: Course) {
        if (!_isOnline.value) return
        val db = firestore ?: return
        try {
            val data = mapOf(
                "id" to c.id,
                "name" to c.name,
                "teacher" to c.teacher,
                "colorHex" to c.colorHex,
                "iconName" to c.iconName
            )
            db.collection("courses").document(c.id.toString())
                .set(data, SetOptions.merge())
                .await()
            _lastSyncTime.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "Ders buluta yüklenemedi: ${e.message}", e)
        }
    }

    suspend fun deleteCourseFromCloud(id: Long) {
        if (!_isOnline.value) return
        val db = firestore ?: return
        try {
            db.collection("courses").document(id.toString()).delete().await()
            _lastSyncTime.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "Ders buluttan silinemedi: ${e.message}", e)
        }
    }

    /**
     * Initial / fallback full synchronization with conflict-safe last-write-wins logic.
     * Does NOT blindly overwrite newer cloud data with older local data.
     */
    suspend fun syncWithCloud(silent: Boolean = false): Boolean {
        if (!_isOnline.value) {
            if (!silent) _syncState.value = SyncState.OFFLINE
            return false
        }

        if (firestore == null) {
            initFirebaseIfAvailable()
            if (firestore == null) {
                _syncState.value = SyncState.SYNCED
                _lastSyncTime.value = System.currentTimeMillis()
                return true
            }
        }

        val db = firestore ?: return false
        if (!silent) _syncState.value = SyncState.SYNCING

        return try {
            // 1. Courses Sync
            val localCourses = repository.allCourses.first()
            val remoteCoursesSnapshot = db.collection("courses").get().await()

            for (doc in remoteCoursesSnapshot.documents) {
                val id = doc.getLong("id") ?: continue
                val name = doc.getString("name") ?: ""
                val teacher = doc.getString("teacher") ?: ""
                val colorHex = doc.getString("colorHex") ?: "#1B65D4"
                val iconName = doc.getString("iconName") ?: "menu_book"

                val existing = localCourses.find { it.id == id || it.name.trim().equals(name.trim(), ignoreCase = true) }
                if (existing == null) {
                    repository.insertCourse(
                        Course(id = id, name = name, teacher = teacher, colorHex = colorHex, iconName = iconName)
                    )
                }
            }

            for (c in localCourses) {
                if (remoteCoursesSnapshot.documents.none { it.getLong("id") == c.id }) {
                    uploadCourseToCloud(c)
                }
            }

            // 2. Homeworks Sync - Conflict-safe
            val localHomeworks = repository.allHomeworks.first()
            val remoteHomeworksSnapshot = db.collection("homeworks").get().await()

            for (doc in remoteHomeworksSnapshot.documents) {
                val id = doc.getLong("id") ?: continue
                val rawCourseId = doc.getLong("courseId") ?: 1L
                val courseName = doc.getString("courseName") ?: ""
                val courseId = resolveValidCourseId(rawCourseId, courseName)
                val title = doc.getString("title") ?: ""
                val description = doc.getString("description") ?: ""
                val dueDateMillis = doc.getLong("dueDateMillis") ?: System.currentTimeMillis()
                val dueTimeText = doc.getString("dueTimeText") ?: "23:59"
                val priority = doc.getString("priority") ?: "MEDIUM"
                val estimatedMinutes = doc.getLong("estimatedMinutes")?.toInt() ?: 0
                val isCompleted = doc.getBoolean("isCompleted") ?: false
                val createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                val completedAtMillis = doc.getLong("completedAtMillis")?.takeIf { it > 0 }
                val remoteUpdatedAt = doc.getLong("updatedAtMillis")
                    ?: completedAtMillis
                    ?: createdAtMillis
                val assignedTo = doc.getString("assignedTo") ?: "Tuna"

                try {
                    val existing = localHomeworks.find { it.homework.id == id }
                    if (existing == null) {
                        repository.insertHomework(
                            Homework(
                                id = id,
                                courseId = courseId,
                                title = title,
                                description = description,
                                dueDateMillis = dueDateMillis,
                                dueTimeText = dueTimeText,
                                priority = priority,
                                estimatedMinutes = estimatedMinutes,
                                isCompleted = isCompleted,
                                createdAtMillis = createdAtMillis,
                                completedAtMillis = completedAtMillis,
                                assignedTo = assignedTo,
                                updatedAtMillis = remoteUpdatedAt
                            )
                        )
                    } else {
                        val localHw = existing.homework
                        // Veriyi yazan (en güncel zaman damgasına sahip olan) öncelikli olur:
                        if (remoteUpdatedAt >= localHw.updatedAtMillis || localHw.isCompleted != isCompleted) {
                            repository.insertHomework(
                                Homework(
                                    id = id,
                                    courseId = courseId,
                                    title = title,
                                    description = description,
                                    dueDateMillis = dueDateMillis,
                                    dueTimeText = dueTimeText,
                                    priority = priority,
                                    estimatedMinutes = estimatedMinutes,
                                    isCompleted = isCompleted,
                                    createdAtMillis = createdAtMillis,
                                    completedAtMillis = completedAtMillis,
                                    assignedTo = assignedTo,
                                    updatedAtMillis = remoteUpdatedAt
                                )
                            )
                        } else if (localHw.updatedAtMillis > remoteUpdatedAt) {
                            // Yereldeki daha yeniyse buluta aktar
                            uploadHomeworkToCloud(localHw)
                        }
                    }
                } catch (insertErr: Exception) {
                    Log.e(TAG, "Error syncing homework $id: ${insertErr.message}", insertErr)
                }
            }

            // Yerelde olup bulutta hiç olmayan ödevleri buluta yükle
            for (hwWithCourse in localHomeworks) {
                val hw = hwWithCourse.homework
                if (remoteHomeworksSnapshot.documents.none { it.getLong("id") == hw.id }) {
                    uploadHomeworkToCloud(hw)
                }
            }

            _syncState.value = SyncState.SYNCED
            _lastErrorMessage.value = null
            _lastSyncTime.value = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sync error (silent=$silent): ${e.message}", e)
            val friendlyMsg = when {
                e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Güvenlik Kuralı Engeli (PERMISSION_DENIED): Firestore Kurallar (Rules) sekmesinde 'allow read, write: if true;' izni verilmelidir."
                e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND ->
                    "Veritabanı Bulunamadı (NOT_FOUND): Firebase Console'da Cloud Firestore sekmesine gidip 'Veritabanı Oluştur (Create database)' işlemini başlatın."
                e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Sunucuya Erişilemiyor (UNAVAILABLE): İnternet bağlantınızı kontrol edin. Çevrimdışı modda yerel Room DB devrede."
                else ->
                    e.localizedMessage ?: e.message ?: "Bilinmeyen Firestore hatası"
            }
            _lastErrorMessage.value = friendlyMsg
            _syncState.value = SyncState.ERROR
            false
        }
    }
}
