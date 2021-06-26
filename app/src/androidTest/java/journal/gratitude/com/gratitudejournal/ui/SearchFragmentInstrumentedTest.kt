package journal.gratitude.com.gratitudejournal.ui

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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.ApplicationModule
import journal.gratitude.com.gratitudejournal.fakes.FakeEntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.RecyclerViewItemCountAssertion.Companion.withItemCount
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.testUtils.waitFor
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragmentDirections
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SearchFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun search_pressBackButton_navigateUp() {
        val mockNavController = mock<NavController>()
        val scenario = launchFragmentInHiltContainer<SearchFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.requireViewById(android.R.id.content), mockNavController)
        }

        onView(withId(R.id.back_icon)).perform(click())

        verify(mockNavController).navigateUp()
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

    @Test
    fun search_type_clickEntry_navigateToEntry() {
        val expected =
            SearchFragmentDirections.actionSearchFragmentToEntryFragment(LocalDate.now().toString())

        val mockNavController = mock<NavController>()
        val mockNavigationDestination = mock<NavDestination>()
        mockNavigationDestination.id = R.id.searchFragment
        whenever(mockNavController.currentDestination).thenReturn(mockNavigationDestination)
        val scenario = launchFragmentInHiltContainer<SearchFragment>(
            themeResId = R.style.Base_AppTheme
        )
        scenario!!.onActivity { activity ->
            Navigation.setViewNavController(activity.requireViewById(android.R.id.content), mockNavController)
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
