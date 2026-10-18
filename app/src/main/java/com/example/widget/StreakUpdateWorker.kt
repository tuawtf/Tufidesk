package com.example.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.auth.AuthManager
import com.example.data.database.AppDatabase
import com.example.data.model.StreakCalculator
import java.util.Calendar

class StreakUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val homeworks = db.homeworkDao().getAllHomeworksWithCourseList()

            val authManager = AuthManager(applicationContext)
            val user = authManager.currentUser.value

            val streakState = StreakCalculator.calculate(homeworks, user)

            val activeProfile = user?.name?.trim() ?: "Tuna"
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayEnd = todayStart + 86_400_000L

            var completedToday = 0
            var pendingToday = 0

            for (item in homeworks) {
                val hw = item.homework
                val isAssigned = hw.assignedTo.equals(activeProfile, ignoreCase = true) ||
                        hw.assignedTo.equals("Herkes", ignoreCase = true) ||
                        hw.assignedTo.isBlank()
                if (!isAssigned) continue

                if (hw.isCompleted) {
                    val completedTime = hw.completedAtMillis ?: hw.updatedAtMillis
                    if (completedTime in todayStart until todayEnd) {
                        completedToday++
                    }
                } else {
                    if (hw.dueDateMillis in todayStart until todayEnd || hw.dueDateMillis < todayStart || hw.dueDateMillis == 0L) {
                        pendingToday++
                    }
                }
            }

            StreakWidgetCache.save(
                applicationContext,
                streakState,
                completedToday,
                pendingToday
            )

            StreakWidget().updateAll(applicationContext)
            Log.d("StreakUpdateWorker", "Widget successfully updated at midnight or background run")
            Result.success()
        } catch (e: Exception) {
            Log.e("StreakUpdateWorker", "Failed to update streak widget: ${e.message}", e)
            Result.retry()
        }
    }
}
