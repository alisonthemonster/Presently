package journal.gratitude.com.gratitudejournal

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.util.RecyclerViewItemCountAssertion.Companion.withItemCount
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineInstrumentedTest {

    @Test
    fun timelineFragment_showsEntries() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme)

        onView(withId(R.id.timeline_recycler_view)).check(withItemCount(2))
    }

}

