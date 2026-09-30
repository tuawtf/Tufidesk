package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("odev_takip_auth", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(loadSavedUser())
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private fun loadSavedUser(): UserAccount? {
        val profileId = prefs.getString("selected_profile_id", null)
        if (profileId != null) {
            val matching = UserAccount.getById(profileId)
            if (matching != null) return matching
            // If previously saved profile was 'yonetim' or invalid, default to Tuna
            return UserAccount.PROFILES.first()
        }

        val name = prefs.getString("user_name", null) ?: return null
        val matching = UserAccount.PROFILES.find { it.name.equals(name, ignoreCase = true) }
        return matching ?: UserAccount.PROFILES.first()
    }

    fun selectProfile(profile: UserAccount) {
        prefs.edit()
            .putString("selected_profile_id", profile.id)
            .putString("user_role", profile.role.name)
            .putString("user_name", profile.name)
            .putString("user_details", profile.details)
            .apply()

        _currentUser.value = profile
    }

    fun switchProfile() {
        prefs.edit().remove("selected_profile_id").apply()
        _currentUser.value = null
    }

    fun loginAsStudent(name: String, studentNo: String = "") {
        val matching = UserAccount.PROFILES.find { it.name.equals(name, ignoreCase = true) }
        if (matching != null) {
            selectProfile(matching)
        } else {
            selectProfile(UserAccount.PROFILES.first())
        }
    }

    fun loginAsTeacher(name: String, passCode: String = ""): Boolean {
        selectProfile(UserAccount.PROFILES.first())
        return true
    }

    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }
}
