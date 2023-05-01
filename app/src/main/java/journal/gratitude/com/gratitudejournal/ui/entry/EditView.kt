package journal.gratitude.com.gratitudejournal.ui.entry

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
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
import org.threeten.bp.LocalDate

@Composable
fun EditView(
    modifier: Modifier = Modifier,
    date: LocalDate,
    content: String,
    promptNumber: Int?,
    userCanUndo: Boolean,
    userCanRedo: Boolean,
    onTextChanged: (newText: String) -> Unit,
    onHintClicked: (Int) -> Unit,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    val prompts = stringArrayResource(R.array.prompts)

    LaunchedEffect(Unit) {
        //open the keyboard
        focusRequester.requestFocus()
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            Text(
                text = when (date) {
                    LocalDate.now() -> stringResource(R.string.today)
                    LocalDate.now().minusDays(1) -> stringResource(R.string.yesterday)
                    else -> date.toStringWithDayOfWeek()
                },
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.entryDate
            )
            Text(
                modifier = modifier
                    .testTag("question")
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                text = if (promptNumber == null) {
                    if (date == LocalDate.now()) {
                        stringResource(id = R.string.what_are_you_thankful_for)
                    } else {
                        stringResource(id = R.string.what_were_you_thankful_for)
                    }
                } else {
                    prompts[promptNumber]
                },
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.entryDate,
            )
            TextField(
                modifier = modifier
                    .focusRequester(focusRequester)
                    .testTag("editViewTextField"),
                value = content,
                onValueChange = {
                    onTextChanged(it)
                },
                placeholder = {
                    Text(
                        text = if (date == LocalDate.now()) {
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
                    focusedIndicatorColor = Color.Transparent, //hide the indicator
                    textColor = PresentlyTheme.colors.entryBody,
                    cursorColor = PresentlyTheme.colors.debugColor1 //todo pick a color for this
                ),
            )
        }
        TextActions(
            modifier = modifier
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
    modifier: Modifier = Modifier,
    userCanUndo: Boolean,
    userCanRedo: Boolean,
    onPromptSuggestionClicked: () -> Unit,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(30.dp))
            .background(color = PresentlyTheme.colors.entryButtonBackground)
    ) {
        IconButton(
            onClick = { onUndoClicked() },
            enabled = userCanUndo,
        ) {
            Icon(
                painterResource(id = R.drawable.undo),
                contentDescription = stringResource(R.string.undo),
                tint = if (userCanUndo) PresentlyTheme.colors.entryBackground else PresentlyTheme.colors.entryHint,
            )
        }
        IconButton(onClick = { onPromptSuggestionClicked() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_idea_empty), //todo animate
                contentDescription = stringResource(R.string.get_a_new_prompt),
                tint = PresentlyTheme.colors.entryButtonText
            )
        }
        IconButton(
            onClick = { onRedoClicked() },
            enabled = userCanRedo,
        ) {
            Icon(
                painterResource(id = R.drawable.redo),
                contentDescription = stringResource(R.string.redo),
                tint = if (userCanRedo) PresentlyTheme.colors.entryBackground else PresentlyTheme.colors.entryHint,
            )
        }
    }
}