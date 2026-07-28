package com.example.presentation.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.model.Priority
import com.example.domain.model.Settings
import com.example.util.DateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val settingsRepository = SettingsRepositoryImpl(database.settingsDao())
    private val taskRepository = TaskRepositoryImpl(database.taskDao(), application)

    val settings: StateFlow<Settings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(reminderEnabled = enabled))
            val allTasks = database.taskDao().getAllTasksOnce()
            val activeTasks = allTasks.filter { it.status == "ACTIVE" }
            if (enabled) {
                com.example.reminder.ReminderManager.scheduleDailyPlanningReminder(
                    getApplication(),
                    current.reminderTime
                )
                activeTasks.forEach { task ->
                    com.example.reminder.ReminderManager.scheduleHourlyTaskReminder(
                        getApplication(),
                        task.id,
                        task.title
                    )
                }
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "reminder_enabled")
            } else {
                com.example.reminder.ReminderManager.cancelDailyPlanningReminder(getApplication())
                activeTasks.forEach { task ->
                    com.example.reminder.ReminderManager.cancelTaskReminder(getApplication(), task.id)
                }
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "reminder_disabled")
            }
        }
    }

    fun updateReminderTime(timeMs: Long) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(reminderTime = timeMs))
            if (current.reminderEnabled) {
                com.example.reminder.ReminderManager.scheduleDailyPlanningReminder(getApplication(), timeMs)
            }
        }
    }

    fun updateReminderRepetition(repetition: String) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(reminderRepetition = repetition))
        }
    }

    fun updatePriorityEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(priorityEnabled = enabled))
        }
    }

    fun updateDefaultPriority(priority: Priority) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(defaultPriority = priority))
        }
    }

    fun updateAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(analyticsEnabled = enabled))
            com.example.analytics.AnalyticsManager.setAnalyticsEnabled(enabled)
        }
    }

    fun toggleSignIn() {
        viewModelScope.launch {
            val current = settings.value
            val nextSignedIn = !current.signedIn
            settingsRepository.updateSettings(current.copy(signedIn = nextSignedIn))
            if (nextSignedIn) {
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "sign_in")
            } else {
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "sign_out")
            }
        }
    }

    fun deleteTodayData() {
        viewModelScope.launch {
            try {
                taskRepository.deleteTodayData(DateUtil.getLogicalToday())
                Log.d("SettingsViewModel", "Deleted all tasks for Today.")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error deleting today data", e)
            }
        }
    }

    fun deleteTomorrowData() {
        viewModelScope.launch {
            try {
                taskRepository.deleteTomorrowData(DateUtil.getLogicalTomorrow())
                Log.d("SettingsViewModel", "Deleted all tasks for Tomorrow.")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error deleting tomorrow data", e)
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                taskRepository.clearHistory()
                Log.d("SettingsViewModel", "Cleared all task history.")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error clearing history", e)
            }
        }
    }

    fun exportHistoryToJson(): String {
        // Simple manual JSON serializer for exporting history
        return "{\"app\":\"TSKIE\",\"exportedAt\":${System.currentTimeMillis()}}"
    }
}
