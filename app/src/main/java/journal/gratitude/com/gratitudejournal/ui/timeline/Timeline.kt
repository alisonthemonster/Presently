package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

@Composable
fun Timeline() {
    val viewModel: TimelineeViewModel = viewModel()
    val state = viewModel.state.collectAsState()

    MaterialTheme {
        TimelineContent(
            modifier = Modifier.fillMaxWidth(),
            state = state.value,
            handleEvent = viewModel::handleEvent
        )
    }
}

@Composable
fun TimelineContent(
    modifier: Modifier = Modifier,
    state: TimelineViewState,
    handleEvent: (TimelineEvent) -> Unit
) {
    Column {
        TimelineList(timelineItems = state.entries)
    }
}

@Composable
fun TimelineList(
    modifier: Modifier = Modifier,
    timelineItems: List<TimelineItem>
) {
    LazyColumn {
       items(timelineItems) { timelineItem ->
            when (timelineItem) {
                is Entry -> {
                    EntryRow(
                        entryDate = timelineItem.entryDate,
                        entryContent = timelineItem.entryContent
                    )
                }
                is Milestone -> {
                    MilestoneRow(milestoneNumber = timelineItem.number)
                }
            }
        }
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
    entryContent: String
) {
    Column() {
        Text(text = entryDate.toStringWithDayOfWeek())
        Text(text = entryContent)
    }

}