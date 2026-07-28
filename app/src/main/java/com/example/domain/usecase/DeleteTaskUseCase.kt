package com.example.domain.usecase

import com.example.domain.model.SyncState
import com.example.domain.model.TaskStatus
import com.example.domain.repository.TaskRepository

class DeleteTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        val task = taskRepository.getTaskById(taskId)
            ?: return Result.failure(IllegalArgumentException("Task not found"))

        // Soft delete to support sync state tracking
        val updatedTask = task.copy(
            status = TaskStatus.DELETED,
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.SYNC_PENDING
        )
        taskRepository.updateTask(updatedTask)
        return Result.success(Unit)
    }
}
