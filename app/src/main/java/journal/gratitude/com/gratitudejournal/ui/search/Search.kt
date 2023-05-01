package journal.gratitude.com.gratitudejournal.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import org.threeten.bp.LocalDate

@Composable
fun Search(
    onBackClicked: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    onEntryClicked: (date: LocalDate) -> Unit
) {
    val state = viewModel.state.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // open the keyboard
        focusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        SearchContent(
            state = state.value,
            onBackClicked = { onBackClicked() },
            onSearchQueryChanged = viewModel::search,
            onEntryClicked = {
                viewModel.onEntryClicked()
                onEntryClicked(it)
            },
            focusRequester = focusRequester
        )
    }
}

@Composable
fun SearchContent(
    state: SearchViewState,
    onBackClicked: () -> Unit,
    onSearchQueryChanged: (query: String) -> Unit,
    onEntryClicked: (date: LocalDate) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PresentlyTheme.colors.timelineBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
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
            focusRequester = focusRequester,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        if (searchQuery.text.isNotEmpty() && state.results.isEmpty()) {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = stringResource(R.string.no_results),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                items(state.results) { searchResult ->
                    SearchResult(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),
                        result = searchResult,
                        onEntryClicked = { onEntryClicked(it) }
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
    onBackClicked: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    TextField(
        modifier = modifier
            .testTag("searchFieldTestTag")
            .focusRequester(focusRequester),
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
                    onClick = { onValueChange(TextFieldValue()) }
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
            cursorColor = PresentlyTheme.colors.debugColor1, // todo find a color for this
            placeholderColor = PresentlyTheme.colors.timelineHint, // todo is this the best color?
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
