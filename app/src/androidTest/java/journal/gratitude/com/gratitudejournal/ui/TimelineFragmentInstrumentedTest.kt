package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.DaggerTestApplicationRule
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.util.clickXY
import journal.gratitude.com.gratitudejournal.util.scroll
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineFragmentInstrumentedTest {

    private lateinit var repository: EntryRepository

    @get:Rule
    val rule = DaggerTestApplicationRule()

    @Before
    fun setupDaggerComponent() {
        repository = rule.component.entryRepository
    }

    @Test
    fun timelineFragment_showsTimeline() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme
        )
        onView(withId(R.id.timeline_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_clickCalendar_opensCalendar() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_openCalendar_clickingBack_closesCal() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.cal_fab)).perform(click())

        pressBack()

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_openCalendar_clickingClose_closesCal() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.close_button)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_openCalendar_clicksDate_opensEntry() {
        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.timelineFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        onView(withId(R.id.cal_fab)).perform(click())
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(150, 300))

        verify(mockNavController).navigate(eq(R.id.action_timelineFragment_to_entryFragment), any())
    }

    @Test
    fun timelineFragment_closedCalendar_clickingBack_navigatesBack() {
        val mockNavController = mock<NavController>()
        val scenario = launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme
        )
        var activity: Activity? = null
        scenario.onFragment { fragment ->
            activity = fragment.activity
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        onView(withId(R.id.cal_fab)).perform(click())
        onView(withId(R.id.close_button)).perform(click())

        Espresso.pressBackUnconditionally()

        assertTrue(activity!!.isFinishing)
    }

//    @Test
//    fun timelineFragment_clicksNewEntry_opensEntry() {
//
//    }
//
//    @Test
//    fun timelineFragment_clicksExistingEntry_opensEntry() {
//
//    }
//
//    @Test
//    fun timelineFragment_clicksOverflow_opensSettings() {
//
//    }
//
//    @Test
//    fun timelineFragment_clicksOverflow_opensContact() {
//
//    }
//
//    @Test
//    fun timelineFragment_clicksOverflow_backsUp() {
//
//    }
//
//    @Test
//    fun timelineFragment_clicksOverflow_imports() {
//
//    }
//
//    @Test
//    fun timelineFragment_clicksSearch() {
//
//    }

    private fun scrollCalendarBackwardsBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(scroll(100, 10, 300, 0))
        }
    }

}

