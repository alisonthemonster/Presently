package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.presently.ui.PresentlyColors
import com.presently.ui.PresentlyTheme
import com.presently.ui.isDark
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.ui.NavigationDrawer
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.util.*

//todo show the calendar somewhere
@Composable
fun Timeline(
    onEntryClicked: (date: LocalDate) -> Unit,
    onSearchClicked: () -> Unit,
    onThemesClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onContactClicked: () -> Unit,
) {
    val viewModel = hiltViewModel<TimelineViewModel>()
    val state by viewModel.state.collectAsState()
    val theme = viewModel.getSelectedTheme()

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    PresentlyTheme(
        selectedTheme = theme
    ) {
        TimelineContent(
            modifier = Modifier.fillMaxWidth(), //todo is this needed?
            theme = theme,
            state = state,
            onEntryClicked = { date, isNewEntry ->
                viewModel.onEntryClicked(isNewEntry)
                onEntryClicked(date)
            },
            onSearchClicked = {
                viewModel.onSearchClicked()
                onSearchClicked()
            },
            onThemesClicked = {
                viewModel.onThemesClicked()
                onThemesClicked()
            },
            onSettingsClicked = {
                viewModel.onSettingsClicked()
                onSettingsClicked()
            },
            onContactClicked = {
                viewModel.onContactClicked()
                onContactClicked()
            }
        )
    }
}

@Composable
fun TimelineContent(
    modifier: Modifier = Modifier,
    theme: PresentlyColors,
    state: TimelineViewState,
    onEntryClicked: (date: LocalDate, isNewEntry: Boolean) -> Unit,
    onSearchClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onThemesClicked: () -> Unit,
    onContactClicked: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()

    val useDarkIcons = !PresentlyTheme.colors.timelineToolbar.isDark()

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
        scaffoldState = scaffoldState,
        drawerContent = {
            NavigationDrawer(
                scaffoldState = scaffoldState,
                scope = scope,
                onSearchClicked = onSearchClicked,
                onThemesClicked = onThemesClicked,
                onContactClicked = onContactClicked,
                onSettingsClicked = onSettingsClicked,
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.presently),
                        style = PresentlyTheme.typography.titleLarge,
                        color = PresentlyTheme.colors.timelineLogo
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            scaffoldState.drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.menu),
                            tint = PresentlyTheme.colors.timelineOnToolbar
                        )
                    }
                },
                backgroundColor = PresentlyTheme.colors.timelineToolbar,
                contentPadding = WindowInsets.statusBars.asPaddingValues(),
            )
        }
    ) { contentPadding ->
        Surface(
            color = PresentlyTheme.colors.timelineBackground,
            modifier = modifier
                .padding(contentPadding)
                .fillMaxHeight()
        ) {
            TimelineList(
                modifier = modifier,
                theme = theme,
                timelineItems = state.timelineItems,
                onEntryClicked = onEntryClicked
            )
        }
    }
}

@Composable
fun TimelineList(
    modifier: Modifier = Modifier,
    theme: PresentlyColors,
    timelineItems: List<TimelineItem>,
    onEntryClicked: (date: LocalDate, isNewEntry: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .navigationBarsPadding()
    ) {
        //todo add keys to help with recomposition
        itemsIndexed(timelineItems) { index, timelineItem ->
            when (timelineItem) {
                is Entry -> {
                    TimelineRow(
                        modifier = modifier,
                        theme = theme,
                        entryDate = timelineItem.entryDate,
                        entryContent = timelineItem.entryContent,
                        onEntryClicked = onEntryClicked,
                        isLastEntry = index == timelineItems.size - 1,
                    )
                }
                is Milestone -> {
                    MilestoneRow(
                        theme = theme,
                        milestoneNumber = timelineItem.number,
                    )
                }
            }
        }
    }
}