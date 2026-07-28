package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics_cache WHERE date = :dateStr LIMIT 1")
    fun getStatisticsForDate(dateStr: String): Flow<StatisticsEntity?>

    @Query("SELECT * FROM statistics_cache ORDER BY date ASC")
    fun getAllStatistics(): Flow<List<StatisticsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: StatisticsEntity)

    @Query("DELETE FROM statistics_cache")
    suspend fun clearAllStatistics()
}
