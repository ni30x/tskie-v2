package com.example.domain.model

data class Task(
    val id: String,
    val title: String,
    val notes: String?,
    val priority: Priority,
    val reminderTime: Long?,
    val reminderEnabled: Boolean,
    val status: TaskStatus,
    val taskDate: String, // "yyyy-MM-dd"
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val syncState: SyncState
)
