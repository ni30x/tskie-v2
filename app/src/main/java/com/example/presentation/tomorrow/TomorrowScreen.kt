package com.example.presentation.tomorrow

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import com.example.ui.animation.bounceClick
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Priority
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.presentation.today.TaskRow
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtil
import java.util.Calendar

@Composable
fun TomorrowScreen(
    viewModel: TomorrowViewModel,
    settingsViewModel: com.example.presentation.settings.SettingsViewModel,
    modifier: Modifier = Modifier,
    onScroll: (Float) -> Unit = {}
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val expandedNotes = remember { mutableStateMapOf<String, Boolean>() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    var isAddingTask by remember { mutableStateOf(false) }
    var saveAndCloseTrigger by remember { mutableIntStateOf(0) }
    var lastSavedTime by remember { mutableLongStateOf(0L) }

    fun requestAddTask() {
        if (System.currentTimeMillis() - lastSavedTime > 500L && !isAddingTask) {
            isAddingTask = true
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset.toFloat() }
            .collect { offset ->
                onScroll(offset)
            }
    }

    LaunchedEffect(isAddingTask) {
        if (isAddingTask) {
            kotlinx.coroutines.delay(100)
            runCatching {
                focusRequester.requestFocus()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isAddingTask) {
                    saveAndCloseTrigger++
                } else {
                    requestAddTask()
                }
            }
            .padding(horizontal = 24.dp)
    ) {
        // Heading
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tomorrow",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = DateUtil.formatDisplayDate(DateUtil.getLogicalTomorrow()),
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tasks.isEmpty() && !isAddingTask) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .testTag("tomorrow_empty_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.bounceClick { requestAddTask() }
                        ) {
                            Text(
                                text = "No tasks for tomorrow.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "+ Tap anywhere to form a task",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.animateItem()) {
                        TaskRow(
                            task = task,
                            isExpanded = expandedNotes[task.id] == true,
                            onToggleExpand = { expandedNotes[task.id] = !(expandedNotes[task.id] == true) },
                            onCheckChanged = { /* Read-only for Tomorrow */ },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }

            if (isAddingTask) {
                // Inline Task direct filing row sits directly at the end of the list where the task will stay!
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = androidx.compose.animation.expandVertically(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = com.example.ui.animation.AnimationTokens.DurationMedium,
                                easing = com.example.ui.animation.AnimationTokens.EaseOut
                            )
                        ) + androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = com.example.ui.animation.AnimationTokens.DurationShort
                            )
                        ) + androidx.compose.animation.scaleIn(
                            initialScale = com.example.ui.animation.AnimationTokens.EnterScale,
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = com.example.ui.animation.AnimationTokens.DurationMedium,
                                easing = com.example.ui.animation.AnimationTokens.EaseOut
                            )
                        )
                    ) {
                        InlineTaskInputRow(
                            priorityEnabled = settings.priorityEnabled,
                            defaultPriority = settings.defaultPriority,
                            focusRequester = focusRequester,
                            saveTrigger = saveAndCloseTrigger,
                            onSave = { title, notes, priority ->
                                lastSavedTime = System.currentTimeMillis()
                                viewModel.createTask(
                                    title = title,
                                    notes = notes,
                                    priority = priority,
                                    reminderTime = null,
                                    reminderEnabled = false
                                )
                            },
                            onClose = {
                                isAddingTask = false
                            }
                        )
                    }
                }
            } else if (tasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .bounceClick { requestAddTask() }
                            .testTag("add_tomorrow_task_button"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tap to form another task",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillParentMaxHeight(0.3f)
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { requestAddTask() }
                    )
                }
            }
        }
    }
}

@Composable
fun InlineTaskInputRow(
    priorityEnabled: Boolean,
    defaultPriority: Priority,
    focusRequester: FocusRequester,
    saveTrigger: Int = 0,
    onSave: (String, String?, Priority) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(defaultPriority) }
    var isFocused by remember { mutableStateOf(false) }

    fun handleSave() {
        if (title.trim().isNotEmpty()) {
            onSave(title.trim(), notes.ifEmpty { null }, selectedPriority)
            title = ""
            notes = ""
            selectedPriority = defaultPriority
            onClose()
        } else {
            onClose()
        }
    }

    LaunchedEffect(saveTrigger) {
        if (saveTrigger > 0) {
            handleSave()
        }
    }

    com.example.ui.components.GlassBox(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Intercept clicks on card so clicking inside input doesn't trigger screen tap
            }
            .testTag("inline_task_input_card"),
        cornerRadius = 12.dp,
        
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Task placeholder",
                    tint = TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = title,
                    onValueChange = { if (it.length <= 50) title = it },
                    placeholder = {
                        Text(
                            text = "Add a task for tomorrow...",
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 15.sp
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { handleSave() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (state.isFocused) {
                                isFocused = true
                            } else if (isFocused && title.isEmpty() && notes.isEmpty()) {
                                onClose()
                            }
                        }
                        .testTag("inline_task_title_input")
                )

                if (title.trim().isNotEmpty()) {
                    IconButton(
                        onClick = { handleSave() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("inline_task_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Save Task",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable bottom tray for notes and priority
            if (isFocused || title.isNotEmpty() || notes.isNotEmpty()) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Optional Notes Field
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = {
                        Text(
                            text = "Add notes...",
                            color = TextSecondary.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = TextPrimary,
                        fontSize = 13.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_task_notes_input")
                )

                // Priority Selection
                if (priorityEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority:",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Priority.values().forEach { priority ->
                            val isSelected = selectedPriority == priority
                            val priorityColor = when (priority) {
                                Priority.HIGH -> Color(0xFFFF5252)
                                Priority.MEDIUM -> Color(0xFFFFB300)
                                Priority.LOW -> Color(0xFF4CAF50)
                            }
                            val containerColor = if (isSelected) {
                                priorityColor.copy(alpha = 0.2f)
                            } else {
                                Color.White.copy(alpha = 0.03f)
                            }
                            val contentColor = if (isSelected) {
                                priorityColor
                            } else {
                                TextSecondary.copy(alpha = 0.8f)
                            }

                            Box(
                                modifier = Modifier
                                    .background(containerColor, RoundedCornerShape(8.dp))
                                    .bounceClick { selectedPriority = priority }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("inline_priority_button_${priority.name}")
                            ) {
                                Text(
                                    text = priority.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

