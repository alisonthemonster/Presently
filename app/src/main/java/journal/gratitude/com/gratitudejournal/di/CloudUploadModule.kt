package journal.gratitude.com.gratitudejournal.di

import android.content.SharedPreferences
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
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
    fun provideDropboxClient(sharedPreferences: SharedPreferences): DbxClientV2 {
        val accessToken = sharedPreferences.getString("access-token", null)

        val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
            .build()

        return DbxClientV2(requestConfig, accessToken)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideCloudUpload(client: DbxClientV2): CloudProvider {
        return DropboxUploader(client)
    }

}