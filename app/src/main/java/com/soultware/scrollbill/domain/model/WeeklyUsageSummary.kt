package com.soultware.scrollbill.domain.model

data class WeeklyUsageSummary(
    val reportingPeriod: UsagePeriod,
    val totalForegroundDurationMillis: Long,
    val averageDailyDurationMillis: Long,
    val projectedAnnualDurationMillis: Long,
    val rankedApplications: List<AppUsage>,
) {
    val highestUsageApplication: AppUsage?
        get() = rankedApplications.firstOrNull()
}
