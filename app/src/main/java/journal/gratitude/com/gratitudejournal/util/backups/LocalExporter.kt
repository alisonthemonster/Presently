package journal.gratitude.com.gratitudejournal.util.backups

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.backups.CSVWriterImpl.Companion.DEFAULT_LINE_END
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream


object LocalExporter {

    suspend fun write(context: Context, source: Uri, items: List<Entry>): CSVResult =
        withContext(Dispatchers.IO) {
            val resolver: ContentResolver = context.contentResolver

            try {
                resolver.openOutputStream(source)?.use { stream -> stream.writeText(items) }
                    ?: throw IllegalStateException("could not open $source")
                return@withContext CsvUriCreated(source)
            } catch (exception: Exception) {
                return@withContext CsvError(exception)
            }

        }

    private fun OutputStream.writeText(
        items: List<Entry>
    ): Unit = write(convertToBytes(items))

    private fun convertToBytes(items: List<Entry>): ByteArray {
        val baos = ByteArrayOutputStream()
        val out = DataOutputStream(baos)
        val sb = StringBuffer()
        writeRow(sb, Pair(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))
        for (element in items) {
            writeRow(sb, Pair(element.entryDate.toDatabaseString(), element.entryContent))
        }
        out.write(sb.toString().toByteArray())
        return baos.toByteArray()

    }

    private fun writeRow(sb: StringBuffer, pair: Pair<String, String>) {
        sb.append(StringEscapeUtils.escapeCsv(pair.first))
        sb.append(CSVWriterImpl.DEFAULT_SEPARATOR)
        sb.append(StringEscapeUtils.escapeCsv(pair.second))
        sb.append(DEFAULT_LINE_END)
    }

}