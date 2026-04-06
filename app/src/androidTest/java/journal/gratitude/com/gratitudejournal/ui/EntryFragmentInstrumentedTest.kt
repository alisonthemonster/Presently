package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.HiltTestActivity
import journal.gratitude.com.gratitudejournal.testUtils.saveEntryBlocking
import journal.gratitude.com.gratitudejournal.ui.entry.EntryArgs
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EntryFragmentInstrumentedTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @get:Rule(order = 2)
    val intentsRule = IntentsRule()

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun todaysEntry_showsTodayDateStrings() {
        val date = LocalDate.now()
        repository.saveEntryBlocking(Entry(date, "test content"))

        launchEntryFragment(EntryArgs(date.toString(), false, 1, "quote", "hint", emptyList()))

        composeRule.onNodeWithText("Today").assertIsDisplayed()
        composeRule.onNodeWithText("I am grateful for").assertIsDisplayed()
    }

    @Test
    fun yesterdaysEntry_showsYesterdayDateStrings() {
        val date = LocalDate.now().minusDays(1)
        repository.saveEntryBlocking(Entry(date, "Lorem ipsum dolor sit amet, consectetur adipiscing elit."))

        launchEntryFragment(EntryArgs(date.toString(), false, 1, "quote", "hint", emptyList()))

        composeRule.onNodeWithText("Yesterday").assertIsDisplayed()
        composeRule.onNodeWithText("I was grateful for").assertIsDisplayed()
    }

    @Test
    fun writtenEntry_showsShareButton() {
        val date = LocalDate.of(2019, 3, 22)
        repository.saveEntryBlocking(Entry(date, "test content"))

        launchEntryFragment(EntryArgs(date.toString(), false, 1, "quote", "hint", emptyList()))

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("entry_share_button").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("entry_share_button").assertIsDisplayed()
        composeRule.onAllNodesWithTag("entry_prompt_button").assertCountEquals(0)
    }

    @Test
    fun noEntry_showsPromptButton() {
        val date = LocalDate.of(2019, 3, 23)

        launchEntryFragment(EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_prompt_button").assertIsDisplayed()
        composeRule.onAllNodesWithTag("entry_share_button").assertCountEquals(0)
    }

    @Test
    fun newEntry_autoFocusesTextField() {
        val date = LocalDate.of(2019, 3, 23)

        launchEntryFragment(EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_text_field").assertIsFocused()
        assertImeVisibility(isVisible = true)
    }

    @Test
    fun existingEntry_doesNotAutoFocusTextField() {
        val date = LocalDate.of(2019, 3, 23)
        repository.saveEntryBlocking(Entry(date, "Existing entry"))

        launchEntryFragment(EntryArgs(date.toString(), false, 1, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_text_field").assertIsNotFocused()
        assertImeVisibility(isVisible = false)
    }

    @Test
    fun promptButton_changesHintText() {
        val date = LocalDate.of(2019, 3, 23)

        launchEntryFragment(
            EntryArgs(date.toString(), true, 0, "quote", "first hint", listOf("second hint"))
        )

        composeRule.onNodeWithText("first hint").assertIsDisplayed()
        composeRule.onNodeWithTag("entry_prompt_button").performClick()
        composeRule.onNodeWithText("second hint").assertIsDisplayed()
    }

    @Test
    fun typedEntry_survivesActivityRecreation() {
        val date = LocalDate.of(2019, 3, 23)
        launchEntryFragment(EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_text_field").performTextInput("Draft that should survive")
        composeRule.waitForIdle()

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Draft that should survive").assertIsDisplayed()
        composeRule.onNodeWithTag("entry_save_button").assertIsDisplayed()
    }

    @Test
    fun saveButton_onMilestone_showsMilestoneDialog() {
        val date = LocalDate.of(2019, 3, 22)

        launchEntryFragment(EntryArgs(date.toString(), true, 4, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_text_field").performTextInput("Test string!")
        composeRule.onNodeWithTag("entry_save_button").performClick()
        composeRule.waitForIdle()

        onView(withText("Share your achievement")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun saveButton_onMilestone_clickRateOpensStore() {
        val date = LocalDate.of(2019, 3, 22)
        val marketUri = Uri.parse("market://details?id=journal.gratitude.com.gratitudejournal")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=journal.gratitude.com.gratitudejournal")

        intending(
            anyOf(
                allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    IntentMatchers.hasData(marketUri)
                ),
                allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    IntentMatchers.hasData(webUri)
                )
            )
        ).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))

        launchEntryFragment(EntryArgs(date.toString(), true, 4, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_text_field").performTextInput("Test string!")
        composeRule.onNodeWithTag("entry_save_button").performClick()
        composeRule.waitForIdle()

        onView(withId(R.id.rate_presently)).perform(androidx.test.espresso.action.ViewActions.click())

        intended(
            anyOf(
                allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    IntentMatchers.hasData(marketUri)
                ),
                allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    IntentMatchers.hasData(webUri)
                )
            )
        )
    }

    @Test
    fun saveButton_onMilestone_clickShare_opensShareDialog() {
        val date = LocalDate.of(2019, 3, 22)

        launchEntryFragment(EntryArgs(date.toString(), true, 4, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_text_field").performTextInput("Test string!")
        composeRule.onNodeWithTag("entry_save_button").performClick()
        composeRule.waitForIdle()

        onView(withId(R.id.share_presently)).perform(androidx.test.espresso.action.ViewActions.click())

        intended(
            allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
                IntentMatchers.hasExtra(Intent.EXTRA_TITLE, "Share your gratitude")
            )
        )
    }

    @Test
    fun entryFragment_longPressQuote_copiesToClipboard() {
        val date = LocalDate.of(2019, 3, 23)
        val args = EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList())
        launchEntryFragment(args)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        lateinit var clipboard: ClipboardManager
        instrumentation.runOnMainSync {
            clipboard = targetContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Gratitude quote", ""))
        }

        composeRule.onNodeWithTag("entry_quote").performTouchInput { longClick() }
        composeRule.waitForIdle()

        var copiedText: String? = null
        instrumentation.runOnMainSync {
            copiedText = clipboard.primaryClip
                ?.getItemAt(0)
                ?.coerceToText(targetContext)
                ?.toString()
        }

        com.google.common.truth.Truth.assertThat(copiedText).isEqualTo("quote")
    }

    @Test
    fun entryFragment_makeEdit_navigatesBack() {
        val date = LocalDate.of(2019, 3, 22)
        launchEntryFragment(EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList()))

        composeRule.onNodeWithTag("entry_text_field").performTextInput("Test string!Yeehaw!")
        composeRule.waitForIdle()

        composeRule.activity.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        onView(withText(R.string.are_you_sure)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(android.R.id.button2)).inRoot(isDialog())
            .perform(androidx.test.espresso.action.ViewActions.click())
        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())

        composeRule.activity.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        onView(withId(android.R.id.button1)).inRoot(isDialog())
            .perform(androidx.test.espresso.action.ViewActions.click())
        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun entryFragment_noEdit_navigatesBack_noDialog() {
        val date = LocalDate.of(2019, 3, 22)
        launchEntryFragment(EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList()))

        composeRule.activity.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())
    }

    private fun launchEntryFragment(args: EntryArgs) {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(
                    android.R.id.content,
                    EntryFragment().apply {
                        arguments = bundleOf(EntryFragment.ENTRY_ARGS_KEY to args)
                    }
                )
                .commitNow()
        }
        composeRule.waitForIdle()
    }

    private fun assertImeVisibility(isVisible: Boolean) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            ViewCompat.getRootWindowInsets(composeRule.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == isVisible
        }
    }
}
