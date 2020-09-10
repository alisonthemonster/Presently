package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import dagger.Module
import dagger.Provides
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import javax.inject.Singleton


@Module
object CloudUploadModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideCloudUpload(context: Context): CloudProvider {
        return DropboxUploader(context)
    }

}