package com.example.ui.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.bounceClick(
    interactionSource: MutableInteractionSource? = null,
    indication: androidx.compose.foundation.Indication? = null,
    useIndication: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualInteractionSource.collectIsPressedAsState()
    val actualIndication = indication ?: if (useIndication) androidx.compose.foundation.LocalIndication.current else null
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) AnimationTokens.PressScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "BounceClickScale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = actualInteractionSource,
            indication = actualIndication,
            onClick = onClick
        )
}

