package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.R as PreferenceR
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.widget.EditText
import kotlinx.coroutines.runBlocking
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.model.Entry
import org.junit.Assert.assertEquals
import org.hamcrest.CoreMatchers.not
import org.threeten.bp.LocalDate
import javax.inject.Inject
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.rule.GrantPermissionRule
import org.junit.Assume.assumeTrue
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val intentsRule = IntentsRule()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @Inject
    lateinit var settings: FakePresentlySettings

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    private fun scrollToEraseRowAndClick() {
        onView(withId(PreferenceR.id.recycler_view))
            .perform(
                androidx.test.espresso.contrib.RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.erase_all_entries))
                )
            )
        onView(withText(R.string.erase_all_entries)).perform(click())
    }

    @Test
    fun eraseAll_eraseButtonDisabledUntilConfirmWordTyped() {
        launchFragmentInHiltContainer<SettingsFragment>()

        scrollToEraseRowAndClick()

        onView(withText(R.string.erase_all_confirm_button))
            .inRoot(isDialog())
            .check(matches(not(isEnabled())))

        onView(isAssignableFrom(EditText::class.java))
            .inRoot(isDialog())
            .perform(typeText("nope"), closeSoftKeyboard())
        onView(withText(R.string.erase_all_confirm_button))
            .inRoot(isDialog())
            .check(matches(not(isEnabled())))
    }

    @Test
    fun eraseAll_typingConfirmWordAndConfirming_erasesAllEntries() {
        runBlocking {
            repository.addEntry(Entry(LocalDate.of(2020, 1, 1), "First"))
            repository.addEntry(Entry(LocalDate.of(2020, 1, 2), "Second"))
        }
        launchFragmentInHiltContainer<SettingsFragment>()

        scrollToEraseRowAndClick()

        val confirmWord = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.erase_all_confirm_word)
        onView(isAssignableFrom(EditText::class.java))
            .inRoot(isDialog())
            .perform(typeText(confirmWord), closeSoftKeyboard())
        onView(withText(R.string.erase_all_confirm_button))
            .inRoot(isDialog())
            .check(matches(isEnabled()))
            .perform(click())

        assertEquals(emptyList<Entry>(), runBlocking { repository.getEntries() })
    }

    @Test
    fun eraseAll_cancelling_keepsEntries() {
        runBlocking { repository.addEntry(Entry(LocalDate.of(2020, 1, 1), "First")) }
        launchFragmentInHiltContainer<SettingsFragment>()

        scrollToEraseRowAndClick()
        onView(withText(R.string.cancel)).inRoot(isDialog()).perform(click())

        assertEquals(1, runBlocking { repository.getEntries() }.size)
    }

    @Test
    fun settingsFragment_clickingContactUs_opensContact() {
        launchFragmentInHiltContainer<SettingsFragment>()

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(anyIntent()).respondWith(intentResult)

        onView(withId(PreferenceR.id.recycler_view))
            .perform(
                androidx.test.espresso.contrib.RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.contact_us))
                )
            )
        onView(withText(R.string.contact_us)).perform(click())

        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageName = context.packageName
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val emails = arrayOf("gratitude.journal.app@gmail.com")
        val subject = "In App Feedback"
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

    @Test
    fun settingsFragment_remindersOff_hidesTimeAndExactAlarmSettings() {
        settings.notificationsEnabledValue = false
        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(R.string.notifs)).check(matches(isDisplayed()))
        onView(withText(R.string.time_title)).check(doesNotExist())
        onView(withText(R.string.exact_alarms_title)).check(doesNotExist())
    }

    @Test
    fun settingsFragment_remindersOn_showsTimeAndExactAlarmSettings() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        settings.notificationsEnabledValue = true
        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(R.string.time_title)).check(matches(isDisplayed()))
        onView(withText(R.string.exact_alarms_title)).check(matches(isDisplayed()))
    }

    @Test
    fun settingsFragment_remindersOnBeforeS_showsTimeSettingOnly() {
        assumeTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
        settings.notificationsEnabledValue = true
        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(R.string.time_title)).check(matches(isDisplayed()))
        onView(withText(R.string.exact_alarms_title)).check(doesNotExist())
    }
}
