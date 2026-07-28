package com.example.widget

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SafeWidgetUpdater {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun updateSafely(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                HeatmapWidgetProvider.updateAllWidgets(appContext)
            } catch (e: Throwable) {
                Log.e("SafeWidgetUpdater", "Failed to update widgets", e)
            }
        }
    }
}
