package journal.gratitude.com.gratitudejournal.ui

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.di.CloudUploadModule
import journal.gratitude.com.gratitudejournal.fakes.FakeCloudUploader
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider
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
    abstract fun bindCloudProvider(cloudProvider: FakeCloudUploader): CloudProvider
}