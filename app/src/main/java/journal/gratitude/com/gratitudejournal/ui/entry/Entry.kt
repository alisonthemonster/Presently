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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate
import kotlin.random.Random

@Composable
fun Entry(
    date: LocalDate,
    onEntrySaved: (milestoneNumber: Int?) -> Unit,
    onShareClicked: (date: String, content: String) -> Unit
) {
    val viewModel = hiltViewModel<EntryyViewModel>()
    val state = viewModel.state.collectAsState()

    //TODO this gets called waaaaaay too many times
        //think we need to add some remembers to this shit
    viewModel.fetchContent(date)

    MaterialTheme {
        EntryContent(
            modifier = Modifier.fillMaxWidth(),
            state = state.value,
            handleEvent = viewModel::handleEvent,
            onEntrySaved = onEntrySaved,
            onShareClicked = onShareClicked
        )
    }
}

@Composable
fun EntryContent(
    modifier: Modifier = Modifier,
    state: EntryViewState,
    handleEvent: (EntryEvent) -> Unit,
    onEntrySaved: (milestoneNumber: Int?) -> Unit,
    onShareClicked: (date: String, content: String) -> Unit
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
            placeholder = {
                val hintNumber = state.promptNumber
                if (hintNumber == -1) {
                    Text(text = if (state.date == LocalDate.now()) {
                        stringResource(id = R.string.what_are_you_thankful_for)
                    } else {
                        stringResource(id = R.string.what_were_you_thankful_for)
                    })
                } else {
                    //todo this is lost on rotation
                    val hints = stringArrayResource(id = R.array.prompts)
                    hints.shuffle()
                    Text(text = hints[hintNumber % hints.size])
                }
            }
        )
        Row() {
            if (state.shouldShowHintButton) {
                IconButton(onClick = { handleEvent(EntryEvent.OnHintClicked) }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Hint")
                }
            } else {
                IconButton(onClick = { onShareClicked(state.date.toFullString(), state.content) }) {
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
        val quotes = stringArrayResource(id = R.array.inspirations)
        val randomValue: Int = remember { Random.nextInt(quotes.size) }
        Text(text = quotes[randomValue])
    }

}