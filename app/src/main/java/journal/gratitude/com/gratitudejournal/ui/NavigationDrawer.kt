package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
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
    scaffoldState: ScaffoldState,
    scope: CoroutineScope,
    onSearchClicked: () -> Unit,
    onThemesClicked: () -> Unit,
    onContactClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PresentlyTheme.colors.timelineToolbar
    ) {
        Column() {
            Text(
                stringResource(R.string.presently),
                modifier = Modifier.padding(10.dp),
                style = PresentlyTheme.typography.titleLarge,
                color = PresentlyTheme.colors.timelineTitle
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