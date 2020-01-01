package journal.gratitude.com.gratitudejournal.util

import android.view.View
import android.widget.TextView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


fun getText(matcher: Matcher<View>): String {
    val stringHolder = arrayOf<String?>(null)
    onView(matcher).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(TextView::class.java)
        }

        override fun getDescription(): String {
            return "getting text from a TextView"
        }

        override fun perform(uiController: UiController, view: View) {
            val tv = view as TextView //Save, because of check in getConstraints()
            stringHolder[0] = tv.text.toString()
        }
    })
    return stringHolder[0] ?: ""
}

fun isEditTextValueEqualTo(content: String): Matcher<View> {

    return object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description) {
            description.appendText("Match Edit Text Value with View ID Value : :  $content")
        }

        override fun matchesSafely(view: View?): Boolean {
            if (view !is TextView && view !is EditText) {
                return false
            }
            val text: String = if (view is TextView) {
                view.text.toString()
            } else {
                (view as EditText).text.toString()
            }

            return text.equals(content, ignoreCase = true)
        }
    }
}