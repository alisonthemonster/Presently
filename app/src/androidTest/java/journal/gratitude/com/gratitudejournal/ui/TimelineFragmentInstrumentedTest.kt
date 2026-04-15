package journal.gratitude.com.gratitudejournal.ui

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_VIEWED
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.testUtils.saveEntriesBlocking
import journal.gratitude.com.gratitudejournal.testUtils.scroll
import journal.gratitude.com.gratitudejournal.reminders.onboarding.ui.DayOneDialogFragment
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
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
    val composeRule = createAndroidComposeRule<ContainerActivity>()

    @get:Rule
    val intentsRule = IntentsRule()

    @Inject
    lateinit var repository: EntryRepository

    @Inject
    lateinit var settings: FakePresentlySettings

    @Inject
    lateinit var analytics: FakeAnalyticsLogger

    @Before
    fun init() {
        hiltRule.inject()
        settings.clearReminderOnboardingSeen()
        analytics.reset()
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
    fun timelineFragment_clickingSettingsButton_opensSettingsScreen() {
        val scenario = launchTimelineInContainerActivity()

        onView(withId(R.id.overflow_button)).perform(click())

        assertCurrentFragmentIs<SettingsFragment>(scenario)
    }

    @Test
    fun timelineFragment_clickingTimelineEntry_opensEntryScreen() {
        val today = LocalDate.now()
        repository.saveEntriesBlocking(listOf(Entry(today, "Test timeline entry")))
        val scenario = launchTimelineInContainerActivity()

        onView(withId(R.id.timeline_recycler_view))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        assertCurrentFragmentIs<EntryFragment>(scenario)
    }

    @Test
    fun timelineFragment_afterSavingFirstEntry_opensDayOneDialog() {
        launchTimelineWithFirstEntryScreenForCompose()

        onView(withId(R.id.entry_text)).perform(replaceText("First entry"), closeSoftKeyboard())
        composeRule.onNodeWithTag("entry_save_button").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.supportFragmentManager.findFragmentByTag(DayOneDialogFragment.TAG) != null
        }

        composeRule.activity.runOnUiThread {
            val dialogFragment =
                composeRule.activity.supportFragmentManager.findFragmentByTag(DayOneDialogFragment.TAG)
            assertThat(dialogFragment).isInstanceOf(DayOneDialogFragment::class.java)
        }
        assertThat(analytics.recordedEvents).contains(REMINDER_ONBOARDING_PROMPT_VIEWED)
    }

    private fun launchTimelineInContainerActivity(): ActivityScenario<ContainerActivity> {
        return ActivityScenario.launch(ContainerActivity::class.java).onActivity { activity ->
            activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNow()
        }
    }

    private fun launchTimelineWithFirstEntryScreen(): ActivityScenario<ContainerActivity> {
        return ActivityScenario.launch(ContainerActivity::class.java).onActivity { activity ->
            activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNow()

            activity.supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.container_fragment,
                    EntryFragment.newInstance(
                        date = LocalDate.now(),
                        numEntries = 0,
                        isNewEntry = true,
                        resources = activity.resources
                    )
                )
                .addToBackStack(TimelineFragment.TIMELINE_TO_ENTRY)
                .commit()

            activity.supportFragmentManager.executePendingTransactions()
        }
    }

    private fun launchTimelineWithFirstEntryScreenForCompose() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNow()

            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.container_fragment,
                    EntryFragment.newInstance(
                        date = LocalDate.now(),
                        numEntries = 0,
                        isNewEntry = true,
                        resources = composeRule.activity.resources
                    )
                )
                .addToBackStack(TimelineFragment.TIMELINE_TO_ENTRY)
                .commit()

            composeRule.activity.supportFragmentManager.executePendingTransactions()
        }
        composeRule.waitForIdle()
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
