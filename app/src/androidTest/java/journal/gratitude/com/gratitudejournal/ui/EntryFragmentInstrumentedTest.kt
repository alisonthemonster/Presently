package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
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
import journal.gratitude.com.gratitudejournal.testUtils.getText
import journal.gratitude.com.gratitudejournal.testUtils.isEditTextValueEqualTo
import journal.gratitude.com.gratitudejournal.testUtils.saveEntryBlocking
import journal.gratitude.com.gratitudejournal.testUtils.waitFor
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import javax.inject.Inject
import com.presently.testing.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.ui.entry.EntryArgs
import org.hamcrest.Matchers

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EntryFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

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
        Intents.init()

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

        onView(withId(R.id.rate_presently)).perform(click())

        val uri = Uri.parse("market://details?id=journal.gratitude.com.gratitudejournal")
        Intents.intended(
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(uri)
            )
        )

        Intents.release()
    }

    @Test
    fun saveButton_onMilestone_clickShare_opensShareDialog() {
        Intents.init()

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

        Intents.intended(
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
                IntentMatchers.hasExtra(Intent.EXTRA_TITLE, "Share your gratitude")
            )
        )

        Intents.release()
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

        val scenario = launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        var activity: Activity? = null
        scenario?.onActivity { act ->
            activity = act
        }

        onView(withId(R.id.inspiration)).perform(longClick())

        onView(withText(R.string.copied)).inRoot(withDecorView(not((activity?.window?.decorView))))
            .check(matches(isDisplayed()))
    }

    //these tests are all together to save testing debounce time
    @Test
    fun entryFragment_makeEdit_navigatesBack() {
        val date = LocalDate.of(2019, 3, 22)

        val args = EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
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
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mDevice.pressBack()

        //dialog is displayed
        onView(withText(R.string.are_you_sure)).check(matches(isDisplayed()))

        //cancel is pressed
        onView(withId(android.R.id.button1)).perform(click())
        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())

        //back pressed again
        mDevice.pressBack()

        //continue clicked
        onView(withId(android.R.id.button2)).perform(click())
        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun entryFragment_noEdit_navigatesBack_noDialog() {
        val date = LocalDate.of(2019, 3, 22)

        val args = EntryArgs(date.toString(), true, 0, "quote", "hint", emptyList())

        launchFragmentInHiltContainer<EntryFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mDevice.pressBack()

        onView(withText(R.string.are_you_sure)).check(ViewAssertions.doesNotExist())
    }

}
