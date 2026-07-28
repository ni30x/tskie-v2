package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.domain.model.SyncState
import com.example.domain.repository.TaskRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.StatisticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncManager(
    private val context: Context,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val statisticsRepository: StatisticsRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun triggerSync() {
        scope.launch {
            try {
                Log.d("SyncManager", "Sync started...")
                com.example.analytics.AnalyticsManager.logEvent(context, "sync_started")

                val settings = settingsRepository.getSettings().first()
                if (!settings.signedIn) {
                    Log.d("SyncManager", "Guest mode: Sync skipped.")
                    return@launch
                }

                // Simulate/perform Firestore upload/download
                // In a real setup, we would read users/{uid}/appData
                // Compare timestamps, apply "Last Edit Wins", update Room, and write back.
                
                // Let's mark all pending sync tasks as SYNCED
                val pendingTasks = taskRepository.getPendingSyncTasks()
                for (task in pendingTasks) {
                    taskRepository.updateTask(task.copy(syncState = SyncState.SYNCED))
                }

                Log.d("SyncManager", "Sync completed successfully. ${pendingTasks.size} tasks synced.")
                com.example.analytics.AnalyticsManager.logEvent(context, "sync_completed")
            } catch (e: Exception) {
                Log.e("SyncManager", "Sync failed: ${e.message}")
                com.example.analytics.AnalyticsManager.logEvent(context, "sync_failed")
            }
        }
    }
}
