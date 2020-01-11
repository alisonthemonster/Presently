package journal.gratitude.com.gratitudejournal.util.backups

import android.os.Environment
import com.crashlytics.android.Crashlytics
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream


fun exportDB(entries: List<Entry>, exportCallback: ExportCallback) {

    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val date = LocalDateTime.now().withNano(0).toString().replace(':', '-')

    val file = File(dir, "PresentlyBackup$date.csv")
    return try {
        file.createNewFile()
        val csvWrite = CSVWriter(FileWriter(file))
        csvWrite.writeNext(arrayOf("entryDate", "entryContent"))

        for (entry in entries) {
            if (entry.entryContent.isNotEmpty()) {
                csvWrite.writeNext(arrayOf(entry.entryDate.toDatabaseString(), entry.entryContent))
            }
        }
        csvWrite.close()
        exportCallback.onSuccess(file)
    } catch (exception: Exception) {
        Crashlytics.logException(exception)
        exportCallback.onFailure(exception.message ?: "Unknown error")
    }
}

fun parseCsv(inputStream: InputStream): List<Entry> {
    val entries = mutableListOf<Entry>()
    val csvReader = CSVReader(inputStream.bufferedReader())
    val titles = csvReader.readNext()
    if (titles != null && titles.contentEquals(arrayOf("entryDate", "entryContent"))) {
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
        throw IOException("File does not conform to defined pattern. Missing or incorrect header row.")
    }
    return entries
}

interface ExportCallback {

    fun onSuccess(file: File)

    fun onFailure(message: String)
}