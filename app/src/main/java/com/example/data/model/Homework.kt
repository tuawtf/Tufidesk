package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "homeworks",
    indices = [Index(value = ["courseId"])]
)
data class Homework(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long,
    val dueTimeText: String = "23:59",
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val isCompleted: Boolean = false,
    val estimatedMinutes: Int = 45,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null,
    val assignedTo: String = "Tuna", // "Tuna", "Elif", "Herkes"
    val updatedAtMillis: Long = System.currentTimeMillis()
)
