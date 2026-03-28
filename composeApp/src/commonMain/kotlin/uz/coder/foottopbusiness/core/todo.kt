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