package journal.gratitude.com.gratitudejournal

import android.app.Application
import com.airbnb.mvrx.Mavericks
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.EntryPointAccessors
import journal.gratitude.com.gratitudejournal.di.WidgetEntryPoint
import journal.gratitude.com.gratitudejournal.widget.EntryCountWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GratitudeApplication : BaseGratitudeApplication() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        refreshWidgetOnEntryCountChange()
    }

    // Keeps the home screen widget's entry count current whenever entries are added,
    // deleted or restored. Lives here (not in the base class) because instrumented tests
    // replace the database module.
    private fun refreshWidgetOnEntryCountChange() {
        applicationScope.launch {
            EntryPointAccessors.fromApplication(this@GratitudeApplication, WidgetEntryPoint::class.java)
                .entryDao
                .getEntryCount()
                .distinctUntilChanged()
                .drop(1) // the widget renders its own initial value
                .collect { EntryCountWidget().updateAll(this@GratitudeApplication) }
        }
    }
}

open class BaseGratitudeApplication : Application(), Configuration.Provider {

    // Hilt test applications cannot use field injection, so use an entry point instead.
    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface ApplicationEarlyEntryPoint {
        fun getWorkerFactory(): HiltWorkerFactory
    }

    override val workManagerConfiguration: Configuration
        get() {
            val earlyEntryPoint = EarlyEntryPoints.get(this, ApplicationEarlyEntryPoint::class.java)
            return Configuration.Builder()
                .setWorkerFactory(earlyEntryPoint.getWorkerFactory())
                .build()
        }

    override fun onCreate() {
        super.onCreate()

        Mavericks.initialize(this)
        // TODO: If Mavericks runtime mocking is needed again, re-enable it only after upgrading
        // to a version that is compatible with Android 13+ dynamic receiver registration rules.
    }
}
