package com.soultware.scrollbill.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class UsagePeriodTest {
    @Test
    fun `last seven completed days use local midnight boundaries`() {
        val zone = ZoneId.of("America/New_York")
        val clock = Clock.fixed(Instant.parse("2026-09-15T16:00:00Z"), zone)

        val period = UsagePeriod.lastSevenCompletedDays(clock, zone)

        assertEquals(java.time.LocalDate.of(2026, 9, 8), period.localStartDate)
        assertEquals(java.time.LocalDate.of(2026, 9, 15), period.localEndExclusiveDate)
        assertEquals(7L, period.dayCount)
        assertEquals(7, period.days().size)
        assertEquals(
            java.time.LocalDate.of(2026, 9, 8).atStartOfDay(zone).toInstant().toEpochMilli(),
            period.days().first().startEpochMillis,
        )
        assertEquals(
            java.time.LocalDate.of(2026, 9, 15).atStartOfDay(zone).toInstant().toEpochMilli(),
            period.days().last().endEpochMillis,
        )
    }

    @Test
    fun `period remains seven calendar days across daylight saving boundary`() {
        val zone = ZoneId.of("America/New_York")
        val period = UsagePeriod(
            localStartDate = java.time.LocalDate.of(2026, 11, 1),
            localEndExclusiveDate = java.time.LocalDate.of(2026, 11, 8),
            zoneId = zone,
        )

        assertEquals(7L, period.dayCount)
        assertEquals(25, (period.days().first().endEpochMillis - period.days().first().startEpochMillis) / 3_600_000)
    }
}
