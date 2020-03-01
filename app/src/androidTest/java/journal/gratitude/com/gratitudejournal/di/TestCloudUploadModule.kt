package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import dagger.Module
import dagger.Provides
import journal.gratitude.com.gratitudejournal.fakes.FakeCloudUploader
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider
import javax.inject.Singleton

@Module
object TestCloudUploadModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideCloudUpload(context: Context): CloudProvider {
        return FakeCloudUploader()
    }

}