package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import com.presently.settings.PresentlySettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.util.backups.RealUploader
import journal.gratitude.com.gratitudejournal.util.backups.Uploader
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader

@Module
@InstallIn(SingletonComponent::class)
object CloudUploadModule {

    @Provides
    fun provideCloudUpload(@ApplicationContext context: Context, settings: PresentlySettings): CloudProvider {
        return DropboxUploader(context, settings)
    }

    @Provides
    fun provideUploader(uploader: RealUploader): Uploader = uploader
}
