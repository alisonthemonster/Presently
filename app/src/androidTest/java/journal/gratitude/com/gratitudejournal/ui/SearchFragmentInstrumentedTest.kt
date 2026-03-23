package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchScreenTags
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SearchFragmentInstrumentedTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ContainerActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun search_showsResults() {
        launchSearchFragment()

        composeRule.onNodeWithTag(SearchScreenTags.SEARCH_INPUT)
            .performTextInput("query!")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Today's content").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithText("Happy birthday, Alison!")
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Today's content").assertIsDisplayed()
        composeRule.onNodeWithText("Happy birthday, Alison!").assertIsDisplayed()
        composeRule.onAllNodesWithTag(SearchScreenTags.EMPTY_STATE).assertCountEquals(0)
    }

    @Test
    fun search_doesntSearchEmptyStrings() {
        launchSearchFragment()

        composeRule.onNodeWithTag(SearchScreenTags.SEARCH_INPUT).assertIsDisplayed()
        composeRule.onAllNodesWithText("Today's content").assertCountEquals(0)
        composeRule.onAllNodesWithText("Happy birthday, Alison!").assertCountEquals(0)
        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.no_results))
            .assertCountEquals(0)
    }

    @Test
    fun search_showsEmptyStateWhenNoResultsFound() {
        launchSearchFragment()

        composeRule.onNodeWithTag(SearchScreenTags.SEARCH_INPUT)
            .performTextInput("query with no result")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(SearchScreenTags.EMPTY_STATE)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(SearchScreenTags.EMPTY_STATE).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.no_results))
            .assertIsDisplayed()
    }

    @Test
    fun search_clickingResult_opensEntryScreen() {
        launchSearchFragment()

        composeRule.onNodeWithTag(SearchScreenTags.SEARCH_INPUT)
            .performTextInput("query!")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Today's content").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Today's content").performClick()
        composeRule.waitForIdle()

        assertCurrentFragmentIs<EntryFragment>()
    }

    @Test
    fun search_clickingBack_returnsToPreviousScreen() {
        launchSearchFragmentOnTopOfTimeline()

        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForIdle()

        assertCurrentFragmentIs<TimelineFragment>()
    }

    private fun launchSearchFragment() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, SearchFragment())
                .commitNow()
        }
        composeRule.waitForIdle()
    }

    private fun launchSearchFragmentOnTopOfTimeline() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, TimelineFragment())
                .commitNow()

            composeRule.activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, SearchFragment())
                .addToBackStack(TimelineFragment.TIMELINE_TO_SEARCH)
                .commit()

            composeRule.activity.supportFragmentManager.executePendingTransactions()
        }
        composeRule.waitForIdle()
    }

    private inline fun <reified T : Fragment> assertCurrentFragmentIs() {
        val fragment = composeRule.activity.supportFragmentManager.findFragmentById(R.id.container_fragment)
        assertThat(fragment).isInstanceOf(T::class.java)
    }
}
