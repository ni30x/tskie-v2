package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val reminderEnabled: Boolean,
    val reminderTime: Long,
    val reminderRepetition: String,
    val priorityEnabled: Boolean,
    val defaultPriority: String,
    val analyticsEnabled: Boolean,
    val signedIn: Boolean
)
