package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Homework
import com.example.data.model.HomeworkWithCourse
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Transaction
    @Query("SELECT * FROM homeworks ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllHomeworksWithCourse(): Flow<List<HomeworkWithCourse>>

    @Transaction
    @Query("SELECT * FROM homeworks ORDER BY isCompleted ASC, dueDateMillis ASC")
    suspend fun getAllHomeworksWithCourseList(): List<HomeworkWithCourse>

    @Transaction
    @Query("SELECT * FROM homeworks WHERE id = :id LIMIT 1")
    suspend fun getHomeworkWithCourseById(id: Long): HomeworkWithCourse?

    @Transaction
    @Query("SELECT * FROM homeworks WHERE courseId = :courseId ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getHomeworksByCourse(courseId: Long): Flow<List<HomeworkWithCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: Homework): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworks(homeworks: List<Homework>): List<Long>

    @Update
    suspend fun updateHomework(homework: Homework)

    @Delete
    suspend fun deleteHomework(homework: Homework)

    @Query("UPDATE homeworks SET isCompleted = :isCompleted, completedAtMillis = :completedAt, updatedAtMillis = :updatedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, completedAt: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM homeworks")
    suspend fun getAllHomeworksList(): List<Homework>

    @Query("UPDATE homeworks SET courseId = :newCourseId WHERE courseId = :oldCourseId")
    suspend fun remapCourseId(oldCourseId: Long, newCourseId: Long)

    @Query("SELECT COUNT(*) FROM homeworks")
    suspend fun getHomeworkCount(): Int

    @Query("DELETE FROM homeworks WHERE courseId = :courseId")
    suspend fun deleteHomeworksByCourse(courseId: Long)

    @Query("DELETE FROM homeworks")
    suspend fun deleteAllHomeworks()
}
