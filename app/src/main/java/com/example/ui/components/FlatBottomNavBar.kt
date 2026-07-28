package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.animation.AnimationTokens
import com.example.ui.animation.bounceClick
import com.example.ui.theme.FlatNavBarBlack
import com.example.ui.theme.FlatNavActiveWhite
import com.example.ui.theme.FlatNavInactiveGray

data class FlatNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val tag: String,
    val iconRes: Int? = null
)

val tskieNavItems = listOf(
    FlatNavItem("today", "Today", Icons.Default.Today, "nav_today", iconRes = R.drawable.ic_nav_today),
    FlatNavItem("calendar", "Calendar", Icons.Default.CalendarMonth, "nav_calendar", iconRes = R.drawable.ic_nav_calendar),
    FlatNavItem("tomorrow", "Tomorrow", Icons.Default.Event, "nav_tomorrow", iconRes = R.drawable.ic_nav_tomorrow)
)

@Composable
fun FlatBottomNavBar(
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FlatNavBarBlack)
            .navigationBarsPadding()
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tskieNavItems.forEach { item ->
                val isSelected = item.id == selectedTabId
                
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) FlatNavActiveWhite else FlatNavInactiveGray,
                    animationSpec = tween(
                        durationMillis = AnimationTokens.DurationShort,
                        easing = AnimationTokens.EaseOut
                    ),
                    label = "NavContentColor_${item.id}"
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = tween(
                        durationMillis = AnimationTokens.DurationShort,
                        easing = AnimationTokens.EaseOut
                    ),
                    label = "NavIconScale_${item.id}"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick { onTabSelected(item.id) }
                        .padding(vertical = 8.dp)
                        .testTag(item.tag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.iconRes != null) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
