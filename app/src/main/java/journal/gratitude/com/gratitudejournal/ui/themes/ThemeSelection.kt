package journal.gratitude.com.gratitudejournal.ui.themes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme
import com.presently.ui.colorSchemes

@Composable
fun ThemeSelection(
    onThemeChanged: () -> Unit
) {
    val viewModel = hiltViewModel<ThemeViewModel>()

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        ThemeSelectionContent(
            onThemeSelected = {
                viewModel.onThemeSelected(it)
                onThemeChanged()
            }
        )
    }
}

@Composable
private fun ThemeSelectionContent(
    onThemeSelected: (theme: String) -> Unit
) {
    //todo style theme selection screen
    LazyColumn {
        items(colorSchemes.toList()) { colorScheme ->
            Text(
                text = colorScheme.first,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clickable { onThemeSelected(colorScheme.first) }
            )
        }
    }
}