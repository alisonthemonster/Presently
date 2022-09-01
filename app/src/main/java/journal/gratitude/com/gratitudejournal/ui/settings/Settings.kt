package journal.gratitude.com.gratitudejournal.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme
import com.presently.ui.colorSchemes

@Composable
fun Settings(
    onThemeChanged: () -> Unit
) {
    val viewModel = hiltViewModel<SettingsViewModel>()

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        SettingsContent(
            onThemeSelected = {
                viewModel.onThemeSelected(it)
                onThemeChanged()
            }
        )
    }
}

@Composable
private fun SettingsContent(
    onThemeSelected: (theme: String) -> Unit
) {
    LazyColumn {
        items(colorSchemes.toList()) { colorScheme ->
            Text(
                text = colorScheme.first,
                modifier = Modifier.clickable { onThemeSelected(colorScheme.first) }
            )
        }
    }
}