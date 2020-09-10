package journal.gratitude.com.gratitudejournal.util.backups

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import journal.gratitude.com.gratitudejournal.model.CsvUriCreated
import journal.gratitude.com.gratitudejournal.model.CsvUriError
import journal.gratitude.com.gratitudejournal.model.CsvUriResult
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.backups.CsvWriter.DATE_COLUMN_HEADER
import journal.gratitude.com.gratitudejournal.util.backups.CsvWriter.ENTRY_COLUMN_HEADER
import journal.gratitude.com.gratitudejournal.util.backups.CsvWriter.createCsvString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream


object LocalExporter {
    //TODO test on low api level device

    suspend fun exportEntriesToCsvFile(
        context: Context,
        source: Uri,
        items: List<Entry>
    ): CsvUriResult =
        withContext(Dispatchers.IO) {
            val resolver: ContentResolver = context.contentResolver

            try {
                resolver.openOutputStream(source)
                    ?.use { stream -> stream.writeEntriesToStream(items) }
                    ?: throw IllegalStateException("Could not open $source")
                return@withContext CsvUriCreated(
                    source
                )
            } catch (exception: Exception) {
                return@withContext CsvUriError(
                    exception
                )
            }
        }

    private fun OutputStream.writeEntriesToStream(items: List<Entry>) {
        val baos = ByteArrayOutputStream()
        val out = DataOutputStream(baos)
        out.write(createCsvString(items).toByteArray())
        val bytes = baos.toByteArray()
        write(bytes)
    }

    fun convertCsvToEntries(parser: CsvParser): List<Entry> {
        val entries = mutableListOf<Entry>()

        val records = parser.getRecords()
        if (records.isNullOrEmpty()) {
            throw IOException("File does not conform to defined pattern. Missing first row.")
        }
        val titleRow = records.first()
        if (titleRow.values.size != 2 || titleRow.values[0] != DATE_COLUMN_HEADER || titleRow.values[1] != ENTRY_COLUMN_HEADER) {
            throw IOException("File does not conform to defined pattern. Incorrect header row. $titleRow")
        }
        for (x in 1 until records.size) {
            val record = records[x]
            if (record.values.size != 2) {
                throw IOException("Row had wrong number of columns: ${record.values.size}")
            }
            val date = record.values[0].toLocalDate()
            val content = record.values[1]
            if (content.isNotEmpty()) {
                entries.add(Entry(date, content))
            }
        }
        return entries
    }
}