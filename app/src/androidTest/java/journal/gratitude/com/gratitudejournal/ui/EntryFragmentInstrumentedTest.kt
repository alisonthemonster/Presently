package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.airbnb.mvrx.asMavericksArgs
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import junit.framework.TestCase.assertEquals
import journal.gratitude.com.gratitudejournal.ui.entry.EntryArgs
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EntryFragmentInstrumentedTest {

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
    fun todaysEntry_showsTodayDateStrings() {
        val date = LocalDate.now()

        val mockEntry = Entry(date, "test content")
        repository.saveEntryBlocking(mockEntry)

        val args = EntryArgs(date.toString(), false, 1, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.date)).check(matches(withText("Today")))
        onView(withId(R.id.thankful_for)).check(matches(withText("I am grateful for")))
    }

    @Test
    fun yesterdaysEntry_showsYesterdayDateStrings() {
        val date = LocalDate.now().minusDays(1)

        val mockEntry = Entry(date, "Yesterday's entry hello!")
        repository.saveEntryBlocking(mockEntry)

        val args = EntryArgs(date.toString(), false, 1, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.date)).check(matches(withText("Yesterday")))
        onView(withId(R.id.thankful_for)).check(matches(withText("I was grateful for")))
    }

    @Test
    fun writtenEntry_showsShareButton() {
        val date = LocalDate.of(2019, 3, 22)
        val mockEntry = Entry(date, "test content")
        repository.saveEntryBlocking(mockEntry)

        val args = EntryArgs(date.toString(), false, 1, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.share_button))
            .check(matches(isDisplayed()))
        onView(withId(R.id.prompt_button))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun noEntry_showsPromptButton() {
        val date = LocalDate.of(2019, 3, 23)

        val args = EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.share_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.prompt_button)).check(matches(isDisplayed()))
    }

    @Test
    fun promptButton_changesHintText() {
        val date = LocalDate.of(2019, 3, 23)

        val args = EntryArgs(date.toString(), true, 0, "quote", "first hint", listOf("second hint"))


        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )


        onView(withId(R.id.entry_text)).check(matches(withHint("first hint")))
        onView(withId(R.id.prompt_button)).perform(click())
        onView(withId(R.id.entry_text)).check(matches(withHint("second hint")))
    }

    @Test
    fun saveButton_onMilestone_showsMilestoneDialog() {
        val date = LocalDate.of(2019, 3, 22)

        val args = EntryArgs(date.toString(), true, 4, "quote", "hint", emptyList())


        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.entry_text)).perform(
            typeText("Test string!"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_button)).perform(click())

        onView(withText("Share your achievement")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun saveButton_onMilestone_clickRateOpensStore() {
        val date = LocalDate.of(2019, 3, 22)

        val args = EntryArgs(date.toString(), true, 4, "quote", "hint", emptyList())
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

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.entry_text)).perform(
            typeText("Test string!"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_button)).perform(click())

        onView(withText("Share your achievement")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.rate_presently)).perform(click())

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

        val args = EntryArgs(date.toString(), true, 4, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.entry_text)).perform(
            typeText("Test string!"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_button)).perform(click())

        onView(withId(R.id.share_presently)).perform(click())

        androidx.test.espresso.intent.Intents.intended(
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


        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        val quote =
            getText(withId(R.id.inspiration))

        onView(withId(R.id.inspiration)).perform(longClick())
        onView(withId(R.id.entry_text)).perform(click())
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressKeyCode(KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_MASK)


        onView(withId(R.id.entry_text)).check(matches(
            isEditTextValueEqualTo(
                quote
            )
        ))
    }

    @Test
    fun entryFragment_longPressQuote_showsToast() {
        val date = LocalDate.of(2019, 3, 22)

        val args = EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        lateinit var clipboard: ClipboardManager
        instrumentation.runOnMainSync {
            clipboard = targetContext
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Gratitude quote", ""))
        }

        onView(withId(R.id.inspiration)).perform(longClick())

        var copiedText: String? = null
        instrumentation.runOnMainSync {
            copiedText = clipboard.primaryClip
                ?.getItemAt(0)
                ?.coerceToText(targetContext)
                ?.toString()
        }
        assertEquals("quote", copiedText)
    }

    //these tests are all together to save testing debounce time
    @Test
    fun entryFragment_makeEdit_navigatesBack() {
        val date = LocalDate.of(2019, 3, 22)

        val args = EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList())

        val scenario = launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        //Simulate user typing
        onView(withId(R.id.entry_text)).perform(
            typeText("Test string!")
        )
        onView(isRoot()).perform(waitFor(550))
        onView(withId(R.id.entry_text)).perform(
            typeText("Yeehaw!"),
            closeSoftKeyboard()
        )

        //wait for debounce to detect changes
        onView(isRoot()).perform(waitFor(550))

        //back is pressed
        scenario?.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        //dialog is displayed
        onView(withText(R.string.are_you_sure)).inRoot(isDialog()).check(matches(isDisplayed()))

        //cancel is pressed
        onView(withId(android.R.id.button2)).inRoot(isDialog()).perform(click())
        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())

        //back pressed again
        scenario?.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        //continue clicked
        onView(withText(R.string.are_you_sure)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun entryFragment_noEdit_navigatesBack_noDialog() {
        val date = LocalDate.of(2019, 3, 22)

        val args = EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList())

        val scenario = launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        scenario?.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())
    }

}

private fun EntryRepository.saveEntryBlocking(entry: Entry) = runBlocking {
    addEntry(entry)
}

private fun getText(matcher: Matcher<View>): String {
    val stringHolder = arrayOf<String?>(null)
    onView(matcher).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(TextView::class.java)

        override fun getDescription(): String = "getting text from a TextView"

        override fun perform(uiController: UiController, view: View) {
            stringHolder[0] = (view as TextView).text.toString()
        }
    })
    return stringHolder[0] ?: ""
}

private fun isEditTextValueEqualTo(content: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("Match Edit Text Value with View ID Value : :  $content")
        }

        override fun matchesSafely(view: View?): Boolean {
            if (view !is TextView && view !is EditText) {
                return false
            }
            val text = if (view is TextView) {
                view.text.toString()
            } else {
                (view as EditText).text.toString()
            }

            return text.equals(content, ignoreCase = true)
        }
    }
}

private fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()

        override fun getDescription(): String = "wait for ${delay}milliseconds"

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(delay)
        }
    }
}
