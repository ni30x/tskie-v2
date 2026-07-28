package com.example.ui.animation

import androidx.compose.animation.core.CubicBezierEasing

object AnimationTokens {
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
    val EaseDrawer = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
    
    const val PressScale = 0.97f
    const val EnterScale = 0.95f
    
    const val DurationShort = 150
    const val DurationMedium = 250
    const val DurationLong = 400
}
