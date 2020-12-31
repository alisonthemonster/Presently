package journal.gratitude.com.gratitudejournal.di

import com.presently.analytics.PresentlyAnalytics
import dagger.Module
import dagger.Provides
import journal.gratitude.com.gratitudejournal.fakes.FakeEntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import javax.inject.Singleton

@Module
class TestApplicationModule {

    @Singleton
    @Provides
    fun provideRepository(): EntryRepository =
        FakeEntryRepository()

    @Singleton
    @Provides
    fun provideAnalytics(): PresentlyAnalytics {
        return object : PresentlyAnalytics {
            override fun recordEvent(event: String) = Unit

            override fun recordEvent(event: String, details: Map<String, Any>) = Unit

            override fun recordView(viewName: String) = Unit

            override fun optOutOfAnalytics()  = Unit
        }
    }

}
