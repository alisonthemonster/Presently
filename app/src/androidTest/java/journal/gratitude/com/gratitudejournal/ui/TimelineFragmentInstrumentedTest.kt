package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.navigation.*
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.clickXY
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.testUtils.saveEntryBlocking
import journal.gratitude.com.gratitudejournal.testUtils.scroll
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragmentDirections
import kotlinx.android.synthetic.main.fragment_milestone_dialog.*
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TimelineFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun timelineFragment_showsTimeline() {
        launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        onView(withId(R.id.timeline_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_clickCalendar_opensCalendar() {
        launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )

        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_openCalendar_clickingBack_closesCal() {
        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.timelineFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.view, mockNavController)
        }

        onView(withId(R.id.cal_fab)).perform(click())

        pressBack()

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_openCalendar_clickingClose_closesCal() {
        launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
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
        val scenario = launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.view, mockNavController)
        }

        onView(withId(R.id.cal_fab)).perform(click())
        scrollCalendarBackwardsBy(1)
        onView(withId(R.id.compactcalendar_view)).perform(
            clickXY(
                150,
                300
            )
        )

        verify(mockNavController).navigate(any<NavDirections>())
    }

    @Test
    fun timelineFragment_closedCalendar_clickingBack_navigatesBack() {
        val mockNavController = mock<NavController>()
        val scenario = launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        var activity: Activity? = null
        scenario!!.onActivity { act ->
            activity = act
            Navigation.setViewNavController(act.view, mockNavController)
        }

        onView(withId(R.id.cal_fab)).perform(click())
        onView(withId(R.id.close_button)).perform(click())

        Espresso.pressBackUnconditionally()

        assertTrue(activity!!.isFinishing)
    }

    @Test
    fun timelineFragment_clicksNewEntry_opensEntry() {
        val expectedDate = LocalDate.now()
        val expected = TimelineFragmentDirections.actionTimelineFragmentToEntryFragment(
            expectedDate.toString(),
            true,
            0
        )

        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.timelineFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.requireViewById(android.R.id.content), mockNavController)
        }

        onView(withId(R.id.timeline_recycler_view))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        verify(mockNavController).navigate(expected)
    }

    @Test
    fun timelineFragment_clicksExistingEntry_opensEntry() {
        val expectedDate = LocalDate.now()
        val mockEntry = Entry(expectedDate, "test content")
        val expected = TimelineFragmentDirections.actionTimelineFragmentToEntryFragment(
            expectedDate.toString(),
            false,
            1
        )
        repository.saveEntryBlocking(mockEntry)

        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.timelineFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.requireViewById(android.R.id.content), mockNavController)
        }

        onView(withId(R.id.timeline_recycler_view))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        verify(mockNavController).navigate(expected)
    }

    @Test
    fun timelineFragment_clicksOverflow_opensSettings() {
        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.timelineFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.requireViewById(android.R.id.content), mockNavController)
        }

        onView(withId(R.id.overflow_button)).perform(click())

        onView(withText("Settings"))
            .perform(click())

        verify(mockNavController).navigate(eq(R.id.action_timelineFragment_to_settingsFragment))
    }

    @Test
    fun timelineFragment_clicksOverflow_opensContact() {
        Intents.init()
        launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(anyIntent()).respondWith(intentResult)

        onView(withId(R.id.overflow_button)).perform(click())

        onView(withText("Contact Us"))
            .perform(click())

        val emails = arrayOf("gratitude.journal.app@gmail.com")
        val subject = "In App Feedback"
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageName = context.packageName
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val text = """
                Device: ${Build.MODEL}
                OS Version: ${Build.VERSION.RELEASE}
                App Version: ${packageInfo.versionName}
                
                
                """.trimIndent()

        Intents.intended(
            allOf(
                hasAction(Intent.ACTION_SENDTO),
                hasExtra(Intent.EXTRA_EMAIL, emails),
                hasExtra(Intent.EXTRA_SUBJECT, subject),
                hasExtra(Intent.EXTRA_TEXT, text)
            )
        )
        Intents.release()
    }

    @Test
    fun timelineFragment_clicksSearch() {
        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.timelineFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInHiltContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.requireViewById(android.R.id.content), mockNavController)
        }

        onView(withId(R.id.search_icon)).perform(click())

        verify(mockNavController).navigate(
            eq(TimelineFragmentDirections.actionTimelineFragmentToSearchFragment()),
            any<Navigator.Extras>()
        )
    }

    private fun scrollCalendarBackwardsBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(
                scroll(100, 300, 300, 250)
            )
        }
    }

}

