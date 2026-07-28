package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import org.junit.Before

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MainActivityTest {

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun testMainActivityStarts() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
        assert(activity != null)
    }
}
