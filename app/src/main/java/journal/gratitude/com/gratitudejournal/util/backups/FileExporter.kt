package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileExporter(val csvWrite: CSVWriter) {

    /*
    * Takes in entries and creates a CSV
    */
    suspend fun exportToCSV(timelineItems: List<TimelineItem>, file: File): CSVResult {
        return withContext(Dispatchers.IO) {
            try {
                //write header row
                csvWrite.writeNext(arrayOf(DATE_COLUMN_HEADER, ENTRY_COLUMN_HEADER))

                //write entries
                for (item in timelineItems) {
                    if (item is Entry && item.entryContent.isNotEmpty()) {
                        csvWrite.writeNext(
                            arrayOf(
                                item.entryDate.toDatabaseString(),
                                item.entryContent
                            )
                        )
                    }
                }
                csvWrite.close()
                CsvCreated(file)
            } catch (exception: Exception) {
                CsvError(exception)
            }
        }
    }
}