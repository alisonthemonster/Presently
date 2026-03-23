package journal.gratitude.com.gratitudejournal.ui.search

import androidx.compose.ui.test.junit4.createComposeRule
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureAcrossThemes
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.threeten.bp.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class SearchScreenRoborazziTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun results_allThemes() {
        composeRule.captureAcrossThemes(screen = "search", scenario = "results") { _ ->
            SearchScreenContent(
                state = SearchScreenState(
                    query = "gratitude",
                    results = sampleResults,
                    showEmptyState = false
                ),
                onQueryChanged = {},
                onSearchTriggered = {},
                onBackClick = {},
                onSearchResultClick = {},
                requestInitialFocus = false
            )
        }
    }

    @Test
    fun emptyState_allThemes() {
        composeRule.captureAcrossThemes(screen = "search", scenario = "empty-state") { _ ->
            SearchScreenContent(
                state = SearchScreenState(
                    query = "no matches here",
                    results = emptyList(),
                    showEmptyState = true
                ),
                onQueryChanged = {},
                onSearchTriggered = {},
                onBackClick = {},
                onSearchResultClick = {},
                requestInitialFocus = false
            )
        }
    }

    companion object {
        private val sampleResults = listOf(
            Entry(
                entryDate = LocalDate.of(2026, 3, 20),
                entryContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            ),
            Entry(
                entryDate = LocalDate.of(2026, 3, 18),
                entryContent = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
            ),
            Entry(
                entryDate = LocalDate.of(2026, 3, 14),
                entryContent = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris."
            )
        )
    }
}
