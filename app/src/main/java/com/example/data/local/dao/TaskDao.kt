package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE taskDate = :dateStr AND status != 'DELETED' ORDER BY createdAt ASC")
    fun getTasksForDate(dateStr: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE taskDate = :dateStr AND status = 'ACTIVE' ORDER BY createdAt ASC")
    suspend fun getActiveTasksForDateOnce(dateStr: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE status != 'DELETED' ORDER BY createdAt ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'DELETED' ORDER BY createdAt ASC")
    suspend fun getAllTasksOnce(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("SELECT * FROM tasks WHERE syncState = :pendingState")
    suspend fun getPendingSyncTasks(pendingState: String): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM tasks WHERE taskDate = :dateStr")
    suspend fun deleteTasksByDate(dateStr: String)
}
