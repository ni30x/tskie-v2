package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.example.widget.HeatmapWidgetProvider
import android.appwidget.AppWidgetManager
import android.content.ComponentName

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class WidgetTest {
    @Test
    fun testWidget() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        HeatmapWidgetProvider.updateAllWidgets(context)
        Thread.sleep(2000)
    }
}
