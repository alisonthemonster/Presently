package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.presently.ui.PresentlyTheme
import com.presently.ui.isDark

@Composable
fun Entry(
    modifier: Modifier = Modifier,
    onEntrySaved: (milestoneNumber: Int?) -> Unit,
    onShareClicked: (date: String, content: String) -> Unit
) {
    val viewModel = hiltViewModel<EntryyViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    if (state.saveState.isSaved) {
        val milestoneNumber =
            if (state.saveState.milestoneWasReached) state.saveState.entryCount else null
        onEntrySaved(milestoneNumber)
        viewModel.onSaveHandled()
    }

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !PresentlyTheme.colors.entryBackground.isDark()

        DisposableEffect(systemUiController, useDarkIcons) {
            // Update all of the system bar colors to be transparent, and use
            // dark icons if we're in light theme
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons
            )
            onDispose {}
        }

        Scaffold(
            modifier = modifier.windowInsetsPadding(WindowInsets.safeContent),
            topBar = {
                if (state.isInEditMode) {
                    EntryEditTopBar(
                        onBackPressed = { viewModel.onExitEditMode() }
                    )
                }
            },
            floatingActionButton = {
                if (!state.isInEditMode) {
                    FloatingActionButton(
                        onClick = viewModel::onFabClicked,
                        backgroundColor = PresentlyTheme.colors.entryButtonBackground,
                        contentColor = PresentlyTheme.colors.entryBackground,
                    ) {
                        Icon(Icons.Filled.Edit, "") //todo add content desc
                    }
                }
            },
            backgroundColor = PresentlyTheme.colors.entryBackground,
        ) {
            EntryContent(
                state = state,
                onTextChanged = viewModel::onTextChanged,
                onHintClicked = viewModel::changeHint,
                onUndoClicked = viewModel::onUndoClicked,
                onRedoClicked = viewModel::onRedoClicked,
                onShareClicked = onShareClicked,
            )
        }
    }
}

@Composable
fun EntryContent(
    modifier: Modifier = Modifier,
    state: EntryViewState,
    onTextChanged: (newText: String) -> Unit,
    onHintClicked: (Int) -> Unit,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    onShareClicked: (date: String, content: String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        if (state.isInEditMode) {
            EditView(
                date = state.date,
                content = state.content,
                promptNumber = state.promptNumber,
                userCanUndo = state.userCanUndo,
                userCanRedo = state.userCanRedo,
                onTextChanged = { newText -> onTextChanged(newText) },
                onHintClicked = { totalHints -> onHintClicked(totalHints) },
                onUndoClicked = { onUndoClicked() },
                onRedoClicked = { onRedoClicked() },
            )
        } else {
            ReadView(
                date = state.date,
                content = state.content,
                onShareClicked = { date, content -> onShareClicked(date, content) }
            )
        }
    }
}
