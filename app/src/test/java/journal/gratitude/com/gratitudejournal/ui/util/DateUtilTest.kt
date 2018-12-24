package journal.gratitude.com.gratitudejournal.journal.gratitude.com.gratitudejournal.ui.util

import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

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
}