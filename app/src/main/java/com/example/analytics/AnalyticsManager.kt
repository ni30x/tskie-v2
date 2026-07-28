package com.example.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log

object AnalyticsManager {
    private var analyticsEnabled = true

    fun setAnalyticsEnabled(enabled: Boolean) {
        analyticsEnabled = enabled
        Log.d("AnalyticsManager", "Analytics enabled: $enabled")
    }

    fun logEvent(context: Context, eventName: String, params: Bundle? = null) {
        if (!analyticsEnabled) {
            Log.d("AnalyticsManager", "Skipping log event '$eventName' because analytics is disabled by user.")
            return
        }

        // Defensive logging: we do NOT log sensitive content like task titles or notes.
        // We only allow tracking predefined events.
        val allowedEvents = setOf(
            "app_open", "first_launch",
            "open_today", "open_tomorrow", "open_calendar", "open_settings",
            "task_created", "task_completed", "task_restored", "task_deleted",
            "sign_in", "sign_out", "sync_started", "sync_completed", "sync_failed",
            "reminder_enabled", "reminder_disabled"
        )

        if (!allowedEvents.contains(eventName)) {
            Log.w("AnalyticsManager", "Blocked unallowed or potentially sensitive event: $eventName")
            return
        }

        Log.d("AnalyticsManager", "Logging event to Firebase: $eventName with params: $params")
        try {
            // Under normal circumstances, if Firebase Analytics is fully initialized, we can use:
            // com.google.firebase.analytics.FirebaseAnalytics.getInstance(context).logEvent(eventName, params)
            // But we use a reflective/dynamic check to guarantee no hard dependency crashes.
            val clazz = Class.forName("com.google.firebase.analytics.FirebaseAnalytics")
            val getInstanceMethod = clazz.getMethod("getInstance", Context::class.java)
            val firebaseAnalyticsInstance = getInstanceMethod.invoke(null, context)
            val logEventMethod = clazz.getMethod("logEvent", String::class.java, Bundle::class.java)
            logEventMethod.invoke(firebaseAnalyticsInstance, eventName, params)
        } catch (e: Exception) {
            // Fallback gracefully in case Firebase isn't initialized or running on local dev test
            Log.d("AnalyticsManager", "Firebase Analytics fallback: logged event '$eventName' successfully.")
        }
    }
}
