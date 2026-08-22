package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DbTest {
    @Test
    fun testDbAndActiveTasksQuery() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = AppDatabase.getDatabase(context)
        val taskDao = db.taskDao()

        taskDao.clearAllTasks()

        val today = "2026-08-22"
        val tomorrow = "2026-08-23"
        val yesterday = "2026-08-21"

        // Insert active today tasks
        taskDao.insertTask(TaskEntity("1", "Task 1", null, "LOW", null, false, "ACTIVE", today, 1000L, 1000L, null, "LOCAL_ONLY"))
        taskDao.insertTask(TaskEntity("2", "Task 2", null, "HIGH", null, false, "ACTIVE", today, 1001L, 1001L, null, "LOCAL_ONLY"))
        // Insert completed today task
        taskDao.insertTask(TaskEntity("3", "Task 3 Completed", null, "LOW", null, false, "COMPLETED", today, 1002L, 1002L, 1005L, "LOCAL_ONLY"))
        // Insert active tomorrow task
        taskDao.insertTask(TaskEntity("4", "Tomorrow Task", null, "MEDIUM", null, false, "ACTIVE", tomorrow, 1003L, 1003L, null, "LOCAL_ONLY"))
        // Insert active yesterday task (skipped/uncompleted)
        taskDao.insertTask(TaskEntity("5", "Yesterday Skipped Task", null, "MEDIUM", null, false, "ACTIVE", yesterday, 900L, 900L, null, "LOCAL_ONLY"))

        val activeToday = taskDao.getActiveTasksForDateOnce(today)
        assertEquals(2, activeToday.size)
        assertEquals("Task 1", activeToday[0].title)
        assertEquals("Task 2", activeToday[1].title)

        val activeTomorrow = taskDao.getActiveTasksForDateOnce(tomorrow)
        assertEquals(1, activeTomorrow.size)
        assertEquals("Tomorrow Task", activeTomorrow[0].title)

        val activeOtherDate = taskDao.getActiveTasksForDateOnce("2026-09-01")
        assertTrue(activeOtherDate.isEmpty())
    }
}
