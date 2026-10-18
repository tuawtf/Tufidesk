package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "homework_verifications",
    indices = [Index(value = ["homeworkId"])]
)
data class HomeworkVerification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val homeworkId: Long,
    val page: Int,
    val hash: Long,
    val status: String, // "APPROVED", "FORCE_COMPLETED"
    val createdAtMillis: Long = System.currentTimeMillis()
)
