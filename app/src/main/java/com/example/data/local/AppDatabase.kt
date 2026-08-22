package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.StatisticsDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.StatisticsEntity
import com.example.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, SettingsEntity::class, StatisticsEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun settingsDao(): SettingsDao
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_taskDate` ON `tasks` (`taskDate`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tskie.db"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration(true)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
