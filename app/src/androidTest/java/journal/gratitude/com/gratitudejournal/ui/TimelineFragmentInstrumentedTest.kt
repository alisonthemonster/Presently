package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.DaggerTestApplicationRule
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.clickXY
import journal.gratitude.com.gratitudejournal.testUtils.saveEntryBlocking
import journal.gratitude.com.gratitudejournal.testUtils.scroll
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate

@RunWith(AndroidJUnit4::class)
class TimelineFragmentInstrumentedTest {

    private lateinit var repository: EntryRepository

    @get:Rule
    val rule = DaggerTestApplicationRule()

    @Before
    fun setupDaggerComponent() {
        repository = rule.component.entryRepository
    }

    @Test
    fun timelineFragment_showsTimeline() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        onView(withId(R.id.timeline_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_clickCalendar_opensCalendar() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )

        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(isDisplayed()))
    }

    @Test
    fun timelineFragment_openCalendar_clickingBack_closesCal() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )

        onView(withId(R.id.cal_fab)).perform(click())

        pressBack()

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_openCalendar_clickingClose_closesCal() {
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )

        onView(withId(R.id.cal_fab)).perform(click())

        onView(withId(R.id.close_button)).perform(click())

        onView(withId(R.id.entry_calendar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun timelineFragment_closedCalendar_clickingBack_navigatesBack() {
        val scenario = launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )
        var activity: Activity? = null
        scenario.onFragment { fragment ->
            activity = fragment.activity
        }

        onView(withId(R.id.cal_fab)).perform(click())
        onView(withId(R.id.close_button)).perform(click())

        Espresso.pressBackUnconditionally()

        assertTrue(activity!!.isFinishing)
    }

    @Test
    fun timelineFragment_clicksOverflow_opensContact() {
        Intents.init()
        launchFragmentInContainer<TimelineFragment>(
            themeResId = R.style.Base_AppTheme
        )

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
        Intents.release()
    }

    private fun scrollCalendarBackwardsBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(
                scroll(100, 300, 300, 250)
            )
        }
    }

}

