package com.example.fitnessap.data.firebase

import com.example.fitnessap.data.local.entity.ActivityLog
import com.example.fitnessap.data.local.entity.FoodLog
import com.example.fitnessap.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncAll(
        activityLogs: List<ActivityLog>,
        foodLogs: List<FoodLog>,
        userProfile: UserProfile
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))

        return try {
            val userDoc = firestore.collection("users").document(uid)

            val batch = firestore.batch()

            // Profile
            val profileData = mapOf(
                "name" to userProfile.name,
                "age" to userProfile.age,
                "weightKg" to userProfile.weightKg,
                "heightCm" to userProfile.heightCm,
                "gender" to userProfile.gender,
                "activityLevel" to userProfile.activityLevel,
                "dailyCalorieGoal" to userProfile.dailyCalorieGoal
            )
            batch.set(userDoc.collection("profile").document("data"), profileData)

            // Activity logs
            activityLogs.forEach { log ->
                val data = mapOf(
                    "dateMillis" to log.dateMillis,
                    "description" to log.description,
                    "caloriesBurned" to log.caloriesBurned
                )
                batch.set(userDoc.collection("activity_logs").document(log.id.toString()), data)
            }

            // Food logs
            foodLogs.forEach { log ->
                val data = mapOf(
                    "dateMillis" to log.dateMillis,
                    "description" to log.description,
                    "caloriesConsumed" to log.caloriesConsumed
                )
                batch.set(userDoc.collection("food_logs").document(log.id.toString()), data)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
