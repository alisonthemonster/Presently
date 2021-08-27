package journal.gratitude.com.gratitudejournal.testUtils

import androidx.test.espresso.ViewAction
import org.hamcrest.CoreMatchers.any
import org.hamcrest.Matcher
import com.facebook.testing.screenshot.Screenshot
import android.view.View
import androidx.test.espresso.UiController
import com.facebook.testing.screenshot.internal.TestNameDetector

fun screenshot(name: String)  = ScreenshotAction(name)

fun screenshot() = screenshot(TestNameDetector.getTestName())

class ScreenshotAction(private val name: String) : ViewAction {
    override fun getConstraints(): Matcher<View> = any(View::class.java)

    override fun getDescription() = "Record screenshot of View"

    override fun perform(uiController: UiController, view: View) {
        Screenshot.snap(view)
            .setName(name)
            .record();
    }
}
