package journal.gratitude.com.gratitudejournal.ui

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.wiring.AnalyticsModule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAnalyticsLogger @Inject constructor() : AnalyticsLogger {
    val recordedEvents = mutableListOf<String>()
    val recordedDetailedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    override fun recordEvent(event: String) {
        recordedEvents += event
    }

    override fun recordEvent(event: String, details: Map<String, Any>) {
        recordedEvents += event
        recordedDetailedEvents += event to details
    }

    override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit

    override fun recordEntryAdded(numEntries: Int) = Unit

    override fun recordView(viewName: String) = Unit

    override fun optOutOfAnalytics() = Unit

    override fun optIntoAnalytics() = Unit

    fun reset() {
        recordedEvents.clear()
        recordedDetailedEvents.clear()
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsModule::class]
)
abstract class FakeAnalyticsBindingModule {
    @Singleton
    @Binds
    abstract fun bindAnalyticsLogger(logger: FakeAnalyticsLogger): AnalyticsLogger
}
