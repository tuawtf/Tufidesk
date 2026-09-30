package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class UserRole(val title: String) {
    STUDENT("Öğrenci")
}

data class UserAccount(
    val id: String = "tuna",
    val role: UserRole = UserRole.STUDENT,
    val name: String = "Tuna",
    val details: String = "Öğrenci",
    val canManage: Boolean = true,
    val avatarColorHex: String = "#2563EB",
    val photoUrl: String = "https://i.imgur.com/M6BWWpu.jpeg",
    @DrawableRes val profileImageRes: Int = R.drawable.profile_tuna
) {
    val isTeacher: Boolean get() = false
    val isTuna: Boolean get() = id.equals("tuna", ignoreCase = true) || name.equals("Tuna", ignoreCase = true)
    val isElif: Boolean get() = id.equals("elif", ignoreCase = true) || name.equals("Elif", ignoreCase = true)
    val isYonetim: Boolean get() = false

    companion object {
        val PROFILES = listOf(
            UserAccount(
                id = "tuna",
                role = UserRole.STUDENT,
                name = "Tuna",
                details = "Öğrenci",
                canManage = true,
                avatarColorHex = "#2563EB",
                photoUrl = "https://i.imgur.com/M6BWWpu.jpeg",
                profileImageRes = R.drawable.profile_tuna
            ),
            UserAccount(
                id = "elif",
                role = UserRole.STUDENT,
                name = "Elif",
                details = "Öğrenci",
                canManage = true,
                avatarColorHex = "#7E22CE",
                photoUrl = "https://i.imgur.com/6ThMMmK.jpeg",
                profileImageRes = R.drawable.profile_elif
            )
        )

        fun getById(id: String?): UserAccount? {
            if (id == null) return null
            return PROFILES.find { 
                it.id.equals(id, ignoreCase = true) || 
                it.name.equals(id, ignoreCase = true)
            }
        }
    }
}

