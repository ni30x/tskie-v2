package com.example.data.repository

import com.example.data.local.dao.StatisticsDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.Statistics
import com.example.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatisticsRepositoryImpl(private val statisticsDao: StatisticsDao) : StatisticsRepository {
    override fun getStatisticsForDate(dateStr: String): Flow<Statistics?> {
        return statisticsDao.getStatisticsForDate(dateStr).map { it?.toDomain() }
    }

    override fun getAllStatistics(): Flow<List<Statistics>> {
        return statisticsDao.getAllStatistics().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertStatistics(statistics: Statistics) {
        statisticsDao.insertStatistics(statistics.toEntity())
    }

    override suspend fun clearStatistics() {
        statisticsDao.clearAllStatistics()
    }
}
