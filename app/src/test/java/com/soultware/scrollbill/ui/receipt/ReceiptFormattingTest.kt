package com.soultware.scrollbill.ui.receipt

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReceiptFormattingTest {
    @Test
    fun `receipt durations use compact consistent precision`() {
        assertEquals("8h 47m", formatReceiptDuration((8L * 60L + 47L) * 60_000L))
        assertEquals("3h 01m", formatReceiptDuration((3L * 60L + 1L) * 60_000L))
        assertEquals("58m", formatReceiptDuration(58L * 60_000L))
        assertEquals("0m", formatReceiptDuration(0L))
    }

    @Test
    fun `receipt date range is stable English and uppercase`() {
        assertEquals(
            "SEP 8 - SEP 14, 2026",
            formatReceiptDateRange(LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 14)),
        )
    }

    @Test
    fun `yearly pace is an approximate number of days`() {
        assertEquals("~53 days", formatYearlyPace(53L * 86_400_000L))
    }
}
