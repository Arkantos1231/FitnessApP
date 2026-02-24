package com.example.fitnessap.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnessap.data.firebase.FirestoreSyncRepository
import com.example.fitnessap.data.local.AppDatabase
import com.example.fitnessap.data.repository.LogRepository
import com.example.fitnessap.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

class FirebaseSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "firebase_sync"
    }

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) return Result.failure()

        val context = applicationContext
        val db = AppDatabase.getInstance(context)
        val logRepository = LogRepository(db)
        val userProfileRepository = UserProfileRepository(context)
        val syncRepository = FirestoreSyncRepository()

        val activityLogs = logRepository.getAllActivityLogs()
        val foodLogs = logRepository.getAllFoodLogs()
        val userProfile = userProfileRepository.userProfileFlow.first()

        val syncResult = syncRepository.syncAll(activityLogs, foodLogs, userProfile)

        return if (syncResult.isSuccess) {
            userProfileRepository.saveLastSyncMillis(System.currentTimeMillis())
            Result.success()
        } else {
            Result.retry()
        }
    }
}
