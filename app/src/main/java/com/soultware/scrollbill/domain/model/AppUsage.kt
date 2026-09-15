package com.soultware.scrollbill.domain.model

data class AppUsage(
    val packageName: String,
    val displayLabel: String,
    val foregroundDurationMillis: Long,
)
