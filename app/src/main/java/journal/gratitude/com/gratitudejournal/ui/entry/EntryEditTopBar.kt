package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.presently.ui.PresentlyTheme

@Composable
fun EntryEditTopBar(
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "", //todo add content desc
                    tint = PresentlyTheme.colors.entryDate
                )
            }
        },
        backgroundColor = PresentlyTheme.colors.entryBackground,
        elevation = 0.dp,
    )
}