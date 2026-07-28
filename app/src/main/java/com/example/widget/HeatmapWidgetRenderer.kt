package com.example.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.presentation.calendar.generateHeatmapWeeksForMonths
import java.util.Calendar

object HeatmapWidgetRenderer {

    /**
     * Renders a clean Heatmap Bitmap adaptively based on the phone's current Light/Dark theme mode.
     */
    fun renderHeatmapBitmap(
        widthPx: Int = 800,
        heightPx: Int = 300,
        heatmapData: Map<String, Int>,
        isDarkMode: Boolean = true,
        totalCompleted: Int = 0,
        totalCreated: Int = 0
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.TRANSPARENT)

        // Generate heatmap weeks matching CalendarScreen (2 months)
        val displayedCal = Calendar.getInstance()
        val weeks = generateHeatmapWeeksForMonths(displayedCal, monthsCount = 2)

        // Tight vertical padding to eliminate extra gaps around top and bottom pills
        val paddingLeft = 16f
        val paddingRight = 16f
        val paddingTop = 10f
        val paddingBottom = 10f

        val gridLeft = paddingLeft + 36f
        val gridTop = paddingTop
        val gridWidth = widthPx - gridLeft - paddingRight
        val gridHeight = heightPx - paddingTop - paddingBottom

        val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

        val textColorHex = if (isDarkMode) "#94A3B8" else "#64748B"
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(textColorHex)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Available height per row (7 days) with tight spacing
        val rowSpacing = 5f
        val cellHeight = (gridHeight - (6 * rowSpacing)) / 7f

        // Draw day labels
        for (i in daysOfWeek.indices) {
            val labelY = gridTop + (i * (cellHeight + rowSpacing)) + (cellHeight * 0.72f)
            canvas.drawText(daysOfWeek[i], paddingLeft + 4f, labelY, labelPaint)
        }

        val numWeeks = weeks.size.coerceAtLeast(1)
        val colSpacing = 6f
        val cellWidth = (gridWidth - ((numWeeks - 1) * colSpacing)) / numWeeks.toFloat()

        // Cell Paint setup
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val emptyBorderColorHex = "#CBD5E1"
        val cellBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            color = Color.parseColor(emptyBorderColorHex)
        }

        val cornerRadius = cellHeight / 2.5f

        // Draw Heatmap Matrix
        weeks.forEachIndexed { weekIdx, week ->
            val cellLeft = gridLeft + (weekIdx * (cellWidth + colSpacing))

            week.days.forEach { day ->
                val dayIdx = day.dayOfWeek // 0..6
                val cellTop = gridTop + (dayIdx * (cellHeight + rowSpacing))
                val rect = RectF(cellLeft, cellTop, cellLeft + cellWidth, cellTop + cellHeight)

                val count = heatmapData[day.dateStr] ?: 0
                val cellColorHex = getHeatmapCellHex(count, day.isFuture)

                cellPaint.color = Color.parseColor(cellColorHex)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint)

                if (count == 0 || day.isFuture) {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellBorderPaint)
                }
            }
        }

        return bitmap
    }

    private fun getHeatmapCellHex(count: Int, isFuture: Boolean): String {
        if (isFuture) return "#FAFAFA"
        return when (count) {
            0 -> "#FAFAFA"
            1 -> "#B0B7C0"
            2 -> "#6C757D"
            3, 4 -> "#343A40"
            else -> "#121417"
        }
    }
}
