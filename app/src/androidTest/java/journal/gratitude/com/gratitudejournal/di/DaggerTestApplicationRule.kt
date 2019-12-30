package journal.gratitude.com.gratitudejournal.di

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that creates a [TestApplicationComponent] and injects the [TestGratitudeApplication].
 *
 * Note that the `testInstrumentationRunner` property needs to point to [CustomTestRunner].
 */
class DaggerTestApplicationRule : TestWatcher() {

    lateinit var component: TestApplicationComponent
        private set

    override fun starting(description: Description?) {
        super.starting(description)

        val app = ApplicationProvider.getApplicationContext<Context>() as TestGratitudeApplication
        val application = ApplicationProvider.getApplicationContext<Context>() as Application

        component = DaggerTestApplicationComponent.factory().create(app, application)
        component.inject(app)

    }
}