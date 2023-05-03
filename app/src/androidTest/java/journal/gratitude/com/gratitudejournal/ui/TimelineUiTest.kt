package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.dropbox.dropshots.Dropshots
import com.dropbox.dropshots.ThresholdValidator
import com.presently.ui.OriginalColors
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.ui.timeline.MilestoneRow
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineRow
import org.junit.Rule
import org.junit.Test
import kotlinx.datetime.LocalDate

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
    val dropshots = Dropshots(resultValidator = ThresholdValidator(0.033f))

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
                    entryDate = LocalDate(2022, 11, 23),
                    entryContent = "I was grateful for my very cuddly and cute dog. We went for a lovely walk today and played in the park for a while. Then we came home and played fetch together before snuggling on the couch.",
                    onEntryClicked = { _, _ -> },
                    shouldShowDayOfWeek = true,
                    numberOfLinesPerRow = 10
                )
            }
        }

        dropshots.assertSnapshot(composeTestRule.activity, "TimelineRow")
    }

    @Test
    fun testTimelineRowNoDayOfWeek() {
        composeTestRule.setContent {
            PresentlyTheme {
                TimelineRow(
                    theme = OriginalColors,
                    entryDate = LocalDate(2022, 11, 23),
                    entryContent = "I was grateful for my very cuddly and cute dog. We went for a lovely walk today and played in the park for a while. Then we came home and played fetch together before snuggling on the couch.",
                    onEntryClicked = { _, _ -> },
                    shouldShowDayOfWeek = false,
                    numberOfLinesPerRow = 10
                )
            }
        }

        dropshots.assertSnapshot(composeTestRule.activity, "TimelineRowNoDayOfWeek")
    }

    @Test
    fun testTimelineRowEndItem() {
        composeTestRule.setContent {
            PresentlyTheme {
                TimelineRow(
                    theme = OriginalColors,
                    entryDate = LocalDate(2022, 11, 23),
                    entryContent = "I was grateful for sushi and soy sauce.",
                    isLastEntry = true,
                    onEntryClicked = { _, _ -> },
                    shouldShowDayOfWeek = true,
                    numberOfLinesPerRow = 10
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
                    entryDate = LocalDate(2020, 12, 2),
                    entryContent = "",
                    onEntryClicked = { _, _ -> },
                    shouldShowDayOfWeek = true,
                    numberOfLinesPerRow = 10
                )
            }
        }

        dropshots.assertSnapshot(composeTestRule.activity, "TimelineRowEmpty")
    }
}
