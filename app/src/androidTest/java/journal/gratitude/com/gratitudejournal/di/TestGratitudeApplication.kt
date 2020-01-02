package journal.gratitude.com.gratitudejournal.di

import androidx.fragment.app.Fragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import journal.gratitude.com.gratitudejournal.GratitudeApplication
import javax.inject.Inject

/**
 * An application used from instrumentation tests. It has a fragment injector to enable
 * tests using FragmentScenario.
 */
class TestGratitudeApplication : GratitudeApplication(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector() = fragmentInjector
}