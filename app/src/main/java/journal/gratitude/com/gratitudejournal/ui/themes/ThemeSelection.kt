package journal.gratitude.com.gratitudejournal.ui.themes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
    val colorSchemes = remember { colorSchemes.toList() }

    LazyColumn {
        items(colorSchemes) { colorScheme ->
            val themeName = colorScheme.first
            val colors = colorScheme.second
            Surface(
                color = colors.timelineBackground,
                modifier = Modifier
                    .clickable { onThemeSelected(themeName) }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier.size(60.dp).padding(8.dp),
                        painter = painterResource(id = colors.iconResource),
                        contentDescription = null,
                    )
                    Text(
                        text = themeName,
                        style = PresentlyTheme.typography.bodyMedium,
                        color = colors.timelineContent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
            }

        }
    }
}