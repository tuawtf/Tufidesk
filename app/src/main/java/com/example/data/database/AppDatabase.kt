package com.example.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CourseDao
import com.example.data.dao.HomeworkDao
import com.example.data.dao.NoteDao
import com.example.data.dao.HomeworkVerificationDao
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.HomeworkVerification
import com.example.data.model.Note
import com.example.data.model.NoteFolder

@Database(
    entities = [Course::class, Homework::class, Note::class, NoteFolder::class, HomeworkVerification::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun noteDao(): NoteDao
    abstract fun homeworkVerificationDao(): HomeworkVerificationDao

    companion object {
        private const val TAG = "AppDatabase"
        private const val DB_NAME = "odev_takip_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.i(TAG, "Migrating database from version 8 to 9 (adding homework_verifications)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `homework_verifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `homeworkId` INTEGER NOT NULL, `page` INTEGER NOT NULL, `hash` INTEGER NOT NULL, `status` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_homework_verifications_homeworkId` ON `homework_verifications` (`homeworkId`)")
            }
        }

        // Explicit migrations from all older versions (1 through 7) to version 9
        val OLD_MIGRATIONS = (1..7).map { fromVersion ->
            object : Migration(fromVersion, 9) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    Log.i(TAG, "Migrating database from version $fromVersion to 9")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `courses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `teacher` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `iconName` TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `homeworks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `courseId` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `dueDateMillis` INTEGER NOT NULL, `dueTimeText` TEXT NOT NULL, `priority` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `estimatedMinutes` INTEGER NOT NULL, `createdAtMillis` INTEGER NOT NULL, `completedAtMillis` INTEGER, `assignedTo` TEXT NOT NULL, `updatedAtMillis` INTEGER NOT NULL)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_homeworks_courseId` ON `homeworks` (`courseId`)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `note_folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `iconName` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `folderId` INTEGER, `colorHex` TEXT NOT NULL, `imageUri` TEXT, `drawingData` TEXT, `reportData` TEXT, `isPinned` INTEGER NOT NULL, `isLocked` INTEGER NOT NULL, `assignedTo` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL, `updatedAtMillis` INTEGER NOT NULL, `type` TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `homework_verifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `homeworkId` INTEGER NOT NULL, `page` INTEGER NOT NULL, `hash` INTEGER NOT NULL, `status` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_homework_verifications_homeworkId` ON `homework_verifications` (`homeworkId`)")
                }
            }
        }

        val ALL_MIGRATIONS = (OLD_MIGRATIONS + listOf(MIGRATION_8_9)).toTypedArray()

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val db = buildDatabase(context)
                    INSTANCE = db
                    db
                } catch (e: Exception) {
                    Log.e(TAG, "Database initialization failed, resetting database: ${e.message}", e)
                    try {
                        context.applicationContext.deleteDatabase(DB_NAME)
                    } catch (_: Exception) {}
                    val db = buildDatabase(context)
                    INSTANCE = db
                    db
                }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .addMigrations(*ALL_MIGRATIONS)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
        }
    }
}
