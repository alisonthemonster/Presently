package journal.gratitude.com.gratitudejournal.model

import android.net.Uri
import java.io.File

sealed class CsvFileResult
class CsvFileCreated(val file: File) : CsvFileResult()
class CsvFileError(val exception: Exception, val message: String = exception.localizedMessage ?: "Unknown error") :
    CsvFileResult()

sealed class CsvUriResult
class CsvUriCreated(val uri: Uri) : CsvUriResult()
class CsvUriError(val exception: Exception, val message: String = exception.localizedMessage ?: "Unknown error") :
    CsvUriResult()
