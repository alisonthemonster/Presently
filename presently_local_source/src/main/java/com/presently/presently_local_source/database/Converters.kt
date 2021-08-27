package com.presently.presently_local_source.database

import com.presently.date_utils.toDatabaseString
import com.presently.date_utils.toLocalDate
import org.threeten.bp.LocalDate

object Converters {
    @androidx.room.TypeConverter
    @JvmStatic
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.toLocalDate()
    }

    @androidx.room.TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toDatabaseString()
    }
}