package journal.gratitude.com.gratitudejournal.sharing

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airbnb.mvrx.test.MvRxTestRule
import journal.gratitude.com.gratitudejournal.sharing.data.SharingArgs
import journal.gratitude.com.gratitudejournal.sharing.view.SharingFragment
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.testUtils.waitFor
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.airbnb.mvrx.asMavericksArgs

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SharingFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val intentsRule = IntentsRule()

    @get:Rule
    val mvrxRule = MvRxTestRule()

    @Test
    fun clickingDoneIconOpensShareSheet() {
        //launch fragment
        val args = SharingArgs("content", "May 5th, 2021").asMavericksArgs()

        launchFragmentInHiltContainer<SharingFragment>(
            fragmentArgs = args
        )

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(anyIntent()).respondWith(intentResult)

        //click done button
        onView(withId(R.id.check_mark)).perform(ViewActions.click())
        onView(isRoot()).perform(waitFor(1000))

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(Intent.EXTRA_TITLE, "Share your gratitude")
            )
        )
    }
}
