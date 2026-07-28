package com.example.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            ReminderManager.initNotificationChannel(applicationContext)
            ReminderManager.rescheduleAllReminders(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
