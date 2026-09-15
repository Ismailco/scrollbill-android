package com.soultware.scrollbill.domain.receipt

import com.soultware.scrollbill.domain.model.WeeklyUsageSummary

object ReceiptFactory {
    fun from(
        summary: WeeklyUsageSummary,
        displayLabels: Map<String, String> = emptyMap(),
    ): ReceiptSnapshot {
        val totalUsage = summary.totalForegroundDurationMillis.coerceAtLeast(0L)
        val topApps = summary.rankedApplications
            .asSequence()
            .take(MAX_TOP_APPS)
            .map { app ->
                ReceiptAppEntry(
                    displayLabel = displayLabels[app.packageName]
                        ?.takeIf { it.isNotBlank() }
                        ?: app.displayLabel.ifBlank { app.packageName },
                    durationMillis = app.foregroundDurationMillis.coerceAtLeast(0L),
                )
            }
            .toList()
        val topAppsUsage = topApps.sumOf { it.durationMillis }

        return ReceiptSnapshot(
            startDate = summary.reportingPeriod.localStartDate,
            endDateInclusive = summary.reportingPeriod.localEndExclusiveDate.minusDays(1),
            totalUsageMillis = totalUsage,
            dailyAverageMillis = summary.averageDailyDurationMillis.coerceAtLeast(0L),
            projectedAnnualUsageMillis = summary.projectedAnnualDurationMillis.coerceAtLeast(0L),
            topApps = topApps,
            otherAppsUsageMillis = (totalUsage - topAppsUsage).coerceAtLeast(0L),
        )
    }

    private const val MAX_TOP_APPS = 5
}
