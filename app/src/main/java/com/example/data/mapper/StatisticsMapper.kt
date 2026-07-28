package com.example.data.mapper

import com.example.data.local.entity.StatisticsEntity
import com.example.domain.model.Statistics

fun StatisticsEntity.toDomain(): Statistics {
    return Statistics(
        date = date,
        tasksCreated = tasksCreated,
        tasksCompleted = tasksCompleted,
        completionRate = completionRate
    )
}

fun Statistics.toEntity(): StatisticsEntity {
    return StatisticsEntity(
        date = date,
        tasksCreated = tasksCreated,
        tasksCompleted = tasksCompleted,
        completionRate = completionRate
    )
}
