package com.example.data.model

import java.util.Calendar

/**
 * Haftalık seri çubuğundaki her bir günü temsil eder.
 */
data class StreakDay(
    val dayLabel: String, // "P", "S", "Ç", "P", "C", "C", "P"
    val dayNumber: Int,
    val dateMillis: Long,
    val isCompleted: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

/**
 * Duolingo benzeri Seri (Streak) durumu.
 */
data class StreakState(
    val current: Int = 0,
    val longest: Int = 0,
    val todayDone: Boolean = false,
    val atRisk: Boolean = false,
    val last7Days: List<StreakDay> = emptyList()
)

/**
 * Mevcut Room veritabanı veya mimariyi bozmadan tamamlanan ödevler üzerinden
 * seri hesaplama mantığını yürüten nesne.
 */
object StreakCalculator {

    fun calculate(
        homeworks: List<HomeworkWithCourse>,
        user: UserAccount?,
        nowMillis: Long = System.currentTimeMillis()
    ): StreakState {
        val activeProfile = user?.name?.trim() ?: "Tuna"

        // Aktif profilin (assignedTo == aktif profil veya "Herkes") tamamlanmış ödevlerinin
        // completedAtMillis değerlerinden, cihazın yerel saat dilimine göre "en az bir ödev tamamlanan günler" kümesini çıkar
        val activeDaysMillis = mutableSetOf<Long>()

        for (item in homeworks) {
            val hw = item.homework
            if (!hw.isCompleted) continue

            val isAssigned = hw.assignedTo.equals(activeProfile, ignoreCase = true) ||
                    hw.assignedTo.equals("Herkes", ignoreCase = true) ||
                    hw.assignedTo.isBlank()

            if (!isAssigned) continue

            val completedTime = hw.completedAtMillis ?: hw.updatedAtMillis
            if (completedTime > 0) {
                activeDaysMillis.add(getStartOfDay(completedTime))
            }
        }

        val todayStart = getStartOfDay(nowMillis)
        val calYesterday = Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayStart = calYesterday.timeInMillis

        val todayDone = activeDaysMillis.contains(todayStart)
        val yesterdayDone = activeDaysMillis.contains(yesterdayStart)

        // Seri = bugünden (bugün tamamlanan varsa) ya da dünden geriye ardışık aktif günlerin sayısı.
        // Bugün ve dün yoksa seri 0.
        var currentStreak = 0
        if (todayDone) {
            currentStreak = 1
            val checkCal = Calendar.getInstance().apply {
                timeInMillis = todayStart
                add(Calendar.DAY_OF_YEAR, -1)
            }
            while (activeDaysMillis.contains(checkCal.timeInMillis)) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else if (yesterdayDone) {
            currentStreak = 1
            val checkCal = Calendar.getInstance().apply {
                timeInMillis = yesterdayStart
                add(Calendar.DAY_OF_YEAR, -1)
            }
            while (activeDaysMillis.contains(checkCal.timeInMillis)) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        val atRisk = currentStreak > 0 && !todayDone

        // En uzun seriyi hesapla
        val sortedDays = activeDaysMillis.sorted()
        var longestStreak = currentStreak
        if (sortedDays.isNotEmpty()) {
            var tempStreak = 1
            for (i in 1 until sortedDays.size) {
                val expectedNext = Calendar.getInstance().apply {
                    timeInMillis = sortedDays[i - 1]
                    add(Calendar.DAY_OF_YEAR, 1)
                }.timeInMillis

                if (sortedDays[i] == expectedNext) {
                    tempStreak++
                } else if (sortedDays[i] > expectedNext) {
                    tempStreak = 1
                }
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
        }
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        // Son 7 gün (Haftalık satır: Pazartesi - Pazar)
        val mondayStart = getMondayOfWeek(nowMillis)
        val dayLabels = listOf("P", "S", "Ç", "P", "C", "C", "P")
        val daysList = mutableListOf<StreakDay>()

        val loopCal = Calendar.getInstance().apply {
            timeInMillis = mondayStart
        }

        for (i in 0 until 7) {
            val dayStart = loopCal.timeInMillis
            val isToday = dayStart == todayStart
            val isFuture = dayStart > todayStart
            val isCompleted = activeDaysMillis.contains(dayStart)
            val dayNum = loopCal.get(Calendar.DAY_OF_MONTH)

            daysList.add(
                StreakDay(
                    dayLabel = dayLabels[i],
                    dayNumber = dayNum,
                    dateMillis = dayStart,
                    isCompleted = isCompleted,
                    isToday = isToday,
                    isFuture = isFuture
                )
            )
            loopCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return StreakState(
            current = currentStreak,
            longest = longestStreak,
            todayDone = todayDone,
            atRisk = atRisk,
            last7Days = daysList
        )
    }

    private fun getStartOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getMondayOfWeek(millis: Long): Long {
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
}
