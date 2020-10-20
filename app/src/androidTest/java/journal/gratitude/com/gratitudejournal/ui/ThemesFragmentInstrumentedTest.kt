package journal.gratitude.com.gratitudejournal.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemesFragmentInstrumentedTest {


    @Test
    fun themes_clickingTheme_navigatesUp() {
        val mockNavController = mock<NavController>()
        val scenario = launchFragmentInContainer<ThemeFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.themes))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )

        verify(mockNavController).navigateUp()
    }

    @Test
    fun themes_clickingBack_navigatesUp() {
        val mockNavController = mock<NavController>()
        val scenario = launchFragmentInContainer<ThemeFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.back_icon)).perform(ViewActions.click())

        verify(mockNavController).navigateUp()
    }


}