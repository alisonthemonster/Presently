package journal.gratitude.com.gratitudejournal.ui.util

import journal.gratitude.com.gratitudejournal.util.getYearString
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toMonthString
import junit.framework.TestCase.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class DateUtilTest {

    @Test
    fun stringToLocalDate() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))

        val expected = LocalDate(2011, 11, 11)
        val actual = "2011-11-11".toLocalDate()

        assertEquals(expected, actual)
    }

    @Test
    fun localDateToDatabaseString() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))

        val expected = "2011-11-11"
        val actual = LocalDate(2011, 11, 11).toDatabaseString()

        assertEquals(expected, actual)
    }

    @Test
    fun localDateToLongString() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))
        val expected = "November 11, 2011"
        val actual = LocalDate(2011, 11, 11).toFullString()

        assertEquals(expected, actual)
    }

    @Test
    fun localDateToLongStringRussian() {
        Locale.setDefault(Locale.forLanguageTag("ru"))

        val expected = "11 ноября 2011 г."
        val actual = LocalDate(2011, 11, 11).toFullString()

        assertEquals(expected, actual)
        Locale.setDefault(Locale.forLanguageTag("en"))
    }

    @Test
    fun localDateToLongStringBritishEnglish() {
        Locale.setDefault(Locale.forLanguageTag("en-GB"))

        val expected = "11 November 2011"
        val actual = LocalDate(2011, 11, 11).toFullString()

        assertEquals(expected, actual)
        Locale.setDefault(Locale.forLanguageTag("en"))
    }

    @Test
    fun getMonthStringFromDateObject() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))

        val expected = "March"
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, Calendar.MARCH)
        cal.set(Calendar.DAY_OF_MONTH, 22)

        val date = cal.time

        assertEquals(expected, date.toMonthString())
    }

    @Test
    fun getMonthStringFromDateObjectRussian() {
        Locale.setDefault(Locale.forLanguageTag("ru"))

        val expected = "март"
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, Calendar.MARCH)
        cal.set(Calendar.DAY_OF_MONTH, 22)

        val date = cal.time

        assertEquals(expected, date.toMonthString())
        Locale.setDefault(Locale.forLanguageTag("en"))
    }

    @Test
    fun getYearStringFromDateObject() {
        val expected = "2019"
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, Calendar.MARCH)
        cal.set(Calendar.DAY_OF_MONTH, 22)
        val date = cal.time

        assertEquals(expected, date.getYearString())
    }
}
