package com.example
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.junit.Test
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase

@RunWith(RobolectricTestRunner::class)
class DbTest {
    @Test
    fun testDb() {
        AppDatabase.getDatabase(ApplicationProvider.getApplicationContext())
    }
}
