package com.example.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.Note
import com.example.data.model.NoteFolder
import com.example.data.model.NoteType
import com.example.data.repository.HomeworkRepository
import com.example.data.repository.NotesRepository
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
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private val scope: CoroutineScope,
    private val notesRepository: NotesRepository? = null
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
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    private fun initFirebaseIfAvailable() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    Log.w(TAG, "Standard FirebaseApp.initializeApp failed, trying fallback options: ${e.message}")
                }
            }

            // If still empty (e.g. google-services.json missing from Gradle build artifacts on GitHub Actions), initialize programmatically!
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:604652700316:android:56eaee97a63cdf305b6359")
                        .setApiKey("AIzaSyC5mIBDPChrJMHnuEWl1QTlL1fFNBL_uwA")
                        .setProjectId("tufidesk")
                        .setStorageBucket("tufidesk.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    Log.i(TAG, "FirebaseApp initialized with programmatic FirebaseOptions fallback")
                } catch (optionsErr: Exception) {
                    Log.e(TAG, "Programmatic FirebaseOptions initialization failed: ${optionsErr.message}", optionsErr)
                }
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
                Log.w(TAG, "FirebaseApp is not initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Firestore: ${e.message}", e)
            _lastErrorMessage.value = "Firebase Başlatma Hatası: ${e.localizedMessage ?: e.message}"
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
                if (!silent) _syncState.value = SyncState.ERROR
                return false
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

    /**
     * Firebase'deki tüm koleksiyonları (homeworks, courses, notes, note_folders, settings, users)
     * ve yerel veritabanını TAMAMEN siler ve boş bırakır.
     */
    suspend fun clearAllFirebaseData(): String = withContext(Dispatchers.IO) {
        initFirebaseIfAvailable()
        val db = firestore

        _syncState.value = SyncState.SYNCING

        try {
            if (db != null) {
                val collections = listOf("courses", "homeworks", "notes", "note_folders", "settings", "users")
                for (colName in collections) {
                    try {
                        val snapshot = db.collection(colName).get().await()
                        for (doc in snapshot.documents) {
                            try {
                                doc.reference.delete().await()
                            } catch (e: Exception) {
                                Log.w(TAG, "Error deleting doc ${doc.id} in $colName: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error getting collection $colName for deletion: ${e.message}")
                    }
                }
            }

            // Yerel verileri de temizle
            repository.clearAllData()
            notesRepository?.clearAll()

            _syncState.value = SyncState.IDLE
            return@withContext "Firebase ve yerel veritabanındaki tüm veriler başarıyla silindi! Veritabanı tamamen boşaltıldı."
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            _lastErrorMessage.value = e.message
            return@withContext "Hata oluştu: ${e.message}"
        }
    }

    /**
     * Firebase'deki tüm koleksiyonları (homeworks, courses, notes, note_folders, settings) temizler
     * ve geçerli haftalık Pazar teslimatlı demo verilerini hem Firebase'e hem yerel Room DB'ye yükler.
     */
    suspend fun resetFirebaseWithDemoData(): String = withContext(Dispatchers.IO) {
        initFirebaseIfAvailable()
        val db = firestore

        _syncState.value = SyncState.SYNCING

        try {
            if (db == null) {
                // Firebase henüz bağlanmamış veya offline ise yerel DB'yi sıfırlayıp demo verileri yükle
                repository.clearAllData()
                notesRepository?.clearAll()
                repository.seedInitialDataIfNeeded(force = true)
                notesRepository?.seedInitialNotesIfNeeded(force = true)

                val localCourses = repository.getAllCoursesList()
                val localHomeworks = repository.getAllHomeworksList()
                val localNotes = notesRepository?.getAllNotesList().orEmpty()

                _syncState.value = SyncState.IDLE
                return@withContext "Yerel veritabanı temizlendi ve güncel demo veriler yüklendi! (${localCourses.size} ders, ${localHomeworks.size} ödev, ${localNotes.size} not). Firebase bağlantısı aktifleştiğinde buluta aktarılacaktır."
            }

            // 1. Delete all remote documents in collections
            val collections = listOf("courses", "homeworks", "notes", "note_folders", "settings")
            for (colName in collections) {
                try {
                    val snapshot = db.collection(colName).get().await()
                    for (doc in snapshot.documents) {
                        try {
                            doc.reference.delete().await()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error deleting doc ${doc.id} in $colName: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting collection $colName for deletion: ${e.message}")
                }
            }

            // 2. Clear local data and reseed demo data
            repository.clearAllData()
            notesRepository?.clearAll()

            repository.seedInitialDataIfNeeded(force = true)
            notesRepository?.seedInitialNotesIfNeeded(force = true)

            // 3. Upload newly seeded courses to Firestore
            val localCourses = repository.getAllCoursesList()
            for (course in localCourses) {
                db.collection("courses").document(course.id.toString()).set(
                    mapOf(
                        "id" to course.id,
                        "name" to course.name,
                        "teacher" to course.teacher,
                        "colorHex" to course.colorHex,
                        "iconName" to course.iconName,
                        "updatedAtMillis" to System.currentTimeMillis()
                    )
                ).await()
            }

            // 4. Upload newly seeded homeworks to Firestore (with Sunday weekly due date)
            val localHomeworks = repository.getAllHomeworksList()
            for (hw in localHomeworks) {
                db.collection("homeworks").document(hw.id.toString()).set(
                    mapOf(
                        "id" to hw.id,
                        "courseId" to hw.courseId,
                        "title" to hw.title,
                        "description" to hw.description,
                        "dueDateMillis" to hw.dueDateMillis,
                        "dueTimeText" to hw.dueTimeText,
                        "priority" to hw.priority,
                        "isCompleted" to hw.isCompleted,
                        "estimatedMinutes" to hw.estimatedMinutes,
                        "createdAtMillis" to hw.createdAtMillis,
                        "completedAtMillis" to (hw.completedAtMillis ?: 0L),
                        "assignedTo" to hw.assignedTo,
                        "updatedAtMillis" to hw.updatedAtMillis
                    )
                ).await()
            }

            // 5. Upload folders and notes
            val localFolders = notesRepository?.getAllFoldersList().orEmpty()
            for (f in localFolders) {
                db.collection("note_folders").document(f.id.toString()).set(
                    mapOf(
                        "id" to f.id,
                        "name" to f.name,
                        "colorHex" to f.colorHex,
                        "iconName" to f.iconName
                    )
                ).await()
            }

            val localNotes = notesRepository?.getAllNotesList().orEmpty()
            for (n in localNotes) {
                db.collection("notes").document(n.id.toString()).set(
                    mapOf(
                        "id" to n.id,
                        "title" to n.title,
                        "content" to n.content,
                        "folderId" to (n.folderId ?: 0L),
                        "colorHex" to n.colorHex,
                        "isPinned" to n.isPinned,
                        "isLocked" to n.isLocked,
                        "noteType" to n.type.name,
                        "createdAtMillis" to n.createdAtMillis,
                        "updatedAtMillis" to n.updatedAtMillis,
                        "assignedTo" to n.assignedTo,
                        "drawingData" to (n.drawingData ?: ""),
                        "reportData" to (n.reportData ?: ""),
                        "imageUri" to (n.imageUri ?: "")
                    )
                ).await()
            }

            _syncState.value = SyncState.SYNCED
            _lastSyncTime.value = System.currentTimeMillis()
            _lastErrorMessage.value = null

            "Firebase tamamen temizlendi ve güncel demo veriler başarıyla yüklendi! (${localCourses.size} ders, ${localHomeworks.size} ödev, ${localNotes.size} not)"
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            _lastErrorMessage.value = e.localizedMessage ?: e.message
            throw e
        }
    }

    /**
     * Firebase ve yerel veritabanındaki tüm dersleri, ödevleri ve notları JSON formatında dışa aktarır.
     */
    suspend fun exportAllDataToJson(): String = withContext(Dispatchers.IO) {
        val rootJson = org.json.JSONObject()
        rootJson.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        rootJson.put("appName", "TufiDESK")
        rootJson.put("version", 1)

        val db = firestore
        val coursesArray = org.json.JSONArray()
        val homeworksArray = org.json.JSONArray()
        val foldersArray = org.json.JSONArray()
        val notesArray = org.json.JSONArray()

        if (db != null) {
            try {
                val coursesDocs = db.collection("courses").get().await().documents
                for (doc in coursesDocs) {
                    val obj = org.json.JSONObject()
                    doc.data?.forEach { (k, v) -> obj.put(k, v) }
                    if (!obj.has("id")) obj.put("id", doc.id.toLongOrNull() ?: 0L)
                    coursesArray.put(obj)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Export: remote courses fetch failed, fallback to local: ${e.message}")
            }

            try {
                val hwDocs = db.collection("homeworks").get().await().documents
                for (doc in hwDocs) {
                    val obj = org.json.JSONObject()
                    doc.data?.forEach { (k, v) -> obj.put(k, v) }
                    if (!obj.has("id")) obj.put("id", doc.id.toLongOrNull() ?: 0L)
                    homeworksArray.put(obj)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Export: remote homeworks fetch failed, fallback to local: ${e.message}")
            }

            try {
                val folderDocs = db.collection("note_folders").get().await().documents
                for (doc in folderDocs) {
                    val obj = org.json.JSONObject()
                    doc.data?.forEach { (k, v) -> obj.put(k, v) }
                    foldersArray.put(obj)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Export: remote folders fetch failed: ${e.message}")
            }

            try {
                val noteDocs = db.collection("notes").get().await().documents
                for (doc in noteDocs) {
                    val obj = org.json.JSONObject()
                    doc.data?.forEach { (k, v) -> obj.put(k, v) }
                    notesArray.put(obj)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Export: remote notes fetch failed: ${e.message}")
            }
        }

        // If remote was empty or unavailable, fallback to local
        if (coursesArray.length() == 0) {
            val localCourses = repository.getAllCoursesList()
            for (c in localCourses) {
                coursesArray.put(org.json.JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("teacher", c.teacher)
                    put("colorHex", c.colorHex)
                    put("iconName", c.iconName)
                })
            }
        }

        if (homeworksArray.length() == 0) {
            val localHws = repository.getAllHomeworksList()
            for (h in localHws) {
                homeworksArray.put(org.json.JSONObject().apply {
                    put("id", h.id)
                    put("courseId", h.courseId)
                    put("title", h.title)
                    put("description", h.description)
                    put("dueDateMillis", h.dueDateMillis)
                    put("dueTimeText", h.dueTimeText)
                    put("priority", h.priority)
                    put("isCompleted", h.isCompleted)
                    put("estimatedMinutes", h.estimatedMinutes)
                    put("assignedTo", h.assignedTo)
                    put("createdAtMillis", h.createdAtMillis)
                    put("completedAtMillis", h.completedAtMillis ?: 0L)
                    put("updatedAtMillis", h.updatedAtMillis)
                })
            }
        }

        if (foldersArray.length() == 0) {
            val localFolders = notesRepository?.getAllFoldersList().orEmpty()
            for (f in localFolders) {
                foldersArray.put(org.json.JSONObject().apply {
                    put("id", f.id)
                    put("name", f.name)
                    put("colorHex", f.colorHex)
                    put("iconName", f.iconName)
                })
            }
        }

        if (notesArray.length() == 0) {
            val localNotes = notesRepository?.getAllNotesList().orEmpty()
            for (n in localNotes) {
                notesArray.put(org.json.JSONObject().apply {
                    put("id", n.id)
                    put("title", n.title)
                    put("content", n.content)
                    put("folderId", n.folderId ?: 0L)
                    put("colorHex", n.colorHex)
                    put("isPinned", n.isPinned)
                    put("isLocked", n.isLocked)
                    put("noteType", n.type.name)
                    put("assignedTo", n.assignedTo)
                    put("createdAtMillis", n.createdAtMillis)
                    put("updatedAtMillis", n.updatedAtMillis)
                    put("drawingData", n.drawingData ?: "")
                    put("reportData", n.reportData ?: "")
                    put("imageUri", n.imageUri ?: "")
                })
            }
        }

        rootJson.put("courses", coursesArray)
        rootJson.put("homeworks", homeworksArray)
        rootJson.put("note_folders", foldersArray)
        rootJson.put("notes", notesArray)

        rootJson.toString(2)
    }

    /**
     * Dışarıdan sağlanan JSON verisini ayrıştırıp hem Firestore'a hem yerel veritabanına yazar.
     */
    suspend fun importDataFromJson(jsonString: String): String = withContext(Dispatchers.IO) {
        val cleanJson = jsonString.trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        initFirebaseIfAvailable()
        val db = firestore

        var importedCourses = 0
        var importedHws = 0
        var importedFolders = 0
        var importedNotes = 0

        if (cleanJson.startsWith("[")) {
            // Direct homework array import
            val hwsArray = org.json.JSONArray(cleanJson)
            for (i in 0 until hwsArray.length()) {
                val obj = hwsArray.getJSONObject(i)
                val id = obj.optLong("id", System.currentTimeMillis() + i)
                val courseId = obj.optLong("courseId", 1L)
                val title = obj.optString("title", "Ödev")
                val description = obj.optString("description", "")
                val dueDateMillis = obj.optLong("dueDateMillis", System.currentTimeMillis())
                val dueTimeText = obj.optString("dueTimeText", "23:59")
                val priority = obj.optString("priority", "MEDIUM")
                val isCompleted = obj.optBoolean("isCompleted", false)
                val estimatedMinutes = obj.optInt("estimatedMinutes", 30)
                val assignedTo = obj.optString("assignedTo", "Tuna")
                val createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                val completedAtMillis = if (obj.has("completedAtMillis") && !obj.isNull("completedAtMillis")) obj.optLong("completedAtMillis").takeIf { it > 0 } else null
                val updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())

                val hw = Homework(
                    id = id,
                    courseId = courseId,
                    title = title,
                    description = description,
                    dueDateMillis = dueDateMillis,
                    dueTimeText = dueTimeText,
                    priority = priority,
                    isCompleted = isCompleted,
                    estimatedMinutes = estimatedMinutes,
                    createdAtMillis = createdAtMillis,
                    completedAtMillis = completedAtMillis,
                    assignedTo = assignedTo,
                    updatedAtMillis = updatedAtMillis
                )
                repository.insertHomework(hw)

                db?.collection("homeworks")?.document(id.toString())?.set(
                    mapOf(
                        "id" to id,
                        "courseId" to courseId,
                        "title" to title,
                        "description" to description,
                        "dueDateMillis" to dueDateMillis,
                        "dueTimeText" to dueTimeText,
                        "priority" to priority,
                        "isCompleted" to isCompleted,
                        "estimatedMinutes" to estimatedMinutes,
                        "createdAtMillis" to createdAtMillis,
                        "completedAtMillis" to (completedAtMillis ?: 0L),
                        "assignedTo" to assignedTo,
                        "updatedAtMillis" to updatedAtMillis
                    )
                )?.await()
                importedHws++
            }
            return@withContext "JSON dizisi başarıyla içe aktarıldı! ($importedHws ödev yüklendi)"
        }

        val root = org.json.JSONObject(cleanJson)

        // 1. Courses
        if (root.has("courses")) {
            val coursesArray = root.getJSONArray("courses")
            for (i in 0 until coursesArray.length()) {
                val obj = coursesArray.getJSONObject(i)
                val id = obj.optLong("id", System.currentTimeMillis() + i)
                val name = obj.optString("name", "Ders $id")
                val teacher = obj.optString("teacher", "")
                val colorHex = obj.optString("colorHex", "#3B82F6")
                val iconName = obj.optString("iconName", "MenuBook")

                val course = Course(id = id, name = name, teacher = teacher, colorHex = colorHex, iconName = iconName)
                repository.insertCourse(course)

                db?.collection("courses")?.document(id.toString())?.set(
                    mapOf(
                        "id" to id,
                        "name" to name,
                        "teacher" to teacher,
                        "colorHex" to colorHex,
                        "iconName" to iconName,
                        "updatedAtMillis" to System.currentTimeMillis()
                    )
                )?.await()
                importedCourses++
            }
        }

        // 2. Folders
        if (root.has("note_folders")) {
            val foldersArray = root.getJSONArray("note_folders")
            for (i in 0 until foldersArray.length()) {
                val obj = foldersArray.getJSONObject(i)
                val id = obj.optLong("id", System.currentTimeMillis() + i)
                val name = obj.optString("name", "Klasör")
                val colorHex = obj.optString("colorHex", "#1E88E5")
                val iconName = obj.optString("iconName", "Folder")

                val folder = NoteFolder(id = id, name = name, colorHex = colorHex, iconName = iconName)
                notesRepository?.insertFolder(folder)

                db?.collection("note_folders")?.document(id.toString())?.set(
                    mapOf("id" to id, "name" to name, "colorHex" to colorHex, "iconName" to iconName)
                )?.await()
                importedFolders++
            }
        }

        // 3. Homeworks
        if (root.has("homeworks")) {
            val hwsArray = root.getJSONArray("homeworks")
            for (i in 0 until hwsArray.length()) {
                val obj = hwsArray.getJSONObject(i)
                val id = obj.optLong("id", System.currentTimeMillis() + i)
                val courseId = obj.optLong("courseId", 1L)
                val title = obj.optString("title", "Ödev")
                val description = obj.optString("description", "")
                val dueDateMillis = obj.optLong("dueDateMillis", System.currentTimeMillis())
                val dueTimeText = obj.optString("dueTimeText", "23:59")
                val priority = obj.optString("priority", "MEDIUM")
                val isCompleted = obj.optBoolean("isCompleted", false)
                val estimatedMinutes = obj.optInt("estimatedMinutes", 30)
                val assignedTo = obj.optString("assignedTo", "Tuna")
                val createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                val completedAtMillis = if (obj.has("completedAtMillis") && !obj.isNull("completedAtMillis")) obj.optLong("completedAtMillis").takeIf { it > 0 } else null
                val updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())

                val hw = Homework(
                    id = id,
                    courseId = courseId,
                    title = title,
                    description = description,
                    dueDateMillis = dueDateMillis,
                    dueTimeText = dueTimeText,
                    priority = priority,
                    isCompleted = isCompleted,
                    estimatedMinutes = estimatedMinutes,
                    createdAtMillis = createdAtMillis,
                    completedAtMillis = completedAtMillis,
                    assignedTo = assignedTo,
                    updatedAtMillis = updatedAtMillis
                )
                repository.insertHomework(hw)

                db?.collection("homeworks")?.document(id.toString())?.set(
                    mapOf(
                        "id" to id,
                        "courseId" to courseId,
                        "title" to title,
                        "description" to description,
                        "dueDateMillis" to dueDateMillis,
                        "dueTimeText" to dueTimeText,
                        "priority" to priority,
                        "isCompleted" to isCompleted,
                        "estimatedMinutes" to estimatedMinutes,
                        "createdAtMillis" to createdAtMillis,
                        "completedAtMillis" to (completedAtMillis ?: 0L),
                        "assignedTo" to assignedTo,
                        "updatedAtMillis" to updatedAtMillis
                    )
                )?.await()
                importedHws++
            }
        }

        // 4. Notes
        if (root.has("notes")) {
            val notesArray = root.getJSONArray("notes")
            for (i in 0 until notesArray.length()) {
                val obj = notesArray.getJSONObject(i)
                val id = obj.optLong("id", System.currentTimeMillis() + i)
                val title = obj.optString("title", "Not")
                val content = obj.optString("content", "")
                val folderId = if (obj.has("folderId") && !obj.isNull("folderId")) obj.optLong("folderId").takeIf { it > 0 } else null
                val colorHex = obj.optString("colorHex", "#FFF9C4")
                val isPinned = obj.optBoolean("isPinned", false)
                val isLocked = obj.optBoolean("isLocked", false)
                val noteTypeName = obj.optString("noteType", "TEXT")
                val nType = try { NoteType.valueOf(noteTypeName) } catch (_: Exception) { NoteType.TEXT }
                val assignedTo = obj.optString("assignedTo", "Tuna")
                val createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                val updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())

                val drawingData = obj.optString("drawingData", "").takeIf { it.isNotBlank() }
                val reportData = obj.optString("reportData", "").takeIf { it.isNotBlank() }
                val imageUri = obj.optString("imageUri", "").takeIf { it.isNotBlank() }

                val note = Note(
                    id = id,
                    title = title,
                    content = content,
                    folderId = folderId,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    isLocked = isLocked,
                    type = nType,
                    createdAtMillis = createdAtMillis,
                    updatedAtMillis = updatedAtMillis,
                    assignedTo = assignedTo,
                    drawingData = drawingData,
                    reportData = reportData,
                    imageUri = imageUri
                )
                notesRepository?.insertNote(note)

                db?.collection("notes")?.document(id.toString())?.set(
                    mapOf(
                        "id" to id,
                        "title" to title,
                        "content" to content,
                        "folderId" to (folderId ?: 0L),
                        "colorHex" to colorHex,
                        "isPinned" to isPinned,
                        "isLocked" to isLocked,
                        "noteType" to nType.name,
                        "assignedTo" to assignedTo,
                        "createdAtMillis" to createdAtMillis,
                        "updatedAtMillis" to updatedAtMillis,
                        "drawingData" to (drawingData ?: ""),
                        "reportData" to (reportData ?: ""),
                        "imageUri" to (imageUri ?: "")
                    )
                )?.await()
                importedNotes++
            }
        }

        "JSON başarıyla içe aktarıldı! ($importedCourses ders, $importedHws ödev, $importedFolders klasör, $importedNotes not)"
    }
}
