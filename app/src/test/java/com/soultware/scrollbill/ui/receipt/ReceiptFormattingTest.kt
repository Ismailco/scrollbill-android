package com.soultware.scrollbill.ui.receipt

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReceiptFormattingTest {
    @Test
    fun `receipt durations use compact consistent precision`() {
        assertEquals("8h 47m", formatReceiptDuration(8L * 60L + 47L))
        assertEquals("3h 01m", formatReceiptDuration(3L * 60L + 1L))
        assertEquals("58m", formatReceiptDuration(58L))
        assertEquals("105h 42m", formatReceiptDuration(105L * 60L + 42L))
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
        assertEquals("~53 days", formatYearlyPace(53L))
        assertEquals("~228 days", formatYearlyPace(228L))
    }

    @Test
    fun `long labels are truncated to measured width`() {
        val measureText: (String) -> Float = { it.length.toFloat() }

        assertEquals(
            "Some Very Long…",
            ellipsizeToWidth("Some Very Long Application Name", maxWidth = 15f, measureText),
        )
        assertEquals(
            "Short label",
            ellipsizeToWidth("Short label", maxWidth = 20f, measureText),
        )
        assertEquals("", ellipsizeToWidth("Long label", maxWidth = 0.5f, measureText))
    }
}
