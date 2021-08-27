package journal.gratitude.com.gratitudejournal.util.backups

import com.presently.date_utils.toDatabaseString
import com.presently.presently_local_source.model.Entry
import org.apache.commons.text.StringEscapeUtils

object CsvWriter {
    fun createCsvString(items: List<Entry>): String {
        val sb = StringBuffer()
        writeRow(
            sb,
            Pair(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER)
        )
        for (element in items) {
            writeRow(
                sb,
                Pair(element.entryDate.toDatabaseString(), element.entryContent)
            )
        }
        return sb.toString()
    }

    private fun writeRow(sb: StringBuffer, pair: Pair<String, String>) {
        sb.append(StringEscapeUtils.escapeCsv(pair.first))
        sb.append(DEFAULT_SEPARATOR)
        sb.append(StringEscapeUtils.escapeCsv(pair.second))
        sb.append(DEFAULT_LINE_END)
    }

    const val DATE_COLUMN_HEADER = "entryDate"
    const val ENTRY_COLUMN_HEADER = "entryContent"
    private const val DEFAULT_LINE_END = "\n"
    private const val DEFAULT_SEPARATOR = ','
}