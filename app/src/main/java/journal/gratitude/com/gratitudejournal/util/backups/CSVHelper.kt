package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import java.io.File
import java.io.IOException

fun parseCsv(csvReader: CSVReader): List<Entry> {
    val entries = mutableListOf<Entry>()
    val titles = csvReader.readNext()
    if (titles != null && titles.contentEquals(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))) {
        var rowNum = 0
        while (csvReader.hasNext()) {
            val row = csvReader.readNext()
            if (row != null) {
                if (row.size != 2) {
                    throw IOException("Row #$rowNum had wrong number of columns: ${row.size}")
                }
                val date = row[0].toLocalDate()
                val content = row[1]
                if (content.isNotEmpty()) {
                    entries.add(Entry(date, content))
                }
            }
            rowNum++
        }
    } else {
        if (titles == null) {
            throw IOException("File does not conform to defined pattern. Missing first row.")
        } else {
            throw IOException("File does not conform to defined pattern. Incorrect header row. ${titles.contentToString()}")
        }
    }
    return entries
}

const val DATE_COLUMN_HEADER = "entryDate"
const val ENTRY_COLUMN_HEADER = "entryContent"


interface ExportCallback {

    fun onSuccess(file: File)

    fun onFailure(message: Exception)
}


sealed class CSVResult
class CsvCreated(val file: File) : CSVResult()
class CsvError(val exception: Exception, val message: String = exception.localizedMessage) :
    CSVResult()

