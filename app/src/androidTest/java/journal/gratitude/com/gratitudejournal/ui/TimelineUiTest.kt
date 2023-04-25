package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.dropbox.dropshots.Dropshots
import com.presently.ui.OriginalColors
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.ui.timeline.MilestoneRow
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineRow
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

/**
 * To update screenshot tests run {./gradlew :app:connectedAndroidTest -Pdropshots.record}
 */
@HiltAndroidTest
class TimelineUiTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val dropshots = Dropshots()

    @Test
    fun testMilestoneRow() {
        composeTestRule.setContent {
            PresentlyTheme {
                MilestoneRow(
                    theme = OriginalColors,
                    milestoneNumber = 150
                )
            }
        }

        dropshots.assertSnapshot(composeTestRule.activity, "TimelineRowMilestone")
    }

    @Test
    fun testTimelineRow() {
        composeTestRule.setContent {
            PresentlyTheme {
                TimelineRow(
                    theme = OriginalColors,
                    entryDate = LocalDate.of(2022, 11, 23),
                    entryContent = "I was grateful for my very cuddly and cute dog. We went for a lovely walk today and played in the park for a while. Then we came home and played fetch together before snuggling on the couch.",
                    onEntryClicked = {_, _ ->}
                )
            }
        }

        dropshots.assertSnapshot(composeTestRule.activity, "TimelineRow")
    }

    @Test
    fun testTimelineRowEndItem() {
        composeTestRule.setContent {
            PresentlyTheme {
                TimelineRow(
                    theme = OriginalColors,
                    entryDate = LocalDate.of(2022, 11, 23),
                    entryContent = "I was grateful for sushi and soy sauce.",
                    isLastEntry = true,
                    onEntryClicked = {_, _ ->}
                )
            }
        }

        dropshots.assertSnapshot(composeTestRule.activity, "TimelineRowLastEntry")
    }

    @Test
    fun testTimelineRowEmpty() {
        composeTestRule.setContent {
            PresentlyTheme {
                TimelineRow(
                    theme = OriginalColors,
                    entryDate = LocalDate.of(2020, 12, 2),
                    entryContent = "",
                    onEntryClicked = {_, _ ->}
                )
            }
        }

        dropshots.assertSnapshot(composeTestRule.activity, "TimelineRowEmpty")
    }



}