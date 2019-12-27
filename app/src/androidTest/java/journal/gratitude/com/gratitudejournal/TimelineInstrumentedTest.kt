package journal.gratitude.com.gratitudejournal

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nhaarman.mockitokotlin2.mock
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.util.clickXY
import journal.gratitude.com.gratitudejournal.util.scroll
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineInstrumentedTest {

    @Rule
    @JvmField
    var mActivityRule =
        ActivityTestRule(ContainerActivity::class.java)

    @Before
    fun setup() {

        val mockNavController = mock<NavController>()

        val scenario = launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

//        launchFragmentInContainer<TimelineFragment>(
//            themeResId = R.style.AppTheme)
    }

    @Test
    fun timelineFragment_showsTimeline() {
        onView(withId(R.id.timeline_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_clickCalendar_opensCalendar() {
        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_openCalendar_clickingBack_closesCal() {
        onView(withId(R.id.cal_fab)).perform(click())

        pressBack()

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_openCalendar_clickingClose_closesCal() {
        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.close_button)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_openCalendar_clicksDate_opensEntry() {

        onView(withId(R.id.cal_fab)).perform(click())
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(150, 300))

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))

        //TODO check that navigation works once navController bug is fixed

    }

    @Test
    fun timelineFragment_closedCalendar_clickingBack_navigatesBack() {
        onView(withId(R.id.cal_fab)).perform(click())
        onView(withId(R.id.close_button)).perform(click())

        Espresso.pressBackUnconditionally()

        assertTrue(mActivityRule.activity.isFinishing)
    }

    @Test
    fun timelineFragment_clicksNewEntry_opensEntry() {

    }
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

