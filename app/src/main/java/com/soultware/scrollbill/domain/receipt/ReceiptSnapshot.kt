package com.soultware.scrollbill.domain.receipt

import java.time.LocalDate

data class ReceiptSnapshot(
    val startDate: LocalDate,
    val endDateInclusive: LocalDate,
    val totalUsageMillis: Long,
    val dailyAverageMillis: Long,
    val projectedAnnualUsageMillis: Long,
    val topApps: List<ReceiptAppEntry>,
    val otherAppsUsageMillis: Long,
)

data class ReceiptAppEntry(
    val displayLabel: String,
    val durationMillis: Long,
)
