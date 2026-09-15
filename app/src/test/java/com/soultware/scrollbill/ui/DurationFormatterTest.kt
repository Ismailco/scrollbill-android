package com.soultware.scrollbill.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatterTest {
    @Test
    fun `duration formatting uses hours and minutes without seconds`() {
        assertEquals("5m", formatDuration(5L * 60L * 1_000L))
        assertEquals("1h 8m", formatDuration(68L * 60L * 1_000L))
        assertEquals("12h", formatDuration(12L * 60L * 60L * 1_000L))
    }

    @Test
    fun `duration formatting handles zero and sub-minute values`() {
        assertEquals("0m", formatDuration(0L))
        assertEquals("0m", formatDuration(59_999L))
    }
}
