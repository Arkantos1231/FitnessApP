package com.example.fitnessap.data.repository

import com.example.fitnessap.data.local.AppDatabase
import com.example.fitnessap.data.local.entity.ActivityLog
import com.example.fitnessap.data.local.entity.FoodLog
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LogRepository(private val db: AppDatabase) {

    private fun todayBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 24 * 60 * 60 * 1000L
        return Pair(start, end)
    }

    fun getTodayActivityLogs(): Flow<List<ActivityLog>> {
        val (start, end) = todayBounds()
        return db.activityLogDao().getLogsForDay(start, end)
    }

    fun getTodayFoodLogs(): Flow<List<FoodLog>> {
        val (start, end) = todayBounds()
        return db.foodLogDao().getLogsForDay(start, end)
    }

    fun getTodayCaloriesBurned(): Flow<Int> {
        val (start, end) = todayBounds()
        return db.activityLogDao().getTotalCaloriesBurnedForDay(start, end)
    }

    fun getTodayCaloriesConsumed(): Flow<Int> {
        val (start, end) = todayBounds()
        return db.foodLogDao().getTotalCaloriesConsumedForDay(start, end)
    }

    suspend fun insertActivityLog(log: ActivityLog) {
        db.activityLogDao().insert(log)
    }

    suspend fun insertFoodLog(log: FoodLog) {
        db.foodLogDao().insert(log)
    }

    suspend fun deleteActivityLog(log: ActivityLog) {
        db.activityLogDao().delete(log)
    }

    suspend fun deleteFoodLog(log: FoodLog) {
        db.foodLogDao().delete(log)
    }

    suspend fun getAllActivityLogs(): List<ActivityLog> = db.activityLogDao().getAll()

    suspend fun getAllFoodLogs(): List<FoodLog> = db.foodLogDao().getAll()
}
