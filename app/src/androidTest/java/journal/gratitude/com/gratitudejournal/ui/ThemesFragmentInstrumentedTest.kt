package journal.gratitude.com.gratitudejournal.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemesFragmentInstrumentedTest {


    @Test
    fun themes_are_presented() {
        launchFragmentInContainer<ThemeFragment>(
            themeResId = R.style.Base_AppTheme
        )

        Espresso.onView(ViewMatchers.withId(R.id.themes))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


}