package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Device rebooted or package replaced. Rescheduling reminders.")
            ReminderManager.initNotificationChannel(context)
            ReminderManager.rescheduleAllReminders(context)
            ReminderManager.scheduleWorkManagerPeriodicCheck(context)
        }
    }
}
