package com.example.domain.usecase

import com.example.domain.model.SyncState
import com.example.domain.model.TaskStatus
import com.example.domain.repository.TaskRepository

class CompleteTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        val task = taskRepository.getTaskById(taskId)
            ?: return Result.failure(IllegalArgumentException("Task not found"))

        val now = System.currentTimeMillis()
        val updatedTask = task.copy(
            status = TaskStatus.COMPLETED,
            completedAt = now,
            updatedAt = now,
            syncState = SyncState.SYNC_PENDING
        )

        taskRepository.updateTask(updatedTask)
        return Result.success(Unit)
    }
}
