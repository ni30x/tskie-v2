package com.example.domain.usecase

import com.example.domain.model.SyncState
import com.example.domain.model.TaskStatus
import com.example.domain.repository.TaskRepository

class RestoreTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        val task = taskRepository.getTaskById(taskId)
            ?: return Result.failure(IllegalArgumentException("Task not found"))

        val updatedTask = task.copy(
            status = TaskStatus.ACTIVE,
            completedAt = null,
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.SYNC_PENDING
        )

        taskRepository.updateTask(updatedTask)
        return Result.success(Unit)
    }
}
