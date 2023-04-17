package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.presently.ui.PresentlyTheme
import com.presently.ui.isDark

@Composable
fun Entry(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel = hiltViewModel(),
    onEntryExit: () -> Unit,
    onShareClicked: (date: String, content: String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    BackHandler {
        if (state.isInEditMode) {
            viewModel.onExitEditMode()
        } else {
            onEntryExit()
        }
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
        
        if (state.shouldShowMilestoneDialog) {
            //todo use the milestone composable here
            Dialog(onDismissRequest = { viewModel.onDismissMilestoneDialog() }) {
                Text(text = "milestone ${state.entryNumber}")
            }
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
                shouldShowQuote = state.shouldShowQuote,
                onShareClicked = { date, content -> onShareClicked(date, content) }
            )
        }
    }
}
