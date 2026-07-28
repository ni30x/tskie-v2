package com.example.domain.repository

import com.example.domain.model.Statistics
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getStatisticsForDate(dateStr: String): Flow<Statistics?>
    fun getAllStatistics(): Flow<List<Statistics>>
    suspend fun insertStatistics(statistics: Statistics)
    suspend fun clearStatistics()
}
