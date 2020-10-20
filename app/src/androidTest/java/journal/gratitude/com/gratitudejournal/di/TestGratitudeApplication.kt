package journal.gratitude.com.gratitudejournal.di

import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import journal.gratitude.com.gratitudejournal.GratitudeApplication
import javax.inject.Inject

/**
 * An application used from instrumentation tests. It has a fragment injector to enable
 * tests using FragmentScenario.
 */
class TestGratitudeApplication : GratitudeApplication(), HasAndroidInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = fragmentInjector
}