package journal.gratitude.com.gratitudejournal.ui

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.di.CloudUploadModule
import journal.gratitude.com.gratitudejournal.fakes.FakeCloudUploader
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CloudUploadModule::class]
)
object FakeCloudUploadModule {

    @Provides
    fun provideCloudUpload(): CloudProvider {
        return FakeCloudUploader()
    }

}