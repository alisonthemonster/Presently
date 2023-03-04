package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.presently.ui.PresentlyColors
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.ui.NavigationDrawer
import journal.gratitude.com.gratitudejournal.ui.dialog.MilestoneDialog
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.util.*

@Composable
fun Timeline(
    locale: Locale,
    navController: NavController,
    onEntryClicked: (date: LocalDate) -> Unit,
    onSearchClicked: () -> Unit,
    onThemesClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onContactClicked: () -> Unit,
) {
    val viewModel = hiltViewModel<TimelineeViewModel>()
    val state by viewModel.state.collectAsState()
    val theme = viewModel.getSelectedTheme()

    val saved: SavedStateHandle? = navController.currentBackStackEntry?.savedStateHandle
    val wasNewEntrySaved = saved?.getStateFlow("isNewEntry", false)?.collectAsState()
    var openDialog by remember { mutableStateOf(wasNewEntrySaved?.value == true && isMilestone(state.datesWritten.size + 1))  }

    PresentlyTheme(
        selectedTheme = theme
    ) {
        TimelineContent(
            modifier = Modifier.fillMaxWidth(),
            locale = locale,
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
        if (openDialog) {
            val milestoneCount = state.datesWritten.size
            MilestoneDialog(
                milestoneNumber = milestoneCount,
                onDismiss = { openDialog = false }
            )
        }
    }
}

//todo analytics
//analyticsLogger.recordEvent(CLICKED_NEW_ENTRY)
//analyticsLogger.recordEvent(CLICKED_EXISTING_ENTRY)
//analyticsLogger.recordEvent(CLICKED_SEARCH)
@Composable
fun TimelineContent(
    modifier: Modifier = Modifier,
    locale: Locale,
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
                backgroundColor = PresentlyTheme.colors.timelineToolbar
            )
        }
    ) {
        Surface(
            color = PresentlyTheme.colors.timelineBackground,
            modifier = modifier.fillMaxHeight()
        ) {
            Column() {
                TimelineCalendar(
                    modifier = modifier,
                    locale = locale,
                    writtenDates = state.datesWritten,
                    onDateClicked = onEntryClicked
                )
                TimelineList(
                    modifier = modifier,
                    theme = theme,
                    timelineItems = state.timelineItems,
                    onEntryClicked = onEntryClicked
                )
            }

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
    LazyColumn {
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