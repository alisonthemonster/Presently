package journal.gratitude.com.gratitudejournal.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.DaggerTestApplicationRule
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.dialog.CelebrateDialogFragment
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CelebrateDialogFragmentInstrumentedTest {

    @get:Rule
    val rule = DaggerTestApplicationRule()

    @Test
    fun celebrate_clickRate_opensStore() {
        Intents.init()

        val args = Bundle()
        args.putInt(CelebrateDialogFragment.NUM_ENTRIES, 5)

        launchFragment<CelebrateDialogFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args
        )

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(intentResult)

        Espresso.onView(ViewMatchers.withId(R.id.rate_presently)).perform(ViewActions.click())

        val uri = Uri.parse("market://details?id=journal.gratitude.com.gratitudejournal")
        intended(
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(uri)
            )
        )

        Intents.release()
    }

    @Test
    fun celebrate_clickShare_openShareDialog() {
        Intents.init()

        val args = Bundle()
        args.putInt(CelebrateDialogFragment.NUM_ENTRIES, 5)

        launchFragment<CelebrateDialogFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args
        )

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(intentResult)

        Espresso.onView(ViewMatchers.withId(R.id.share_presently)).perform(ViewActions.click())

        intended(
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
                IntentMatchers.hasExtra(Intent.EXTRA_TITLE, "Share your gratitude")
            )
        )

        Intents.release()
    }
}