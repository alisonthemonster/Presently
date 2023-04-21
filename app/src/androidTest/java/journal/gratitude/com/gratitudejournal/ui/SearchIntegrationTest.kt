package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.search.Search
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
class SearchIntegrationTest {

    //so that it uses the fake dependencies
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() = runTest {
        hiltRule.inject()
        //add some fake data
        repository.addEntries(
            listOf(Entry(LocalDate.of(2022, 10, 9), "An entry from October of 2022"),)
        )
    }

    @Test
    fun searchIntegrationTest() = runTest {
        composeTestRule.setContent {
            PresentlyTheme {
                Search(onEntryClicked = {})
            }
        }

        composeTestRule.onNodeWithTag("searchFieldTestTag").assertIsDisplayed()
        composeTestRule.onNodeWithTag("searchFieldTestTag").performTextInput("October")

        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithText("An entry from October of 2022")
                .fetchSemanticsNodes().size == 1
        }

        //verify search result is shown
        composeTestRule.onNodeWithText("An entry from October of 2022").assertIsDisplayed()
    }
}