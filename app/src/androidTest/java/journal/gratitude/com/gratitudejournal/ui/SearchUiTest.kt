package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.search.SearchContent
import journal.gratitude.com.gratitudejournal.ui.search.SearchViewState
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

@HiltAndroidTest
class SearchUiTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testSearchWithResults() {
        val state = SearchViewState(
            query = "dogs",
            results = listOf(
                Entry(LocalDate.of(2011, 11, 11), "Search result one"),
                Entry(LocalDate.of(2001, 1, 1), "Search result two"),
            ),
        )

        composeTestRule.setContent {
            PresentlyTheme {
                SearchContent(
                    state = state,
                    onEntryClicked = {},
                    onSearchQueryChanged = {},
                    onBackClicked = {},
                    focusRequester = FocusRequester(),
                )
            }
        }

        composeTestRule.onNodeWithText("Search result one").assertIsDisplayed()
        composeTestRule.onNodeWithText("Friday, November 11, 2011").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search result two").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monday, January 1, 2001").assertIsDisplayed()
    }

    @Test
    fun testSearchTypingTrigger() {
        var onSearchQueryChangeQuery = ""
        val state = SearchViewState(
            query = "",
            results = listOf(
                Entry(LocalDate.of(2011, 11, 11), "Search result one"),
                Entry(LocalDate.of(2001, 1, 1), "Search result two"),
            ),
        )

        composeTestRule.setContent {
            PresentlyTheme {
                SearchContent(
                    state = state,
                    onEntryClicked = {},
                    onSearchQueryChanged = {
                        onSearchQueryChangeQuery = it
                    },
                    onBackClicked = {},
                    focusRequester = FocusRequester(),
                )
            }
        }

        composeTestRule.onNodeWithTag("searchFieldTestTag").performTextInput("searchQuery")
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithText("searchQuery")
                .fetchSemanticsNodes().size == 1
        }
        assertThat(onSearchQueryChangeQuery).isEqualTo("searchQuery")
    }

    @Test
    fun testClearingSearchField() {
        val state = SearchViewState(
            query = "",
            results = listOf(
                Entry(LocalDate.of(2011, 11, 11), "Search result one"),
                Entry(LocalDate.of(2001, 1, 1), "Search result two"),
            ),
        )

        composeTestRule.setContent {
            PresentlyTheme {
                SearchContent(
                    state = state,
                    onEntryClicked = {},
                    onSearchQueryChanged = {},
                    onBackClicked = {},
                    focusRequester = FocusRequester(),
                )
            }
        }

        composeTestRule.onNodeWithTag("searchFieldTestTag").performTextInput("searchQuery")
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithText("searchQuery")
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodesWithText("Search")
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithTag("searchFieldTestTag").assertTextEquals("Search", includeEditableText = false) // hint text
    }

    @Test
    fun testSearchClickingResult() {
        var onEntryClickedDate = LocalDate.now()
        val state = SearchViewState(
            query = "dogs",
            results = listOf(
                Entry(LocalDate.of(2011, 11, 11), "Search result one"),
                Entry(LocalDate.of(2001, 1, 1), "Search result two"),
            ),
        )

        composeTestRule.setContent {
            PresentlyTheme {
                SearchContent(
                    state = state,
                    onEntryClicked = {
                        onEntryClickedDate = it
                    },
                    onSearchQueryChanged = {},
                    onBackClicked = {},
                    focusRequester = FocusRequester(),
                )
            }
        }

        composeTestRule.onNodeWithText("Search result one").performClick()
        assertThat(onEntryClickedDate).isEqualTo(LocalDate.of(2011, 11, 11))
    }
}
