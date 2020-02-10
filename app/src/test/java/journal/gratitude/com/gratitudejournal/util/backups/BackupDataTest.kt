package journal.gratitude.com.gratitudejournal.util.backups

import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.util.CrashlyticsWrapper
import org.junit.Test
import org.threeten.bp.LocalDate
import java.io.File
import java.io.IOException


class BackupDataTest {

    @Test
    fun `exportDB writes header row`() {
        val expected = arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER)

        val writer = mock<CSVWriter>()
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))

        exportDB(items, mock(), mock(), writer)

        verify(writer).writeNext(expected)
    }

    @Test
    fun `exportDB doesn't write empty entries`() {
        val writer = mock<CSVWriter>()
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), ""))

        exportDB(items, mock(), mock(), writer)

        verify(writer, times(1)).writeNext(any())
    }

    @Test
    fun `exportDB skips milestones`() {
        val writer = mock<CSVWriter>()
        val items = listOf(Entry(LocalDate.now(), "string"),
            Milestone(1, "1"))

        exportDB(items, mock(), mock(), writer)

        verify(writer, times(2)).writeNext(any())
    }

    @Test
    fun `exportDB writes entry row`() {
        val dateString = "2020-11-29"
        val entryContent = "hello"
        val expected = arrayOf(dateString, entryContent)

        val writer = mock<CSVWriter>()
        val items = listOf<TimelineItem>(Entry(LocalDate.of(2020, 11, 29), entryContent))

        exportDB(items, mock(), mock(), writer)

        verify(writer).writeNext(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))
        verify(writer).writeNext(expected)
    }

    @Test
    fun `exportDB closes CSV writer`() {
        val writer = mock<CSVWriter>()
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))

        exportDB(items, mock(), mock(), writer)

        verify(writer).close()
    }

    @Test
    fun `exportDB calls callback success`() {
        val writer = mock<CSVWriter>()
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))
        val callback = mock<ExportCallback>()
        val file = mock<File>()

        exportDB(items, callback, file, writer)

        verify(callback).onSuccess(file)
    }

    @Test
    fun `exportDB logs thrown exceptions`() {
        val expected = IOException()
        val writer = mock<CSVWriter>()
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))
        val callback = mock<ExportCallback>()
        val file = mock<File>()
        val crashlytics = mock<CrashlyticsWrapper>()

        given(writer.writeNext(any())).willAnswer {
            throw expected
        }

        exportDB(items, callback, file, writer, crashlytics)

        verify(crashlytics).logException(expected)
    }

    @Test
    fun `exportDB calls onFailure`() {
        val expected = "error message"
        val writer = mock<CSVWriter>()
        val items = listOf<TimelineItem>(Entry(LocalDate.now(), "string"))
        val callback = mock<ExportCallback>()
        val file = mock<File>()
        val crashlytics = mock<CrashlyticsWrapper>()

        given(writer.writeNext(any())).willAnswer {
            throw IOException(expected)
        }
        exportDB(items, callback, file, writer, crashlytics)

        verify(callback).onFailure(expected)
    }

}