package journal.gratitude.com.gratitudejournal.di

import androidx.work.ListenableWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.UploadToCloudWorker

@Module
interface WorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(UploadToCloudWorker::class)
    fun bindHelloWorker(factory: UploadToCloudWorker.Factory): IWorkerFactory<out ListenableWorker>
    // every time you add a worker, add a binding here
}