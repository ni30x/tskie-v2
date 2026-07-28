package com.example.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.animation.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.ui.components.GlassBox
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HeatmapDay(
    val dateStr: String, // "yyyy-MM-dd"
    val dayOfWeek: Int, // 0 = Mon, ..., 6 = Sun
    val dayOfMonth: Int,
    val isFuture: Boolean,
    val inTargetMonth: Boolean
)

data class HeatmapWeek(
    val weekIndex: Int,
    val days: List<HeatmapDay>
)

fun generateHeatmapWeeksForMonths(displayedMonthCal: Calendar, monthsCount: Int = 2): List<HeatmapWeek> {
    val weeks = mutableListOf<HeatmapWeek>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val todayStr = DateUtil.getLogicalToday()
    val todayCal = Calendar.getInstance()
    val todayDate = DateUtil.parseDateStr(todayStr) ?: todayCal.time
    todayCal.time = todayDate

    val targetYear = displayedMonthCal.get(Calendar.YEAR)
    val targetMonth = displayedMonthCal.get(Calendar.MONTH)

    // Calculate start month (displayedMonthCal - (monthsCount - 1) months)
    val startMonthCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, targetYear)
        set(Calendar.MONTH, targetMonth)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -(monthsCount - 1))
    }

    // End of target month
    val endMonthCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, targetYear)
        set(Calendar.MONTH, targetMonth)
        val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, maxDay)
    }

    // Find Monday on or before 1st of start month
    val javaDayOfWeek = startMonthCal.get(Calendar.DAY_OF_WEEK)
    val monBasedDayOfWeek = (javaDayOfWeek + 5) % 7
    val startCal = startMonthCal.clone() as Calendar
    startCal.add(Calendar.DAY_OF_YEAR, -monBasedDayOfWeek)

    val iterCal = startCal.clone() as Calendar
    var weekIndex = 0

    while (
        iterCal.before(endMonthCal) ||
        (iterCal.get(Calendar.YEAR) == endMonthCal.get(Calendar.YEAR) &&
         iterCal.get(Calendar.MONTH) == endMonthCal.get(Calendar.MONTH) &&
         iterCal.get(Calendar.DAY_OF_MONTH) <= endMonthCal.get(Calendar.DAY_OF_MONTH))
    ) {
        val daysInWeek = mutableListOf<HeatmapDay>()

        for (d in 0 until 7) {
            val dateStr = sdf.format(iterCal.time)
            val y = iterCal.get(Calendar.YEAR)
            val m = iterCal.get(Calendar.MONTH)

            val inRange = (y > startMonthCal.get(Calendar.YEAR) || (y == startMonthCal.get(Calendar.YEAR) && m >= startMonthCal.get(Calendar.MONTH))) &&
                          (y < endMonthCal.get(Calendar.YEAR) || (y == endMonthCal.get(Calendar.YEAR) && m <= endMonthCal.get(Calendar.MONTH)))

            val isFuture = iterCal.after(todayCal) && dateStr != todayStr

            daysInWeek.add(
                HeatmapDay(
                    dateStr = dateStr,
                    dayOfWeek = d,
                    dayOfMonth = iterCal.get(Calendar.DAY_OF_MONTH),
                    isFuture = isFuture,
                    inTargetMonth = inRange
                )
            )
            iterCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        weeks.add(
            HeatmapWeek(
                weekIndex = weekIndex,
                days = daysInWeek
            )
        )
        weekIndex++
    }

    return weeks
}

@Composable
fun getHeatmapCellColor(count: Int, isFuture: Boolean): Color {
    if (isFuture) {
        return Color(0xFFFAFAFA) // Off-white for future dates
    }
    return when (count) {
        0 -> Color(0xFFFAFAFA) // Off-white default when no tasks
        1 -> Color(0xFFB0B7C0) // Cool brushed silver
        2 -> Color(0xFF6C757D) // Pewter / dark silver
        3, 4 -> Color(0xFF343A40) // Gunmetal metallic
        else -> Color(0xFF121417) // Deep onyx black
    }
}

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit = {},
    modifier: Modifier = Modifier,
    onScroll: (Float) -> Unit = {}
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val stats by viewModel.statsState.collectAsStateWithLifecycle()

    val monthHeaderSdf = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    var displayedCal by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                val todayStr = DateUtil.getLogicalToday()
                DateUtil.parseDateStr(todayStr)?.let { time = it }
            }
        )
    }

    val heatmapWeeks = remember(displayedCal.get(Calendar.YEAR), displayedCal.get(Calendar.MONTH)) {
        generateHeatmapWeeksForMonths(displayedCal, monthsCount = 2)
    }
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset.toFloat() }
            .collect { offset ->
                onScroll(offset)
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 24.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Calendar",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Activity & History",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("calendar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // GitHub / Contributer Style Heatmap Card
        item {
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Card Header with Title, Legend and Going-Right Page Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable { onNavigateToStatistics() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Activity Heatmap",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Less -> More Legend
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(text = "Less", fontSize = 10.sp, color = TextSecondary)
                                Box(modifier = Modifier.size(10.dp).background(getHeatmapCellColor(0, false), RoundedCornerShape(2.dp)).border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.size(10.dp).background(getHeatmapCellColor(1, false), RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.size(10.dp).background(getHeatmapCellColor(2, false), RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.size(10.dp).background(getHeatmapCellColor(3, false), RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.size(10.dp).background(getHeatmapCellColor(5, false), RoundedCornerShape(2.dp)))
                                Text(text = "More", fontSize = 10.sp, color = TextSecondary)
                            }

                            // Going Right Indicator Button to open new page
                            IconButton(
                                onClick = onNavigateToStatistics,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("heatmap_going_right_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Heatmap Details Page",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))



                    // Big Numbers Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${stats.totalCompleted}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Completed tasks",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        Column {
                            Text(
                                text = "${stats.totalCreated}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Total tasks logged",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Heatmap Matrix Container (Day Row labels on Left + Monthly Weeks on Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Day Labels (Mon, Tue, Wed, Thu, Fri, Sat, Sun) aligned with 7 rows of 22.dp boxes with 4.dp spacing
                        Column(
                            modifier = Modifier.width(28.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Mon", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.height(22.dp))
                            Text("Tue", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.height(22.dp))
                            Text("Wed", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.height(22.dp))
                            Text("Thu", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.height(22.dp))
                            Text("Fri", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.height(22.dp))
                            Text("Sat", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.height(22.dp))
                            Text("Sun", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.height(22.dp))
                        }

                        // Monthly Grid of Weeks spread across available width
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            heatmapWeeks.forEach { week ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    week.days.forEach { day ->
                                        val count = stats.heatmapData[day.dateStr] ?: 0
                                        val isSelected = day.dateStr == selectedDate

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(22.dp)
                                                .background(
                                                    color = getHeatmapCellColor(count, day.isFuture),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 0.5.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else if (count == 0 || day.isFuture) Color(0xFFCBD5E1) else Color.Transparent,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    if (!day.isFuture) {
                                                        viewModel.selectDate(day.dateStr)
                                                    }
                                                }
                                                .testTag("heatmap_cell_${day.dateStr}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selected Date Info Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected: ${DateUtil.formatDisplayDate(selectedDate)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        val completedForSelected = stats.heatmapData[selectedDate] ?: 0
                        Text(
                            text = "$completedForSelected completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Statistics Cards Section
        item {
            Text(
                text = "Key Metrics",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        label = "Completion Rate",
                        value = "${stats.completionRate}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Tasks Completed",
                        value = "${stats.totalCompleted}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        label = "Current Streak",
                        value = "${stats.currentStreak} d",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Longest Streak",
                        value = "${stats.longestStreak} d",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Selected Date Task History
        item {
            Text(
                text = "History: " + DateUtil.formatDisplayDate(selectedDate),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (tasksForDate.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks recorded for this date.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(tasksForDate, key = { it.id }) { task ->
                Box(modifier = Modifier.animateItem()) {
                    ReadOnlyHistoryRow(task = task)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    GlassBox(
        modifier = modifier.bounceClick { },
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun ReadOnlyHistoryRow(
    task: Task
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isCompleted,
                onClick = null,
                enabled = false,
                colors = RadioButtonDefaults.colors(
                    disabledSelectedColor = MaterialTheme.colorScheme.primary,
                    disabledUnselectedColor = TextSecondary.copy(alpha = 0.5f)
                ),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = task.title,
                fontSize = 15.sp,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isCompleted) TextSecondary else TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

