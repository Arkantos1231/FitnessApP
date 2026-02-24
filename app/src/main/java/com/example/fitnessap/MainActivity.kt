package com.example.fitnessap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fitnessap.navigation.AppNavigation
import com.example.fitnessap.notification.FoodReminderWorker
import com.example.fitnessap.ui.theme.FitnessApTheme

class  MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            FitnessApTheme {
                AppNavigation(context = applicationContext)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            FoodReminderWorker.CHANNEL_ID,
            "Food Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you to log your food every 6 hours"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
