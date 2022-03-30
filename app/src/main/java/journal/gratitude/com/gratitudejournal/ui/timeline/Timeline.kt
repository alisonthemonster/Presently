package journal.gratitude.com.gratitudejournal.ui.timeline

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

@Composable
fun Timeline(
    onEntryClicked: (date: LocalDate) -> Unit
) {
    val viewModel = hiltViewModel<TimelineeViewModel>()
    val state = viewModel.state.collectAsState()

    MaterialTheme {
        TimelineContent(
            modifier = Modifier.fillMaxWidth(),
            state = state.value,
            handleEvent = viewModel::handleEvent,
            onEntryClicked = onEntryClicked
        )
    }
}

@Composable
fun TimelineContent(
    modifier: Modifier = Modifier,
    state: TimelineViewState,
    handleEvent: (TimelineEvent) -> Unit,
    onEntryClicked: (date: LocalDate) -> Unit
) {
    Column {
        TimelineList(
            modifier = modifier,
            timelineItems = state.entries,
            onEntryClicked = onEntryClicked
        )
    }
}

@Composable
fun TimelineList(
    modifier: Modifier = Modifier,
    timelineItems: List<TimelineItem>,
    onEntryClicked: (date: LocalDate) -> Unit
) {
    LazyColumn {
        items(timelineItems) { timelineItem ->
            when (timelineItem) {
                is Entry -> {
                    if (timelineItem.entryContent.isEmpty()) {
                        HintRow(
                            entryDate = timelineItem.entryDate,
                            onEntryClicked = onEntryClicked
                        )
                    } else {
                        EntryRow(
                            modifier = modifier,
                            entryDate = timelineItem.entryDate,
                            entryContent = timelineItem.entryContent,
                            onEntryClicked = onEntryClicked
                        )
                    }
                }
                is Milestone -> {
                    MilestoneRow(milestoneNumber = timelineItem.number)
                }
            }
        }
    }
}

@Composable
fun HintRow(
    modifier: Modifier = Modifier,
    entryDate: LocalDate,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    Column(
        modifier = modifier.clickable {
            onEntryClicked(entryDate)
        },
    ) {
        Text(text = entryDate.toStringWithDayOfWeek())
        Text(text = if (entryDate == LocalDate.now()) "What are you grateful for?" else "What were you grateful for?")
    }
}

@Composable
fun MilestoneRow(
    modifier: Modifier = Modifier,
    milestoneNumber: Int
) {
    Text("Ya wrote $milestoneNumber entries")
}

@Composable
fun EntryRow(
    modifier: Modifier = Modifier,
    entryDate: LocalDate,
    entryContent: String,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    Column(
        modifier = modifier.clickable {
            onEntryClicked(entryDate)
        },
    ) {
        Text(text = entryDate.toStringWithDayOfWeek())
        Text(text = entryContent)
    }

}