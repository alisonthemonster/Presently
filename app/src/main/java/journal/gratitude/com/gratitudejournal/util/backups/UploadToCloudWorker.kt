package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import journal.gratitude.com.gratitudejournal.di.IWorkerFactory
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Provider

class UploadToCloudWorker(
    private val context: Context,
    params: WorkerParameters,
    private val repository: EntryRepository,
    private val cloudProvider: CloudProvider
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        //get items
        val items = repository.getEntries()

        //do not backup data if there is nothing to back up
        if (items.isEmpty()) {
            return Result.success()
        }

        //create temp file
        val file = withContext(IO) { File.createTempFile("tempPresentlyBackup", null, context.cacheDir) }

        //create csv
        val fileExporter = withContext(IO) { FileExporter(CSVWriterImpl(FileWriter(file))) }
        val csvResult = when (val csvResult = fileExporter.exportToCSV(items, file)) {
            is CsvFileCreated -> {
                //upload to cloud
                when (cloudProvider.uploadToCloud(csvResult.file)) {
                    is UploadError -> Result.failure()
                    is UploadSuccess -> Result.success()
                }
            }
            is CsvError -> Result.failure()
            is CsvUriCreated -> TODO()
        }

        //delete temp file
        file.delete()

        return csvResult
    }

    class Factory @Inject constructor(
        private val context: Provider<Context>, // provide from Application Module
        private val repository: Provider<EntryRepository>, // provide from Application Module
        private val cloudProvider: Provider<CloudProvider>
    ) : IWorkerFactory<UploadToCloudWorker> {
        override fun create(params: WorkerParameters): UploadToCloudWorker {
            return UploadToCloudWorker(context.get(), params, repository.get(), cloudProvider.get())
        }
    }
}

interface CloudProvider {
    suspend fun uploadToCloud(file: File): CloudUploadResult
}