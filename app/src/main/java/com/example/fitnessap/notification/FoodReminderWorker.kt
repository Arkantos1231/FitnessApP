package com.example.fitnessap.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class FoodReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Food Reminder")
            .setContentText("Don't forget to log what you've eaten!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "food_reminder_channel"
        private const val NOTIFICATION_ID = 1001

        const val WORK_NAME_7  = "food_reminder_07"
        const val WORK_NAME_13 = "food_reminder_13"
        const val WORK_NAME_20 = "food_reminder_20"
        val ALL_WORK_NAMES = listOf(WORK_NAME_7, WORK_NAME_13, WORK_NAME_20)
    }
}
