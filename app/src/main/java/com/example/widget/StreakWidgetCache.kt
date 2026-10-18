package com.example.widget

import android.content.Context
import com.example.data.model.StreakDay
import com.example.data.model.StreakState
import org.json.JSONArray
import org.json.JSONObject

data class StreakWidgetData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayDone: Boolean = false,
    val atRisk: Boolean = false,
    val completedTodayCount: Int = 0,
    val pendingTodayCount: Int = 0,
    val days: List<StreakDay> = emptyList()
)

object StreakWidgetCache {
    private const val PREFS_NAME = "streak_widget_cache_prefs"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_LONGEST_STREAK = "longest_streak"
    private const val KEY_TODAY_DONE = "today_done"
    private const val KEY_AT_RISK = "at_risk"
    private const val KEY_COMPLETED_TODAY_COUNT = "completed_today_count"
    private const val KEY_PENDING_TODAY_COUNT = "pending_today_count"
    private const val KEY_DAYS_JSON = "days_json"

    fun save(
        context: Context,
        state: StreakState,
        completedTodayCount: Int,
        pendingTodayCount: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (day in state.last7Days) {
            val obj = JSONObject().apply {
                put("dayLabel", day.dayLabel)
                put("dayNumber", day.dayNumber)
                put("dateMillis", day.dateMillis)
                put("isCompleted", day.isCompleted)
                put("isToday", day.isToday)
                put("isFuture", day.isFuture)
            }
            jsonArray.put(obj)
        }

        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, state.current)
            .putInt(KEY_LONGEST_STREAK, state.longest)
            .putBoolean(KEY_TODAY_DONE, state.todayDone)
            .putBoolean(KEY_AT_RISK, state.atRisk)
            .putInt(KEY_COMPLETED_TODAY_COUNT, completedTodayCount)
            .putInt(KEY_PENDING_TODAY_COUNT, pendingTodayCount)
            .putString(KEY_DAYS_JSON, jsonArray.toString())
            .apply()
    }

    fun get(context: Context): StreakWidgetData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)
        val todayDone = prefs.getBoolean(KEY_TODAY_DONE, false)
        val atRisk = prefs.getBoolean(KEY_AT_RISK, false)
        val completedTodayCount = prefs.getInt(KEY_COMPLETED_TODAY_COUNT, 0)
        val pendingTodayCount = prefs.getInt(KEY_PENDING_TODAY_COUNT, 0)
        val daysJsonStr = prefs.getString(KEY_DAYS_JSON, null)

        val daysList = mutableListOf<StreakDay>()
        if (!daysJsonStr.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(daysJsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    daysList.add(
                        StreakDay(
                            dayLabel = obj.optString("dayLabel", ""),
                            dayNumber = obj.optInt("dayNumber", 0),
                            dateMillis = obj.optLong("dateMillis", 0L),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            isToday = obj.optBoolean("isToday", false),
                            isFuture = obj.optBoolean("isFuture", false)
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        return StreakWidgetData(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            todayDone = todayDone,
            atRisk = atRisk,
            completedTodayCount = completedTodayCount,
            pendingTodayCount = pendingTodayCount,
            days = daysList
        )
    }
}
