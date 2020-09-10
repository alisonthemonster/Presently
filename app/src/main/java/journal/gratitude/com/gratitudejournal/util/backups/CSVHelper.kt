package journal.gratitude.com.gratitudejournal.util.backups

import android.net.Uri
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import java.io.File
import java.io.IOException


interface ExportCallback {

    fun onSuccess(file: Uri)

    fun onFailure(exception: Exception)
}

//TODO file vs uri
sealed class CSVResult
class CsvFileCreated(val file: File) : CSVResult()
class CsvUriCreated(val uri: Uri) : CSVResult()
class CsvError(val exception: Exception, val message: String = exception.localizedMessage) :
    CSVResult()

