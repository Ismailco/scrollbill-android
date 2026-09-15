package com.soultware.scrollbill.domain.receipt

import com.soultware.scrollbill.domain.model.WeeklyUsageSummary
import kotlin.math.roundToLong

object ReceiptFactory {
    fun from(
        summary: WeeklyUsageSummary,
        displayLabels: Map<String, String> = emptyMap(),
    ): ReceiptSnapshot {
        val totalUsageMinutes = summary.totalForegroundDurationMillis.toDisplayMinutes()
        var remainingMinutes = totalUsageMinutes
        val topApps = buildList {
            summary.rankedApplications
                .asSequence()
                .take(MAX_TOP_APPS)
                .forEach { app ->
                    val durationMinutes = app.foregroundDurationMillis
                        .toDisplayMinutes()
                        .coerceAtMost(remainingMinutes)
                    if (durationMinutes > 0L) {
                        add(
                            ReceiptAppEntry(
                                displayLabel = displayLabels[app.packageName]
                                    ?.takeIf { it.isNotBlank() }
                                    ?: app.displayLabel.ifBlank { app.packageName },
                                durationMinutes = durationMinutes,
                            ),
                        )
                        remainingMinutes -= durationMinutes
                    }
                }
        }
        val topAppsUsageMinutes = topApps.sumOf { it.durationMinutes }

        return ReceiptSnapshot(
            startDate = summary.reportingPeriod.localStartDate,
            endDateInclusive = summary.reportingPeriod.localEndExclusiveDate.minusDays(1),
            totalUsageMinutes = totalUsageMinutes,
            dailyAverageMinutes = summary.averageDailyDurationMillis.toDisplayMinutes(),
            projectedAnnualDays = summary.projectedAnnualDurationMillis.toDaysRounded(),
            topApps = topApps,
            otherAppsUsageMinutes = (totalUsageMinutes - topAppsUsageMinutes).coerceAtLeast(0L),
        )
    }

    private fun Long.toDisplayMinutes(): Long = coerceAtLeast(0L) / MILLIS_PER_MINUTE

    private fun Long.toDaysRounded(): Long =
        (coerceAtLeast(0L).toDouble() / MILLIS_PER_DAY).roundToLong()

    private const val MAX_TOP_APPS = 5
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MILLIS_PER_DAY = 86_400_000L
}
