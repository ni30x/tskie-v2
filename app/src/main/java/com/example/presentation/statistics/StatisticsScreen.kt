package com.example.presentation.statistics

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.ui.animation.AnimationTokens
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Priority
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.presentation.calendar.CalendarViewModel
import com.example.presentation.calendar.getHeatmapCellColor
import com.example.ui.components.GlassBox
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statsState by viewModel.statsState.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    val consolidatedStats = remember(allTasks) {
        computeConsolidatedStats(allTasks)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp)
            .testTag("statistics_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Bar Header
        item {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("statistics_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Activity Insights",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Connected Analytics & Performance Charts",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Expanded Activity Heatmap Card
        item {
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Annual Activity Grid",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Legend
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Stats Highlights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMiniHighlight(
                            label = "Completion Rate",
                            value = "${statsState.completionRate}%",
                            icon = Icons.Default.CheckCircle
                        )
                        StatMiniHighlight(
                            label = "Current Streak",
                            value = "${statsState.currentStreak} Days",
                            icon = Icons.Default.LocalFireDepartment
                        )
                        StatMiniHighlight(
                            label = "Longest Streak",
                            value = "${statsState.longestStreak} Days",
                            icon = Icons.Default.EmojiEvents
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Detailed Heatmap Grid (Last 12 Weeks / 84 Days)
                    Text(
                        text = "Daily Completion Density",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val (weeks, _) = remember(statsState.heatmapData) {
                        generateExpandedHeatmapData(statsState.heatmapData)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weeks.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                week.forEach { dayInfo ->
                                    val count = dayInfo.count
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(getHeatmapCellColor(count, dayInfo.isFuture))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // GRAPH 1: Smooth Line Chart with Gradient Fill
        // ==========================================
        item {
            LineChartCard(stats = consolidatedStats.lineChartByFilter)
        }

        // ==========================================
        // GRAPH 2: Dual Grouped Vertical Bar Chart
        // ==========================================
        item {
            GroupedBarChartCard(stats = consolidatedStats.groupedBarByFilter)
        }

        // ==========================================
        // GRAPH 3: Stacked Multi-Layered Area Chart
        // ==========================================
        item {
            StackedAreaChartCard(stats = consolidatedStats.stackedAreaByFilter)
        }

        // Performance Metrics Cards
        item {
            Text(
                text = "Productivity Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "Total Tasks",
                    value = "${statsState.totalCreated}",
                    subtitle = "${statsState.totalCompleted} Completed",
                    icon = Icons.Default.TaskAlt,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "Daily Average",
                    value = statsState.averageCompletedPerDay,
                    subtitle = "Tasks / Day",
                    icon = Icons.Default.Speed,
                    iconColor = Color(0xFF10B981)
                )
            }
        }

        // Priority Distribution Card
        item {
            val (highCount, mediumCount, lowCount, total) = consolidatedStats.priorityDistribution

            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Tasks by Priority",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    PriorityBar(label = "High Priority", count = highCount, total = total, color = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.height(10.dp))
                    PriorityBar(label = "Medium Priority", count = mediumCount, total = total, color = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(10.dp))
                    PriorityBar(label = "Low Priority", count = lowCount, total = total, color = Color(0xFF10B981))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ==========================================
// COMPONENT 1: Line Chart connected to app tasks
// ==========================================
@Composable
private fun LineChartCard(stats: Map<String, LineChartData>) {
    var selectedFilter by remember { mutableStateOf("Weekly") }

    val chartData = stats[selectedFilter] ?: LineChartData(emptyList(), emptyList(), 0)
    val linePoints = chartData.points
    val labels = chartData.labels
    val totalCompletedInPeriod = chartData.totalCompleted

    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Completion Velocity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$totalCompletedInPeriod completed in $selectedFilter",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TimeFilterPill(
                    selectedFilter = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 28.dp.toPx()
                    val chartHeight = height - bottomPadding
                    val stepX = width / (linePoints.size - 1).coerceAtLeast(1)

                    // Draw horizontal dashed grid lines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = chartHeight * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.25f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    if (linePoints.isNotEmpty()) {
                        // Build line and fill paths
                        val path = Path()
                        val fillPath = Path()

                        val firstPoint = Offset(0f, chartHeight * (1 - linePoints.first()))
                        path.moveTo(firstPoint.x, firstPoint.y)
                        fillPath.moveTo(0f, chartHeight)
                        fillPath.lineTo(firstPoint.x, firstPoint.y)

                        for (i in 0 until linePoints.size - 1) {
                            val p1 = Offset(i * stepX, chartHeight * (1 - linePoints[i]))
                            val p2 = Offset((i + 1) * stepX, chartHeight * (1 - linePoints[i + 1]))

                            val controlPoint1 = Offset(p1.x + stepX / 2, p1.y)
                            val controlPoint2 = Offset(p1.x + stepX / 2, p2.y)

                            path.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p2.x, p2.y
                            )
                            fillPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p2.x, p2.y
                            )
                        }

                        val lastX = (linePoints.size - 1) * stepX
                        fillPath.lineTo(lastX, chartHeight)
                        fillPath.close()

                        // Draw Gradient Fill under line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    primaryColor.copy(alpha = 0.02f)
                                ),
                                startY = 0f,
                                endY = chartHeight
                            )
                        )

                        // Draw Smooth Line
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw highlight node at peak
                        val maxVal = linePoints.maxOrNull() ?: 0f
                        if (maxVal > 0f) {
                            val peakIndex = linePoints.indexOf(maxVal)
                            val peakX = peakIndex * stepX
                            val peakY = chartHeight * (1 - maxVal)

                            drawCircle(
                                color = primaryColor,
                                radius = 6.dp.toPx(),
                                center = Offset(peakX, peakY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(peakX, peakY)
                            )
                        }
                    }
                }

                // X-Axis Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 2: Grouped Bar Chart connected to app tasks
// ==========================================
@Composable
private fun GroupedBarChartCard(stats: Map<String, GroupedBarData>) {
    var selectedFilter by remember { mutableStateOf("Weekly") }

    val chartData = stats[selectedFilter] ?: GroupedBarData(emptyList(), emptyList())
    val dataPairs = chartData.pairs
    val dates = chartData.dates

    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Created vs Completed",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                TimeFilterPill(
                    selectedFilter = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val darkColor = MaterialTheme.colorScheme.onSurface
            val softPurple = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(darkColor, RoundedCornerShape(2.dp)))
                    Text(text = "Completed", fontSize = 11.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(softPurple, RoundedCornerShape(2.dp)))
                    Text(text = "Created", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 24.dp.toPx()
                    val chartHeight = height - bottomPadding

                    val numGroups = dataPairs.size.coerceAtLeast(1)
                    val groupWidth = width / numGroups
                    val barWidth = 14.dp.toPx()
                    val barSpacing = 4.dp.toPx()

                    for (i in dataPairs.indices) {
                        val groupCenterX = i * groupWidth + groupWidth / 2
                        val (completedRatio, createdRatio) = dataPairs[i]

                        // Bar 1: Completed
                        val bar1Height = if (completedRatio > 0f) (chartHeight * completedRatio).coerceAtLeast(3.dp.toPx()) else 0f
                        val bar1X = groupCenterX - barWidth - (barSpacing / 2)
                        val bar1Y = chartHeight - bar1Height

                        if (bar1Height > 0f) {
                            drawRoundRect(
                                color = darkColor,
                                topLeft = Offset(bar1X, bar1Y),
                                size = Size(barWidth, bar1Height),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }

                        // Bar 2: Created
                        val bar2Height = if (createdRatio > 0f) (chartHeight * createdRatio).coerceAtLeast(3.dp.toPx()) else 0f
                        val bar2X = groupCenterX + (barSpacing / 2)
                        val bar2Y = chartHeight - bar2Height

                        if (bar2Height > 0f) {
                            drawRoundRect(
                                color = softPurple,
                                topLeft = Offset(bar2X, bar2Y),
                                size = Size(barWidth, bar2Height),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }

                // X-Axis Date Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    dates.forEach { dateLabel ->
                        Text(
                            text = dateLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 3: Stacked Multi-Layered Area Chart connected to app tasks
// ==========================================
@Composable
private fun StackedAreaChartCard(stats: Map<String, StackedAreaData>) {
    var selectedFilter by remember { mutableStateOf("Weekly") }

    val chartData = stats[selectedFilter] ?: StackedAreaData(emptyList(), emptyList(), emptyList(), 0, 0)
    val backLayerPoints = chartData.backLayerPoints
    val frontLayerPoints = chartData.frontLayerPoints
    val labels = chartData.labels
    val activeLoadCount = chartData.activeLoadCount
    val efficiencyRate = chartData.efficiencyRate

    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task Overview Chart",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                TimeFilterPill(
                    selectedFilter = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Metric highlights inside card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassBox(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(text = "Active Load", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "$activeLoadCount Tasks", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }

                GlassBox(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(text = "Efficiency", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "$efficiencyRate%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val darkColor = MaterialTheme.colorScheme.onSurface
                val grayLayerColor = Color.LightGray.copy(alpha = 0.4f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 24.dp.toPx()
                    val chartHeight = height - bottomPadding
                    val stepX = width / (labels.size - 1).coerceAtLeast(1)

                    if (backLayerPoints.isNotEmpty() && frontLayerPoints.isNotEmpty()) {
                        // 1. Draw Back Light Gray Area Path (Cumulative Created)
                        val backPath = Path()
                        backPath.moveTo(0f, chartHeight)
                        backPath.lineTo(0f, chartHeight * (1 - backLayerPoints.first()))
                        for (i in 0 until backLayerPoints.size - 1) {
                            val p1 = Offset(i * stepX, chartHeight * (1 - backLayerPoints[i]))
                            val p2 = Offset((i + 1) * stepX, chartHeight * (1 - backLayerPoints[i + 1]))
                            val cx = (p1.x + p2.x) / 2
                            backPath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        }
                        backPath.lineTo(width, chartHeight)
                        backPath.close()

                        drawPath(
                            path = backPath,
                            color = grayLayerColor
                        )

                        // 2. Draw Front Dark Solid Area Path (Cumulative Completed)
                        val frontPath = Path()
                        frontPath.moveTo(0f, chartHeight)
                        frontPath.lineTo(0f, chartHeight * (1 - frontLayerPoints.first()))
                        for (i in 0 until frontLayerPoints.size - 1) {
                            val p1 = Offset(i * stepX, chartHeight * (1 - frontLayerPoints[i]))
                            val p2 = Offset((i + 1) * stepX, chartHeight * (1 - frontLayerPoints[i + 1]))
                            val cx = (p1.x + p2.x) / 2
                            frontPath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        }
                        frontPath.lineTo(width, chartHeight)
                        frontPath.close()

                        drawPath(
                            path = frontPath,
                            color = darkColor
                        )
                    }
                }

                // X-Axis Day Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// DYNAMIC TASK DATA CALCULATION HELPERS
// ==========================================

private data class LineChartData(
    val points: List<Float>,
    val labels: List<String>,
    val totalCompleted: Int
)

private data class GroupedBarData(
    val pairs: List<Pair<Float, Float>>,
    val dates: List<String>
)

private data class StackedAreaData(
    val backLayerPoints: List<Float>,
    val frontLayerPoints: List<Float>,
    val labels: List<String>,
    val activeLoadCount: Int,
    val efficiencyRate: Int
)

private data class PriorityDistribution(
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val total: Int
)

private class ConsolidatedStats(
    val lineChartByFilter: Map<String, LineChartData>,
    val groupedBarByFilter: Map<String, GroupedBarData>,
    val stackedAreaByFilter: Map<String, StackedAreaData>,
    val priorityDistribution: PriorityDistribution
)

private fun computeConsolidatedStats(allTasks: List<Task>): ConsolidatedStats {
    val validTasks = allTasks.filter { it.status != TaskStatus.DELETED }

    var highCount = 0
    var mediumCount = 0
    var lowCount = 0
    var activeCount = 0
    var totalCompleted = 0

    for (task in validTasks) {
        when (task.priority) {
            Priority.HIGH -> highCount++
            Priority.MEDIUM -> mediumCount++
            Priority.LOW -> lowCount++
        }
        when (task.status) {
            TaskStatus.ACTIVE -> activeCount++
            TaskStatus.COMPLETED -> totalCompleted++
            else -> {}
        }
    }

    val totalCreated = validTasks.size
    val totalPriority = totalCreated.coerceAtLeast(1)
    val efficiency = if (totalCreated > 0) (totalCompleted * 100) / totalCreated else 0

    val filters = listOf("Today", "Weekly", "Monthly")
    val lineMap = filters.associateWith { filter ->
        computeLineChartData(validTasks, filter)
    }
    val barMap = filters.associateWith { filter ->
        computeGroupedBarData(validTasks, filter)
    }
    val areaMap = filters.associateWith { filter ->
        computeStackedAreaData(validTasks, filter, activeCount, efficiency)
    }

    return ConsolidatedStats(
        lineChartByFilter = lineMap,
        groupedBarByFilter = barMap,
        stackedAreaByFilter = areaMap,
        priorityDistribution = PriorityDistribution(highCount, mediumCount, lowCount, totalPriority)
    )
}

private fun computeLineChartData(
    validTasks: List<Task>,
    filter: String
): LineChartData {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()
    val todayStr = sdf.format(calendar.time)

    when (filter) {
        "Today" -> {
            val labels = listOf("12A", "4A", "8A", "12P", "4P", "8P", "11P")
            val counts = MutableList(7) { 0 }
            val completedToday = validTasks.filter {
                it.status == TaskStatus.COMPLETED && (it.taskDate == todayStr || (it.completedAt != null && sdf.format(Date(it.completedAt)) == todayStr))
            }

            completedToday.forEach { task ->
                val taskCal = Calendar.getInstance()
                if (task.completedAt != null) {
                    taskCal.timeInMillis = task.completedAt
                }
                val hour = taskCal.get(Calendar.HOUR_OF_DAY)
                val index = (hour / 3.5f).toInt().coerceIn(0, 6)
                counts[index]++
            }

            val total = counts.sum()
            val maxCount = counts.maxOrNull() ?: 0
            val points = counts.map { count ->
                if (total == 0 || maxCount == 0) 0.0f else (0.1f + 0.85f * (count.toFloat() / maxCount.toFloat()))
            }
            return LineChartData(points, labels, total)
        }
        "Weekly" -> {
            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val labels = mutableListOf<String>()
            val counts = mutableListOf<Int>()

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)

            for (i in 0 until 7) {
                val dateStr = sdf.format(cal.time)
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                labels.add(dayNames[dayOfWeek.coerceIn(0, 6)])

                val dayCompleted = validTasks.count {
                    it.status == TaskStatus.COMPLETED && (it.taskDate == dateStr || (it.completedAt != null && sdf.format(Date(it.completedAt)) == dateStr))
                }
                counts.add(dayCompleted)
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val total = counts.sum()
            val maxCount = counts.maxOrNull() ?: 0
            val points = counts.map { count ->
                if (total == 0 || maxCount == 0) 0.0f else (0.1f + 0.85f * (count.toFloat() / maxCount.toFloat()))
            }
            return LineChartData(points, labels, total)
        }
        else -> { // "Monthly"
            val labels = listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7")
            val counts = MutableList(7) { 0 }

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -28)

            for (i in 0 until 28) {
                val dateStr = sdf.format(cal.time)
                val bucket = (i / 4).coerceIn(0, 6)
                val dayCompleted = validTasks.count {
                    it.status == TaskStatus.COMPLETED && (it.taskDate == dateStr || (it.completedAt != null && sdf.format(Date(it.completedAt)) == dateStr))
                }
                counts[bucket] += dayCompleted
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val total = counts.sum()
            val maxCount = counts.maxOrNull() ?: 0
            val points = counts.map { count ->
                if (total == 0 || maxCount == 0) 0.0f else (0.1f + 0.85f * (count.toFloat() / maxCount.toFloat()))
            }
            return LineChartData(points, labels, total)
        }
    }
}

private fun computeGroupedBarData(
    validTasks: List<Task>,
    filter: String
): GroupedBarData {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val shortSdf = SimpleDateFormat("MMM d", Locale.US)

    when (filter) {
        "Today" -> {
            val dates = listOf("Morn", "Aft", "Eve", "Night")
            val pairs = mutableListOf<Pair<Float, Float>>()
            val todayStr = sdf.format(Date())

            val todayTasks = validTasks.filter {
                it.taskDate == todayStr || (it.createdAt > 0 && sdf.format(Date(it.createdAt)) == todayStr)
            }

            val createdCounts = intArrayOf(0, 0, 0, 0)
            val completedCounts = intArrayOf(0, 0, 0, 0)

            todayTasks.forEach { task ->
                val cal = Calendar.getInstance()
                if (task.createdAt > 0) {
                    cal.timeInMillis = task.createdAt
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val idx = when (hour) {
                        in 5..11 -> 0
                        in 12..16 -> 1
                        in 17..20 -> 2
                        else -> 3
                    }
                    createdCounts[idx]++
                }
                if (task.status == TaskStatus.COMPLETED && task.completedAt != null) {
                    cal.timeInMillis = task.completedAt
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val idx = when (hour) {
                        in 5..11 -> 0
                        in 12..16 -> 1
                        in 17..20 -> 2
                        else -> 3
                    }
                    completedCounts[idx]++
                }
            }

            val maxVal = (createdCounts.maxOrNull() ?: 0).coerceAtLeast(completedCounts.maxOrNull() ?: 0).toFloat()
            for (i in 0 until 4) {
                val comp = completedCounts[i]
                val creat = createdCounts[i]
                val compR = if (comp == 0 || maxVal == 0f) 0f else (comp.toFloat() / maxVal).coerceIn(0.1f, 1f)
                val creatR = if (creat == 0 || maxVal == 0f) 0f else (creat.toFloat() / maxVal).coerceIn(0.1f, 1f)
                pairs.add(Pair(compR, creatR))
            }
            return GroupedBarData(pairs, dates)
        }
        "Weekly" -> {
            val dates = mutableListOf<String>()
            val counts = mutableListOf<Pair<Int, Int>>() // Pair(completed, created)

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -3)

            for (i in 0 until 4) {
                val dateStr = sdf.format(cal.time)
                dates.add(shortSdf.format(cal.time))

                val created = validTasks.count {
                    it.taskDate == dateStr || (it.createdAt > 0 && sdf.format(Date(it.createdAt)) == dateStr)
                }
                val completed = validTasks.count {
                    it.status == TaskStatus.COMPLETED && (it.taskDate == dateStr || (it.completedAt != null && sdf.format(Date(it.completedAt)) == dateStr))
                }

                counts.add(Pair(completed, created))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val maxVal = counts.flatMap { listOf(it.first, it.second) }.maxOrNull()?.toFloat() ?: 0f
            val pairs = counts.map { (comp, creat) ->
                Pair(
                    if (comp == 0 || maxVal == 0f) 0f else (comp.toFloat() / maxVal).coerceIn(0.1f, 1f),
                    if (creat == 0 || maxVal == 0f) 0f else (creat.toFloat() / maxVal).coerceIn(0.1f, 1f)
                )
            }
            return GroupedBarData(pairs, dates)
        }
        else -> { // "Monthly"
            val dates = listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4")
            val counts = mutableListOf<Pair<Int, Int>>()

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -27)

            for (w in 0 until 4) {
                var weekCreated = 0
                var weekCompleted = 0
                for (d in 0 until 7) {
                    val dateStr = sdf.format(cal.time)
                    weekCreated += validTasks.count {
                        it.taskDate == dateStr || (it.createdAt > 0 && sdf.format(Date(it.createdAt)) == dateStr)
                    }
                    weekCompleted += validTasks.count {
                        it.status == TaskStatus.COMPLETED && (it.taskDate == dateStr || (it.completedAt != null && sdf.format(Date(it.completedAt)) == dateStr))
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                counts.add(Pair(weekCompleted, weekCreated))
            }

            val maxVal = counts.flatMap { listOf(it.first, it.second) }.maxOrNull()?.toFloat() ?: 0f
            val pairs = counts.map { (comp, creat) ->
                Pair(
                    if (comp == 0 || maxVal == 0f) 0f else (comp.toFloat() / maxVal).coerceIn(0.1f, 1f),
                    if (creat == 0 || maxVal == 0f) 0f else (creat.toFloat() / maxVal).coerceIn(0.1f, 1f)
                )
            }
            return GroupedBarData(pairs, dates)
        }
    }
}

private fun computeStackedAreaData(
    validTasks: List<Task>,
    filter: String,
    activeCount: Int,
    efficiency: Int
): StackedAreaData {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()

    val labels: List<String>
    val backPoints = mutableListOf<Float>()
    val frontPoints = mutableListOf<Float>()

    when (filter) {
        "Today" -> {
            labels = listOf("12A", "4A", "8A", "12P", "4P", "8P", "11P")
            val todayStr = sdf.format(cal.time)
            val createdCounts = MutableList(7) { 0 }
            val completedCounts = MutableList(7) { 0 }

            val todayTasks = validTasks.filter {
                it.taskDate == todayStr || (it.createdAt > 0 && sdf.format(Date(it.createdAt)) == todayStr) || (it.completedAt != null && sdf.format(Date(it.completedAt)) == todayStr)
            }

            todayTasks.forEach { task ->
                val taskCal = Calendar.getInstance()
                if (task.createdAt > 0) {
                    taskCal.timeInMillis = task.createdAt
                    val hour = taskCal.get(Calendar.HOUR_OF_DAY)
                    val idx = (hour / 3.5f).toInt().coerceIn(0, 6)
                    createdCounts[idx]++
                }
                if (task.status == TaskStatus.COMPLETED && task.completedAt != null) {
                    taskCal.timeInMillis = task.completedAt
                    val hour = taskCal.get(Calendar.HOUR_OF_DAY)
                    val idx = (hour / 3.5f).toInt().coerceIn(0, 6)
                    completedCounts[idx]++
                }
            }

            var cumCreat = 0
            var cumComp = 0
            for (i in 0 until 7) {
                cumCreat += createdCounts[i]
                cumComp += completedCounts[i]
                backPoints.add(cumCreat.toFloat())
                frontPoints.add(cumComp.toFloat())
            }
        }
        "Weekly" -> {
            labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            cal.add(Calendar.DAY_OF_YEAR, -6)

            val weekLabels = mutableListOf<String>()
            var cumCreat = 0
            var cumComp = 0

            for (i in 0 until 7) {
                val dateStr = sdf.format(cal.time)
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                weekLabels.add(dayNames[dayOfWeek.coerceIn(0, 6)])

                val dayCreated = validTasks.count {
                    it.taskDate == dateStr || (it.createdAt > 0 && sdf.format(Date(it.createdAt)) == dateStr)
                }
                val dayCompleted = validTasks.count {
                    it.status == TaskStatus.COMPLETED && (it.taskDate == dateStr || (it.completedAt != null && sdf.format(Date(it.completedAt)) == dateStr))
                }

                cumCreat += dayCreated
                cumComp += dayCompleted
                backPoints.add(cumCreat.toFloat())
                frontPoints.add(cumComp.toFloat())
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            val maxValW = backPoints.maxOrNull() ?: 0f
            val normBackW = backPoints.map { if (it == 0f || maxValW == 0f) 0f else (0.15f + 0.8f * (it / maxValW)) }
            val normFrontW = frontPoints.map { if (it == 0f || maxValW == 0f) 0f else (0.08f + 0.75f * (it / maxValW)) }
            return StackedAreaData(normBackW, normFrontW, weekLabels, activeCount, efficiency)
        }
        else -> { // "Monthly"
            labels = listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7")
            cal.add(Calendar.DAY_OF_YEAR, -28)

            val createdBuckets = MutableList(7) { 0 }
            val completedBuckets = MutableList(7) { 0 }

            for (i in 0 until 28) {
                val dateStr = sdf.format(cal.time)
                val bucket = (i / 4).coerceIn(0, 6)
                createdBuckets[bucket] += validTasks.count {
                    it.taskDate == dateStr || (it.createdAt > 0 && sdf.format(Date(it.createdAt)) == dateStr)
                }
                completedBuckets[bucket] += validTasks.count {
                    it.status == TaskStatus.COMPLETED && (it.taskDate == dateStr || (it.completedAt != null && sdf.format(Date(it.completedAt)) == dateStr))
                }
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            var cumCreat = 0
            var cumComp = 0
            for (i in 0 until 7) {
                cumCreat += createdBuckets[i]
                cumComp += completedBuckets[i]
                backPoints.add(cumCreat.toFloat())
                frontPoints.add(cumComp.toFloat())
            }
        }
    }

    val maxVal = backPoints.maxOrNull() ?: 0f
    val normBack = backPoints.map { if (it == 0f || maxVal == 0f) 0f else (0.15f + 0.8f * (it / maxVal)) }
    val normFront = frontPoints.map { if (it == 0f || maxVal == 0f) 0f else (0.08f + 0.75f * (it / maxVal)) }

    return StackedAreaData(normBack, normFront, labels, activeCount, efficiency)
}

@Composable
private fun TimeFilterPill(
    selectedFilter: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Monthly", "Weekly", "Today").forEach { option ->
            val isSelected = selectedFilter == option
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.surface else TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun StatMiniHighlight(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun MetricBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    GlassBox(
        modifier = modifier,
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PriorityBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = AnimationTokens.DurationLong, easing = AnimationTokens.EaseOut),
        label = "PriorityBarFraction_$label"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(text = "$count tasks (${(fraction * 100).toInt()}%)", fontSize = 12.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

private data class ExpandedDayInfo(
    val dateStr: String,
    val count: Int,
    val isFuture: Boolean
)

private fun generateExpandedHeatmapData(heatmapData: Map<String, Int>): Pair<List<List<ExpandedDayInfo>>, List<String>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()
    val todayMillis = calendar.timeInMillis

    // Start 12 weeks ago (84 days) on Sunday
    calendar.add(Calendar.DAY_OF_YEAR, -83)
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }

    val weeks = mutableListOf<List<ExpandedDayInfo>>()
    for (w in 0 until 12) {
        val weekDays = mutableListOf<ExpandedDayInfo>()
        for (d in 0 until 7) {
            val dateStr = sdf.format(calendar.time)
            val isFuture = calendar.timeInMillis > todayMillis + 24 * 3600 * 1000L
            val count = heatmapData[dateStr] ?: 0
            weekDays.add(ExpandedDayInfo(dateStr, count, isFuture))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        weeks.add(weekDays)
    }

    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return Pair(weeks, dayLabels)
}
