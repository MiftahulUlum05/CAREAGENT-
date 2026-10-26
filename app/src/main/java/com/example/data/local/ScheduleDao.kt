package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items ORDER BY dateStr ASC, startTimeStr ASC")
    fun getAllSchedules(): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items WHERE dateStr = :dateStr ORDER BY startTimeStr ASC")
    fun getSchedulesForDate(dateStr: String): Flow<List<ScheduleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(item: ScheduleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(items: List<ScheduleItemEntity>): List<Long>

    @Update
    suspend fun updateSchedule(item: ScheduleItemEntity)

    @Delete
    suspend fun deleteSchedule(item: ScheduleItemEntity)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("DELETE FROM schedule_items WHERE isAIProposed = 1 AND isApproved = 0")
    suspend fun clearPendingProposals()
}
