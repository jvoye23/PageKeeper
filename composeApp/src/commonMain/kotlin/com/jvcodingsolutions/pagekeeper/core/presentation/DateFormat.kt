package com.jvcodingsolutions.pagekeeper.core.presentation

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val monthAbbreviations = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/** Formats an epoch-millis timestamp as `HH:mm MMM d, yyyy` (e.g. "12:57 Feb 25, 2026"). */
fun Long.toBookmarkDisplayString(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    val month = monthAbbreviations[dt.monthNumber - 1]
    return "$hour:$minute $month ${dt.dayOfMonth}, ${dt.year}"
}
