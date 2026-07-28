package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.runBlocking

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "Task Reminder"
        val message = intent.getStringExtra(EXTRA_REMINDER_MESSAGE) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        val db = com.example.data.local.AppDatabase.getDatabase(context)
        val settingsEntity = runBlocking {
            db.settingsDao().getSettingsOnce()
        }

        if (settingsEntity != null && !settingsEntity.reminderEnabled) {
            if (!taskId.isNullOrEmpty()) {
                ReminderManager.cancelTaskReminder(context, taskId)
            } else if (type == TYPE_PLANNING) {
                ReminderManager.cancelDailyPlanningReminder(context)
            }
            return
        }

        if (type == TYPE_PLANNING) {
            showNotification(
                context = context,
                title = title.ifEmpty { "Daily Planning" },
                message = message.ifEmpty { "Take a moment to plan your tasks for tomorrow." },
                notificationId = notificationId,
                navTarget = "tomorrow"
            )
            // Reschedule Daily Planning Reminder for tomorrow
            if (settingsEntity != null) {
                ReminderManager.scheduleDailyPlanningReminder(context, settingsEntity.reminderTime)
            }
        } else if (type == TYPE_TASK && !taskId.isNullOrEmpty()) {
            val task = runBlocking {
                db.taskDao().getTaskById(taskId)
            }

            // If task is missing, completed, or deleted: cancel task reminders immediately
            if (task == null || task.status != "ACTIVE") {
                ReminderManager.cancelTaskReminder(context, taskId)
                return
            }

            showNotification(
                context = context,
                title = title.ifEmpty { "Task Pending" },
                message = message.ifEmpty { "Don't forget to complete: ${task.title}" },
                notificationId = notificationId,
                navTarget = "today"
            )

            // Reschedule next task reminder
            ReminderManager.scheduleHourlyTaskReminder(context, task.id, task.title)
        } else {
            showNotification(
                context = context,
                title = title,
                message = message,
                notificationId = notificationId,
                navTarget = "today"
            )
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        navTarget: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = ReminderManager.CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                ReminderManager.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigation_target", navTarget)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            clickIntent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_REMINDER_TITLE = "reminder_title"
        const val EXTRA_REMINDER_MESSAGE = "reminder_message"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TASK_ID = "task_id"

        const val TYPE_PLANNING = "planning"
        const val TYPE_TASK = "task"
    }
}
