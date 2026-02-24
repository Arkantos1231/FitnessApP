package com.example.fitnessap.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object UserPreferencesKeys {
    val NAME = stringPreferencesKey("name")
    val AGE = intPreferencesKey("age")
    val WEIGHT_KG = floatPreferencesKey("weight_kg")
    val HEIGHT_CM = floatPreferencesKey("height_cm")
    val GENDER = stringPreferencesKey("gender")
    val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
    val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
    val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
    val FOOD_REMINDER_ENABLED = booleanPreferencesKey("food_reminder_enabled")
    val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
}
