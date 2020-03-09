package journal.gratitude.com.gratitudejournal.util.backups

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.BDDMockito
import org.threeten.bp.LocalDate
import java.io.File
import kotlin.test.assertTrue

class FileExporterTest  {

    private val writer = mock<CSVWriter>()
    private val fileExporter = FileExporter(writer)

    @Test
    fun `exportDB writes header row`() {
        val expected = arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER)

        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))

        runBlocking {
            fileExporter.exportToCSV(items, mock())
        }
        verify(writer).writeNext(expected)
    }

    @Test
    fun `exportDB doesn't write empty entries`() {
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), ""))

        runBlocking {
            fileExporter.exportToCSV(items, mock())
        }

        verify(writer, times(1)).writeNext(any())
    }

    @Test
    fun `exportDB skips milestones`() {
        val items = listOf(
            Entry(LocalDate.now(), "string"),
            Milestone(1, "1")
        )

        runBlocking {
            fileExporter.exportToCSV(items, mock())
        }
        verify(writer, times(2)).writeNext(any())
    }

    @Test
    fun `exportDB writes entry row`() {
        val dateString = "2020-11-29"
        val entryContent = "hello"
        val expected = arrayOf(dateString, entryContent)

        val items = listOf<TimelineItem>(Entry(LocalDate.of(2020, 11, 29), entryContent))

        runBlocking {
            fileExporter.exportToCSV(items, mock())
        }
        verify(writer).writeNext(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))
        verify(writer).writeNext(expected)
    }

    @Test
    fun `exportDB closes CSV writer`() {
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))

        runBlocking {
            fileExporter.exportToCSV(items, mock())
        }
        verify(writer).close()
    }

    @Test
    fun `exportDB returns success`() {
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))
        val file = mock<File>()

        val result = runBlocking {
            fileExporter.exportToCSV(items, file)
        }
        assertTrue(result is CsvCreated)
    }


    @Test
    @Throws(Exception::class)
    fun `exportDB returns error`() {
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))
        val file = mock<File>()

        val writer = mock<CSVWriter>()
        BDDMockito.given(writer.writeNext(any())).willAnswer { throw Exception("abc msg") }

        val fileExporter = FileExporter(writer)

        val result = runBlocking {
            fileExporter.exportToCSV(items, file)
        }
        assertTrue(result is CsvError)
    }
}