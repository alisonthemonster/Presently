package journal.gratitude.com.gratitudejournal.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

//todo style this

//todo log analytics

//todo open keyboard when screen is launched

@Composable
fun Search(
    onBackClicked: () -> Unit,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    val viewModel = hiltViewModel<SearchViewModel>()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        SearchContent(
            modifier = Modifier.fillMaxWidth(),
            state = state.value,
            onBackClicked = { onBackClicked() },
            onSearchQueryChanged = viewModel::search,
            onEntryClicked = onEntryClicked
        )
    }
}

@Composable
fun SearchContent(
    modifier: Modifier = Modifier,
    state: SearchViewState,
    onBackClicked: () -> Unit,
    onSearchQueryChanged: (query: String) -> Unit,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    Surface(
        color = PresentlyTheme.colors.timelineBackground,
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column {
            var searchQuery by remember { mutableStateOf(TextFieldValue(state.query)) }
            SearchTextField(
                value = searchQuery,
                onValueChange = { value ->
                    searchQuery = value
                    onSearchQueryChanged(value.text)
                },
                onBackClicked = {
                    onBackClicked()
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            if (searchQuery.text.isNotEmpty() && state.results.isEmpty()) {
                Text(
                    modifier = modifier.fillMaxSize(),
                    text = stringResource(R.string.no_results),
                    textAlign = TextAlign.Center,
                )
            } else {
                //todo this doesn't scroll if the keyboard is up
                LazyColumn(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                ) {
                    items(state.results) { searchResult ->
                        SearchResult(
                            result = searchResult,
                            onEntryClicked = { onEntryClicked(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    TextField(
        modifier = modifier.testTag("searchFieldTestTag"),
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            IconButton(onClick = { onBackClicked() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = PresentlyTheme.colors.timelineOnToolbar
                )
            }
        },
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
                        contentDescription = stringResource(R.string.clear),
                        tint = PresentlyTheme.colors.timelineOnToolbar
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
        textStyle = PresentlyTheme.typography.bodyLarge,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = PresentlyTheme.colors.timelineToolbar,
            textColor = PresentlyTheme.colors.timelineOnToolbar,
            cursorColor = PresentlyTheme.colors.debugColor1, //todo find a color for this
            placeholderColor = PresentlyTheme.colors.timelineHint, //todo is this the best color?
        ),
    )
}
