package com.soultware.scrollbill.ui

import kotlin.math.roundToInt

fun formatDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis.coerceAtLeast(0L) / MILLIS_PER_MINUTE)
    val hours = totalMinutes / MINUTES_PER_HOUR
    val minutes = totalMinutes % MINUTES_PER_HOUR

    return when {
        hours > 0L && minutes > 0L -> "${hours}h ${minutes}m"
        hours > 0L -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatProjectedAnnualUsage(durationMillis: Long): String {
    val days = (durationMillis.coerceAtLeast(0L).toDouble() / MILLIS_PER_DAY).roundToInt()
    return "≈ $days days/year"
}

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MILLIS_PER_DAY = 86_400_000L
