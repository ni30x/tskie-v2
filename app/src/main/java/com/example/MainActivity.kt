package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.calendar.CalendarScreen
import com.example.presentation.calendar.CalendarViewModel
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.settings.SettingsViewModel
import com.example.presentation.statistics.StatisticsScreen
import com.example.presentation.today.TodayScreen
import com.example.presentation.today.TodayViewModel
import com.example.presentation.tomorrow.TomorrowScreen
import com.example.presentation.tomorrow.TomorrowViewModel
import com.example.reminder.ReminderManager
import com.example.ui.animation.AnimationTokens
import com.example.ui.components.FlatBottomNavBar
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val navTargetState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize single notification channel "TSKIE Notifications"
        ReminderManager.initNotificationChannel(this)
        ReminderManager.scheduleWorkManagerPeriodicCheck(this)
        
        com.example.widget.SafeWidgetUpdater.updateSafely(this)

        // Read initial intent target if app launched from notification or widget
        navTargetState.value = intent?.getStringExtra("navigation_target")

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            // Request POST_NOTIFICATIONS permission dynamically on API 33+
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    ReminderManager.rescheduleAllReminders(context)
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainContainer(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = !isDarkMode },
                    initialNavTarget = navTargetState.value
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("navigation_target")?.let { target ->
            navTargetState.value = target
        }
    }
}

@Composable
fun MainContainer(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    initialNavTarget: String? = null
) {
    var selectedBottomTab by remember {
        mutableStateOf(
            when (initialNavTarget?.lowercase()) {
                "tomorrow" -> "tomorrow"
                "today" -> "today"
                "calendar" -> "calendar"
                else -> "today"
            }
        )
    }

    LaunchedEffect(initialNavTarget) {
        if (!initialNavTarget.isNullOrEmpty()) {
            when (initialNavTarget.lowercase()) {
                "tomorrow" -> selectedBottomTab = "tomorrow"
                "today" -> selectedBottomTab = "today"
                "calendar" -> selectedBottomTab = "calendar"
            }
        }
    }

    // ViewModels
    val todayViewModel: TodayViewModel = viewModel()
    val tomorrowViewModel: TomorrowViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val tabOrder = remember { listOf("today", "calendar", "tomorrow", "statistics", "settings") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FlatBottomNavBar(
                selectedTabId = selectedBottomTab,
                onTabSelected = { tabId ->
                    selectedBottomTab = tabId
                }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedBottomTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            transitionSpec = {
                val initialIdx = tabOrder.indexOf(initialState).coerceAtLeast(0)
                val targetIdx = tabOrder.indexOf(targetState).coerceAtLeast(0)
                if (targetIdx >= initialIdx) {
                    (slideInHorizontally(
                        animationSpec = tween(AnimationTokens.DurationMedium, easing = AnimationTokens.EaseOut),
                        initialOffsetX = { (it * 0.10f).toInt() }
                    ) + fadeIn(animationSpec = tween(AnimationTokens.DurationShort)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(AnimationTokens.DurationMedium, easing = AnimationTokens.EaseOut),
                                targetOffsetX = { (-it * 0.08f).toInt() }
                            ) + fadeOut(animationSpec = tween(AnimationTokens.DurationShort))
                        )
                } else {
                    (slideInHorizontally(
                        animationSpec = tween(AnimationTokens.DurationMedium, easing = AnimationTokens.EaseOut),
                        initialOffsetX = { (-it * 0.10f).toInt() }
                    ) + fadeIn(animationSpec = tween(AnimationTokens.DurationShort)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(AnimationTokens.DurationMedium, easing = AnimationTokens.EaseOut),
                                targetOffsetX = { (it * 0.08f).toInt() }
                            ) + fadeOut(animationSpec = tween(AnimationTokens.DurationShort))
                        )
                }
            },
            label = "TSKIEScreenTransition"
        ) { bottomTab ->
            when (bottomTab) {
                "today" -> TodayScreen(
                    viewModel = todayViewModel
                )
                "calendar" -> CalendarScreen(
                    viewModel = calendarViewModel,
                    onNavigateToSettings = { selectedBottomTab = "settings" },
                    onNavigateToStatistics = { selectedBottomTab = "statistics" }
                )
                "statistics" -> StatisticsScreen(
                    viewModel = calendarViewModel,
                    onBack = { selectedBottomTab = "calendar" }
                )
                "tomorrow" -> TomorrowScreen(
                    viewModel = tomorrowViewModel,
                    settingsViewModel = settingsViewModel
                )
                "settings" -> SettingsScreen(
                    viewModel = settingsViewModel,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }
        }
    }
}
