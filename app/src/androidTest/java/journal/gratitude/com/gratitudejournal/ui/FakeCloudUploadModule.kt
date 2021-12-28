package journal.gratitude.com.gratitudejournal.ui

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.di.CloudUploadModule
import journal.gratitude.com.gratitudejournal.fakes.FakeCloudUploader
import journal.gratitude.com.gratitudejournal.fakes.FakeUploader
import journal.gratitude.com.gratitudejournal.util.backups.RealUploader
import journal.gratitude.com.gratitudejournal.util.backups.Uploader
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import javax.inject.Singleton

/**
 * CloudProvider binding to use in tests.
 *
 * Hilt will inject a [FakeCloudUploader] instead of a [DropboxUploader].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CloudUploadModule::class]
)
abstract class FakeCloudUploadModule {
    @Singleton
    @Binds
    abstract fun bindCloudProvider(repo: FakeCloudUploader): CloudProvider

    @Singleton
    @Binds
    abstract fun provideUploader(uploader: FakeUploader): Uploader
}