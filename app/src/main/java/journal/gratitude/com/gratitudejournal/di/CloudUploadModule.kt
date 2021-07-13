package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import com.presently.backups.DropboxWebService
import com.presently.settings.PresentlySettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader


@Module
@InstallIn(SingletonComponent::class)
object CloudUploadModule {

    @Provides
    fun provideCloudUpload(
        dropboxWebService: DropboxWebService
    ): CloudProvider {
        return DropboxUploader(dropboxWebService)
    }

}