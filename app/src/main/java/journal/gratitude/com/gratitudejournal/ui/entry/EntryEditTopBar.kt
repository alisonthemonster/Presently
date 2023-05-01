package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R

@Composable
fun EntryEditTopBar(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = PresentlyTheme.colors.entryDate
                )
            }
        },
        backgroundColor = PresentlyTheme.colors.entryBackground,
        elevation = 0.dp
    )
}
