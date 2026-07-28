package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String?,
    val priority: String, // Enum: HIGH/MEDIUM/LOW
    val reminderTime: Long?,
    val reminderEnabled: Boolean,
    val status: String, // Enum: ACTIVE/COMPLETED/DELETED
    val taskDate: String, // format: "yyyy-MM-dd"
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val syncState: String // Enum: LOCAL_ONLY/SYNC_PENDING/SYNCED/SYNC_FAILED
)
