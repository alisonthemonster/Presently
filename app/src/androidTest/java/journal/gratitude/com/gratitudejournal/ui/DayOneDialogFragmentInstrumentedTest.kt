package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.fakes.FakeEntryRepository
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_ACCEPTED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_DISMISSED
import journal.gratitude.com.gratitudejournal.reminders.onboarding.ui.DayOneDialogFragment
import journal.gratitude.com.gratitudejournal.reminders.onboarding.ui.ReminderOnboardingFragment
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DayOneDialogFragmentInstrumentedTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ContainerActivity>()

    @Inject
    lateinit var repository: FakeEntryRepository

    @Inject
    lateinit var settings: FakePresentlySettings

    @Inject
    lateinit var analytics: FakeAnalyticsLogger

    @Before
    fun setUp() {
        hiltRule.inject()
        repository.entriesDatabase.clear()
        settings.clearReminderOnboardingSeen()
        analytics.reset()
    }

    @Test
    fun savingFirstEntry_dismissingDayOneDialog_closesPromptAndStaysOnTimeline() {
        launchTimelineWithFirstEntryScreen()

        onView(withId(R.id.entry_text)).perform(replaceText("First entry"), closeSoftKeyboard())
        composeRule.onNodeWithTag("entry_save_button").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.supportFragmentManager
                .findFragmentByTag(DayOneDialogFragment.TAG) != null
        }

        pressBack()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.supportFragmentManager
                .findFragmentByTag(DayOneDialogFragment.TAG) == null
        }

        assertCurrentFragmentIs<TimelineFragment>()
        assertThat(settings.hasSeenReminderOnboarding()).isFalse()
        assertThat(analytics.recordedEvents).contains(REMINDER_ONBOARDING_PROMPT_DISMISSED)
    }

    @Test
    fun savingFirstEntry_dayOneDialogCtaOpensReminderOnboarding() {
        launchTimelineWithFirstEntryScreen()

        onView(withId(R.id.entry_text)).perform(replaceText("First entry"), closeSoftKeyboard())
        composeRule.onNodeWithTag("entry_save_button").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.supportFragmentManager
                .findFragmentByTag(DayOneDialogFragment.TAG) != null
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.reminder_prompt_cta))
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.supportFragmentManager
                .findFragmentById(R.id.container_fragment) is ReminderOnboardingFragment
        }

        assertCurrentFragmentIs<ReminderOnboardingFragment>()
        assertThat(settings.hasSeenReminderOnboarding()).isTrue()
        assertThat(analytics.recordedEvents).contains(REMINDER_ONBOARDING_PROMPT_ACCEPTED)
        assertThat(analytics.recordedEvents).doesNotContain(REMINDER_ONBOARDING_PROMPT_DISMISSED)
    }

    private fun launchTimelineWithFirstEntryScreen() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNowAllowingStateLoss()

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
                .commitAllowingStateLoss()
        }
        composeRule.waitForIdle()
    }

    private inline fun <reified T : Fragment> assertCurrentFragmentIs() {
        val fragment = composeRule.activity.supportFragmentManager.findFragmentById(R.id.container_fragment)
        assertThat(fragment).isInstanceOf(T::class.java)
    }
}
