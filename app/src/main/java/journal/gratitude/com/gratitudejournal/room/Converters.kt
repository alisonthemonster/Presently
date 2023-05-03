package journal.gratitude.com.gratitudejournal.room

import androidx.room.TypeConverter
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.LocalDate

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.toLocalDate()
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toDatabaseString()
    }
}
