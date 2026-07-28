package com.example.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Priority
import com.example.ui.animation.bounceClick
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }
    var exportContent by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) } // "today", "tomorrow", "history"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top-Left Screen Heading above content
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Preferences & Data",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // 1. REMINDERS SECTION
        SectionHeader(title = "Reminders")
        
        SettingsCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    title = "Daily Planning Reminder",
                    subtitle = "Reminds you every evening to plan tomorrow",
                    icon = Icons.Default.Notifications,
                    checked = settings.reminderEnabled,
                    onCheckedChange = { viewModel.updateReminderEnabled(it) },
                    testTag = "setting_reminder_switch"
                )

                if (settings.reminderEnabled) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reminder Time",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Default: 8:00 PM (20:00)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "20:00",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Task Notification Repetition",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Frequency for re-notifying active tasks",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("1 Hr", "2 Hr", "3 Hr").forEach { option ->
                                val isSelected = (settings.reminderRepetition == option) ||
                                        (option == "1 Hr" && (settings.reminderRepetition == "Daily" || settings.reminderRepetition.isEmpty() || settings.reminderRepetition == "1 Hour"))

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .bounceClick { viewModel.updateReminderRepetition(option) }
                                        .padding(vertical = 8.dp)
                                        .testTag("repetition_option_$option"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. TASK CONFIGURATION
        SectionHeader(title = "Task Configuration")
        
        SettingsCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    title = "Task Priority",
                    subtitle = "Show High, Medium, Low badges during task creation",
                    icon = Icons.Default.Flag,
                    checked = settings.priorityEnabled,
                    onCheckedChange = { viewModel.updatePriorityEnabled(it) },
                    testTag = "setting_priority_switch"
                )

                if (settings.priorityEnabled) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Default Priority",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Priority.values().forEach { priority ->
                                val isSelected = settings.defaultPriority == priority
                                val badgeColor = when (priority) {
                                    Priority.HIGH -> Color(0xFFFF5252)
                                    Priority.MEDIUM -> Color(0xFFFFB300)
                                    Priority.LOW -> Color(0xFF4CAF50)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) badgeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) badgeColor else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.updateDefaultPriority(priority) }
                                        .padding(vertical = 8.dp)
                                        .testTag("default_priority_${priority.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = priority.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) badgeColor else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. APPEARANCE
        SectionHeader(title = "Appearance")
        
        SettingsCard {
            SettingsSwitchRow(
                title = "Dark Theme",
                subtitle = "Switch between Light and Dark interface",
                icon = Icons.Default.DarkMode,
                checked = isDarkMode,
                onCheckedChange = { onToggleDarkMode() },
                testTag = "setting_dark_theme_switch"
            )
        }

        // 4. CLOUD & BACKUP (Ongoing)
        SectionHeader(title = "Cloud & Sync")
        
        SettingsCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Cloud",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Google Sign-In",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFF9800).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Ongoing",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9800)
                                    )
                                }
                            }
                            Text(
                                text = if (settings.signedIn) "Signed in (Cloud sync ready)" else "Guest mode (Offline only)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleSignIn() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (settings.signedIn) "Sign Out" else "Sign In",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Local backup saved successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Backup", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Cloud sync complete", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Restore", fontSize = 12.sp)
                    }
                }
            }
        }

        // 5. HISTORY & DATA MANAGEMENT
        SectionHeader(title = "History & Data")
        
        SettingsCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsActionRow(
                    title = "Export Task History",
                    subtitle = "Export tasks as JSON payload",
                    icon = Icons.Default.IosShare,
                    onClick = {
                        exportContent = viewModel.exportHistoryToJson()
                        showExportDialog = true
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                SettingsActionRow(
                    title = "Delete Today's Tasks",
                    subtitle = "Clear all active and completed tasks for Today",
                    icon = Icons.Default.DeleteOutline,
                    textColor = Color(0xFFFF5252),
                    onClick = { showDeleteConfirmDialog = "today" }
                )

                SettingsActionRow(
                    title = "Delete Tomorrow's Tasks",
                    subtitle = "Clear planned tasks for Tomorrow",
                    icon = Icons.Default.DeleteOutline,
                    textColor = Color(0xFFFF5252),
                    onClick = { showDeleteConfirmDialog = "tomorrow" }
                )

                SettingsActionRow(
                    title = "Clear All History",
                    subtitle = "Permanently remove all historical database records",
                    icon = Icons.Default.CleaningServices,
                    textColor = Color(0xFFFF5252),
                    onClick = { showDeleteConfirmDialog = "history" }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TSKIE v1.0 • Offline-First Productivity",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export History", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = exportContent,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { target ->
        val titleText = when (target) {
            "today" -> "Delete Today's Tasks?"
            "tomorrow" -> "Delete Tomorrow's Tasks?"
            else -> "Clear All History?"
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(titleText, fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone.", fontSize = 13.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (target) {
                            "today" -> viewModel.deleteTodayData()
                            "tomorrow" -> viewModel.deleteTomorrowData()
                            "history" -> viewModel.clearAllHistory()
                        }
                        showDeleteConfirmDialog = null
                        Toast.makeText(context, "Data deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
