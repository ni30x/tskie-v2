package com.example.presentation.tomorrow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.model.Priority
import com.example.domain.model.Task
import com.example.domain.usecase.CreateTaskUseCase
import com.example.domain.usecase.DeleteTaskUseCase
import com.example.util.DateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TomorrowViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val taskRepository = TaskRepositoryImpl(database.taskDao(), application)

    private val createTaskUseCase = CreateTaskUseCase(taskRepository)
    private val deleteTaskUseCase = DeleteTaskUseCase(taskRepository)

    val tasks: StateFlow<List<Task>> = taskRepository.getTasksForDate(DateUtil.getLogicalTomorrow())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createTask(
        title: String,
        notes: String?,
        priority: Priority,
        reminderTime: Long?,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            try {
                val result = createTaskUseCase(
                    title = title,
                    notes = notes,
                    priority = priority,
                    taskDate = DateUtil.getLogicalTomorrow(),
                    reminderTime = reminderTime,
                    reminderEnabled = reminderEnabled
                )
                if (result.isSuccess) {
                    com.example.analytics.AnalyticsManager.logEvent(getApplication(), "task_created")
                }
            } catch (e: Exception) {
                android.util.Log.e("TomorrowViewModel", "Error creating task", e)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                deleteTaskUseCase(taskId)
                com.example.analytics.AnalyticsManager.logEvent(getApplication(), "task_deleted")
            } catch (e: Exception) {
                android.util.Log.e("TomorrowViewModel", "Error deleting task", e)
            }
        }
    }
}
