package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

@Composable
fun EditView(
    date: LocalDate,
    content: String,
    promptNumber: Int?,
    userCanUndo: Boolean,
    userCanRedo: Boolean,
    onTextChanged: (newText: String) -> Unit,
    onHintClicked: (Int) -> Unit,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    val prompts = stringArrayResource(R.array.prompts)

    LaunchedEffect(Unit) {
        // open the keyboard
        focusRequester.requestFocus()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            Text(
                text = when (date) {
                    today -> stringResource(R.string.today)
                    today.minus(1, DateTimeUnit.DAY) -> stringResource(R.string.yesterday)
                    else -> date.toStringWithDayOfWeek()
                },
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.entryDate
            )
            Text(
                modifier = Modifier
                    .testTag("question")
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                text = if (promptNumber == null) {
                    if (date == today) {
                        stringResource(id = R.string.what_are_you_thankful_for)
                    } else {
                        stringResource(id = R.string.what_were_you_thankful_for)
                    }
                } else {
                    prompts[promptNumber]
                },
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.entryDate
            )
            TextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .testTag("editViewTextField"),
                value = content,
                onValueChange = {
                    onTextChanged(it)
                },
                placeholder = {
                    Text(
                        text = if (date == today) {
                            stringResource(id = R.string.what_are_you_thankful_for)
                        } else {
                            stringResource(id = R.string.what_were_you_thankful_for)
                        },
                        color = PresentlyTheme.colors.entryHint
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                textStyle = PresentlyTheme.typography.bodyMedium,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = PresentlyTheme.colors.entryBackground,
                    focusedIndicatorColor = Color.Transparent, // hide the indicator
                    textColor = PresentlyTheme.colors.entryBody,
                    cursorColor = PresentlyTheme.colors.entryHint
                )
            )
        }
        TextActions(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            userCanUndo = userCanUndo,
            userCanRedo = userCanRedo,
            onPromptSuggestionClicked = { onHintClicked(prompts.size) },
            onUndoClicked = { onUndoClicked() },
            onRedoClicked = { onRedoClicked() }
        )
    }
}

@Composable
fun TextActions(
    userCanUndo: Boolean,
    userCanRedo: Boolean,
    onPromptSuggestionClicked: () -> Unit,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(30.dp))
            .background(color = PresentlyTheme.colors.entryButtonBackground)
    ) {
        IconButton(
            onClick = { onUndoClicked() },
            enabled = userCanUndo
        ) {
            Icon(
                painterResource(id = R.drawable.undo),
                contentDescription = stringResource(R.string.undo),
                tint = if (userCanUndo) PresentlyTheme.colors.entryBackground else PresentlyTheme.colors.entryHint
            )
        }
        IconButton(onClick = { onPromptSuggestionClicked() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_idea_empty), // todo animate
                contentDescription = stringResource(R.string.get_a_new_prompt),
                tint = PresentlyTheme.colors.entryButtonText
            )
        }
        IconButton(
            onClick = { onRedoClicked() },
            enabled = userCanRedo
        ) {
            Icon(
                painterResource(id = R.drawable.redo),
                contentDescription = stringResource(R.string.redo),
                tint = if (userCanRedo) PresentlyTheme.colors.entryBackground else PresentlyTheme.colors.entryHint
            )
        }
    }
}
