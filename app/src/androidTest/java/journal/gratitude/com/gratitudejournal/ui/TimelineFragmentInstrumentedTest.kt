package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_VIEWED
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.reminders.onboarding.ui.DayOneDialogFragment
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.saveEntriesBlocking
import journal.gratitude.com.gratitudejournal.ui.calendar.EntryCalendarTags
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.settings.BackupSettingsFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineScreenTags
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
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.LIST).assertIsDisplayed()
    }

    @Test
    fun timelineFragment_clickCalendar_opensCalendar() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR).assertIsDisplayed()
    }

    @Test
    fun timelineFragment_openCalendar_clickingBack_closesCal() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isNotEmpty()
        }

        pressBack()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun timelineFragment_openCalendar_clickingClose_closesCal() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(EntryCalendarTags.CLOSE_BUTTON).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun timelineFragment_openCalendar_clickingWrittenDate_opensEntryScreen() {
        val today = LocalDate.now()
        repository.saveEntriesBlocking(listOf(Entry(today, "Calendar entry")))
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(EntryCalendarTags.day(today)).performClick()

        assertCurrentFragmentIs<EntryFragment>()
    }

    @Test
    fun timelineFragment_openCalendar_withEntries_showsRandomButton() {
        repository.saveEntriesBlocking(listOf(Entry(LocalDate.now(), "Calendar entry")))
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(EntryCalendarTags.RANDOM_BUTTON).assertIsDisplayed()
    }

    @Test
    fun timelineFragment_openCalendar_withoutEntries_hidesRandomButton() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TimelineScreenTags.CALENDAR).fetchSemanticsNodes().isNotEmpty()
        }
        assertThat(
            composeRule.onAllNodesWithTag(EntryCalendarTags.RANDOM_BUTTON).fetchSemanticsNodes()
        ).isEmpty()
    }

    @Test
    fun timelineFragment_clickingSearchIcon_opensSearchScreen() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.search)
        ).performClick()

        assertCurrentFragmentIs<SearchFragment>()
    }

    @Test
    fun timelineFragment_clickingSettingsButton_opensSettingsScreen() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.settings)
        ).performClick()

        assertCurrentFragmentIs<SettingsFragment>()
    }

    @Test
    fun settingsScreen_clickingBackupAndRestore_opensBackupSettingsScreen() {
        val scenario = launchTimelineInContainerActivity()

        onView(withId(R.id.overflow_button)).perform(click())
        onView(withText(R.string.notification_settings)).perform(click())
        onView(withText(R.string.backup_and_restore_summary)).perform(scrollTo(), click())

        assertCurrentFragmentIs<BackupSettingsFragment>(scenario)
    }

    @Test
    fun timelineFragment_clickingTimelineEntry_opensEntryScreen() {
        val today = LocalDate.now()
        repository.saveEntriesBlocking(listOf(Entry(today, "Test timeline entry")))
        launchTimelineInComposeContainer()

        composeRule.onNodeWithText("Test timeline entry").performClick()

        assertCurrentFragmentIs<EntryFragment>()
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

    private fun launchTimelineInComposeContainer() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNow()
        }
        composeRule.waitForIdle()
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

    private inline fun <reified T : Fragment> assertCurrentFragmentIs() {
        composeRule.activity.runOnUiThread {
            val fragment =
                composeRule.activity.supportFragmentManager.findFragmentById(R.id.container_fragment)
            assertThat(fragment).isInstanceOf(T::class.java)
        }
    }
}
