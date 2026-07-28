package com.example.ui.animation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier that draws an animated hand-cut / scratch-out strike line across content
 * from left to right as [progress] animates from 0f to 1f.
 */
fun Modifier.scratchCut(
    progress: Float,
    strokeColor: Color,
    strokeWidthDp: Dp = 2.5.dp
): Modifier = this.drawWithContent {
    drawContent()
    if (progress > 0f) {
        val strokeWidthPx = strokeWidthDp.toPx()
        val currentX = size.width * progress
        val startY = size.height * 0.52f
        val endY = size.height * 0.46f

        // Primary organic scratch stroke
        val path = Path().apply {
            moveTo(-2.dp.toPx(), startY)
            val midX = currentX * 0.5f
            val midY = (startY + endY) / 2f + 1.2.dp.toPx()
            quadraticTo(midX, midY, currentX, startY + (endY - startY) * progress)
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Secondary subtle cut line for scratch accent feel
        if (progress > 0.25f) {
            val subProgress = ((progress - 0.25f) / 0.75f).coerceIn(0f, 1f)
            val subPath = Path().apply {
                moveTo(size.width * 0.05f, startY + 2.5.dp.toPx())
                lineTo(size.width * 0.05f + (size.width * 0.9f * subProgress), endY + 2.5.dp.toPx())
            }
            drawPath(
                path = subPath,
                color = strokeColor.copy(alpha = 0.5f),
                style = Stroke(
                    width = strokeWidthPx * 0.55f,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
