package com.example.fitnessap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fitnessap.data.local.entity.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Insert
    suspend fun insert(log: ActivityLog)

    @Delete
    suspend fun delete(log: ActivityLog)

    @Query("SELECT * FROM activity_logs WHERE dateMillis >= :startMillis AND dateMillis < :endMillis ORDER BY dateMillis DESC")
    fun getLogsForDay(startMillis: Long, endMillis: Long): Flow<List<ActivityLog>>

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM activity_logs WHERE dateMillis >= :startMillis AND dateMillis < :endMillis")
    fun getTotalCaloriesBurnedForDay(startMillis: Long, endMillis: Long): Flow<Int>

    @Query("SELECT * FROM activity_logs ORDER BY dateMillis DESC")
    suspend fun getAll(): List<ActivityLog>
}
