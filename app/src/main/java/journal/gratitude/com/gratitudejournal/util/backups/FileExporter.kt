package journal.gratitude.com.gratitudejournal.util.backups

import com.presently.presently_local_source.model.Entry
import journal.gratitude.com.gratitudejournal.model.CsvFileCreated
import journal.gratitude.com.gratitudejournal.model.CsvFileError
import journal.gratitude.com.gratitudejournal.model.CsvFileResult
import journal.gratitude.com.gratitudejournal.util.backups.CsvWriter.createCsvString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class FileExporter(private val fileWriter: FileWriter) {

    /*
    * Takes in entries and creates a CSV
    */
    suspend fun exportToCSV(items: List<Entry>, file: File): CsvFileResult = withContext(Dispatchers.IO) {
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