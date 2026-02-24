package com.example.fitnessap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitnessap.data.local.dao.ActivityLogDao
import com.example.fitnessap.data.local.dao.FoodLogDao
import com.example.fitnessap.data.local.entity.ActivityLog
import com.example.fitnessap.data.local.entity.FoodLog

@Database(
    entities = [ActivityLog::class, FoodLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_ap_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
