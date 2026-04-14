package uz.coder.foottopbusiness.core

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

fun LocalDateTime.formatAsDate(): String{
    val day = date.day.toString().padStart(2, '0')
    val month = date.month.number.toString().padStart(2, '0')
    val year = date.year.toString()
    return "$day/$month/$year"
}
fun LocalDateTime.formatAsTime():String{
    val minute = time.minute.toString().padStart(2,'0')
    val hour = time.hour.toString().padStart(2, '0')
    return "$hour:$minute"
}

fun String?.formatToTime(): String {
    if (this == null) return ""
    return try {
        if (this.contains("T")) {
            // ISO format: 2026-02-12T09:00:00
            val timePart = this.split("T").last()
            timePart.take(5)
        } else if (this.contains(":")) {
            // Allready time format: 09:00:00 or 09:00
            this.take(5)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}