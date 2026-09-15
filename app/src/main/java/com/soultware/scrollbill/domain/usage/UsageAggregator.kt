package com.soultware.scrollbill.domain.usage

import com.soultware.scrollbill.domain.model.AppUsage
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.domain.model.WeeklyUsageSummary

data class UsageInputRecord(
    val packageName: String,
    val foregroundDurationMillis: Long,
)

class UsageAggregator {
    fun summarize(
        period: UsagePeriod,
        records: Iterable<UsageInputRecord>,
        ownPackageName: String,
    ): WeeklyUsageSummary {
        val durationsByPackage = records
            .asSequence()
            .filter { it.packageName != ownPackageName }
            .filter { it.packageName != ANDROID_PACKAGE }
            .filter { it.foregroundDurationMillis > 0L }
            .groupingBy { it.packageName }
            .fold(0L) { total, record -> total + record.foregroundDurationMillis }

        val rankedApplications = durationsByPackage
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Long>> { it.value }.thenBy { it.key })
            .map { (packageName, duration) ->
                AppUsage(
                    packageName = packageName,
                    displayLabel = packageName,
                    foregroundDurationMillis = duration,
                )
            }

        val total = rankedApplications.sumOf { it.foregroundDurationMillis }
        val averageDaily = total / period.dayCount
        val projectedAnnual = (total.toDouble() / period.dayCount * DAYS_PER_YEAR).toLong()

        return WeeklyUsageSummary(
            reportingPeriod = period,
            totalForegroundDurationMillis = total,
            averageDailyDurationMillis = averageDaily,
            projectedAnnualDurationMillis = projectedAnnual,
            rankedApplications = rankedApplications,
        )
    }

    companion object {
        private const val ANDROID_PACKAGE = "android"
        private const val DAYS_PER_YEAR = 365
    }
}
