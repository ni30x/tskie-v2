package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.SyncState
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val context: android.content.Context? = null
) : TaskRepository {
    override fun getTasksForDate(dateStr: String): Flow<List<Task>> {
        return taskDao.getTasksForDate(dateStr).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(id: String) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun getPendingSyncTasks(): List<Task> {
        return taskDao.getPendingSyncTasks(SyncState.SYNC_PENDING.name).map { it.toDomain() }
    }

    override suspend fun clearHistory() {
        taskDao.clearAllTasks()
    }

    override suspend fun deleteTodayData(dateStr: String) {
        taskDao.deleteTasksByDate(dateStr)
    }

    override suspend fun deleteTomorrowData(dateStr: String) {
        taskDao.deleteTasksByDate(dateStr)
    }
}
