package journal.gratitude.com.gratitudejournal

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.isDialog
import com.nhaarman.mockitokotlin2.mock
import journal.gratitude.com.gratitudejournal.room.EntryDao
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import org.junit.After
import org.junit.Rule


@RunWith(AndroidJUnit4::class)
class EntryFragmentInstrumentedTest {

    @get:Rule
    var activityRule = IntentsTestRule<ContainerActivity>(ContainerActivity::class.java)

    @Test
    fun writtenEntry_showsShareButton() {
        launchFragmentInContainer<EntryFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.share_button)).check(matches(not(isDisplayed())))


        onView(withId(R.id.entry_text)).perform(
            typeText("Test string!"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.share_button)).check(matches(isDisplayed()))
        onView(withId(R.id.prompt_button)).check(matches(not(isDisplayed())))

    }

    @Test
    fun noEntry_showsPromptButton() {
        launchFragmentInContainer<EntryFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.share_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.prompt_button)).check(matches(isDisplayed()))
    }

    @Test
    fun promptButton_changesHintText() {
        launchFragmentInContainer<EntryFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.entry_text)).check(matches(withHint("What are you grateful for?")))
        onView(withId(R.id.prompt_button)).perform(click())
        onView(withId(R.id.entry_text)).check(matches(not(withHint("What are you grateful for?"))))
    }

    @Test
    fun shareButton_opensShareActivity() {
        launchFragmentInContainer<EntryFragment>(
            themeResId = R.style.AppTheme
        )

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

        intending(anyIntent()).respondWith(intentResult)

        val content = "Test string!"

        onView(withId(R.id.entry_text)).perform(
            typeText(content),
            closeSoftKeyboard()
        )
        onView(withId(R.id.share_button)).perform(click())

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(Intent.EXTRA_TITLE, "Share your gratitude progress")
            )
        )
    }

    @Test
    fun saveButton_navigatesBack() {
        //TODO when navController bug is fixed
//        val mockNavController = mock<NavController>()
//        val scenario = launchFragmentInContainer<TimelineFragment>(
//            themeResId = R.style.AppTheme)
//        scenario.onFragment { fragment ->
//            Navigation.setViewNavController(fragment.requireView(), mockNavController)
//        }

        launchFragmentInContainer<EntryFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.entry_text)).perform(
            typeText("Test string!"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_button)).perform(click())

        //verify(mockNavController).navigate(R.id.action_title_screen_to_in_game)
    }

    @Test
    fun saveButton_onMilestone_showsMilestoneDialog() {
        //TODO when navController bug is fixed
//        val mockNavController = mock<NavController>()
//        val fragmentArgs = Bundle().apply {
//            putInt(EntryFragment.NUM_ENTRIES, 4)
//        }
//        val scenario = launchFragmentInContainer<TimelineFragment>(
//            fragmentArgs = fragmentArgs,
//            themeResId = R.style.AppTheme)
//        scenario.onFragment { fragment ->
//            Navigation.setViewNavController(fragment.requireView(), mockNavController)
//        }

        val fragmentArgs = Bundle().apply {
            putInt(EntryFragment.NUM_ENTRIES, 4)
        }
        launchFragmentInContainer<EntryFragment>(
            fragmentArgs,
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.entry_text)).perform(
            typeText("Test string!"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_button)).perform(click())

        onView(withText("Share your achievement")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
}