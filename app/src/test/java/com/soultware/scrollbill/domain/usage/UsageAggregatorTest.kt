package com.soultware.scrollbill.domain.usage

import com.soultware.scrollbill.domain.model.UsagePeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class UsageAggregatorTest {
    private val period = UsagePeriod(
        localStartDate = LocalDate.of(2026, 9, 8),
        localEndExclusiveDate = LocalDate.of(2026, 9, 15),
        zoneId = ZoneId.of("UTC"),
    )
    private val aggregator = UsageAggregator()

    @Test
    fun `multiple records for one package are aggregated and ranked`() {
        val summary = aggregator.summarize(
            period,
            listOf(
                UsageInputRecord("chat", 10_000L),
                UsageInputRecord("video", 40_000L),
                UsageInputRecord("chat", 20_000L),
            ),
            exclusionPolicy = UsageExclusionPolicy.forApplication("scrollbill", homePackageName = null),
        )

        assertEquals(listOf("video", "chat"), summary.rankedApplications.map { it.packageName })
        assertEquals(30_000L, summary.rankedApplications.last().foregroundDurationMillis)
        assertEquals(70_000L, summary.totalForegroundDurationMillis)
    }

    @Test
    fun `summary calculates average and annual projection from seven days`() {
        val summary = aggregator.summarize(
            period,
            listOf(UsageInputRecord("video", 7L * 60L * 60L * 1_000L)),
            exclusionPolicy = UsageExclusionPolicy.forApplication("scrollbill", homePackageName = null),
        )

        assertEquals(60L * 60L * 1_000L, summary.averageDailyDurationMillis)
        assertEquals(365L * 60L * 60L * 1_000L, summary.projectedAnnualDurationMillis)
    }

    @Test
    fun `empty and zero duration records produce an empty summary`() {
        val summary = aggregator.summarize(
            period,
            listOf(
                UsageInputRecord("zero", 0L),
                UsageInputRecord("negative", -1L),
            ),
            exclusionPolicy = UsageExclusionPolicy.forApplication("scrollbill", homePackageName = null),
        )

        assertEquals(0L, summary.totalForegroundDurationMillis)
        assertEquals(0L, summary.averageDailyDurationMillis)
        assertEquals(0L, summary.projectedAnnualDurationMillis)
        assertEquals(emptyList<Any>(), summary.rankedApplications)
        assertNull(summary.highestUsageApplication)
    }

    @Test
    fun `own package and android infrastructure are excluded`() {
        val summary = aggregator.summarize(
            period,
            listOf(
                UsageInputRecord("scrollbill", 100L),
                UsageInputRecord("android", 200L),
                UsageInputRecord("visible.app", 300L),
            ),
            exclusionPolicy = UsageExclusionPolicy.forApplication("scrollbill", homePackageName = null),
        )

        assertEquals(listOf("visible.app"), summary.rankedApplications.map { it.packageName })
    }

    @Test
    fun `dynamically supplied home and system ui packages are excluded from all metrics`() {
        val summary = aggregator.summarize(
            period,
            listOf(
                UsageInputRecord("com.example.home", 10L * 60L * 60L * 1_000L),
                UsageInputRecord("com.android.systemui", 2L * 60L * 60L * 1_000L),
                UsageInputRecord("normal.app", 7L * 60L * 60L * 1_000L),
            ),
            exclusionPolicy = UsageExclusionPolicy.forApplication(
                ownPackageName = "scrollbill",
                homePackageName = "com.example.home",
            ),
        )

        assertEquals(listOf("normal.app"), summary.rankedApplications.map { it.packageName })
        assertEquals(7L * 60L * 60L * 1_000L, summary.totalForegroundDurationMillis)
        assertEquals(60L * 60L * 1_000L, summary.averageDailyDurationMillis)
        assertEquals(365L * 60L * 60L * 1_000L, summary.projectedAnnualDurationMillis)
        assertEquals("normal.app", summary.highestUsageApplication?.packageName)
    }
}
