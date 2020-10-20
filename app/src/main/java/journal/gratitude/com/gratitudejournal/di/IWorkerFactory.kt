package journal.gratitude.com.gratitudejournal.di

import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface IWorkerFactory<T : ListenableWorker> {
    fun create(params: WorkerParameters): T
}