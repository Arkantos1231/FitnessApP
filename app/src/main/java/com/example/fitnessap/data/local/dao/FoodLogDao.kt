package com.example.fitnessap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fitnessap.data.local.entity.FoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Insert
    suspend fun insert(log: FoodLog)

    @Delete
    suspend fun delete(log: FoodLog)

    @Query("SELECT * FROM food_logs WHERE dateMillis >= :startMillis AND dateMillis < :endMillis ORDER BY dateMillis DESC")
    fun getLogsForDay(startMillis: Long, endMillis: Long): Flow<List<FoodLog>>

    @Query("SELECT COALESCE(SUM(caloriesConsumed), 0) FROM food_logs WHERE dateMillis >= :startMillis AND dateMillis < :endMillis")
    fun getTotalCaloriesConsumedForDay(startMillis: Long, endMillis: Long): Flow<Int>

    @Query("SELECT * FROM food_logs ORDER BY dateMillis DESC")
    suspend fun getAll(): List<FoodLog>
}
