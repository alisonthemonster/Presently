package journal.gratitude.com.gratitudejournal.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyFontFamilies
import journal.gratitude.com.gratitudejournal.util.toShortMonthString
import org.threeten.bp.LocalDate

object SearchScreenTags {
    const val SEARCH_INPUT = "search_input"
    const val EMPTY_STATE = "search_empty_state"
}

data class SearchScreenState(
    val query: String,
    val results: List<Entry>,
    val showEmptyState: Boolean
)

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    onSearchResultClick: (LocalDate) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val results = List(searchResults.itemCount) { index -> searchResults[index] }.filterNotNull()
    val state = SearchScreenState(
        query = uiState.value.query,
        results = results,
        showEmptyState = uiState.value.query.isNotBlank() &&
            searchResults.loadState.refresh is LoadState.NotLoading &&
            searchResults.itemCount == 0
    )

    SearchScreenContent(
        state = state,
        onQueryChanged = viewModel::onSearchQueryChanged,
        onSearchTriggered = { viewModel.onSearchTriggered(uiState.value.query) },
        onBackClick = onBackClick,
        onSearchResultClick = onSearchResultClick
    )
}

@Composable
fun SearchScreenContent(
    state: SearchScreenState,
    onQueryChanged: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onBackClick: () -> Unit,
    onSearchResultClick: (LocalDate) -> Unit,
    requestInitialFocus: Boolean = true
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val theme = LocalPresentlyTheme.current

    if (requestInitialFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.timelineBackground)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.toolbar)
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    onBackClick()
                },
                modifier = Modifier.semantics { contentDescription = "Back" }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = null,
                    tint = theme.toolbarItem
                )
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .focusRequester(focusRequester)
                    .testTag(SearchScreenTags.SEARCH_INPUT)
                    .semantics { contentDescription = context.getString(R.string.search) },
                placeholder = {
                    Text(
                        text = context.getString(R.string.search),
                        color = theme.timelineHint
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = theme.toolbarItem
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        onSearchTriggered()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = theme.toolbarItem,
                    focusedContainerColor = theme.toolbar,
                    unfocusedContainerColor = theme.toolbar
                )
            )

            IconButton(
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    onSearchTriggered()
                },
                modifier = Modifier.semantics { contentDescription = "Search" }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = theme.toolbarItem
                )
            }
        }

        if (state.showEmptyState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(SearchScreenTags.EMPTY_STATE),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchEmptyStateIcon(
                        resId = theme.timelineIconRes
                    )
                    Text(
                        text = context.getString(R.string.no_results),
                        color = theme.timelineBody
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(
                    items = state.results,
                    key = { entry -> entry.entryDate.toString() }
                ) { entry ->
                    SearchResultRow(
                        entry = entry,
                        dateColor = theme.timelineBody,
                        bodyColor = theme.timelineBody,
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            onSearchResultClick(entry.entryDate)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    entry: Entry,
    dateColor: Color,
    bodyColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = entry.entryDate.month.toShortMonthString(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                color = dateColor,
                fontFamily = PresentlyFontFamilies.accent
            )
            Text(
                text = entry.entryDate.dayOfMonth.toString(),
                textAlign = TextAlign.Center,
                fontSize = 34.sp,
                color = dateColor,
                fontFamily = PresentlyFontFamilies.accent
            )
            Text(
                text = entry.entryDate.year.toString(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                color = dateColor,
                fontFamily = PresentlyFontFamilies.accent
            )
        }

        Text(
            text = entry.entryContent,
            modifier = Modifier
                .weight(5f)
                .padding(horizontal = 8.dp),
            color = bodyColor,
            maxLines = 6
        )
    }
}

@Composable
private fun SearchEmptyStateIcon(
    resId: Int,
) {
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = Modifier
            .size(160.dp)
            .padding(horizontal = 8.dp),
        contentScale = ContentScale.Fit,
        colorFilter = null
    )
}
