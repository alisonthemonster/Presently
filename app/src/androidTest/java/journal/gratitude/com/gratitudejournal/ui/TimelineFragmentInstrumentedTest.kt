package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.testUtils.saveEntriesBlocking
import journal.gratitude.com.gratitudejournal.testUtils.scroll
import journal.gratitude.com.gratitudejournal.ui.entryviewpager.EntryViewPagerFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
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

    @get:Rule
    val intentsRule = IntentsRule()

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
        launchFragmentInHiltContainer<TimelineFragment>()

        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_openCalendar_clickingBack_closesCal() {
        launchFragmentInHiltContainer<TimelineFragment>()

        onView(withId(R.id.cal_fab)).perform(click())

        pressBack()

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_openCalendar_clickingClose_closesCal() {
        launchFragmentInHiltContainer<TimelineFragment>()

        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.close_button)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_clickingSearchIcon_opensSearchScreen() {
        val scenario = launchTimelineInContainerActivity()

        onView(withId(R.id.search_icon)).perform(click())

        assertCurrentFragmentIs<SearchFragment>(scenario)
    }

    @Test
    fun timelineFragment_clickingSettingsMenu_opensSettingsScreen() {
        val scenario = launchTimelineInContainerActivity()

        onView(withId(R.id.overflow_button)).perform(click())
        onView(withText(R.string.notification_settings)).perform(click())

        assertCurrentFragmentIs<SettingsFragment>(scenario)
    }

    @Test
    fun timelineFragment_clickingTimelineEntry_opensEntryViewPagerScreen() {
        val today = LocalDate.now()
        repository.saveEntriesBlocking(listOf(Entry(today, "Test timeline entry")))
        val scenario = launchTimelineInContainerActivity()

        onView(withId(R.id.timeline_recycler_view))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        assertCurrentFragmentIs<EntryViewPagerFragment>(scenario)
        onView(withId(R.id.view_pager)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_clicksOverflow_opensContact() {
        launchFragmentInHiltContainer<TimelineFragment>()

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
    }

    private fun launchTimelineInContainerActivity(): ActivityScenario<ContainerActivity> {
        return ActivityScenario.launch(ContainerActivity::class.java).onActivity { activity ->
            activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNow()
        }
    }

    private inline fun <reified T : Fragment> assertCurrentFragmentIs(
        scenario: ActivityScenario<ContainerActivity>
    ) {
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.container_fragment)
            assertThat(fragment).isInstanceOf(T::class.java)
        }
    }

    private fun scrollCalendarBackwardsBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(
                scroll(100, 300, 300, 250)
            )
        }
    }

}
