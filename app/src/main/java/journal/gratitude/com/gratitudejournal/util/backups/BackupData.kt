package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.util.CrashlyticsWrapper
import journal.gratitude.com.gratitudejournal.util.CrashlyticsWrapperImpl
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import java.io.File
import java.io.IOException
import java.io.InputStream


fun exportDB(timelineItems: List<TimelineItem>,
             exportCallback: ExportCallback,
             file: File,
             csvWrite: CSVWriter,
             crashlytics: CrashlyticsWrapper = CrashlyticsWrapperImpl()) {
    return try {
        //write header row
        csvWrite.writeNext(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))

        //write entries
        for (item in timelineItems) {
            if (item is Entry) {
                if (item.entryContent.isNotEmpty()) {
                    csvWrite.writeNext(
                        arrayOf(
                            item.entryDate.toDatabaseString(),
                            item.entryContent
                        )
                    )
                }
            }
        }
        csvWrite.close()
        exportCallback.onSuccess(file)
    } catch (exception: Exception) {
        crashlytics.logException(exception)
        exportCallback.onFailure(exception.message ?: "Unknown error")
    }
}

fun parseCsv(inputStream: InputStream): List<Entry> {
    val entries = mutableListOf<Entry>()
    val csvReader = CSVReader(inputStream.bufferedReader())
    val titles = csvReader.readNext()
    if (titles != null && titles.contentEquals(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))) {
        var rowNum = 0
        while (csvReader.hasNext) {
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

    fun onFailure(message: String)
}