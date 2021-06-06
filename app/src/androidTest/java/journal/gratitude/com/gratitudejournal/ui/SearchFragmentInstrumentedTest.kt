package journal.gratitude.com.gratitudejournal.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.DaggerTestApplicationRule
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.RecyclerViewItemCountAssertion.Companion.withItemCount
import journal.gratitude.com.gratitudejournal.testUtils.waitFor
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragmentDirections
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

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
            themeResId = R.style.Base_AppTheme
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        onView(withId(R.id.back_icon)).perform(click())

        verify(mockNavController).navigateUp()
    }

    @Test
    fun search_showsResults() {
        launchFragmentInContainer<SearchFragment>(
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
        launchFragmentInContainer<SearchFragment>(
            themeResId = R.style.Base_AppTheme
        )

        onView(withId(R.id.search_text)).perform(
            typeText("")
        )

        onView(withId(R.id.search_results)).check(withItemCount(0))
    }

    @Test
    fun search_type_clickEntry_navigateToEntry() {
        val expected =
            SearchFragmentDirections.actionSearchFragmentToEntryFragment(LocalDate.now().toString())

        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.searchFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInContainer<SearchFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        onView(withId(R.id.search_text)).perform(
            typeText("Test string!")
        )

        onView(isRoot()).perform(waitFor(400))

        onView(withId(R.id.search_results))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        verify(mockNavController).navigate(expected)
    }

}
