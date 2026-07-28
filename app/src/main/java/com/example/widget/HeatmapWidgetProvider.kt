package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.domain.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeatmapWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val handler = kotlinx.coroutines.CoroutineExceptionHandler { _, exception ->
                android.util.Log.e("HeatmapWidgetProvider", "Error updating widget in coroutine", exception)
            }
            CoroutineScope(Dispatchers.IO + handler).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val activeAndCompleted = db.taskDao().getAllTasksOnce()
                        .filter { it.status != TaskStatus.DELETED.name }

                    val totalCreated = activeAndCompleted.size
                    val totalCompleted = activeAndCompleted.count { it.status == TaskStatus.COMPLETED.name }

                    val completedByDate = activeAndCompleted
                        .filter { it.status == TaskStatus.COMPLETED.name && it.completedAt != null }
                        .groupBy { it.taskDate }

                    val heatmapData = completedByDate.mapValues { it.value.size }

                    val uiMode = context.resources.configuration.uiMode
                    val isDarkMode = (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

                    var bitmap = HeatmapWidgetRenderer.renderHeatmapBitmap(
                        widthPx = 800,
                        heightPx = 300,
                        heatmapData = heatmapData,
                        isDarkMode = isDarkMode,
                        totalCompleted = totalCompleted,
                        totalCreated = totalCreated
                    )
                    
                    bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 400, 150, true)

                    val views = RemoteViews(context.packageName, R.layout.widget_heatmap)
                    views.setImageViewBitmap(R.id.widget_heatmap_image, bitmap)

                    // Intent to open MainActivity on Calendar tab when widget is tapped
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("navigation_target", "calendar")
                    }

                    val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        pendingIntentFlags
                    )

                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    android.util.Log.e("HeatmapWidgetProvider", "Error updating widget", e)
                }
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, HeatmapWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (appWidgetId in appWidgetIds) {
                    updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}
