package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
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
import journal.gratitude.com.gratitudejournal.testUtils.scroll
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineScreenTags
import org.hamcrest.CoreMatchers.allOf
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
            composeRule.activity.findViewById<View>(R.id.entry_calendar) != null
        }
    }

    @Test
    fun timelineFragment_openCalendar_clickingBack_closesCal() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.findViewById<View>(R.id.entry_calendar) != null
        }

        pressBack()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.findViewById<View>(R.id.entry_calendar) == null
        }
    }

    @Test
    fun timelineFragment_openCalendar_clickingClose_closesCal() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.findViewById<View>(R.id.entry_calendar) != null
        }

        onView(withId(R.id.close_button)).perform(click())

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.findViewById<View>(R.id.entry_calendar) == null
        }
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
    fun timelineFragment_clickingSettingsMenu_opensSettingsScreen() {
        launchTimelineInComposeContainer()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.settings)
        ).performClick()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.notification_settings)
        ).performClick()

        assertCurrentFragmentIs<SettingsFragment>()
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

    @Test
    fun timelineFragment_clicksOverflow_opensContact() {
        launchTimelineInComposeContainer()

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(anyIntent()).respondWith(intentResult)

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.settings)
        ).performClick()
        composeRule.onNodeWithText("Contact Us").performClick()

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

    private fun scrollCalendarBackwardsBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(
                scroll(100, 300, 300, 250)
            )
        }
    }
}
