package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope,
    onSearchClicked: () -> Unit,
    onThemesClicked: () -> Unit,
    onContactClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    Surface(
        modifier = modifier
            .background(PresentlyTheme.colors.timelineToolbar)
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxSize(),
        color = PresentlyTheme.colors.timelineToolbar
    ) {
        Column() {
            Text(
                stringResource(R.string.presently),
                modifier = modifier.padding(10.dp),
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.timelineLogo
            )
            NavigationDrawerItem(
                title = stringResource(id = R.string.search),
                onClicked = onSearchClicked,
                scope = scope,
                scaffoldState = scaffoldState
            )
            NavigationDrawerItem(
                title = stringResource(id = R.string.theme),
                onClicked = onThemesClicked,
                scope = scope,
                scaffoldState = scaffoldState
            )
            NavigationDrawerItem(
                title = stringResource(id = R.string.contact_us),
                onClicked = { onContactClicked() },
                scope = scope,
                scaffoldState = scaffoldState
            )
            NavigationDrawerItem(
                title = stringResource(id = R.string.settings),
                onClicked = onSettingsClicked,
                scope = scope,
                scaffoldState = scaffoldState
            )
        }
    }
}

@Composable
fun NavigationDrawerItem(
    title: String,
    onClicked: () -> Unit,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                scope.launch {
                    scaffoldState.drawerState.apply {
                        close()
                    }
                }
                onClicked()
            }
            .padding(10.dp),
        style = PresentlyTheme.typography.titleLarge,
        color = PresentlyTheme.colors.timelineOnToolbar
    )
}