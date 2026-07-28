package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderManager {

    const val CHANNEL_ID = "tskie_notifications_channel"
    const val CHANNEL_NAME = "TSKIE Notifications"
    const val DAILY_PLANNING_NOTIFICATION_ID = 1001

    /**
     * Initializes the single required notification channel "TSKIE Notifications" with DEFAULT importance.
     */
    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "TSKIE Task and Daily Planning Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules the Daily Planning Reminder to fire once per day at the selected time.
     */
    fun scheduleDailyPlanningReminder(context: Context, reminderTimeMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Extract hour and minute from reminderTimeMs or default to 8:00 PM (20:00)
        val calTime = Calendar.getInstance().apply {
            timeInMillis = reminderTimeMs
        }
        val targetHour = if (reminderTimeMs > 0) calTime.get(Calendar.HOUR_OF_DAY) else 20
        val targetMinute = if (reminderTimeMs > 0) calTime.get(Calendar.MINUTE) else 0

        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1) // Target tomorrow if time passed today
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_TYPE, ReminderReceiver.TYPE_PLANNING)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TITLE, "Daily Planning")
            putExtra(ReminderReceiver.EXTRA_REMINDER_MESSAGE, "Take a moment to plan your tasks for tomorrow.")
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, DAILY_PLANNING_NOTIFICATION_ID)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_PLANNING_NOTIFICATION_ID,
            intent,
            getPendingIntentFlags()
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetCal.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetCal.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("ReminderManager", "Scheduled daily planning reminder for ${targetCal.time}")
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to schedule daily planning reminder: ${e.message}")
        }
    }

    /**
     * Cancels the Daily Planning Reminder.
     */
    fun cancelDailyPlanningReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_PLANNING_NOTIFICATION_ID,
            intent,
            getPendingIntentFlags()
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Schedules a specific task reminder.
     */
    fun scheduleTaskReminder(
        context: Context,
        taskId: String,
        taskTitle: String,
        delayMs: Long? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val actualDelayMs = delayMs ?: (2 * 60 * 60 * 1000L) // Default 2 hours delay if unspecified
        val triggerTimeMs = System.currentTimeMillis() + actualDelayMs

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_TYPE, ReminderReceiver.TYPE_TASK)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TITLE, "Task Pending")
            putExtra(ReminderReceiver.EXTRA_REMINDER_MESSAGE, "Don't forget to complete: $taskTitle")
            putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, taskId.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            getPendingIntentFlags()
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to schedule task reminder: ${e.message}")
        }
    }

    fun scheduleHourlyTaskReminder(
        context: Context,
        taskId: String,
        taskTitle: String,
        delayMs: Long? = null
    ) {
        scheduleTaskReminder(context, taskId, taskTitle, delayMs)
    }

    /**
     * Cancels a scheduled task reminder.
     */
    fun cancelTaskReminder(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            getPendingIntentFlags()
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Reschedules all active reminders (Daily Planning & active Task Reminders).
     */
    fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val settings = db.settingsDao().getSettingsOnce()
            if (settings != null && settings.reminderEnabled) {
                scheduleDailyPlanningReminder(context, settings.reminderTime)
                val activeTasks = db.taskDao().getAllTasksOnce().filter { it.status == "ACTIVE" }
                activeTasks.forEach { task ->
                    scheduleTaskReminder(context, task.id, task.title)
                }
            } else {
                cancelDailyPlanningReminder(context)
            }
        }
    }

    /**
     * Schedules periodic background verification via WorkManager.
     */
    fun scheduleWorkManagerPeriodicCheck(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "TSKIE_ReminderCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to enqueue WorkManager check: ${e.message}")
        }
    }

    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }
}
