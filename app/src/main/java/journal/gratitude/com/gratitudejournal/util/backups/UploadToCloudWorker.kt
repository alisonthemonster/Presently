package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.presently.logging.CrashReporter
import com.presently.presently_local_source.PresentlyLocalSource
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.di.IWorkerFactory
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.model.CsvFileError
import journal.gratitude.com.gratitudejournal.model.CsvFileCreated
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Provider

class UploadToCloudWorker(
    private val context: Context,
    params: WorkerParameters,
    private val localSource: PresentlyLocalSource,
    private val cloudProvider: CloudProvider,
    private val crashReporter: CrashReporter
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        //get items
        val items = localSource.getEntries()

        //do not backup data if there is nothing to back up
        if (items.isEmpty()) {
            return Result.success()
        }

        //create temp file
        val file = withContext(IO) { File.createTempFile("tempPresentlyBackup", null, context.cacheDir) }

        //create csv
        val fileExporter = withContext(IO) {
            FileExporter(
                FileWriter(file)
            )
        }
        val csvResult = when (val csvResult = fileExporter.exportToCSV(items, file)) {
            is CsvFileCreated -> {
                //upload to cloud
                when (val result = cloudProvider.uploadToCloud(csvResult.file)) {
                    is UploadError -> {
                        crashReporter.logHandledException(result.exception)
                        Result.failure()
                    }
                    is UploadSuccess -> Result.success()
                }
            }
            is CsvFileError -> Result.failure()
        }

        //delete temp file
        file.delete()

        return csvResult
    }

    class Factory @Inject constructor(
        @ApplicationContext private val context: Provider<Context>, // provide from Application Module
        private val localSource: Provider<PresentlyLocalSource>, // provide from Application Module
        private val cloudProvider: Provider<CloudProvider>,
        private val crashReporter: Provider<CrashReporter>
    ) : IWorkerFactory<UploadToCloudWorker> {
        override fun create(params: WorkerParameters): UploadToCloudWorker {
            return UploadToCloudWorker(
                context.get(),
                params,
                localSource.get(),
                cloudProvider.get(),
                crashReporter.get()
            )
        }
    }
}

interface CloudProvider {
    suspend fun uploadToCloud(file: File): CloudUploadResult
}