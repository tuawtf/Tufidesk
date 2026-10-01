package com.example.data.repository

import com.example.data.dao.NoteDao
import com.example.data.model.Note
import com.example.data.model.NoteFolder
import com.example.data.model.NoteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NotesRepository(
    private val noteDao: NoteDao
) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    val allFolders: Flow<List<NoteFolder>> = noteDao.getAllFolders()

    suspend fun insertNote(note: Note): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun togglePin(id: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        noteDao.togglePin(id, isPinned)
    }

    suspend fun insertFolder(folder: NoteFolder): Long = withContext(Dispatchers.IO) {
        noteDao.insertFolder(folder)
    }

    suspend fun deleteFolder(folder: NoteFolder) = withContext(Dispatchers.IO) {
        noteDao.deleteNotesByFolder(folder.id)
        noteDao.deleteFolder(folder)
    }

    suspend fun seedInitialNotesIfNeeded() = withContext(Dispatchers.IO) {
        if (noteDao.getNotesCount() == 0) {
            val mathFolderId = noteDao.insertFolder(
                NoteFolder(name = "Matematik", colorHex = "#1E88E5", iconName = "Calculate")
            )
            val scienceFolderId = noteDao.insertFolder(
                NoteFolder(name = "Fen Bilimleri", colorHex = "#43A047", iconName = "Science")
            )
            val reportFolderId = noteDao.insertFolder(
                NoteFolder(name = "Deneme Sınavları", colorHex = "#E53935", iconName = "Assessment")
            )
            val turkishFolderId = noteDao.insertFolder(
                NoteFolder(name = "Türkçe", colorHex = "#8E24AA", iconName = "MenuBook")
            )

            // Sample Google Keep style notes
            noteDao.insertNote(
                Note(
                    title = "Matematik Formülleri 📝",
                    content = "1. Çarpanlar ve Katlar: EBOB(a,b) x EKOK(a,b) = a x b\n2. Üslü Sayılar: a^m * a^n = a^(m+n)\n3. Kareköklü İfadeler: √a * √b = √(a*b)",
                    folderId = mathFolderId,
                    colorHex = "#FFF9C4", // Soft Yellow
                    isPinned = true,
                    assignedTo = "Tuna",
                    type = NoteType.TEXT
                )
            )

            noteDao.insertNote(
                Note(
                    title = "Fen: Katı ve Sıvı Basıncı Deney Notu 🧪",
                    content = "P = F / S (Katı Basıncı: Yüzey alanı küçüldükçe basınç artar! Bıçak, çivi örneği).\nSıvı Basıncı: P = h * d * g (Derinlik ve yoğunlukla doğru orantılı).",
                    folderId = scienceFolderId,
                    colorHex = "#E1F5FE", // Soft Blue
                    isPinned = false,
                    assignedTo = "Tuna",
                    type = NoteType.TEXT
                )
            )

            // Initial Report / TYT Deneme Neti
            noteDao.insertNote(
                Note(
                    title = "TYT 1. Genel Deneme Sınavı 🎯",
                    content = "📊 Sınav: TYT 1. Genel Deneme\nTürkçe (40): 33 D, 4 Y • 32.0 Net\nTemel Matematik (40): 29 D, 3 Y • 28.25 Net\nFen Bilimleri (20): 16 D, 2 Y • 15.5 Net\nSosyal Bilimler (20): 17 D, 2 Y • 16.5 Net\n\n🎯 TOPLAM NET: 92.25 / 120 (%77 Başarı)",
                    folderId = reportFolderId,
                    colorHex = "#1E293B",
                    isPinned = true,
                    assignedTo = "Tuna",
                    type = NoteType.REPORT,
                    reportData = "{\"examName\":\"TYT 1. Genel Deneme\",\"turkce\":{\"d\":33,\"y\":4},\"matematik\":{\"d\":29,\"y\":3},\"fen\":{\"d\":16,\"y\":2},\"sosyal\":{\"d\":17,\"y\":2},\"totalNet\":92.25}"
                )
            )

            noteDao.insertNote(
                Note(
                    title = "Türkçe Fiilimsiler Özeti",
                    content = "- İsim-fiil: -ma, -ış, -mak (Mayışmak)\n- Sıfat-fiil: -an, -ası, -mez, -ar, -dik, -ecek, -miş (Anası mezar dikecekmiş)\n- Zarf-fiil: -ken, -alı, -esiye, -meden, -ince, -ip, -erek...",
                    folderId = turkishFolderId,
                    colorHex = "#F3E5F5", // Soft Purple
                    isPinned = false,
                    assignedTo = "Elif",
                    type = NoteType.TEXT
                )
            )
        }
    }
}
