package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import java.io.File

class BackupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val cloudProvider: CloudProvider,
    private val repository: EntryRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        //get items
        val items = repository.getEntries()

        //create temp file
        val file = File("pathname")

        //create csv
        val csvResult = when (val csvResult = exportToCSV(items, file)) {
            is CsvCreated -> {
                //upload to cloud
                when (cloudProvider.uploadToCloud(csvResult.file)) {
                    is UploadError -> Result.failure()
                    is UploadSuccess -> Result.success()
                }
            }
            is CsvError -> Result.failure()
        }

        //delete temp file
        file.delete()

        return csvResult
    }
}

interface CloudProvider {
    suspend fun uploadToCloud(file: File): CloudUploadResult
}

sealed class CloudUploadResult
object UploadSuccess : CloudUploadResult()
class UploadError(val exception: Exception, val message: String = exception.localizedMessage) :
    CloudUploadResult()
