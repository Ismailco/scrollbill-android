package com.soultware.scrollbill.domain.receipt

import java.time.LocalDate

data class ReceiptSnapshot(
    val startDate: LocalDate,
    val endDateInclusive: LocalDate,
    val totalUsageMinutes: Long,
    val dailyAverageMinutes: Long,
    val projectedAnnualDays: Long,
    val topApps: List<ReceiptAppEntry>,
    val otherAppsUsageMinutes: Long,
)

data class ReceiptAppEntry(
    val displayLabel: String,
    val durationMinutes: Long,
)
