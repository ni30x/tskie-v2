package com.example.data.mapper

import com.example.data.local.entity.TaskEntity
import com.example.domain.model.Priority
import com.example.domain.model.SyncState
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        notes = notes,
        priority = Priority.valueOf(priority),
        reminderTime = reminderTime,
        reminderEnabled = reminderEnabled,
        status = TaskStatus.valueOf(status),
        taskDate = taskDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        syncState = SyncState.valueOf(syncState)
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        notes = notes,
        priority = priority.name,
        reminderTime = reminderTime,
        reminderEnabled = reminderEnabled,
        status = status.name,
        taskDate = taskDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        syncState = syncState.name
    )
}
