package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.createCsvString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class FileExporter(private val fileWriter: FileWriter) {

    /*
    * Takes in entries and creates a CSV
    */
    suspend fun exportToCSV(items: List<Entry>, file: File): CSVResult {
        return withContext(Dispatchers.IO) {
            try {
                val csvString = createCsvString(items)
                fileWriter.write(csvString)
                fileWriter.close()
                CsvFileCreated(file)
            } catch (exception: Exception) {
                CsvError(exception)
            }
        }
    }
}