package journal.gratitude.com.gratitudejournal.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

@Composable
fun Search(
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    val viewModel = hiltViewModel<SearchViewModell>()
    val state = viewModel.state.collectAsState()

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        SearchContent(
            modifier = Modifier.fillMaxWidth(),
            state = state.value,
            onSearchQueryChanged = viewModel::search,
            onEntryClicked = onEntryClicked
        )
    }
}

@Composable
private fun SearchContent(
    modifier: Modifier = Modifier,
    state: SearchViewState,
    onSearchQueryChanged: (query: String) -> Unit,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    Column() {
        var searchQuery by remember { mutableStateOf(TextFieldValue(state.query)) }
        SearchTextField(
            value = searchQuery,
            onValueChange = { value ->
                searchQuery = value
                onSearchQueryChanged(value.text)
            },
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn {
            items(state.results) { searchResult ->
                Column(
                    modifier = modifier.clickable {
                        onEntryClicked(searchResult.entryDate)
                    },
                ) {
                    Text(
                        text = searchResult.entryDate.toStringWithDayOfWeek(),
                        style = PresentlyTheme.typography.bodyLarge,
                        color = PresentlyTheme.colors.debugColor1
                    )
                    Text(
                        text = searchResult.entryContent,
                        style = PresentlyTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        trailingIcon = {
            AnimatedVisibility(
                visible = value.text.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onValueChange(TextFieldValue()) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "clear" //todo extract and translate
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = stringResource(R.string.search),
                style = PresentlyTheme.typography.bodyLarge
            )
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = 1,
        singleLine = true,
        modifier = modifier,
        textStyle = PresentlyTheme.typography.bodyLarge
    )
}
