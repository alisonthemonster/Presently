package journal.gratitude.com.gratitudejournal.util.backups

import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.util.CrashlyticsWrapper
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.threeten.bp.LocalDate
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.mockito.BDDMockito.willAnswer
import org.mockito.BDDMockito.given


class BackupDataTest {


    @Test
    fun `parseCsv with null title throws exception`() {
        val reader = mock<CSVReader>()

        assertFailsWith<IOException> {
            parseCsv(reader)
        }
    }

    @Test
    fun `parseCsv with bad title row throws exception`() {
        val reader = mock<CSVReader>()
        whenever(reader.hasNext()).thenReturn(false)
        whenever(reader.readNext()).thenReturn(arrayOf("bad title", "woopsie", "extra column header!"))

        assertFailsWith<IOException> {
            parseCsv(reader)
        }
    }

    @Test
    fun `parseCsv with parses entry row`() {
        val expected = listOf(Entry(LocalDate.of(2000, 1, 1), "blah"))
        val reader = mock<CSVReader>()
        whenever(reader.hasNext())
            .thenReturn(true)
            .thenReturn(false)
        whenever(reader.readNext())
            .thenReturn(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))
            .thenReturn(arrayOf("2000-01-01", "blah"))

        assertEquals(expected, parseCsv(reader))
    }

    @Test
    fun `parseCsv throws exception for row with too few columns`() {
        val reader = mock<CSVReader>()
        whenever(reader.hasNext())
            .thenReturn(true)
            .thenReturn(false)
        whenever(reader.readNext())
            .thenReturn(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))
            .thenReturn(arrayOf("2000-01-01"))

        assertFailsWith<IOException> {
            parseCsv(reader)
        }
    }

    @Test
    fun `parseCsv skips empty entry`() {
        val expected = listOf(Entry(LocalDate.of(2000, 1, 1), "blah"))

        val reader = mock<CSVReader>()
        whenever(reader.hasNext())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false)
        whenever(reader.readNext())
            .thenReturn(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))
            .thenReturn(arrayOf("2000-01-02", ""))
            .thenReturn(arrayOf("2000-01-01", "blah"))

        assertEquals(expected, parseCsv(reader))
    }

}