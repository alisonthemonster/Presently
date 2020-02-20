package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import dagger.Module
import dagger.Provides
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider
import journal.gratitude.com.gratitudejournal.util.backups.DropboxUploader
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