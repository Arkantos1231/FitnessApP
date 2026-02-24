package com.example.fitnessap.util

import java.util.Calendar
import java.util.TimeZone

fun millisUntilNextHourET(hour: Int): Long {
    val et = TimeZone.getTimeZone("America/New_York")
    val now = Calendar.getInstance(et)
    val target = Calendar.getInstance(et).apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (target.timeInMillis <= now.timeInMillis) {
        target.add(Calendar.DAY_OF_MONTH, 1)
    }
    return target.timeInMillis - now.timeInMillis
}
