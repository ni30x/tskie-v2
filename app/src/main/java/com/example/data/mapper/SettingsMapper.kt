package com.example.data.mapper

import com.example.data.local.entity.SettingsEntity
import com.example.domain.model.Priority
import com.example.domain.model.Settings

fun SettingsEntity.toDomain(): Settings {
    return Settings(
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderRepetition = reminderRepetition,
        priorityEnabled = priorityEnabled,
        defaultPriority = Priority.valueOf(defaultPriority),
        analyticsEnabled = analyticsEnabled,
        signedIn = signedIn
    )
}

fun Settings.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = 1,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderRepetition = reminderRepetition,
        priorityEnabled = priorityEnabled,
        defaultPriority = defaultPriority.name,
        analyticsEnabled = analyticsEnabled,
        signedIn = signedIn
    )
}
