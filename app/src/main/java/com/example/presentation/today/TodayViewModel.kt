package com.example.presentation.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.model.Task
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTaskUseCase
import com.example.domain.usecase.RestoreTaskUseCase
import com.example.reminder.ReminderManager
import com.example.util.DateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val taskRepository = TaskRepositoryImpl(database.taskDao(), application)

    private val completeTaskUseCase = CompleteTaskUseCase(taskRepository)
    private val deleteTaskUseCase = DeleteTaskUseCase(taskRepository)
    private val restoreTaskUseCase = RestoreTaskUseCase(taskRepository)

    val tasks: StateFlow<List<Task>> = taskRepository.getTasksForDate(DateUtil.getLogicalToday())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                completeTaskUseCase(taskId)
                ReminderManager.scheduleTodayTasksReminder(getApplication())
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "task_completed")
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error completing task", e)
            }
        }
    }

    fun restoreTask(taskId: String) {
        viewModelScope.launch {
            try {
                restoreTaskUseCase(taskId)
                ReminderManager.scheduleTodayTasksReminder(getApplication())
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "task_restored")
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error restoring task", e)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                deleteTaskUseCase(taskId)
                ReminderManager.scheduleTodayTasksReminder(getApplication())
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "task_deleted")
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error deleting task", e)
            }
        }
    }
}
