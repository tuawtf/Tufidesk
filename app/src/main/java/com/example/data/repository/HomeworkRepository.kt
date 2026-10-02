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

    suspend fun getAllHomeworksList(): List<Homework> = withContext(Dispatchers.IO) {
        homeworkDao.getAllHomeworksList()
    }

    suspend fun getAllCoursesList(): List<Course> = withContext(Dispatchers.IO) {
        courseDao.getAllCoursesList()
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

            // Clean up any remaining legacy "pazar teslim" phrasing and migrate "Herkes" to "Tuna"
            for (hw in homeworkDao.getAllHomeworksList()) {
                var updatedHw = hw
                if (hw.description.contains("pazar teslim", ignoreCase = true) || hw.description.contains("Pazar teslim", ignoreCase = true)) {
                    val sanitizedDesc = hw.description
                        .replace("pazar teslimi ödevi.", "ödevi.", ignoreCase = true)
                        .replace("pazar teslimi ödevi", "ödevi", ignoreCase = true)
                        .replace("pazar teslim raporu.", "raporu.", ignoreCase = true)
                        .replace("pazar teslim raporu", "raporu", ignoreCase = true)
                        .replace("Pazar teslimi", "Haftalık teslim", ignoreCase = true)
                    updatedHw = updatedHw.copy(description = sanitizedDesc)
                }
                if (updatedHw.assignedTo.equals("Herkes", ignoreCase = true)) {
                    updatedHw = updatedHw.copy(assignedTo = "Tuna")
                }
                if (updatedHw != hw) {
                    homeworkDao.updateHomework(updatedHw)
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

        val startOfToday = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayDue = startOfToday + 20 * 3600000L + 30 * 60000L // 20:30 today

        // ================= TUNA'NIN ÖDEVLERİ =================
        // Tuna Bugünün Ödevleri
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "Matematik: Kareköklü İfadeler Soru Çözümü",
                description = "Sayfa 55-60 arası test 4 ve test 5 bitirilecek.",
                dueDateMillis = todayDue,
                dueTimeText = "20:30",
                priority = "HIGH",
                estimatedMinutes = 45,
                isCompleted = false,
                assignedTo = "Tuna"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = scienceId,
                title = "Fen Bilimleri: Basınç Ünitesi Çalışma Notları",
                description = "Katı ve sıvı basıncı formülleri tekrar edilecek.",
                dueDateMillis = todayDue,
                dueTimeText = "21:30",
                priority = "MEDIUM",
                estimatedMinutes = 30,
                isCompleted = false,
                assignedTo = "Tuna"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "Türkçe: Paragrafta Ana Düşünce Alıştırması",
                description = "20 soru çözüldü ve kontrol edildi.",
                dueDateMillis = todayDue,
                dueTimeText = "19:00",
                priority = "LOW",
                estimatedMinutes = 25,
                isCompleted = true,
                completedAtMillis = System.currentTimeMillis() - 3600000L,
                assignedTo = "Tuna"
            )
        )

        // Tuna 1. Hafta: %80 (5 ödevden 4'ü tamamlandı)
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
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
            )
        )

        // Tuna 2. Hafta: %50 (4 ödevden 2'si tamamlandı)
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
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
            )
        )

        // Tuna 3. Hafta (Bu Hafta)
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "3. Hafta Matematik: Kareköklü İfadeler Sayfa 45-52",
                description = "Hafta sonuna kadar çözülecek.",
                dueDateMillis = thisWeekSunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = false,
                assignedTo = "Tuna"
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
                assignedTo = "Tuna"
            )
        )

        // ================= ELİF'İN ÖDEVLERİ =================
        // Elif Bugünün Ödevleri
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "Türkçe: Noktalama İşaretleri ve Yazım Kuralları",
                description = "Defterdeki 3. bölüm yazım kuralları tamamlanacak.",
                dueDateMillis = todayDue,
                dueTimeText = "20:00",
                priority = "HIGH",
                estimatedMinutes = 35,
                isCompleted = false,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = socialId,
                title = "Sosyal Bilgiler: Kültür ve Miras Özeti",
                description = "Konu özeti çıkarılıp soruları cevaplanacak.",
                dueDateMillis = todayDue,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                estimatedMinutes = 30,
                isCompleted = false,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "Matematik: Kesirler ve Sayı Doğrusu Çizimi",
                description = "Örnek problemler çözüldü.",
                dueDateMillis = todayDue,
                dueTimeText = "18:30",
                priority = "LOW",
                estimatedMinutes = 20,
                isCompleted = true,
                completedAtMillis = System.currentTimeMillis() - 7200000L,
                assignedTo = "Elif"
            )
        )

        // Elif 1. Hafta (%75 - 4 ödevden 3'ü tamamlandı)
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "1. Hafta Türkçe: Sözcükte Anlam ve Deyimler",
                description = "Deyimler ve atasözleri çalışma föyü.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = true,
                completedAtMillis = week1Sunday - 7200000L,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "1. Hafta Matematik: Doğal Sayılarla İşlemler",
                description = "Dört işlem alıştırmaları.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = true,
                completedAtMillis = week1Sunday - 5400000L,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = scienceId,
                title = "1. Hafta Fen: Güneş Sistemi ve Gezegenler",
                description = "Gezegen modelleri çizelgesi.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = true,
                completedAtMillis = week1Sunday - 3600000L,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = englishId,
                title = "1. Hafta İngilizce: Hello Unit Vocabulary",
                description = "Kelime ezber kartları.",
                dueDateMillis = week1Sunday,
                dueTimeText = "21:00",
                priority = "LOW",
                isCompleted = false,
                assignedTo = "Elif"
            )
        )

        // Elif 2. Hafta (%66 - 3 ödevden 2'si tamamlandı)
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "2. Hafta Türkçe: Metin Türleri ve Şiir İncelemesi",
                description = "Şiir tahlili çalışma kağıdı.",
                dueDateMillis = week2Sunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = true,
                completedAtMillis = week2Sunday - 7200000L,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = scienceId,
                title = "2. Hafta Fen Bilimleri: Ayın Evreleri Deneyi",
                description = "Gözlem günlüğü hazırlandı.",
                dueDateMillis = week2Sunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = true,
                completedAtMillis = week2Sunday - 3600000L,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = socialId,
                title = "2. Hafta Sosyal Bilgiler: Haklar ve Sorumluluklar",
                description = "Haklarımız konusundaki örnek olaylar.",
                dueDateMillis = week2Sunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = false,
                assignedTo = "Elif"
            )
        )

        // Elif 3. Hafta (Bu Hafta)
        homeworkDao.insertHomework(
            Homework(
                courseId = turkishId,
                title = "3. Hafta Türkçe: Hikaye Haritası ve Kompozisyon",
                description = "Serbest hikaye yazma ödevi.",
                dueDateMillis = thisWeekSunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = false,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = socialId,
                title = "3. Hafta Sosyal: Tarihe Yolculuk ve İlk Medeniyetler",
                description = "İlk yerleşim yerleri harita çalışması.",
                dueDateMillis = thisWeekSunday,
                dueTimeText = "21:00",
                priority = "MEDIUM",
                isCompleted = false,
                assignedTo = "Elif"
            )
        )
        homeworkDao.insertHomework(
            Homework(
                courseId = mathId,
                title = "3. Hafta Matematik: Ondalık Gösterim Testi",
                description = "Sayfa 32-35 arası testler.",
                dueDateMillis = thisWeekSunday,
                dueTimeText = "21:00",
                priority = "HIGH",
                isCompleted = true,
                completedAtMillis = System.currentTimeMillis(),
                assignedTo = "Elif"
            )
        )
    }
}
