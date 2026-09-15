package com.soultware.scrollbill.domain.receipt

import com.soultware.scrollbill.domain.model.AppUsage
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.domain.model.WeeklyUsageSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReceiptFactoryTest {
    @Test
    fun `real device shaped fixture reconciles displayed minutes`() {
        val summary = summary(
            totalMillis = minutes(1_474) + 50_000L,
            apps = listOf(
                app("Instagram", minutes(527) + 10_000L),
                app("YouTube", minutes(181) + 10_000L),
                app("LinkedIn", minutes(129) + 10_000L),
                app("Gmail", minutes(62) + 10_000L),
                app("Vivaldi", minutes(58) + 10_000L),
                app("Other source app", minutes(517)),
            ),
        )

        val snapshot = ReceiptFactory.from(summary)

        assertEquals(listOf(527L, 181L, 129L, 62L, 58L), snapshot.topApps.map { it.durationMinutes })
        assertEquals(517L, snapshot.otherAppsUsageMinutes)
        assertEquals(1_474L, snapshot.totalUsageMinutes)
        assertEquals(snapshot.totalUsageMinutes, snapshot.topApps.sumOf { it.durationMinutes } + snapshot.otherAppsUsageMinutes)
    }

    @Test
    fun `exact minute durations reconcile without precision loss`() {
        val snapshot = ReceiptFactory.from(
            summary(
                totalMillis = minutes(20),
                apps = listOf(app("One", minutes(8)), app("Two", minutes(7))),
            ),
        )

        assertEquals(5L, snapshot.otherAppsUsageMinutes)
        assertEquals(snapshot.totalUsageMinutes, snapshot.topApps.sumOf { it.durationMinutes } + snapshot.otherAppsUsageMinutes)
    }

    @Test
    fun `discarded seconds are reconciled at the displayed minute level`() {
        val snapshot = ReceiptFactory.from(
            summary(
                totalMillis = minutes(9) + 40_000L,
                apps = listOf(
                    app("One", minutes(5) + 20_000L),
                    app("Two", minutes(4) + 20_000L),
                ),
            ),
        )

        assertEquals(9L, snapshot.totalUsageMinutes)
        assertEquals(listOf(5L, 4L), snapshot.topApps.map { it.durationMinutes })
        assertEquals(0L, snapshot.otherAppsUsageMinutes)
        assertEquals(snapshot.totalUsageMinutes, snapshot.topApps.sumOf { it.durationMinutes } + snapshot.otherAppsUsageMinutes)
    }

    @Test
    fun `several top apps with discarded seconds still reconcile`() {
        val snapshot = ReceiptFactory.from(
            summary(
                totalMillis = minutes(15) + 50_000L,
                apps = (1..5).map { index -> app("App $index", minutes(2) + 5_000L) } +
                    app("Other source app", minutes(5) + 25_000L),
            ),
        )

        assertEquals(5, snapshot.topApps.size)
        assertEquals(5L, snapshot.otherAppsUsageMinutes)
        assertEquals(snapshot.totalUsageMinutes, snapshot.topApps.sumOf { it.durationMinutes } + snapshot.otherAppsUsageMinutes)
    }

    @Test
    fun `fewer than five apps and zero other apps are represented correctly`() {
        val snapshot = ReceiptFactory.from(
            summary(
                totalMillis = minutes(15),
                apps = listOf(app("One", minutes(8)), app("Two", minutes(7))),
            ),
        )

        assertEquals(2, snapshot.topApps.size)
        assertEquals(0L, snapshot.otherAppsUsageMinutes)
    }

    @Test
    fun `other apps cannot become negative for malformed totals`() {
        val snapshot = ReceiptFactory.from(
            summary(
                totalMillis = minutes(1),
                apps = listOf(app("Too large", minutes(2))),
                averageMillis = -1L,
                projectedMillis = -1L,
            ),
        )

        assertEquals(1L, snapshot.topApps.single().durationMinutes)
        assertEquals(0L, snapshot.otherAppsUsageMinutes)
        assertEquals(snapshot.totalUsageMinutes, snapshot.topApps.sumOf { it.durationMinutes } + snapshot.otherAppsUsageMinutes)
        assertEquals(0L, snapshot.dailyAverageMinutes)
        assertEquals(0L, snapshot.projectedAnnualDays)
    }

    @Test
    fun `sub-minute total displays as zero minutes without a negative remainder`() {
        val snapshot = ReceiptFactory.from(
            summary(totalMillis = 59_999L, apps = listOf(app("Tiny", 30_000L))),
        )

        assertEquals(0L, snapshot.totalUsageMinutes)
        assertTrue(snapshot.topApps.isEmpty())
        assertEquals(0L, snapshot.otherAppsUsageMinutes)
    }

    @Test
    fun `display labels are copied without package metadata dependencies`() {
        val snapshot = ReceiptFactory.from(
            summary(totalMillis = minutes(5), apps = listOf(app("com.example.app", minutes(5)))),
            displayLabels = mapOf("com.example.app" to "Readable App"),
        )

        assertEquals("Readable App", snapshot.topApps.single().displayLabel)
    }

    private fun summary(
        totalMillis: Long,
        apps: List<AppUsage>,
        averageMillis: Long = minutes(1),
        projectedMillis: Long = 365L * 86_400_000L,
    ) = WeeklyUsageSummary(
        reportingPeriod = UsagePeriod(
            localStartDate = LocalDate.of(2026, 9, 8),
            localEndExclusiveDate = LocalDate.of(2026, 9, 15),
            zoneId = ZoneId.of("UTC"),
        ),
        totalForegroundDurationMillis = totalMillis,
        averageDailyDurationMillis = averageMillis,
        projectedAnnualDurationMillis = projectedMillis,
        rankedApplications = apps,
    )

    private fun app(label: String, durationMillis: Long) = AppUsage(label, label, durationMillis)

    private companion object {
        fun minutes(value: Long): Long = value * 60_000L
    }
}
