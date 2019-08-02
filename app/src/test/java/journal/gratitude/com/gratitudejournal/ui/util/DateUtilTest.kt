package journal.gratitude.com.gratitudejournal.journal.gratitude.com.gratitudejournal.ui.util

import journal.gratitude.com.gratitudejournal.util.*
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*

class DateUtilTest {

    @Test
    fun stringToLocalDate() {
        val expected = LocalDate.of(2011, 11, 11)
        val actual = "2011-11-11".toLocalDate()

        assertEquals(expected, actual)
    }

    @Test
    fun localDateToDatabaseString() {
        val expected = "2011-11-11"
        val actual = LocalDate.of(2011, 11, 11).toDatabaseString()

        assertEquals(expected, actual)
    }

    @Test
    fun localDateToLongString() {
        val expected = "November 11, 2011"
        val actual = LocalDate.of(2011, 11, 11).toFullString()

        assertEquals(expected, actual)
    }

    @Test
    fun getMonthStringFromDateObject() {
        val expected = "March"
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, Calendar.MARCH)
        cal.set(Calendar.DAY_OF_MONTH, 22)

        val date = cal.time

        assertEquals(expected, date.getMonthString())
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

    @Test
    fun convertDateToLocalDate() {
        val expected = LocalDate.of(2019, 3, 22)

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, Calendar.MARCH)
        cal.set(Calendar.DAY_OF_MONTH, 22)
        val date = cal.time

        assertEquals(expected, date.toLocalDate())
    }

    @Test
    fun convertLocalDateToDate() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, Calendar.MARCH)
        cal.set(Calendar.DAY_OF_MONTH, 22)
        val expected = cal.time

        val localDate = LocalDate.of(2019, 3, 22)

        val fmt = SimpleDateFormat("yyyyMMdd")
        assertEquals(fmt.format(expected), fmt.format(localDate.toDate()))
    }
}