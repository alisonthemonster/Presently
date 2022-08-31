package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.ui.NavigationDrawer
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

@Composable
fun Timeline(
    onEntryClicked: (date: LocalDate) -> Unit,
    onSearchClicked: () -> Unit,
    onThemesClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val viewModel = hiltViewModel<TimelineeViewModel>()
    val state = viewModel.state.collectAsState()

    MaterialTheme {
        TimelineContent(
            modifier = Modifier.fillMaxWidth(),
            state = state.value,
            handleEvent = viewModel::handleEvent,
            onEntryClicked = onEntryClicked,
            onSearchClicked = onSearchClicked,
            onThemesClicked = onThemesClicked,
            onSettingsClicked = onSettingsClicked,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineContent(
    modifier: Modifier = Modifier,
    state: TimelineViewState,
    handleEvent: (TimelineEvent) -> Unit,
    onEntryClicked: (date: LocalDate) -> Unit,
    onSearchClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onThemesClicked: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            NavigationDrawer(
                scaffoldState,
                scope,
                onSearchClicked,
                onThemesClicked,
                { onContactClicked(context) },
                onSettingsClicked,
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.presently)) },
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
                            contentDescription = "Menu" //todo extract into string resource
                        )
                    }
                }
            )
        }
    ) {
        Column {
            TimelineList(
                modifier = modifier,
                timelineItems = state.entries,
                onEntryClicked = onEntryClicked
            )
        }
    }
}

private fun onContactClicked(context: Context) {
    //analyticsLogger.recordEvent(OPENED_CONTACT_FORM)

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")

        val emails = arrayOf("gratitude.journal.app@gmail.com")
        val subject = "In App Feedback"
        putExtra(Intent.EXTRA_EMAIL, emails)
        putExtra(Intent.EXTRA_SUBJECT, subject)

        val packageName = context.packageName
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val text = """
                Device: ${Build.MODEL}
                OS Version: ${Build.VERSION.RELEASE}
                App Version: ${packageInfo.versionName}
                
                
                """.trimIndent()
        putExtra(Intent.EXTRA_TEXT, text)
    }

    try {
        context.startActivity(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
        //crashReporter.logHandledException(activityNotFoundException)
        Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun TimelineList(
    modifier: Modifier = Modifier,
    timelineItems: List<TimelineItem>,
    onEntryClicked: (date: LocalDate) -> Unit
) {
    LazyColumn {
        itemsIndexed(timelineItems) { index, timelineItem ->
            when (timelineItem) {
                is Entry -> {
                    EntryRow(
                        modifier = modifier,
                        entryDate = timelineItem.entryDate,
                        entryContent = timelineItem.entryContent,
                        onEntryClicked = onEntryClicked,
                        isLastEntry = index == timelineItems.size - 1
                    )
                }
                is Milestone -> {
                    MilestoneRow(milestoneNumber = timelineItem.number)
                }
            }
        }
    }
}

@Composable
fun MilestoneRow(
    modifier: Modifier = Modifier,
    milestoneNumber: Int
) {
    Text("Ya wrote $milestoneNumber entries")
}

@Composable
fun EntryRow(
    modifier: Modifier = Modifier,
    entryDate: LocalDate,
    entryContent: String,
    isLastEntry: Boolean = false,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    Column(
        modifier = modifier
            .clickable { onEntryClicked(entryDate) }
            .padding(8.dp),
    ) {
        val content = entryContent.ifEmpty {
                if (entryDate == LocalDate.now()) {
                    "What are you grateful for?"
                } else {
                    "What were you grateful for?"
                }
            }
        Text(
            text = entryDate.toStringWithDayOfWeek(),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = content,
            fontSize = 14.sp
        )
        if (isLastEntry) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null
            )
        }
    }

}