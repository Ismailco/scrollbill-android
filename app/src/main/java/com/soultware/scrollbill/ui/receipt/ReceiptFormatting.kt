package com.soultware.scrollbill.ui.receipt

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

fun formatReceiptDuration(minutes: Long): String {
    val totalMinutes = minutes.coerceAtLeast(0L)
    val hours = totalMinutes / MINUTES_PER_HOUR
    val remainingMinutes = totalMinutes % MINUTES_PER_HOUR

    return if (hours > 0L) {
        "${hours}h ${remainingMinutes.toString().padStart(2, '0')}m"
    } else {
        "${totalMinutes}m"
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

fun formatYearlyPace(projectedAnnualDays: Long): String = "~${projectedAnnualDays.coerceAtLeast(0L)} days"

internal fun ellipsizeToWidth(
    text: String,
    maxWidth: Float,
    measureText: (String) -> Float,
): String {
    if (maxWidth <= 0f) return ""
    if (measureText(text) <= maxWidth) return text

    val ellipsis = "…"
    val ellipsisWidth = measureText(ellipsis)
    if (ellipsisWidth > maxWidth) return ""
    val availableWidth = maxWidth - ellipsisWidth
    if (availableWidth <= 0f) return ellipsis

    var end = text.length
    while (end > 0 && measureText(text.substring(0, end)) > availableWidth) {
        end--
    }
    return text.substring(0, end).trimEnd() + ellipsis
}

private const val MINUTES_PER_HOUR = 60L
