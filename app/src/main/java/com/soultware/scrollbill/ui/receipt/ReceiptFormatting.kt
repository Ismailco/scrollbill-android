package com.soultware.scrollbill.ui.receipt

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

fun formatReceiptDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis.coerceAtLeast(0L) / MILLIS_PER_MINUTE
    val hours = totalMinutes / MINUTES_PER_HOUR
    val minutes = totalMinutes % MINUTES_PER_HOUR

    return if (hours > 0L) {
        "${hours}h ${minutes.toString().padStart(2, '0')}m"
    } else {
        "${minutes}m"
    }
}

fun formatReceiptDateRange(startDate: LocalDate, endDateInclusive: LocalDate): String {
    val monthDay = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    val formattedStart = monthDay.format(startDate).uppercase(Locale.US)
    val formattedEnd = monthDay.format(endDateInclusive).uppercase(Locale.US)

    return if (startDate.year == endDateInclusive.year) {
        "$formattedStart - $formattedEnd, ${startDate.year}"
    } else {
        val withYear = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
        "${withYear.format(startDate).uppercase(Locale.US)} - " +
            withYear.format(endDateInclusive).uppercase(Locale.US)
    }
}

fun formatYearlyPace(projectedAnnualUsageMillis: Long): String {
    val days = (projectedAnnualUsageMillis.coerceAtLeast(0L).toDouble() / MILLIS_PER_DAY)
        .roundToLong()
    return "~$days days"
}

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MILLIS_PER_DAY = 86_400_000L
