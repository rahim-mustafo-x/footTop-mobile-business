package uz.coder.foottopbusiness.core

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.plusMinutes(minutes: Int): LocalDateTime {
    val tz = TimeZone.currentSystemDefault()
    return this.toInstant(tz).plus(minutes, DateTimeUnit.MINUTE).toLocalDateTime(tz)
}

fun isOverlap(s1: LocalDateTime, e1: LocalDateTime, s2: LocalDateTime, e2: LocalDateTime): Boolean {
    return s1 < e2 && e1 > s2
}

fun String?.toLocalDateTimeSafe(): LocalDateTime? {
    if (this == null) return null
    return try {
        if (this.contains("T")) {
            LocalDateTime.parse(this)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
