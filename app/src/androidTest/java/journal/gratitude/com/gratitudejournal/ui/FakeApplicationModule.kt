package journal.gratitude.com.gratitudejournal.ui

import com.presently.presently_local_source.PresentlyLocalSource
import com.presently.presently_local_source.wiring.PresentlyLocalSourceModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.fakes.FakePresentlyLocalSource
import javax.inject.Singleton

/**
 * PresentlyLocalSource binding to use in tests.
 *
 * Hilt will inject a [FakePresentlyLocalSource] instead of a [RealPresentlyLocalSource].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PresentlyLocalSourceModule::class]
)
abstract class FakeLocalSourceModule {
    @Singleton
    @Binds
    abstract fun bindLocalSource(repo: FakePresentlyLocalSource): PresentlyLocalSource
}