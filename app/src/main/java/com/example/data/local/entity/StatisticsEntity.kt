package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics_cache")
data class StatisticsEntity(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val tasksCreated: Int,
    val tasksCompleted: Int,
    val completionRate: Float
)
