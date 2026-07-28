package com.example.domain.usecase

import com.example.domain.model.Priority
import com.example.domain.model.SyncState
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.domain.repository.TaskRepository
import java.util.UUID

class CreateTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(
        title: String,
        notes: String?,
        priority: Priority,
        taskDate: String,
        reminderTime: Long?,
        reminderEnabled: Boolean
    ): Result<Task> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }

        val now = System.currentTimeMillis()
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = trimmedTitle,
            notes = notes?.trim()?.ifEmpty { null },
            priority = priority,
            reminderTime = reminderTime,
            reminderEnabled = reminderEnabled,
            status = TaskStatus.ACTIVE,
            taskDate = taskDate,
            createdAt = now,
            updatedAt = now,
            completedAt = null,
            syncState = SyncState.LOCAL_ONLY
        )

        taskRepository.insertTask(task)
        return Result.success(task)
    }
}
