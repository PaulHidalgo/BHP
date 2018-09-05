package com.bhp.securitytest

import android.annotation.SuppressLint
import android.content.res.Resources
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object Utils {

    val formatDefaultDate = "dd-MM-yyyy"
    val formatDefaultHour = "HH:mm"

    fun isValid(time: Long): Boolean {
        return DateTime(time).plusYears(2).isAfterNow
    }

    fun getRemainingTime(res: Resources, time: Long): String {
        val period = Period(LocalDateTime.now(), LocalDateTime(time).plusYears(2))
//        val period = Period(LocalDateTime.now(), LocalDateTime(time))
        val formatter = PeriodFormatterBuilder().printZeroNever()
                .appendYears().appendSuffix(" ${res.getQuantityString(R.plurals.years, period.years)} ")
                .appendMonths().appendSuffix(" ${res.getQuantityString(R.plurals.months, period.months)} ")
                .appendDays().appendSuffix(" ${res.getQuantityString(R.plurals.days, period.days)}").toFormatter()
        return formatter.print(period)
    }

    @Throws(ParseException::class)
    fun parseDate(date: String, format: String): Date {
        val formatter = SimpleDateFormat(format)
        return formatter.parse(date)
    }

    @Throws(ParseException::class)
    fun parseDate(date: Date): Date {
        val formatter = SimpleDateFormat(formatDefaultDate)
        return formatter.parse(parseDateString(date))
    }

    @Throws(ParseException::class)
    fun parseDateTime(date: Date): DateTime {
        val formatter = SimpleDateFormat(formatDefaultHour)
        return DateTime(formatter.parse(parseDateString(date, formatDefaultHour)))
    }

    @Throws(ParseException::class)
    fun parseDateString(date: DateTime): String {
        return SimpleDateFormat(formatDefaultHour).format(date.toDate())
    }

    @Throws(ParseException::class)
    fun parseDateString(date: Date): String {
        return SimpleDateFormat(formatDefaultDate).format(date)
    }

    @Throws(ParseException::class)
    fun parseDateString(date: Date, format: String): String {
        return SimpleDateFormat(format).format(date)
    }

    fun stripSpecialCharacters(str: String): String {
        val sb = StringBuffer()
        for (i in 0 until str.length) {
            val ch = str[i]
            if (ch != ',') {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}