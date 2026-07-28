package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.StatisticsDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.StatisticsEntity
import com.example.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, SettingsEntity::class, StatisticsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun settingsDao(): SettingsDao
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tskie.db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
