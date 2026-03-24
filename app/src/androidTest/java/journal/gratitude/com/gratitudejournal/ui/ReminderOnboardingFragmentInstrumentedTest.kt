package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.fakes.FakeEntryRepository
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
class ReminderOnboardingFragmentInstrumentedTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ContainerActivity>()

    @Inject
    lateinit var repository: FakeEntryRepository

    @Inject
    lateinit var settings: FakePresentlySettings

    @Before
    fun setUp() {
        hiltRule.inject()
        repository.entriesDatabase.clear()
        settings.clearReminderOnboardingSeen()
    }

    @Test
    fun onboardingFragment_displaysComposeFlow() {
        launchReminderOnboardingFragment()

        composeRule.onNodeWithTag("reminder_onboarding_root").assertIsDisplayed()
    }

    @Test
    fun onboardingFragment_closeDismissesBackToTimeline() {
        launchReminderOnboardingOnTopOfTimeline()

        composeRule.onNodeWithTag("reminder_onboarding_close").performClick()
        composeRule.waitForIdle()

        assertCurrentFragmentIs<TimelineFragment>()
    }

    @Test
    fun savingFirstEntry_opensReminderOnboarding() {
        launchTimelineWithFirstEntryScreen()

        onView(withId(R.id.entry_text)).perform(typeText("First entry"))
        closeSoftKeyboard()
        onView(withId(R.id.save_button)).perform(click())

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.supportFragmentManager
                .findFragmentById(R.id.container_fragment) is ReminderOnboardingFragment
        }

        assertCurrentFragmentIs<ReminderOnboardingFragment>()
    }

    private fun launchReminderOnboardingFragment() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, ReminderOnboardingFragment())
                .commitNow()
        }
        composeRule.waitForIdle()
    }

    private fun launchReminderOnboardingOnTopOfTimeline() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNow()

            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, ReminderOnboardingFragment())
                .addToBackStack(TimelineFragment.TIMELINE_TO_REMINDER_ONBOARDING)
                .commit()

            composeRule.activity.supportFragmentManager.executePendingTransactions()
        }
        composeRule.waitForIdle()
    }

    private fun launchTimelineWithFirstEntryScreen() {
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
        val fragment = composeRule.activity.supportFragmentManager.findFragmentById(R.id.container_fragment)
        assertThat(fragment).isInstanceOf(T::class.java)
    }
}
