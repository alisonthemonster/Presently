package journal.gratitude.com.gratitudejournal.ui.entry

import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.graphics.drawable.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyFontFamilies
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.StateFlow
import journal.gratitude.com.gratitudejournal.util.toFullString

@Composable
fun EntryScreen(
    state: StateFlow<EntryUiState>,
    onPromptClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onQuoteLongClick: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    val uiState by state.collectAsStateWithLifecycle()
    EntryScreenContent(
        state = uiState,
        onPromptClick = onPromptClick,
        onShareClick = onShareClick,
        onSaveClick = onSaveClick,
        onQuoteLongClick = onQuoteLongClick,
        onTextChanged = onTextChanged
    )
}

@Composable
internal fun EntryScreenContent(
    state: EntryUiState,
    onPromptClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onQuoteLongClick: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    val theme = LocalPresentlyTheme.current
    val headerMode = state.date.toEntryHeaderMode()
    val dateText = when (headerMode) {
        EntryHeaderMode.TODAY -> stringResource(R.string.today)
        EntryHeaderMode.YESTERDAY -> stringResource(R.string.yesterday)
        EntryHeaderMode.PAST -> state.date.toFullString()
    }
    val thankfulText = when (headerMode) {
        EntryHeaderMode.TODAY -> stringResource(R.string.iam)
        EntryHeaderMode.YESTERDAY, EntryHeaderMode.PAST -> stringResource(R.string.iwas)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("entry_root"),
        color = theme.entryBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            HeaderText(
                text = dateText,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("entry_date"),
                textAlign = TextAlign.Start,
                contentAlignment = Alignment.BottomStart
            )
            HeaderText(
                text = thankfulText,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("entry_thankful_for"),
                textAlign = TextAlign.Start,
                contentAlignment = Alignment.TopStart
            )
            DividerLine(theme.entryBody)

            Box(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxWidth()
            ) {
                val context = LocalContext.current
                val onTextChangedState = rememberUpdatedState(onTextChanged)
                var entryEditText by remember { mutableStateOf<AppCompatEditText?>(null) }
                var didRequestInitialFocus by remember(state.isNewEntry) { mutableStateOf(false) }

                LaunchedEffect(state.isNewEntry, entryEditText) {
                    val editText = entryEditText
                    if (state.isNewEntry && !didRequestInitialFocus && editText != null) {
                        didRequestInitialFocus = true
                        editText.requestFocus()
                        val inputMethodManager = context.getSystemService(InputMethodManager::class.java)
                        editText.post {
                            inputMethodManager?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp)
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("entry_text_field"),
                        factory = { viewContext ->
                            AppCompatEditText(viewContext).apply {
                                entryEditText = this
                                id = R.id.entry_text
                                background = null
                                gravity = Gravity.TOP or Gravity.START
                                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                                setSingleLine(false)
                                setHorizontallyScrolling(false)
                                // Compose BasicTextField could not stop Samsung Keyboard from
                                // committing the highlighted suggestion on Enter, so this screen
                                // uses AppCompatEditText to set TYPE_TEXT_FLAG_NO_SUGGESTIONS.
                                inputType = InputType.TYPE_CLASS_TEXT or
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                imeOptions =
                                    EditorInfo.IME_FLAG_NO_ENTER_ACTION or
                                        EditorInfo.IME_FLAG_NO_FULLSCREEN
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                                typeface = ResourcesCompat.getFont(viewContext, R.font.larsseit_medium)
                                doAfterTextChanged { editable ->
                                    onTextChangedState.value(editable?.toString().orEmpty())
                                }
                            }
                        },
                        update = { editText ->
                            entryEditText = editText
                            editText.setTextColor(theme.entryBody.toArgb())
                            editText.setHintTextColor(theme.entryHint.toArgb())
                            editText.hint = state.hint
                            if (editText.text?.toString().orEmpty() != state.entryContent) {
                                editText.setText(state.entryContent)
                                editText.setSelection(editText.text?.length ?: 0)
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.isEmpty) {
                    PromptButton(onClick = onPromptClick)
                } else {
                    ActionIconButton(
                        iconRes = R.drawable.ic_share,
                        contentDescription = stringResource(R.string.share_your_gratitude),
                        tint = theme.entryHeader,
                        onClick = onShareClick,
                        testTag = "entry_share_button"
                    )
                }

                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.entryBody,
                        contentColor = theme.entryBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("entry_save_button")
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }

            DividerLine(
                color = theme.entryBody,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (state.showQuote) {
                QuoteText(
                    text = state.quote,
                    onLongClick = onQuoteLongClick,
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("entry_quote")
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun HeaderText(
    text: String,
    modifier: Modifier,
    textAlign: TextAlign,
    contentAlignment: Alignment
) {
    val tokens = LocalPresentlyTheme.current
    val textMeasurer = rememberTextMeasurer()
    val baseStyle = MaterialTheme.typography.headlineMedium.copy(
        fontFamily = PresentlyFontFamilies.accent,
        fontSize = 36.sp
    )
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = modifier.onSizeChanged { containerSize = it },
        contentAlignment = contentAlignment
    ) {
        val autoSizedFont = rememberAutoSizedHeaderFont(
            text = text,
            maxWidthPx = containerSize.width,
            maxHeightPx = containerSize.height,
            textMeasurer = textMeasurer,
            baseStyle = baseStyle
        )
        Text(
            text = text,
            color = tokens.entryHeader,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            style = baseStyle.copy(fontSize = autoSizedFont)
        )
    }
}

@Composable
private fun rememberAutoSizedHeaderFont(
    text: String,
    maxWidthPx: Int,
    maxHeightPx: Int,
    textMeasurer: TextMeasurer,
    baseStyle: TextStyle
) = remember(text, maxWidthPx, maxHeightPx) {
    if (maxWidthPx <= 0 || maxHeightPx <= 0) {
        return@remember 36.sp
    }

    for (fontSize in 36 downTo 16) {
        val layoutResult = textMeasurer.measure(
            text = text,
            style = baseStyle.copy(fontSize = fontSize.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            constraints = Constraints(maxWidth = maxWidthPx, maxHeight = maxHeightPx)
        )

        if (!layoutResult.hasVisualOverflow) {
            return@remember fontSize.sp
        }
    }

    16.sp
}

@Composable
private fun DividerLine(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(color)
    )
}

@Composable
private fun ActionIconButton(
    iconRes: Int,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(50.dp)
            .testTag(testTag)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun PromptButton(onClick: () -> Unit) {
    val tokens = LocalPresentlyTheme.current
    var animationTick by remember { mutableIntStateOf(0) }

    IconButton(
        onClick = {
            animationTick += 1
            onClick()
        },
        modifier = Modifier
            .size(50.dp)
            .testTag("entry_prompt_button")
    ) {
        AndroidView(
            factory = { context ->
                AppCompatImageView(context).apply {
                    setImageResource(R.drawable.avd_idea)
                    contentDescription = context.getString(R.string.get_a_new_prompt)
                }
            },
            update = { imageView ->
                imageView.setColorFilter(tokens.entryHint.toArgb())
                if (animationTick > 0) {
                    (imageView.drawable as? Animatable)?.start()
                }
            },
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuoteText(
    text: String,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalPresentlyTheme.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = tokens.entryHint,
            textAlign = TextAlign.Center,
            maxLines = 4,
            style = TextStyle(
                fontFamily = PresentlyFontFamilies.body,
                fontSize = 15.sp
            ),
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
        )
    }
}
