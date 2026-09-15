package com.soultware.scrollbill.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.domain.model.WeeklyUsageSummary
import com.soultware.scrollbill.domain.usage.UsageAggregator
import com.soultware.scrollbill.domain.usage.UsageInputRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UsageStatsRepository {
    suspend fun loadWeeklySummary(period: UsagePeriod): WeeklyUsageSummary
}

class UsageStatsAccessException(cause: SecurityException) : Exception(cause)

class AndroidUsageStatsRepository(
    context: Context,
    private val exclusionPolicyProvider: UsageExclusionPolicyProvider,
    private val aggregator: UsageAggregator = UsageAggregator(),
) : UsageStatsRepository {
    private val usageStatsManager =
        context.getSystemService(UsageStatsManager::class.java)

    override suspend fun loadWeeklySummary(period: UsagePeriod): WeeklyUsageSummary =
        withContext(Dispatchers.IO) {
            val records = try {
                period.days().flatMap { day ->
                    usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        day.startEpochMillis,
                        day.endEpochMillis,
                    ).orEmpty().map { usageStats ->
                        UsageInputRecord(
                            packageName = usageStats.packageName,
                            foregroundDurationMillis = usageStats.totalTimeInForeground,
                        )
                    }
                }
            } catch (exception: SecurityException) {
                throw UsageStatsAccessException(exception)
            }

            aggregator.summarize(
                period = period,
                records = records,
                exclusionPolicy = exclusionPolicyProvider.currentPolicy(),
            )
        }
}
