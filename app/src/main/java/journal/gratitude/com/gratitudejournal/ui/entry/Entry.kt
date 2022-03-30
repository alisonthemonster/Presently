package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

@Composable
fun Entry(
    date: LocalDate,
    onEntrySaved: (milestoneNumber: Int?) -> Unit
) {
    val viewModel = hiltViewModel<EntryyViewModel>()
    val state = viewModel.state.collectAsState()

    viewModel.fetchContent(date)

    MaterialTheme {
        EntryContent(
            modifier = Modifier.fillMaxWidth(),
            state = state.value,
            handleEvent = viewModel::handleEvent,
            onEntrySaved = onEntrySaved
        )
    }
}

@Composable
fun EntryContent(
    modifier: Modifier = Modifier,
    state: EntryViewState,
    handleEvent: (EntryEvent) -> Unit,
    onEntrySaved: (milestoneNumber: Int?) -> Unit
) {
    Column {
        Text(
            text = when (state.date) {
                LocalDate.now() -> {
                    "Today"
                }
                LocalDate.now().minusDays(1) -> {
                    "Yesterday"
                }
                else -> {
                    state.date.toStringWithDayOfWeek()
                }
            }
        )
        Text(text = if (state.date == LocalDate.now()) "I am grateful for" else "I was grateful for")
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.content,
            onValueChange = {
                handleEvent(EntryEvent.OnTextChanged(it))
            },
            placeholder = { Text(text = state.hint) }
        )
        Row() {
            if (state.shouldShowHintButton) {
                IconButton(onClick = { handleEvent(EntryEvent.OnHintClicked) }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Hint")
                }
            } else {
                IconButton(onClick = { handleEvent(EntryEvent.OnShareClicked) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Hint")
                }
            }
            Button(onClick = {
                handleEvent(EntryEvent.OnSaveClicked)
                onEntrySaved(state.milestoneNumber)
            }) {
                Text(text = "Save")
            }
        }
        Text(text = "A quote will go here!")

    }

}