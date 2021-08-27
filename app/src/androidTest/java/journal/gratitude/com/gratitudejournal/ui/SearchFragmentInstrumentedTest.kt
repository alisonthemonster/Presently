package journal.gratitude.com.gratitudejournal.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.presently.presently_local_source.PresentlyLocalSource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.testUtils.RecyclerViewItemCountAssertion.Companion.withItemCount
import com.presently.testing.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.testUtils.waitFor
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SearchFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var localSource: PresentlyLocalSource

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun search_showsResults() {
        launchFragmentInHiltContainer<SearchFragment>(
            themeResId = R.style.Base_AppTheme
        )

        onView(withId(R.id.search_text)).perform(
            typeText("query!")
        )

        //wait for debounce to perform search
        onView(isRoot()).perform(waitFor(400))

        onView(withId(R.id.no_results_icon)).check(ViewAssertions.matches(not(isDisplayed())))
        onView(withId(R.id.search_results)).check(withItemCount(2))
    }

    @Test
    fun search_doesntSearchEmptyStrings() {
        launchFragmentInHiltContainer<SearchFragment>(
            themeResId = R.style.Base_AppTheme
        )

        onView(withId(R.id.search_text)).perform(
            typeText("")
        )

        onView(withId(R.id.search_results)).check(withItemCount(0))
    }
}
