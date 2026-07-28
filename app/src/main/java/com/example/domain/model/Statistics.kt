package com.example.domain.model

data class Statistics(
    val date: String, // "yyyy-MM-dd"
    val tasksCreated: Int,
    val tasksCompleted: Int,
    val completionRate: Float
)
