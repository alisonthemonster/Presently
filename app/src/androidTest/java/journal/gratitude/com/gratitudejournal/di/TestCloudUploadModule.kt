package journal.gratitude.com.gratitudejournal.di

import dagger.Module
import dagger.Provides
import journal.gratitude.com.gratitudejournal.fakes.FakeCloudUploader
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import javax.inject.Singleton

@Module
object TestCloudUploadModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideCloudUpload(): CloudProvider {
        return FakeCloudUploader()
    }

}