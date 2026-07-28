package com.example.domain.repository

import com.example.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForDate(dateStr: String): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
    suspend fun getPendingSyncTasks(): List<Task>
    suspend fun clearHistory()
    suspend fun deleteTodayData(dateStr: String)
    suspend fun deleteTomorrowData(dateStr: String)
}
