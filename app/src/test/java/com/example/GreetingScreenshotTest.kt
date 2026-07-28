package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Priority
import com.example.domain.model.Task
import com.example.domain.model.TaskStatus
import com.example.domain.model.SyncState
import com.example.presentation.today.TaskRow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureBlack
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PureBlack)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "TSKIE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Demo active task row
                    TaskRow(
                        task = Task(
                            id = "demo-1",
                            title = "Design System Sync",
                            notes = "Discuss the new elevation tokens and glassmorphism implementation for mobile views. Ensure standard margins are documented.",
                            priority = Priority.MEDIUM,
                            reminderTime = null,
                            reminderEnabled = false,
                            status = TaskStatus.ACTIVE,
                            taskDate = "2026-07-11",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            completedAt = null,
                            syncState = SyncState.LOCAL_ONLY
                        ),
                        isExpanded = true,
                        onToggleExpand = {},
                        onCheckChanged = {},
                        onDelete = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
