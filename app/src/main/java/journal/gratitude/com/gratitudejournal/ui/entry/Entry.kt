package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.presently.ui.PresentlyTheme
import com.presently.ui.isDark
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.milestone.MilestoneCelebration
import journal.gratitude.com.gratitudejournal.ui.milestone.MilestoneScreen

@Composable
fun Entry(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel = hiltViewModel(),
    onEntryExit: () -> Unit,
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
            MilestoneScreen(
                theme = viewModel.getSelectedTheme(),
                milestoneNumber = state.entryNumber!!,
                onDismiss = { viewModel.onDismissMilestoneDialog() }) {
            }
        }

        Scaffold(
            modifier = modifier
                .background(PresentlyTheme.colors.entryBackground)
                .windowInsetsPadding(WindowInsets.safeContent),
            topBar = {
                EntryEditTopBar(
                    onBackPressed = {
                        if (state.isInEditMode) {
                            viewModel.onExitEditMode()
                        } else {
                            onEntryExit()
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!state.isInEditMode) {
                    FloatingActionButton(
                        onClick = viewModel::onFabClicked,
                        backgroundColor = PresentlyTheme.colors.entryButtonBackground,
                        contentColor = PresentlyTheme.colors.entryButtonText,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit),
                        )
                    }
                }
            },
        ) {
            EntryContent(
                state = state,
                onTextChanged = viewModel::onTextChanged,
                onHintClicked = viewModel::changeHint,
                onUndoClicked = viewModel::onUndoClicked,
                onRedoClicked = viewModel::onRedoClicked,
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class) //for AnimatedContent
@Composable
fun EntryContent(
    modifier: Modifier = Modifier,
    state: EntryViewState,
    onTextChanged: (newText: String) -> Unit,
    onHintClicked: (Int) -> Unit,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PresentlyTheme.colors.entryBackground),
    ) {
        AnimatedContent(targetState = state.isInEditMode) {isInEditMode ->
            if (isInEditMode) {
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
                )
            }
        }
    }
}
