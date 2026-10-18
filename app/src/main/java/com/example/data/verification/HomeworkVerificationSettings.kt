package com.example.data.verification

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HomeworkVerificationSettings {
    private const val PREFS_NAME = "homework_verification_prefs"

    private const val KEY_PHOTO_VERIFICATION_ENABLED = "pref_photo_verification_enabled"
    private const val KEY_VERIFICATION_MODEL = "pref_verification_model"
    private const val KEY_ALLOW_GALLERY = "pref_allow_gallery"
    private const val KEY_REQUIRE_HANDWRITING = "pref_require_handwriting"
    private const val KEY_ALLOW_FORCE_COMPLETE = "pref_allow_force_complete"
    private const val KEY_MONTHLY_COUNT_PREFIX = "monthly_photos_"

    const val DEFAULT_MODEL = "deepseek/deepseek-v4-flash-vision-exp"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getCurrentMonthKey(): String {
        val sdf = SimpleDateFormat("yyyy_MM", Locale.US)
        return KEY_MONTHLY_COUNT_PREFIX + sdf.format(Date())
    }

    fun isPhotoVerificationEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PHOTO_VERIFICATION_ENABLED, true)
    }

    fun setPhotoVerificationEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PHOTO_VERIFICATION_ENABLED, enabled).apply()
    }

    fun getVerificationModel(context: Context): String {
        val saved = getPrefs(context).getString(KEY_VERIFICATION_MODEL, DEFAULT_MODEL)?.trim()
        return if (!saved.isNullOrBlank()) saved else DEFAULT_MODEL
    }

    fun setVerificationModel(context: Context, model: String) {
        val cleaned = model.trim().ifBlank { DEFAULT_MODEL }
        getPrefs(context).edit().putString(KEY_VERIFICATION_MODEL, cleaned).apply()
    }

    fun isGalleryAllowed(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ALLOW_GALLERY, false)
    }

    fun setGalleryAllowed(context: Context, allowed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ALLOW_GALLERY, allowed).apply()
    }

    fun isHandwritingRequired(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REQUIRE_HANDWRITING, true)
    }

    fun setHandwritingRequired(context: Context, required: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_REQUIRE_HANDWRITING, required).apply()
    }

    fun isForceCompleteAllowed(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ALLOW_FORCE_COMPLETE, true)
    }

    fun setForceCompleteAllowed(context: Context, allowed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ALLOW_FORCE_COMPLETE, allowed).apply()
    }

    fun getMonthlyCheckedPhotoCount(context: Context): Int {
        return getPrefs(context).getInt(getCurrentMonthKey(), 0)
    }

    fun incrementMonthlyCheckedPhotoCount(context: Context) {
        val key = getCurrentMonthKey()
        val current = getPrefs(context).getInt(key, 0)
        getPrefs(context).edit().putInt(key, current + 1).apply()
    }
}
