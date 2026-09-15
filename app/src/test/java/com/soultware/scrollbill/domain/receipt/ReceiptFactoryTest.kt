package com.soultware.scrollbill.domain.receipt

import com.soultware.scrollbill.domain.model.AppUsage
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.domain.model.WeeklyUsageSummary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReceiptFactoryTest {
    private val summary = WeeklyUsageSummary(
        reportingPeriod = UsagePeriod(
            localStartDate = LocalDate.of(2026, 9, 8),
            localEndExclusiveDate = LocalDate.of(2026, 9, 15),
            zoneId = ZoneId.of("UTC"),
        ),
        totalForegroundDurationMillis = 23L * MILLIS_PER_HOUR,
        averageDailyDurationMillis = 3L * MILLIS_PER_HOUR,
        projectedAnnualDurationMillis = 1095L * MILLIS_PER_HOUR,
        rankedApplications = listOf(
            AppUsage("one", "One", 8L * MILLIS_PER_HOUR),
            AppUsage("two", "Two", 5L * MILLIS_PER_HOUR),
            AppUsage("three", "Three", 4L * MILLIS_PER_HOUR),
            AppUsage("four", "Four", 3L * MILLIS_PER_HOUR),
            AppUsage("five", "Five", 2L * MILLIS_PER_HOUR),
            AppUsage("six", "Six", 1L * MILLIS_PER_HOUR),
        ),
    )

    @Test
    fun `factory selects five ranked apps and reconciles other apps`() {
        val snapshot = ReceiptFactory.from(
            summary = summary,
            displayLabels = mapOf("one" to "Readable One"),
        )

        assertEquals(listOf("Readable One", "Two", "Three", "Four", "Five"), snapshot.topApps.map { it.displayLabel })
        assertEquals(1L * MILLIS_PER_HOUR, snapshot.otherAppsUsageMillis)
        assertEquals(summary.totalForegroundDurationMillis, snapshot.totalUsageMillis)
        assertEquals(summary.averageDailyDurationMillis, snapshot.dailyAverageMillis)
        assertEquals(summary.projectedAnnualDurationMillis, snapshot.projectedAnnualUsageMillis)
    }

    @Test
    fun `factory keeps fewer than five apps without adding empty rows`() {
        val shortSummary = summary.copy(rankedApplications = summary.rankedApplications.take(2))

        val snapshot = ReceiptFactory.from(shortSummary)

        assertEquals(2, snapshot.topApps.size)
        assertEquals(10L * MILLIS_PER_HOUR, snapshot.otherAppsUsageMillis)
    }

    @Test
    fun `other apps never becomes negative for malformed totals`() {
        val malformed = summary.copy(
            totalForegroundDurationMillis = 1L,
            averageDailyDurationMillis = -1L,
            projectedAnnualDurationMillis = -1L,
        )

        val snapshot = ReceiptFactory.from(malformed)

        assertEquals(0L, snapshot.otherAppsUsageMillis)
        assertEquals(0L, snapshot.dailyAverageMillis)
        assertEquals(0L, snapshot.projectedAnnualUsageMillis)
        assertEquals(1L, snapshot.totalUsageMillis)
    }

    @Test
    fun `receipt dates use the completed period boundaries`() {
        val snapshot = ReceiptFactory.from(summary)

        assertEquals(LocalDate.of(2026, 9, 8), snapshot.startDate)
        assertEquals(LocalDate.of(2026, 9, 14), snapshot.endDateInclusive)
    }

    private companion object {
        const val MILLIS_PER_HOUR = 60L * 60L * 1_000L
    }
}
