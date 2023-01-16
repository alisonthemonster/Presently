package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.CalmColors
import com.presently.ui.OriginalColors
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate
import kotlin.random.Random

@Composable
fun Entry(
    date: LocalDate,
    onEntrySaved: (isNewEntry: Boolean) -> Unit,
    onShareClicked: (date: String, content: String) -> Unit
) {
    val viewModel = hiltViewModel<EntryyViewModel>()
    val state = viewModel.state.collectAsState()

    //TODO this gets called waaaaaay too many times
    //think we need to add some remembers to this shit
    viewModel.fetchContent(date)

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        EntryContent(
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
    onEntrySaved: (isNewEntry: Boolean) -> Unit,
    onShareClicked: (date: String, content: String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PresentlyTheme.colors.entryBackground
    ) {
        Column {
            Text(
                text = when (state.date) {
                    LocalDate.now() -> stringResource(R.string.today)
                    LocalDate.now().minusDays(1) -> stringResource(R.string.yesterday)
                    else -> state.date.toStringWithDayOfWeek()
                },
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.entryDate
            )
            Text(
                text = if (state.date == LocalDate.now()) stringResource(R.string.iam) else stringResource(R.string.iwas),
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.entryDate
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.content,
                onValueChange = {
                    handleEvent(EntryEvent.OnTextChanged(it))
                },
                placeholder = {
                    val hintNumber = state.promptNumber
                    if (hintNumber == -1) {
                        Text(
                            text = if (state.date == LocalDate.now()) {
                                stringResource(id = R.string.what_are_you_thankful_for)
                            } else {
                                stringResource(id = R.string.what_were_you_thankful_for)
                            },
                            color = PresentlyTheme.colors.entryHint
                        )
                    } else {
                        //todo this is lost on rotation
                        val hints = stringArrayResource(id = R.array.prompts)
                        hints.shuffle()
                        Text(
                            text = hints[hintNumber % hints.size],
                            color = PresentlyTheme.colors.entryHint
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                textStyle = PresentlyTheme.typography.bodyMedium,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = PresentlyTheme.colors.entryBackground,
                    focusedIndicatorColor = Color.Transparent, //hide the indicator
                    textColor = PresentlyTheme.colors.entryBody,
                    cursorColor = PresentlyTheme.colors.debugColor1
                ),
            )
            Row() {
                if (state.shouldShowHintButton) {
                    IconButton(onClick = { handleEvent(EntryEvent.OnHintClicked) }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = stringResource(R.string.get_a_new_prompt),
                            tint = PresentlyTheme.colors.entryButtonBackground
                        )
                    }
                } else {
                    IconButton(onClick = {
                        onShareClicked(
                            state.date.toFullString(),
                            state.content
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_your_gratitude),
                            tint = PresentlyTheme.colors.entryButtonBackground
                        )
                    }
                }
                Button(
                    onClick = {
                        handleEvent(EntryEvent.OnSaveClicked)
                        onEntrySaved(state.isNewEntry)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = PresentlyTheme.colors.entryButtonBackground,
                        contentColor = PresentlyTheme.colors.entryButtonText
                    )
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }
            val quotes = stringArrayResource(id = R.array.inspirations)
            val randomValue: Int = remember { Random.nextInt(quotes.size) }
            Text(
                text = quotes[randomValue],
                style = PresentlyTheme.typography.bodyExtraSmall,
                color = PresentlyTheme.colors.entryQuoteText
            )
        }
    }
}