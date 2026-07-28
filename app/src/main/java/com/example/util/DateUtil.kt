package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getLogicalToday(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour < 2) {
            // Before 2:00 AM, logical today is still yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun getLogicalTomorrow(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour < 2) {
            // Before 2:00 AM, logical tomorrow is calendar today
        } else {
            // After 2:00 AM, logical tomorrow is calendar tomorrow
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun getYesterdayStr(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour < 2) {
            cal.add(Calendar.DAY_OF_YEAR, -2)
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
            if (date != null) {
                SimpleDateFormat("EEE, MMM d", Locale.US).format(date)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun parseDateStr(dateStr: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
