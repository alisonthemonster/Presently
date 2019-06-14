package journal.gratitude.com.gratitudejournal

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TimelineInstrumentedTest {

    @Test
    fun timelineFragment_showsEntries() {
        launchFragmentInContainer<TimelineFragment>()

        onView(withId(R.id.title)).check(matches(withText("Presently")))
    }

}

