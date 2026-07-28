package com.example.presentation.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.ui.animation.AnimationTokens
import com.example.ui.animation.bounceClick
import com.example.ui.animation.scratchCut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Priority
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.ui.components.GlassBox
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtil
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    modifier: Modifier = Modifier,
    onScroll: (Float) -> Unit = {}
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    val activeTasks = remember(tasks) { tasks.filter { it.status == TaskStatus.ACTIVE } }
    val completedTasks = remember(tasks) { tasks.filter { it.status == TaskStatus.COMPLETED } }

    var isCompletedSectionExpanded by remember { mutableStateOf(false) }
    val expandedNotes = remember { mutableStateMapOf<String, Boolean>() }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset.toFloat() }
            .collect { offset ->
                onScroll(offset)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 24.dp)
    ) {
        // Heading
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Today",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = DateUtil.formatDisplayDate(DateUtil.getLogicalToday()),
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (tasks.isNotEmpty()) {
            val totalCount = tasks.size
            val completedCount = completedTasks.size
            val targetProgress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
            val animatedProgress by animateFloatAsState(
                targetValue = targetProgress,
                animationSpec = tween(durationMillis = AnimationTokens.DurationLong, easing = AnimationTokens.EaseOut),
                label = "DailyProgress"
            )

            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                cornerRadius = 14.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Progress",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        AnimatedContent(
                            targetState = "$completedCount of $totalCount completed",
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(AnimationTokens.DurationShort)) togetherWith
                                        fadeOut(animationSpec = tween(AnimationTokens.DurationShort)))
                            },
                            label = "ProgressCounter"
                        ) { countText ->
                            Text(
                                text = countText,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nothing planned today.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enjoy your day.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Tasks
                items(activeTasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.animateItem()) {
                        TaskRow(
                            task = task,
                            isExpanded = expandedNotes[task.id] == true,
                            onToggleExpand = { expandedNotes[task.id] = !(expandedNotes[task.id] == true) },
                            onCheckChanged = { viewModel.completeTask(task.id) }
                        )
                    }
                }

                // Completed Section Header
                if (completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick { isCompletedSectionExpanded = !isCompletedSectionExpanded }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Completed (${completedTasks.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = if (isCompletedSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isCompletedSectionExpanded) "Collapse completed" else "Expand completed",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (isCompletedSectionExpanded) {
                        items(completedTasks, key = { it.id }) { task ->
                            Box(modifier = Modifier.animateItem()) {
                                CompletedTaskRow(
                                    task = task,
                                    onCheckChanged = { viewModel.restoreTask(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCheckChanged: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var isCompleting by remember(task.id) { mutableStateOf(false) }
    val scratchProgress = remember(task.id) { Animatable(0f) }
    val checkScale = remember(task.id) { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary

    val handleCheckClick = {
        if (!isCompleting) {
            isCompleting = true
            scope.launch {
                launch {
                    checkScale.animateTo(1.35f, animationSpec = tween(90, easing = AnimationTokens.EaseOut))
                    checkScale.animateTo(1.0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    scratchProgress.animateTo(1f, animationSpec = tween(220, easing = AnimationTokens.EaseOut))
                }
            }.invokeOnCompletion {
                onCheckChanged()
            }
        }
    }

    com.example.ui.components.GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("task_row_${task.id}"),
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick { onToggleExpand() }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { handleCheckClick() },
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = checkScale.value
                            scaleY = checkScale.value
                        }
                        .testTag("task_checkbox_${task.id}")
                ) {
                    Icon(
                        imageVector = if (isCompleting) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Complete task",
                        tint = if (isCompleting) primaryColor else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = task.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleting) TextSecondary else TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.scratchCut(
                                progress = scratchProgress.value,
                                strokeColor = primaryColor
                            )
                        )
                        if (task.priority != Priority.LOW) {
                            val badgeColor = when (task.priority) {
                                Priority.HIGH -> Color(0xFFFF5252)
                                Priority.MEDIUM -> Color(0xFFFFB300)
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(badgeColor, RoundedCornerShape(50))
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("task_delete_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete task",
                            tint = TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !task.notes.isNullOrEmpty(),
                enter = expandVertically(tween(AnimationTokens.DurationShort)) + fadeIn(tween(AnimationTokens.DurationShort)),
                exit = shrinkVertically(tween(AnimationTokens.DurationShort)) + fadeOut(tween(AnimationTokens.DurationShort))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = task.notes ?: "",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 44.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedTaskRow(
    task: Task,
    onCheckChanged: () -> Unit
) {
    com.example.ui.components.GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.6f)
            .testTag("task_row_completed_${task.id}"),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCheckChanged,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("task_restore_checkbox_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Restore task",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = task.title,
                fontSize = 16.sp,
                textDecoration = TextDecoration.LineThrough,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
