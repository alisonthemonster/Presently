package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.ui.test.junit4.createComposeRule
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureAcrossThemes
import journal.gratitude.com.gratitudejournal.testUtils.captureInTheme
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotDevices
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class EntryScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private companion object {
        // Fixed "today" so baselines don't depend on when the tests run.
        val TEST_TODAY: LocalDate = LocalDate.of(2026, 4, 15)
    }

    @Test
    fun emptyDraft_allThemes() {
        composeRule.captureAcrossThemes(screen = "entry", scenario = "empty-draft") { _ ->
            EntryScreenContent(
                today = TEST_TODAY,
                state = EntryUiState(
                    date = TEST_TODAY,
                    entryContent = "",
                    isNewEntry = true,
                    numberExistingEntries = 0,
                    hint = "What are you grateful for?",
                    quote = "\"Gratitude is the sign of noble souls\" \nAesop",
                    showQuote = true,
                    promptNumber = 0,
                    promptsList = listOf("What made you smile today?"),
                    isLoading = false,
                    hasUnsavedChanges = false
                ),
                onPromptClick = {},
                onShareClick = {},
                onSaveClick = {},
                onQuoteLongClick = {},
                onTextChanged = {}
            )
        }
    }

    @Test
    fun writtenEntry_allThemes() {
        composeRule.captureAcrossThemes(screen = "entry", scenario = "written-entry") { _ ->
            EntryScreenContent(
                today = TEST_TODAY,
                state = EntryUiState(
                    date = LocalDate.of(2026, 3, 28),
                    entryContent = "Sunshine on the balcony",
                    isNewEntry = false,
                    numberExistingEntries = 42,
                    hint = "What were you grateful for?",
                    quote = "\"Gratitude is the sign of noble souls\" \nAesop",
                    showQuote = true,
                    promptNumber = 0,
                    promptsList = listOf("What are you carrying forward?"),
                    isLoading = false,
                    hasUnsavedChanges = false
                ),
                onPromptClick = {},
                onShareClick = {},
                onSaveClick = {},
                onQuoteLongClick = {},
                onTextChanged = {}
            )
        }
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h1280dp-xxhdpi")
    fun writtenEntry_tablet_originalTheme() {
        composeRule.captureInTheme(
            screen = "entry",
            scenario = "written-entry-tablet",
            themeSpec = PresentlyThemeSpec.Original,
            device = ScreenshotDevices.TabletPortrait
        ) {
            EntryScreenContent(
                today = TEST_TODAY,
                state = EntryUiState(
                    date = LocalDate.of(2026, 3, 28),
                    entryContent = "Sunshine on the balcony",
                    isNewEntry = false,
                    numberExistingEntries = 42,
                    hint = "What were you grateful for?",
                    quote = "\"Gratitude is the sign of noble souls\" \nAesop",
                    showQuote = true,
                    promptNumber = 0,
                    promptsList = listOf("What are you carrying forward?"),
                    isLoading = false,
                    hasUnsavedChanges = false
                ),
                onPromptClick = {},
                onShareClick = {},
                onSaveClick = {},
                onQuoteLongClick = {},
                onTextChanged = {}
            )
        }
    }
}
