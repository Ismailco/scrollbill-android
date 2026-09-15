package com.soultware.scrollbill.domain.model

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class UsagePeriod(
    val localStartDate: LocalDate,
    val localEndExclusiveDate: LocalDate,
    val zoneId: ZoneId,
) {
    init {
        require(localStartDate.isBefore(localEndExclusiveDate)) {
            "Usage period must have a non-empty date range"
        }
    }

    val dayCount: Long
        get() = ChronoUnit.DAYS.between(localStartDate, localEndExclusiveDate)

    fun days(): List<UsageDay> = buildList {
        var date = localStartDate
        while (date.isBefore(localEndExclusiveDate)) {
            add(UsageDay(date = date, startEpochMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli(), endEpochMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()))
            date = date.plusDays(1)
        }
    }

    companion object {
        fun lastSevenCompletedDays(clock: Clock, zoneId: ZoneId): UsagePeriod {
            val today = LocalDate.now(clock.withZone(zoneId))
            return UsagePeriod(
                localStartDate = today.minusDays(7),
                localEndExclusiveDate = today,
                zoneId = zoneId,
            )
        }
    }
}

data class UsageDay(
    val date: LocalDate,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
)
