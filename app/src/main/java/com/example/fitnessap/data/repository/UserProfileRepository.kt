package com.example.fitnessap.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.fitnessap.data.datastore.UserPreferencesKeys
import com.example.fitnessap.data.datastore.userPreferencesDataStore
import com.example.fitnessap.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepository(private val context: Context) {

    val userProfileFlow: Flow<UserProfile> = context.userPreferencesDataStore.data.map { prefs ->
        UserProfile(
            name = prefs[UserPreferencesKeys.NAME] ?: "",
            age = prefs[UserPreferencesKeys.AGE] ?: 0,
            weightKg = prefs[UserPreferencesKeys.WEIGHT_KG] ?: 0f,
            heightCm = prefs[UserPreferencesKeys.HEIGHT_CM] ?: 0f,
            gender = prefs[UserPreferencesKeys.GENDER] ?: "",
            activityLevel = prefs[UserPreferencesKeys.ACTIVITY_LEVEL] ?: "",
            dailyCalorieGoal = prefs[UserPreferencesKeys.DAILY_CALORIE_GOAL] ?: 0
        )
    }

    val apiKeyFlow: Flow<String> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[UserPreferencesKeys.OPENAI_API_KEY] ?: ""
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[UserPreferencesKeys.NAME] = profile.name
            prefs[UserPreferencesKeys.AGE] = profile.age
            prefs[UserPreferencesKeys.WEIGHT_KG] = profile.weightKg
            prefs[UserPreferencesKeys.HEIGHT_CM] = profile.heightCm
            prefs[UserPreferencesKeys.GENDER] = profile.gender
            prefs[UserPreferencesKeys.ACTIVITY_LEVEL] = profile.activityLevel
            prefs[UserPreferencesKeys.DAILY_CALORIE_GOAL] = profile.dailyCalorieGoal
        }
    }

    suspend fun saveApiKey(apiKey: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[UserPreferencesKeys.OPENAI_API_KEY] = apiKey
        }
    }

    val foodReminderEnabledFlow: Flow<Boolean> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[UserPreferencesKeys.FOOD_REMINDER_ENABLED] ?: false
    }

    suspend fun saveFoodReminderEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[UserPreferencesKeys.FOOD_REMINDER_ENABLED] = enabled
        }
    }

    val lastSyncMillisFlow: Flow<Long> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[UserPreferencesKeys.LAST_SYNC_MILLIS] ?: 0L
    }

    suspend fun saveLastSyncMillis(millis: Long) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[UserPreferencesKeys.LAST_SYNC_MILLIS] = millis
        }
    }
}
