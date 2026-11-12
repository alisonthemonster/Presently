package journal.gratitude.com.gratitudejournal.ui.timeline

import android.os.Build
import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyFontFamilies
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Only show the scrubber when the list has at least this many rows. */
internal const val MIN_ROWS_FOR_SCRUBBER = 30

/**
 * Distance kept clear above the bottom edge of the list so the handle never travels under the
 * calendar button (about 56dp + 16dp padding). One constant so a future floating toolbar can
 * change it in one place.
 */
internal val SCRUBBER_BOTTOM_INSET = 96.dp

/** Handle auto-hides this long after the last scroll or touch. */
internal const val SCRUBBER_HIDE_DELAY_MS = 1500L

/** Bubble, ticks and labels linger this long after a drag ends. */
internal const val SCRUBBER_OVERLAY_LINGER_MS = 600L

/** Haptic tuning (needs a real API 34+ device to judge). */
internal const val SCRUBBER_HAPTIC_MIN_INTERVAL_MS = 40L
internal val SCRUBBER_HAPTIC_BOUNDARY = TimelineScrubberMath.HapticBoundary.MONTH

/** Year labels closer than this to one already drawn are skipped . */
internal const val SCRUBBER_LABEL_MIN_GAP_DP = 20f

private val HandleTouchSize = 48.dp
private val HandleWidth = 36.dp
private val HandleHeight = 48.dp
private val HandleEndInset = 4.dp
/** Gap from the end edge to year labels and the bubble: handle inset + width + 8dp. */
private val LabelEndInset = 48.dp
private val LabelHeight = SCRUBBER_LABEL_MIN_GAP_DP.dp

/**
 * Draggable scrubber over the timeline list. Overlay it on the list; it fills its parent.
 *
 * @param hidden true while the calendar overlay is up: the handle is hidden and not interactive.
 * @param isListTouched whether a finger is currently down on the list (keeps the handle visible).
 * @param onDragStarted called once per drag session.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TimelineScrubber(
    listState: LazyListState,
    items: List<TimelineRowState>,
    hidden: Boolean,
    isListTouched: () -> Boolean,
    onDragStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalPresentlyTheme.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val locale = LocalConfiguration.current.locales[0]
    val labels = remember(locale) { TimelineScrubberLabels(locale) }

    val scrubberIndex = remember(items) { TimelineScrubberIndex.from(items) }
    val currentIndex by rememberUpdatedState(scrubberIndex)
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)

    var dragFraction by remember { mutableStateOf<Float?>(null) }

    val scrollFraction by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val first = layoutInfo.visibleItemsInfo.firstOrNull()
            TimelineScrubberMath.scrollFraction(
                firstVisibleIndex = listState.firstVisibleItemIndex,
                firstItemOffsetPx = listState.firstVisibleItemScrollOffset,
                firstItemSizePx = first?.size ?: 0,
                size = currentIndex.size,
                canScrollForward = listState.canScrollForward
            )
        }
    }

    // Month under the finger while dragging (null when not dragging). Changes only on month change.
    val dragMonthKey by remember {
        derivedStateOf {
            dragFraction?.let {
                currentIndex.monthKeyAt(TimelineScrubberMath.fractionToIndex(it, currentIndex.size))
            }
        }
    }
    val scrollMonthKey by remember(listState) {
        derivedStateOf { currentIndex.monthKeyAt(listState.firstVisibleItemIndex) }
    }
    var lastDragMonthKey by remember { mutableIntStateOf(TimelineScrubberIndex.NO_MONTH) }
    LaunchedEffect(dragMonthKey) { dragMonthKey?.let { lastDragMonthKey = it } }

    // Dragging scrolls the list instantly (no animation, no fling).
    LaunchedEffect(listState) {
        snapshotFlow {
            dragFraction?.let { TimelineScrubberMath.fractionToIndex(it, currentIndex.size) }
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collectLatest { listState.scrollToItem(it) }
    }

    // Haptic tick when the dragged position crosses a month (or year) boundary. API 34+ only.
    LaunchedEffect(haptics) {
        var previous: Int? = null
        var lastHapticMs: Long? = null
        snapshotFlow { dragMonthKey }.collect { key ->
            val before = previous
            previous = key
            if (key == null || before == null) return@collect
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return@collect
            if (!TimelineScrubberMath.crossedBoundary(before, key, SCRUBBER_HAPTIC_BOUNDARY)) return@collect
            val now = SystemClock.elapsedRealtime()
            if (TimelineScrubberMath.shouldPerformHaptic(lastHapticMs, now, SCRUBBER_HAPTIC_MIN_INTERVAL_MS)) {
                lastHapticMs = now
                haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            }
        }
    }

    // Handle visibility: shown while scrolling / touched / dragged, hidden after a quiet period.
    var recentlyActive by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress || isListTouched() || dragFraction != null }
            .collectLatest { active ->
                if (active) {
                    recentlyActive = true
                } else {
                    delay(SCRUBBER_HIDE_DELAY_MS)
                    recentlyActive = false
                }
            }
    }
    // Bubble + ticks + labels: while dragging, plus a short linger.
    var overlayVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        snapshotFlow { dragFraction != null }.collectLatest { dragging ->
            if (dragging) {
                overlayVisible = true
            } else {
                delay(SCRUBBER_OVERLAY_LINGER_MS)
                overlayVisible = false
            }
        }
    }

    val handleShown = recentlyActive && !hidden
    val handleAlpha by animateFloatAsState(if (handleShown) 1f else 0f, tween(200), label = "scrubberHandleAlpha")
    val overlayShown = overlayVisible && !hidden
    val overlayAlpha by animateFloatAsState(if (overlayShown) 1f else 0f, tween(150), label = "scrubberOverlayAlpha")

    if (hidden && handleAlpha == 0f && overlayAlpha == 0f) return

    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(bottom = SCRUBBER_BOTTOM_INSET)) {
        val handleHeightPx = with(density) { HandleHeight.toPx() }
        val travelPx = (with(density) { maxHeight.toPx() } - handleHeightPx).coerceAtLeast(0f)
        val travelDp = with(density) { travelPx.toDp().value }
        val handleYPx: () -> Int = {
            ((dragFraction ?: scrollFraction) * travelPx).roundToInt()
        }

        val placements = remember(scrubberIndex, travelDp) {
            TimelineScrubberMath.placeYearTicks(
                ticks = scrubberIndex.yearTicks,
                size = scrubberIndex.size,
                travelDp = travelDp,
                minGapDp = SCRUBBER_LABEL_MIN_GAP_DP
            )
        }

        // Year labels in small bubbles (orientation only: no touch handling).
        if (overlayAlpha > 0f && lastDragMonthKey != TimelineScrubberIndex.NO_MONTH) {
            val handleHalfPx = handleHeightPx / 2f
            val labelHalfPx = with(density) { LabelHeight.toPx() } / 2f
            placements.forEach { tick ->
                val centerY = handleHalfPx + tick.fraction * travelPx
                if (tick.labelled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset { IntOffset(0, (centerY - labelHalfPx).roundToInt()) }
                            .padding(end = LabelEndInset)
                            .height(LabelHeight)
                            .graphicsLayer { alpha = overlayAlpha }
                            .background(theme.timelineHint, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labels.year(tick.year),
                            color = theme.timelineHeader,
                            fontFamily = PresentlyFontFamilies.accent,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Month and year bubble: on top of the year labels, hugging the handle.
        val bubbleKey = dragMonthKey ?: lastDragMonthKey
        val bubbleText = if (overlayAlpha > 0f) labels.monthYear(bubbleKey) else null
        if (bubbleText != null) {
            // Sits right beside the handle and is drawn over the year labels (Google Photos style).
            val bubbleEnd = LabelEndInset
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, handleYPx()) }
                    .height(HandleHeight)
                    .padding(end = bubbleEnd)
                    .graphicsLayer { alpha = overlayAlpha },
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = bubbleText,
                    color = theme.fabText,
                    fontFamily = PresentlyFontFamilies.accent,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .testTag(TimelineScreenTags.SCRUBBER_BUBBLE)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .background(theme.fab, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Accessibility proxy: always present for a long list (the visible handle is hidden at
        // rest), so TalkBack can find and operate the scrubber without dragging.
        val a11yLabel = stringResource(R.string.timeline_scrubber)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, handleYPx()) }
                .size(HandleTouchSize)
                .testTag(TimelineScreenTags.SCRUBBER_SEMANTICS)
                .semantics {
                    contentDescription = a11yLabel
                    labels.monthYear(dragMonthKey ?: scrollMonthKey)?.let { stateDescription = it }
                    progressBarRangeInfo = ProgressBarRangeInfo(dragFraction ?: scrollFraction, 0f..1f)
                    setProgress { value ->
                        val target = TimelineScrubberMath.fractionToIndex(value, currentIndex.size)
                        scope.launch { listState.scrollToItem(target) }
                        true
                    }
                }
        )

        // The visible, draggable handle. Not composed (so not touchable) unless showing.
        if (handleAlpha > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, handleYPx()) }
                    .size(HandleTouchSize)
                    .semantics { invisibleToUser() }
                    .then(
                        if (handleShown) {
                            Modifier
                                .systemGestureExclusion()
                                .pointerInput(travelPx) {
                                    detectDragGestures(
                                        onDragStart = {
                                            dragFraction = scrollFraction
                                            currentOnDragStarted()
                                        },
                                        onDragEnd = { dragFraction = null },
                                        onDragCancel = { dragFraction = null }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val current = dragFraction ?: return@detectDragGestures
                                        dragFraction = if (travelPx > 0f) {
                                            (current + dragAmount.y / travelPx).coerceIn(0f, 1f)
                                        } else {
                                            current
                                        }
                                    }
                                }
                        } else {
                            Modifier
                        }
                    )
                    .testTag(TimelineScreenTags.SCRUBBER_HANDLE),
                contentAlignment = Alignment.CenterEnd
            ) {
                Canvas(
                    modifier = Modifier
                        .padding(end = HandleEndInset)
                        .size(width = HandleWidth, height = HandleHeight)
                        .graphicsLayer { alpha = handleAlpha }
                ) {
                    // Google Photos style: a wide pill with an up and a down chevron.
                    drawRoundRect(
                        color = theme.fab,
                        cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)
                    )
                    val cx = size.width / 2f
                    val half = size.width * 0.2f
                    val stroke = 2.dp.toPx()
                    listOf(1f to size.height / 2f - half * 1.2f, -1f to size.height / 2f + half * 1.2f)
                        .forEach { (dir, y) ->
                            val path = Path().apply {
                                moveTo(cx - half, y + dir * half * 0.5f)
                                lineTo(cx, y - dir * half * 0.5f)
                                lineTo(cx + half, y + dir * half * 0.5f)
                            }
                            drawPath(
                                path,
                                color = theme.fabText,
                                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                }
            }
        }
    }
}
