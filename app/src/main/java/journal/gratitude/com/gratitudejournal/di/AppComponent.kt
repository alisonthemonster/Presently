package journal.gratitude.com.gratitudejournal.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(
    includes = [
        ApplicationModule::class,
        CloudUploadModule::class,
        WorkerModule::class
    ]
)
interface ApplicationComponent { }