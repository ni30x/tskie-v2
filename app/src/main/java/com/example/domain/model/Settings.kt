package com.example.domain.model

data class Settings(
    val reminderEnabled: Boolean = true,
    val reminderTime: Long = 14 * 60 * 60 * 1000L, // 2:00 PM in millis from start of day
    val reminderRepetition: String = "Daily",
    val priorityEnabled: Boolean = true,
    val defaultPriority: Priority = Priority.LOW,
    val analyticsEnabled: Boolean = true,
    val signedIn: Boolean = false
)
