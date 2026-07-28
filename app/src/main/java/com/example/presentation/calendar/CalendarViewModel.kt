package com.example.presentation.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.util.DateUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val taskRepository = TaskRepositoryImpl(database.taskDao(), application)

    // Currently selected date on the calendar/history view
    private val _selectedDate = MutableStateFlow(DateUtil.getLogicalToday())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // All active and completed tasks
    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Tasks for the currently selected date in Calendar view
    val tasksForSelectedDate: StateFlow<List<Task>> = combine(allTasks, selectedDate) { tasks, date ->
        tasks.filter { it.taskDate == date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    // Statistics UI State
    val statsState: StateFlow<StatsState> = allTasks.map { tasks ->
        try {
            calculateStats(tasks)
        } catch (t: Throwable) {
            android.util.Log.e("CalendarViewModel", "Error calculating stats", t)
            StatsState()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsState()
    )

    private fun calculateStats(tasks: List<Task>): StatsState {
        if (tasks.isEmpty()) return StatsState()

        val activeAndCompleted = tasks.filter { it.status != TaskStatus.DELETED }
        val totalCreated = activeAndCompleted.size
        val totalCompleted = activeAndCompleted.count { it.status == TaskStatus.COMPLETED }
        val completionRate = if (totalCreated > 0) (totalCompleted.toFloat() / totalCreated.toFloat() * 100).toInt() else 0

        // Group completed tasks by date
        val completedByDate = activeAndCompleted
            .filter { it.status == TaskStatus.COMPLETED && it.completedAt != null }
            .groupBy { it.taskDate }

        // Heatmap counts (date -> completed task count)
        val heatmapData = completedByDate.mapValues { it.value.size }

        // Calculate streaks
        var currentStreak = 0
        var longestStreak = 0
        
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            
            val dateObjects = completedByDate.keys.mapNotNull { 
                try { sdf.parse(it) } catch(e: Exception) { null } 
            }.sorted()

            if (dateObjects.isNotEmpty()) {
                var tempCurrent = 1
                var tempLongest = 1
                var previousDate = dateObjects.first()
                
                for (i in 1 until dateObjects.size) {
                    val currentDate = dateObjects[i]
                    val diffInMillis = currentDate.time - previousDate.time
                    val daysBetween = diffInMillis / (1000 * 60 * 60 * 24)
                    
                    if (daysBetween == 1L) {
                        tempCurrent++
                        if (tempCurrent > tempLongest) {
                            tempLongest = tempCurrent
                        }
                    } else if (daysBetween > 1L) {
                        tempCurrent = 1
                    }
                    previousDate = currentDate
                }
                longestStreak = tempLongest

                // Calculate current streak
                val todayStr = DateUtil.getLogicalToday()
                val yesterdayStr = DateUtil.getYesterdayStr()
                val today = try { sdf.parse(todayStr) } catch(e: Exception) { null }
                val yesterday = try { sdf.parse(yesterdayStr) } catch(e: Exception) { null }

                if (dateObjects.contains(today) || dateObjects.contains(yesterday)) {
                    var streak = 1
                    val reversed = dateObjects.reversed()
                    for (i in 0 until reversed.size - 1) {
                        val d1 = reversed[i]
                        val d2 = reversed[i+1]
                        val diff = (d1.time - d2.time) / (1000 * 60 * 60 * 24)
                        if (diff == 1L) {
                            streak++
                        } else {
                            break
                        }
                    }
                    currentStreak = streak
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("CalendarViewModel", "Error calculating streaks", t)
        }

        // Calculate average completed/day
        val distinctCreatedDates = activeAndCompleted.map { it.taskDate }.distinct().size
        val averageCompletedPerDay = if (distinctCreatedDates > 0) {
            String.format(Locale.US, "%.1f", totalCompleted.toFloat() / distinctCreatedDates.toFloat())
        } else {
            "0.0"
        }

        return StatsState(
            totalCreated = totalCreated,
            totalCompleted = totalCompleted,
            completionRate = completionRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            averageCompletedPerDay = averageCompletedPerDay,
            heatmapData = heatmapData
        )
    }
}

data class StatsState(
    val totalCreated: Int = 0,
    val totalCompleted: Int = 0,
    val completionRate: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageCompletedPerDay: String = "0.0",
    val heatmapData: Map<String, Int> = emptyMap()
)
