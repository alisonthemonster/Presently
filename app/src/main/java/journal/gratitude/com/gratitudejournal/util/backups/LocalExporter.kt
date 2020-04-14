package journal.gratitude.com.gratitudejournal.util.backups

import android.os.Environment
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.FileWriter

object LocalExporter {

    suspend fun exportToCSV(repository: EntryRepository): CSVResult {

        val items = repository.getEntries()

        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val date = LocalDateTime.now().withNano(0).toString().replace(':', '-')
        val file = File(dir, "PresentlyBackup$date.csv")

        //create csv
        val fileExporter = FileExporter(CSVWriterImpl(FileWriter(file)))
        return fileExporter.exportToCSV(items, file)
    }
}