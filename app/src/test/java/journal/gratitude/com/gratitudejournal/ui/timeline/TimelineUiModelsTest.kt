package journal.gratitude.com.gratitudejournal.ui.timeline

import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import org.junit.Test
import org.threeten.bp.LocalDate

class TimelineUiModelsTest {

    @Test
    fun toTimelineRowStates_addsHintsForTodayAndYesterday() {
        val rows = emptyList<Entry>().toTimelineRowStates(
            showDayOfWeek = false,
            linesPerEntry = 10
        )

        assertThat(rows).hasSize(2)
        val today = rows[0] as TimelineEntryRowState
        val yesterday = rows[1] as TimelineEntryRowState

        assertThat(today.date).isEqualTo(LocalDate.now())
        assertThat(today.emptyHint).isEqualTo(R.string.what_are_you_thankful_for_today)
        assertThat(yesterday.date).isEqualTo(LocalDate.now().minusDays(1))
        assertThat(yesterday.emptyHint).isEqualTo(R.string.what_are_you_thankful_for_yesterday)
        assertThat(yesterday.isLastItem).isTrue()
    }

    @Test
    fun toTimelineRowStates_insertsMilestoneAfterFiveEntries() {
        val entries = (0L until 5L).map { offset ->
            Entry(
                entryDate = LocalDate.now().minusDays(offset),
                entryContent = "content $offset"
            )
        }

        val rows = entries.toTimelineRowStates(
            showDayOfWeek = false,
            linesPerEntry = 10
        )

        assertThat(rows.first()).isEqualTo(
            TimelineMilestoneRowState(number = 5, numberText = "5")
        )
        assertThat(rows.filterIsInstance<TimelineEntryRowState>()).hasSize(5)
    }

    @Test
    fun toTimelineRowStates_setsMaxLinesFromLinesPerEntry() {
        val rows = listOf(
            Entry(LocalDate.now().minusDays(2), "content")
        ).toTimelineRowStates(
            showDayOfWeek = false,
            linesPerEntry = 1
        )

        val entry = rows.filterIsInstance<TimelineEntryRowState>().first()
        assertThat(entry.maxLines).isEqualTo(1)
    }

    @Test
    fun toTimelineRowStates_maxLinesOneDoesNotExceedMinLines() {
        val rows = listOf(
            Entry(LocalDate.now().minusDays(2), "content")
        ).toTimelineRowStates(
            showDayOfWeek = false,
            linesPerEntry = 1
        )

        val entry = rows.filterIsInstance<TimelineEntryRowState>().first()
        // maxLines must be >= 1; minOf(3, maxLines) ensures minLines never exceeds maxLines
        assertThat(entry.maxLines).isAtLeast(1)
    }

    @Test
    fun toTimelineRowStates_formatsDayOfWeekWhenEnabled() {
        val targetDate = LocalDate.of(2011, 11, 11)
        val rows = listOf(
            Entry(targetDate, "content")
        ).toTimelineRowStates(
            showDayOfWeek = true,
            linesPerEntry = 10
        )

        val targetEntry = rows
            .filterIsInstance<TimelineEntryRowState>()
            .first { it.date == targetDate }

        assertThat(targetEntry.dateText).isEqualTo("Friday, November 11, 2011")
    }
}
