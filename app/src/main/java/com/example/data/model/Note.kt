package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteType {
    TEXT,
    DRAWING,
    IMAGE,
    REPORT
}

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val colorHex: String = "#FFFFFF",
    val imageUri: String? = null,
    val drawingData: String? = null,
    val reportData: String? = null,
    val isPinned: Boolean = false,
    val assignedTo: String = "Tuna",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val type: NoteType = NoteType.TEXT
)
