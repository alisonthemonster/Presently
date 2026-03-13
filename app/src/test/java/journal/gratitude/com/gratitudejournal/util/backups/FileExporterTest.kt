package journal.gratitude.com.gratitudejournal.util.backups

import com.nhaarman.mockitokotlin2.*
import com.presently.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.model.CsvFileCreated
import journal.gratitude.com.gratitudejournal.model.CsvFileError
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.backups.CsvWriter.createCsvString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.threeten.bp.LocalDate
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.jvm.Throws
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FileExporterTest  {

    private val writer = mock<FileWriter>()

    private fun TestScope.createFileExporter(): FileExporter {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = AppCoroutineDispatchers(
            io = dispatcher,
            computation = dispatcher,
            main = dispatcher
        )
        return FileExporter(writer, dispatchers)
    }

    @Test
    fun `GIVEN list of entries WHEN exportToCSV is called THEN writer writes the csv string`() = runTest {
        val items = listOf(Entry(LocalDate.now(), "string"))
        val fileExporter = createFileExporter()

        fileExporter.exportToCSV(items, mock())
        val expected = createCsvString(items)

        verify(writer).write(expected)
    }

    @Test
    fun `GIVEN list of entries WHEN exportToCSV is called THEN writer is closed`() = runTest {
        val items = listOf(Entry(LocalDate.now(), "string"))
        val fileExporter = createFileExporter()

        fileExporter.exportToCSV(items, mock())

        verify(writer).close()
    }

    @Test
    fun `GIVEN list of entries WHEN exportToCSV is called THEN success is returned`() = runTest {
        val items = listOf(Entry(LocalDate.now(), "string"))
        val file = mock<File>()
        val fileExporter = createFileExporter()

        val result = fileExporter.exportToCSV(items, file)
        assertTrue(result is CsvFileCreated)
    }

    @Test
    @Throws(Exception::class)
    fun `GIVEN list of entries WHEN exportToCSV is called AND writer throws exception THEN return failure`() = runTest {
        val items = listOf(Entry(LocalDate.now(), "string"))
        val file = mock<File>()

        whenever(writer.write(anyString())).thenThrow(IOException("Error"))

        val fileExporter = createFileExporter()

        val result = fileExporter.exportToCSV(items, file)
        assertTrue(result is CsvFileError)
    }
}
