package journal.gratitude.com.gratitudejournal.util.backups

import android.os.Environment
import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.FileWriter

object LocalExporter {

    suspend fun exportToCSV(items: List<Entry>): CSVResult {

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