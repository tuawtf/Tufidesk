package com.example.data.repository

import com.example.data.dao.CourseDao
import com.example.data.dao.HomeworkDao
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.HomeworkWithCourse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class HomeworkRepository(
    private val courseDao: CourseDao,
    private val homeworkDao: HomeworkDao
) {
    val allHomeworks: Flow<List<HomeworkWithCourse>> = homeworkDao.getAllHomeworksWithCourse()
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses().map { list ->
        list.distinctBy { it.name.trim().lowercase() }
    }

    suspend fun insertCourse(course: Course): Long = withContext(Dispatchers.IO) {
        val existing = courseDao.getAllCoursesList().find { it.name.trim().equals(course.name.trim(), ignoreCase = true) }
        if (existing != null) {
            existing.id
        } else {
            courseDao.insertCourse(course)
        }
    }

    suspend fun updateCourse(course: Course) = withContext(Dispatchers.IO) {
        courseDao.updateCourse(course)
    }

    suspend fun deleteCourse(course: Course) = withContext(Dispatchers.IO) {
        homeworkDao.deleteHomeworksByCourse(course.id)
        courseDao.deleteCourse(course)
    }

    suspend fun insertHomework(homework: Homework): Long = withContext(Dispatchers.IO) {
        homeworkDao.insertHomework(homework)
    }

    suspend fun updateHomework(homework: Homework) = withContext(Dispatchers.IO) {
        homeworkDao.updateHomework(homework)
    }

    suspend fun deleteHomework(homework: Homework) = withContext(Dispatchers.IO) {
        homeworkDao.deleteHomework(homework)
    }

    suspend fun toggleComplete(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val completedAt = if (isCompleted) updatedAt else null
        homeworkDao.updateCompletionStatus(id, isCompleted, completedAt, updatedAt)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        homeworkDao.deleteAllHomeworks()
        courseDao.deleteAllCourses()
    }

    suspend fun resetToDemoData() = withContext(Dispatchers.IO) {
        clearAllData()
        seedDemoWeeklyData()
    }

    suspend fun cleanupDuplicates() = withContext(Dispatchers.IO) {
        try {
            // Deduplicate courses
            val allCoursesList = courseDao.getAllCoursesList()
            val groupedByName = allCoursesList.groupBy { it.name.trim().lowercase() }
            for ((_, courses) in groupedByName) {
                if (courses.size > 1) {
                    val primary = courses.first()
                    val duplicates = courses.drop(1)
                    for (dup in duplicates) {
                        homeworkDao.remapCourseId(oldCourseId = dup.id, newCourseId = primary.id)
                        courseDao.deleteCourse(dup)
                    }
                }
            }

            // Deduplicate homeworks and clean up legacy texts
            val allHws = homeworkDao.getAllHomeworksList()
            val groupedHws = allHws.groupBy { "${it.title.trim().lowercase()}_${it.dueDateMillis}_${it.assignedTo.trim().lowercase()}" }
            for ((_, hws) in groupedHws) {
                if (hws.size > 1) {
                    val duplicates = hws.drop(1)
                    for (dup in duplicates) {
                        homeworkDao.deleteHomework(dup)
                    }
                }
            }

            // Clean up any remaining legacy "pazar teslim" phrasing from descriptions
            for (hw in homeworkDao.getAllHomeworksList()) {
                if (hw.description.contains("pazar teslim", ignoreCase = true) || hw.description.contains("Pazar teslim", ignoreCase = true)) {
                    val sanitizedDesc = hw.description
                        .replace("pazar teslimi ödevi.", "ödevi.", ignoreCase = true)
                        .replace("pazar teslimi ödevi", "ödevi", ignoreCase = true)
                        .replace("pazar teslim raporu.", "raporu.", ignoreCase = true)
                        .replace("pazar teslim raporu", "raporu", ignoreCase = true)
                        .replace("Pazar teslimi", "Haftalık teslim", ignoreCase = true)
                    homeworkDao.updateHomework(hw.copy(description = sanitizedDesc))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeworkRepo", "Error cleaning up duplicates: ${e.message}", e)
        }
    }

    suspend fun seedInitialDataIfNeeded(force: Boolean = false) = withContext(Dispatchers.IO) {
        cleanupDuplicates()

        val courseCount = courseDao.getCourseCount()
        val homeworkCount = homeworkDao.getHomeworkCount()
        if (force || (courseCount == 0 && homeworkCount == 0)) {
            seedDemoWeeklyData()
        }
    }

    private suspend fun seedDemoWeeklyData() {
        val existing = courseDao.getAllCoursesList()
        val mathId = existing.find { it.name.equals("Matematik", ignoreCase = true) }?.id
            ?: courseDao.insertCourse(Course(name = "Matematik", teacher = "", colorHex = "#3B82F6", iconName = "Calculate"))
        val turkishId = existing.find { it.name.equals("Türkçe", ignoreCase = true) }?.id
            ?: courseDao.insertCourse(Course(name = "Türkçe", teacher = "", colorHex = "#EF4444", iconName = "MenuBook"))
        val scienceId = existing.find { it.name.equals("Fen Bilimleri", ignoreCase = true) }?.id
            ?: courseDao.insertCourse(Course(name = "Fen Bilimleri", teacher = "", colorHex = "#10B981", iconName = "Science"))
        val englishId = existing.find { it.name.equals("İngilizce", ignoreCase = true) }?.id
            ?: courseDao.insertCourse(Course(name = "İngilizce", teacher = "", colorHex = "#8B5CF6", iconName = "Translate"))
        val socialId = existing.find { it.name.equals("Sosyal Bilgiler", ignoreCase = true) }?.id
            ?: courseDao.insertCourse(Course(name = "Sosyal Bilgiler", teacher = "", colorHex = "#F59E0B", iconName = "Public"))

        // Pazar günlerini hesapla (Haftalık Pazar teslimleri)
        val now = Calendar.getInstance()
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val daysSinceSunday = if (currentDayOfWeek == Calendar.SUNDAY) 0 else (currentDayOfWeek - Calendar.SUNDAY)
        
        // Geçen Pazar (1 hafta önce)
        val lastSundayCal = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -daysSinceSunday)
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val week2Sunday = lastSundayCal.timeInMillis

        // 2 hafta önceki Pazar (1. Hafta)
        val twoSundaysAgoCal = (lastSundayCal.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        val week1Sunday = twoSundaysAgoCal.timeInMillis

        // Bu haftaki gelecek Pazar (Gelecek teslimat)
        val upcomingSundayCal = (now.clone() as Calendar).apply {
            val daysUntilSunday = if (currentDayOfWeek == Calendar.SUNDAY) 0 else ((Calendar.SUNDAY + 7 - currentDayOfWeek) % 7)
            add(Calendar.DAY_OF_MONTH, if (daysUntilSunday == 0) 7 else daysUntilSunday)
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val thisWeekSunday = upcomingSundayCal.timeInMillis

        // 1. Hafta: %80 (5 ödevden 4'ü tamamlandı)
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "1. Hafta Matematik: Çarpanlar ve Katlar",
                description = "1. hafta ders çalışma ödevi.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = true,
                completedAtMillis = week1Sunday - 7200000L,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "1. Hafta Türkçe: Cümlede Anlam ve Paragraf",
                description = "1. hafta ders çalışma ödevi.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = true,
                completedAtMillis = week1Sunday - 5400000L,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = scienceId,
                title = "1. Hafta Fen Bilimleri: Mevsimler ve İklim",
                description = "1. hafta ders çalışma ödevi.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = true,
                completedAtMillis = week1Sunday - 3600000L,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = englishId,
                title = "1. Hafta İngilizce: Friendship Vocabulary",
                description = "1. hafta ders çalışma ödevi.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "LOW",
                isCompleted = true,
                completedAtMillis = week1Sunday - 1800000L,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = socialId,
                title = "1. Hafta Sosyal Bilgiler: Bir Kahraman Doğuyor",
                description = "1. hafta ders çalışma ödevi.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = false,
                assignedTo = "Herkes"
            )
        )

        // 2. Hafta: %50 (4 ödevden 2'si tamamlandı)
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "2. Hafta Matematik: Üslü İfadeler Soru Bankası",
                description = "2. hafta ders çalışma ödevi.",
                dueDateMillis = week2Sunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = true,
                completedAtMillis = week2Sunday - 7200000L,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "2. Hafta Türkçe: Fiilimsiler Çalışma Föyü",
                description = "2. hafta ders çalışma ödevi.",
                dueDateMillis = week2Sunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = true,
                completedAtMillis = week2Sunday - 3600000L,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = scienceId,
                title = "2. Hafta Fen Bilimleri: DNA ve Genetik Kod",
                description = "2. hafta ders çalışma ödevi.",
                dueDateMillis = week2Sunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = false,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = englishId,
                title = "2. Hafta İngilizce: Teen Life Quiz Hazırlığı",
                description = "2. hafta ders çalışma ödevi.",
                dueDateMillis = week2Sunday,
                dueTimeText = "21:00",
                priority = "LOW",
                isCompleted = false,
                assignedTo = "Herkes"
            )
        )

        // Bu Hafta (Aktif ödevler)
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "3. Hafta Matematik: Kareköklü İfadeler Sayfa 45-52",
                description = "Hafta sonuna kadar çözülecek.",
                dueDateMillis = thisWeekSunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = false,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = scienceId,
                title = "3. Hafta Fen Bilimleri: Katı ve Sıvı Basıncı Deneyi",
                description = "Deney raporu hazırlanacak.",
                dueDateMillis = thisWeekSunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = false,
                assignedTo = "Herkes"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "3. Hafta Türkçe: Noktalama İşaretleri ve Yazım Kuralları",
                description = "Hafta sonuna kadar tamamlanacak.",
                dueDateMillis = thisWeekSunday,
                dueTimeText = "21:00",
                priority = "LOW",
                isCompleted = true,
                completedAtMillis = System.currentTimeMillis(),
                assignedTo = "Herkes"
            )
        )
    }
}
