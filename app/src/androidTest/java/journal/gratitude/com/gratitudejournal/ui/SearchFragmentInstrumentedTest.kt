package journal.gratitude.com.gratitudejournal.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.DaggerTestApplicationRule
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.util.RecyclerViewItemCountAssertion.Companion.withItemCount
import journal.gratitude.com.gratitudejournal.util.waitFor
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchFragmentInstrumentedTest {

    private lateinit var repository: EntryRepository

    @get:Rule
    val rule = DaggerTestApplicationRule()

    @Before
    fun setupDaggerComponent() {
        repository = rule.component.entryRepository
    }

    @Test
    fun search_pressBackButton_navigateUp() {
        val mockNavController = mock<NavController>()
        val scenario = launchFragmentInContainer<SearchFragment>(
            themeResId = R.style.AppTheme
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        onView(withId(R.id.back_icon)).perform(click())

        verify(mockNavController).navigateUp()
    }

//    @Test
//    fun search_showsResults() {
//        launchFragmentInContainer<SearchFragment>(
//            themeResId = R.style.AppTheme
//        )
//
//        onView(withId(R.id.search_text)).perform(
//            typeText("query!")
//        )
//
//        //wait for debounce to perform search
//        onView(isRoot()).perform(waitFor(400))
//
//        onView(withId(R.id.no_results_icon)).check(ViewAssertions.matches(not(isDisplayed())))
//        onView(withId(R.id.search_results)).check(withItemCount(2))
//    }

//    @Test
//    fun search_type_clickEntry_navigateToEntry() {
//        val mockNavController = mock<NavController>()
//        val scenario = launchFragmentInContainer<SearchFragment>(
//            themeResId = R.style.AppTheme
//        )
//        scenario.onFragment { fragment ->
//            Navigation.setViewNavController(fragment.requireView(), mockNavController)
//        }
//
//        onView(withId(R.id.search_text)).perform(
//            typeText("Test string!")
//        )
//
//        onView(isRoot()).perform(waitFor(400))
//
//        onView(withId(R.id.search_results))
//            .perform(
//                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
//                    0,
//                    click()
//                )
//            )
//
//        verify(mockNavController).navigate(eq(R.id.action_searchFragment_to_entryFragment), any())
//    }

}