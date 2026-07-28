package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.example.widget.HeatmapWidgetRenderer
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WidgetRenderTest {
    @Test
    fun testWidgetRender() {
        HeatmapWidgetRenderer.renderHeatmapBitmap(
            heatmapData = mapOf(),
            isDarkMode = false
        )
    }
}
