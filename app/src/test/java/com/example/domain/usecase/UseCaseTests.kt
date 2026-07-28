package com.example.domain.usecase

import com.example.domain.model.Priority
import com.example.domain.model.SyncState
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCaseTests {

    private val fakeRepository = FakeTaskRepository()

    private val createTaskUseCase = CreateTaskUseCase(fakeRepository)
    private val completeTaskUseCase = CompleteTaskUseCase(fakeRepository)
    private val deleteTaskUseCase = DeleteTaskUseCase(fakeRepository)
    private val restoreTaskUseCase = RestoreTaskUseCase(fakeRepository)

    @Test
    fun testCreateTaskUseCase_success() = runBlocking {
        val result = createTaskUseCase(
            title = "Test Task",
            notes = "Test Notes",
            priority = Priority.HIGH,
            taskDate = "2026-07-11",
            reminderTime = 123456L,
            reminderEnabled = true
        )

        assertTrue(result.isSuccess)
        val task = result.getOrNull()
        assertNotNull(task)
        assertEquals("Test Task", task?.title)
        assertEquals("Test Notes", task?.notes)
        assertEquals(Priority.HIGH, task?.priority)
        assertEquals(TaskStatus.ACTIVE, task?.status)
    }

    @Test
    fun testCompleteTaskUseCase_success() = runBlocking {
        // First create
        val task = createTaskUseCase("Complete Me", null, Priority.LOW, "2026-07-11", null, false).getOrThrow()
        
        val result = completeTaskUseCase(task.id)
        assertTrue(result.isSuccess)

        val updatedTask = fakeRepository.getTaskById(task.id)
        assertEquals(TaskStatus.COMPLETED, updatedTask?.status)
        assertNotNull(updatedTask?.completedAt)
    }

    @Test
    fun testDeleteTaskUseCase_success() = runBlocking {
        val task = createTaskUseCase("Delete Me", null, Priority.LOW, "2026-07-11", null, false).getOrThrow()

        val result = deleteTaskUseCase(task.id)
        assertTrue(result.isSuccess)

        val updatedTask = fakeRepository.getTaskById(task.id)
        assertEquals(TaskStatus.DELETED, updatedTask?.status)
    }

    @Test
    fun testRestoreTaskUseCase_success() = runBlocking {
        val task = createTaskUseCase("Restore Me", null, Priority.LOW, "2026-07-11", null, false).getOrThrow()
        completeTaskUseCase(task.id)

        val result = restoreTaskUseCase(task.id)
        assertTrue(result.isSuccess)

        val updatedTask = fakeRepository.getTaskById(task.id)
        assertEquals(TaskStatus.ACTIVE, updatedTask?.status)
        assertEquals(null, updatedTask?.completedAt)
    }
}

class FakeTaskRepository : TaskRepository {
    private val tasks = mutableMapOf<String, Task>()

    override fun getTasksForDate(dateStr: String): Flow<List<Task>> {
        return flowOf(tasks.values.filter { it.taskDate == dateStr })
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return flowOf(tasks.values.toList())
    }

    override suspend fun getTaskById(id: String): Task? {
        return tasks[id]
    }

    override suspend fun insertTask(task: Task) {
        tasks[task.id] = task
    }

    override suspend fun updateTask(task: Task) {
        tasks[task.id] = task
    }

    override suspend fun deleteTask(id: String) {
        tasks.remove(id)
    }

    override suspend fun getPendingSyncTasks(): List<Task> {
        return tasks.values.filter { it.syncState == SyncState.SYNC_PENDING }
    }

    override suspend fun clearHistory() {
        tasks.clear()
    }

    override suspend fun deleteTodayData(dateStr: String) {
        tasks.entries.removeIf { it.value.taskDate == dateStr }
    }

    override suspend fun deleteTomorrowData(dateStr: String) {
        tasks.entries.removeIf { it.value.taskDate == dateStr }
    }
}
