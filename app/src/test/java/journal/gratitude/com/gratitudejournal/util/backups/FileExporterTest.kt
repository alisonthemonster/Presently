package journal.gratitude.com.gratitudejournal.util.backups

import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.createCsvString
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito
import org.threeten.bp.LocalDate
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.jvm.Throws
import kotlin.test.assertTrue

class FileExporterTest  {

    private val writer = mock<FileWriter>()
    private val fileExporter = FileExporter(writer)

    @Test
    fun `GIVEN list of entries WHEN exportToCSV is called THEN writer writes the csv string`() {
        val items = listOf(Entry(LocalDate.now(), "string"))

        runBlocking {
            fileExporter.exportToCSV(items, mock())
        }
        val expected = createCsvString(items)

        verify(writer).write(expected)
    }

    @Test
    fun `GIVEN list of entries WHEN exportToCSV is called THEN writer is closed`() {
        val items = listOf(Entry(LocalDate.now(), "string"))

        runBlocking {
            fileExporter.exportToCSV(items, mock())
        }

        verify(writer).close()
    }

    @Test
    fun `GIVEN list of entries WHEN exportToCSV is called THEN success is returned`() {
        val items = listOf(Entry(LocalDate.now(), "string"))
        val file = mock<File>()

        val result = runBlocking {
            fileExporter.exportToCSV(items, file)
        }
        assertTrue(result is CsvFileCreated)
    }

    @Test
    @Throws(Exception::class)
    fun `GIVEN list of entries WHEN exportToCSV is called AND writer throws exception THEN return failure`() {
        val items = listOf(Entry(LocalDate.now(), "string"))
        val file = mock<File>()

        whenever(writer.write(anyString())).thenThrow(IOException("Error"))

        val fileExporter = FileExporter(writer)

        val result = runBlocking {
            fileExporter.exportToCSV(items, file)
        }
        assertTrue(result is CsvError)
    }
}