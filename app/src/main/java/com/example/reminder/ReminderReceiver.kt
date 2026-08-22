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
import com.example.data.local.AppDatabase
import com.example.util.DateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        val db = AppDatabase.getDatabase(context)
        val settingsEntity = runBlocking(Dispatchers.IO) {
            db.settingsDao().getSettingsOnce()
        }

        if (settingsEntity != null && !settingsEntity.reminderEnabled) {
            ReminderManager.cancelDailyPlanningReminder(context)
            ReminderManager.cancelTodayTasksReminder(context)
            ReminderManager.dismissTaskNotification(context)
            return
        }

        if (type == TYPE_PLANNING) {
            val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "Daily Planning"
            val message = intent.getStringExtra(EXTRA_REMINDER_MESSAGE) ?: "Take a moment to plan your tasks for tomorrow."
            
            showNotification(
                context = context,
                title = title.ifEmpty { "Daily Planning" },
                message = message.ifEmpty { "Take a moment to plan your tasks for tomorrow." },
                bigText = null,
                notificationId = ReminderManager.DAILY_PLANNING_NOTIFICATION_ID,
                navTarget = "tomorrow"
            )
            // Reschedule Daily Planning Reminder for tomorrow
            if (settingsEntity != null) {
                ReminderManager.scheduleDailyPlanningReminder(context, settingsEntity.reminderTime)
            }
        } else if (type == TYPE_TASK) {
            // ONLY check active tasks for TODAY
            val todayStr = DateUtil.getLogicalToday()
            val activeTasks = runBlocking(Dispatchers.IO) {
                db.taskDao().getActiveTasksForDateOnce(todayStr)
            }

            // If no active tasks remain for today, cancel reminder and dismiss any notification
            if (activeTasks.isEmpty()) {
                ReminderManager.cancelTodayTasksReminder(context)
                ReminderManager.dismissTaskNotification(context)
                return
            }

            val taskCount = activeTasks.size
            val title = if (taskCount == 1) {
                "1 Task Pending Today"
            } else {
                "$taskCount Tasks Pending Today"
            }

            val shortMessage = if (taskCount == 1) {
                "Don't forget: ${activeTasks[0].title}"
            } else {
                activeTasks.joinToString(", ") { it.title }
            }

            val expandedText = if (taskCount == 1) {
                "Don't forget to complete:\n• ${activeTasks[0].title}"
            } else {
                "Tasks to complete today:\n" + activeTasks.joinToString("\n") { "• ${it.title}" }
            }

            showNotification(
                context = context,
                title = title,
                message = shortMessage,
                bigText = expandedText,
                notificationId = ReminderManager.TASK_REMINDER_NOTIFICATION_ID,
                navTarget = "today"
            )

            // Reschedule next task reminder for today's tasks
            ReminderManager.scheduleTodayTasksReminder(context)
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        bigText: String?,
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

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!bigText.isNullOrEmpty()) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }

        notificationManager.notify(notificationId, builder.build())
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
