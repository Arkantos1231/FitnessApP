package com.example.fitnessap.data.model

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val gender: String = "",
    val activityLevel: String = "",
    val dailyCalorieGoal: Int = 0
)
