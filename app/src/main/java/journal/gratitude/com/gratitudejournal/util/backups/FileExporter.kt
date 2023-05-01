package journal.gratitude.com.gratitudejournal.util.backups

import com.presently.coroutineutils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.model.CsvFileCreated
import journal.gratitude.com.gratitudejournal.model.CsvFileError
import journal.gratitude.com.gratitudejournal.model.CsvFileResult
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.backups.CsvWriter.createCsvString
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class FileExporter(private val fileWriter: FileWriter, private val dispatchers: AppCoroutineDispatchers) {

    /*
    * Takes in entries and creates a CSV
    */
    suspend fun exportToCSV(items: List<Entry>, file: File): CsvFileResult {
        return withContext(dispatchers.io) {
            try {
                val csvString = createCsvString(items)
                fileWriter.write(csvString)
                fileWriter.close()
                CsvFileCreated(file)
            } catch (exception: Exception) {
                CsvFileError(
                    exception
                )
            }
        }
    }
}
