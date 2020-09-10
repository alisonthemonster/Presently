package journal.gratitude.com.gratitudejournal.util.backups

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream


object LocalExporter {

    suspend fun writeEntriesToFile(context: Context, source: Uri, items: List<Entry>): CSVResult =
        withContext(Dispatchers.IO) {
            val resolver: ContentResolver = context.contentResolver

            try {
                resolver.openOutputStream(source)?.use { stream -> stream.writeText(items) }
                    ?: throw IllegalStateException("Could not open $source")
                return@withContext CsvUriCreated(source)
            } catch (exception: Exception) {
                return@withContext CsvError(exception)
            }
        }

    private fun OutputStream.writeText(
        items: List<Entry>
    ): Unit = write(convertToBytes(items))

    fun createCsvString(items: List<Entry>): String {
        val sb = StringBuffer()
        writeRow(sb, Pair(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))
        for (element in items) {
            writeRow(sb, Pair(element.entryDate.toDatabaseString(), element.entryContent))
        }
        return sb.toString()
    }

    fun parseCsvString(parser: CsvParser): List<Entry> {
        val entries = mutableListOf<Entry>()

        val records = parser.getRecords()
        if (records.isNullOrEmpty()) {
            throw IOException("File does not conform to defined pattern. Missing first row.")
        }
        val titleRow = records.first()
        if (titleRow.values.size != 2 || titleRow.values[0] != DATE_COLUMN_HEADER || titleRow.values[1] != ENTRY_COLUMN_HEADER) {
            throw IOException("File does not conform to defined pattern. Incorrect header row. $titleRow")
        }
        for( x in 1 until records.size) {
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

    private fun convertToBytes(items: List<Entry>): ByteArray {
        val baos = ByteArrayOutputStream()
        val out = DataOutputStream(baos)
        out.write(createCsvString(items).toByteArray())
        return baos.toByteArray()
    }

    private fun writeRow(sb: StringBuffer, pair: Pair<String, String>) {
        sb.append(StringEscapeUtils.escapeCsv(pair.first))
        sb.append(DEFAULT_SEPARATOR)
        sb.append(StringEscapeUtils.escapeCsv(pair.second))
        sb.append(DEFAULT_LINE_END)
    }

    private const val DATE_COLUMN_HEADER = "entryDate"
    private const val ENTRY_COLUMN_HEADER = "entryContent"

    private const val DEFAULT_LINE_END = "\n"
    private const val DEFAULT_SEPARATOR = ','
}