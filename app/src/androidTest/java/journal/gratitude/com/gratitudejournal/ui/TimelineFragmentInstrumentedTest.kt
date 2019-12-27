package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.DaggerTestApplicationRule
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragmentDirections
import journal.gratitude.com.gratitudejournal.util.clickXY
import journal.gratitude.com.gratitudejournal.util.saveEntryBlocking
import journal.gratitude.com.gratitudejournal.util.scroll
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate

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
        //TODO make sure to change calendar back a month to make sure we're selecting a date in the past
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

    @Test
    fun timelineFragment_clicksNewEntry_opensEntry() {
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

        onView(withId(R.id.timeline_recycler_view))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        verify(mockNavController).navigate(eq(R.id.action_timelineFragment_to_entryFragment), any())
    }

    @Test
    fun timelineFragment_clicksExistingEntry_opensEntry() {
        val mockEntry = Entry(LocalDate.now(), "test content")
        repository.saveEntryBlocking(mockEntry)

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

        onView(withId(R.id.timeline_recycler_view))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        verify(mockNavController).navigate(eq(R.id.action_timelineFragment_to_entryFragment), any())
    }

    @Test
    fun timelineFragment_clicksOverflow_opensSettings() {
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

        onView(withId(R.id.overflow_button)).perform(click())

        onView(withText("Settings"))
            .perform(click())

        verify(mockNavController).navigate(eq(R.id.action_timelineFragment_to_settingsFragment))
    }

    @Test
    fun timelineFragment_clicksOverflow_opensContact() {
        Intents.init()
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.AppTheme
        )

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(intentResult)

        onView(withId(R.id.overflow_button)).perform(click())

        onView(withText("Contact Us"))
            .perform(click())

        val subject = "In App Feedback"
        val uri =  Uri.parse("mailto:gratitude.journal.app@gmail.com?subject=$subject")

        Intents.intended(
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(uri)
            )
        )
        Intents.release()

    }

//    @Test
//    fun timelineFragment_clicksOverflow_backsUp() {
//        launchFragmentInContainer<TimelineFragment>(
//            themeResId = R.style.AppTheme
//        )
//
//        val intent = Intent()
//        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
//        Intents.intending(IntentMatchers.anyIntent()).respondWith(intentResult)
//
//        onView(withId(R.id.overflow_button)).perform(click())
//
//        onView(withText("Backup Entries"))
//            .perform(click())
//
//        //TODO find out about testing permissions and looking for toast
//    }
//
//    @Test
//    fun timelineFragment_clicksOverflow_clicksImport_seesDialog() {
//        launchFragmentInContainer<TimelineFragment>(
//            themeResId = R.style.AppTheme
//        )
//
//        val intent = Intent()
//        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
//        Intents.intending(IntentMatchers.anyIntent()).respondWith(intentResult)
//
//        onView(withId(R.id.overflow_button)).perform(click())
//
//        onView(withText("Import entries from backup"))
//            .perform(click())
//
//
//        //TODO check dialog is good
//    }
//
//    @Test
//    fun timelineFragment_clicksOverflow_clicksImport_cancelsDialog() {
//        launchFragmentInContainer<TimelineFragment>(
//            themeResId = R.style.AppTheme
//        )
//
//        val intent = Intent()
//        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
//        Intents.intending(IntentMatchers.anyIntent()).respondWith(intentResult)
//
//        onView(withId(R.id.overflow_button)).perform(click())
//
//        onView(withText("Import entries from backup"))
//            .perform(click())
//
//
//        //TODO click cancel in dialog
//    }
//
//    @Test
//    fun timelineFragment_clicksOverflow_clicksImport_agreesToImport() {
//        launchFragmentInContainer<TimelineFragment>(
//            themeResId = R.style.AppTheme
//        )
//
//        val intent = Intent()
//        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
//        Intents.intending(IntentMatchers.anyIntent()).respondWith(intentResult)
//
//        onView(withId(R.id.overflow_button)).perform(click())
//
//        onView(withText("Import entries from backup"))
//            .perform(click())
//
//
//        //TODO verify file picker opens
//            //TODO how to verify file gets selected properly?
//    }

    @Test
    fun timelineFragment_clicksSearch() {
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

        onView(withId(R.id.search_icon)).perform(click())

        verify(mockNavController).navigate(eq(TimelineFragmentDirections.actionTimelineFragmentToSearchFragment()), any<Navigator.Extras>())

    }

    private fun scrollCalendarBackwardsBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(scroll(100, 10, 300, 0))
        }
    }

}

