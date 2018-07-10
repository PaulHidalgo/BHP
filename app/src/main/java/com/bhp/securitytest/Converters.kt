package com.bhp.securitytest

import android.arch.persistence.room.TypeConverter
import org.joda.time.DateTime
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return Utils.parseDate(Date(value!!))
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromLongDateTime(value: Long?): DateTime? {
        return Utils.parseDateTime(Date(value!!))
    }

    @TypeConverter
    fun dateTimeTolong(date: DateTime?): Long? {
        return date?.millis
    }
}