package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HomeworkVerification
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkVerificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: HomeworkVerification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerifications(verifications: List<HomeworkVerification>): List<Long>

    @Query("SELECT * FROM homework_verifications WHERE homeworkId = :homeworkId ORDER BY page ASC")
    suspend fun getVerificationsForHomework(homeworkId: Long): List<HomeworkVerification>

    @Query("SELECT * FROM homework_verifications WHERE homeworkId = :homeworkId ORDER BY page ASC")
    fun getVerificationsFlowForHomework(homeworkId: Long): Flow<List<HomeworkVerification>>

    @Query("SELECT hash FROM homework_verifications WHERE hash != 0")
    suspend fun getAllHashes(): List<Long>

    @Query("SELECT * FROM homework_verifications")
    fun getAllVerificationsFlow(): Flow<List<HomeworkVerification>>

    @Query("DELETE FROM homework_verifications WHERE homeworkId = :homeworkId")
    suspend fun deleteVerificationsForHomework(homeworkId: Long)
}
