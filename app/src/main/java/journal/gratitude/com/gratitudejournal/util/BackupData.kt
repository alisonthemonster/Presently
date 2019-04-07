package journal.gratitude.com.gratitudejournal.util

import android.os.Environment
import journal.gratitude.com.gratitudejournal.model.Entry
import java.io.File
import java.io.FileWriter


fun exportDB(entries: List<Entry>, exportCallback: ExportCallback) {

    val sdCard = Environment.getExternalStorageDirectory()
    val dir = File(sdCard.absolutePath + "/PresentlyBackups")
    dir.mkdirs()

    val file = File(dir, "PresentlyBackup.csv")
    return try {
        file.createNewFile()
        val csvWrite = CSVWriter(FileWriter(file))
        csvWrite.writeNext(arrayOf("entryDate", "entryContent"))

        for (entry in entries) {
            csvWrite.writeNext(arrayOf(entry.entryDate.toDatabaseString(), entry.entryContent))
        }
        csvWrite.close()
        exportCallback.onSuccess(file)
    } catch (sqlEx: Exception) {
        exportCallback.onFailure(sqlEx.message ?: "Unknown error")
    }
}

interface ExportCallback {

    fun onSuccess(file: File)

    fun onFailure(message: String)
}