package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

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
/** Handle is a perfect circle of this diameter, centred in a 48dp-tall touch row. */
private val HandleDiameter = 40.dp
private val HandleHeight = 48.dp
/** How far the circle hangs off the end edge (cut off by the screen, like Google Photos). */
private val HandleHang = 8.dp
/** Gap from the end edge to year labels and the bubble: visible part of the handle + 8dp. */
private val LabelEndInset = HandleDiameter - HandleHang + 8.dp
private val LabelMinHeight = SCRUBBER_LABEL_MIN_GAP_DP.dp
private val LabelTextSize = 12.sp

/**
 * Draggable scrubber over the timeline list. Overlay it on the list; it fills its parent.
 *
 * @param hidden true while the calendar overlay is up: the handle is hidden and not interactive.
 * @param isListTouched whether a finger is currently down on the list (keeps the handle visible).
 * @param onDragStarted called once per drag session.
 * @param onDraggingChanged called whenever a drag starts or ends.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TimelineScrubber(
    listState: LazyListState,
    items: List<TimelineRowState>,
    hidden: Boolean,
    isListTouched: () -> Boolean,
    onDragStarted: () -> Unit,
    onDraggingChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalPresentlyTheme.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    val labels = remember(locale) { TimelineScrubberLabels(locale) }

    val scrubberIndex = remember(items) { TimelineScrubberIndex.from(items) }
    val state = rememberTimelineScrubberState(
        listState = listState,
        index = scrubberIndex,
        isListTouched = isListTouched,
        onDragStarted = onDragStarted,
        onDraggingChanged = onDraggingChanged
    )

    val handleShown = state.isHandleActive && !hidden
    val handleAlpha by animateFloatAsState(if (handleShown) 1f else 0f, tween(200), label = "scrubberHandleAlpha")
    val overlayShown = state.isOverlayActive && !hidden
    val overlayAlpha by animateFloatAsState(if (overlayShown) 1f else 0f, tween(150), label = "scrubberOverlayAlpha")

    // Derived so the composable recomposes when these flip, not on every frame of the fades.
    val handleVisible by remember { derivedStateOf { handleAlpha > 0f } }
    val overlayVisible by remember { derivedStateOf { overlayAlpha > 0f } }

    if (hidden && !handleVisible && !overlayVisible) return

    // Year pills scale with the font size, and neighbouring pills must stay clear of each other.
    val labelHeight = maxOf(LabelMinHeight, with(density) { LabelTextSize.toDp() } * 1.67f)

    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(bottom = SCRUBBER_BOTTOM_INSET)) {
        val handleHeightPx = with(density) { HandleHeight.toPx() }
        val travelPx = (with(density) { maxHeight.toPx() } - handleHeightPx).coerceAtLeast(0f)
        val travelDp = with(density) { travelPx.toDp().value }
        // Read through State so the drag detector never has to restart when the size changes.
        val currentTravelPx by rememberUpdatedState(travelPx)
        val dragState = rememberDraggableState { delta ->
            val travel = currentTravelPx
            if (travel > 0f) state.dragBy(delta / travel)
        }
        // The handle going away (e.g. the calendar opening) must not leave a drag running.
        LaunchedEffect(handleShown) { if (!handleShown) state.endDrag() }
        val handleYPx: () -> Int = {
            (state.handleFraction * travelPx).roundToInt()
        }

        val placements = remember(scrubberIndex, travelDp, labelHeight) {
            TimelineScrubberMath.placeYearTicks(
                ticks = scrubberIndex.yearTicks,
                size = scrubberIndex.size,
                travelDp = travelDp,
                minGapDp = labelHeight.value
            )
        }

        // Year labels in small bubbles (orientation only: no touch handling).
        if (overlayVisible && state.lastDragMonthKey != TimelineScrubberIndex.NO_MONTH) {
            val handleHalfPx = handleHeightPx / 2f
            placements.forEach { tick ->
                val centerY = handleHalfPx + tick.fraction * travelPx
                if (tick.labelled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .centeredAtY { centerY }
                            .padding(end = LabelEndInset)
                            .height(labelHeight)
                            .graphicsLayer { alpha = overlayAlpha }
                            .background(theme.timelineHint, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labels.year(tick.year),
                            color = theme.timelineHeader,
                            fontFamily = PresentlyFontFamilies.accent,
                            fontSize = LabelTextSize
                        )
                    }
                }
            }
        }

        // Month and year bubble: on top of the year labels, hugging the handle.
        val bubbleKey = state.dragMonthKey ?: state.lastDragMonthKey
        val bubbleText = if (overlayVisible) labels.monthYear(bubbleKey) else null
        if (bubbleText != null) {
            // Sits right beside the handle and is drawn over the year labels (Google Photos style).
            val bubbleEnd = LabelEndInset
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .centeredAtY { handleYPx() + handleHeightPx / 2f }
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
        val a11yMonth by remember(labels) {
            derivedStateOf { labels.monthYear(state.dragMonthKey ?: state.scrollMonthKey) }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, handleYPx()) }
                .size(HandleTouchSize)
                .testTag(TimelineScreenTags.SCRUBBER_SEMANTICS)
                .semantics {
                    contentDescription = a11yLabel
                    a11yMonth?.let { stateDescription = it }
                    progressBarRangeInfo = ProgressBarRangeInfo(state.semanticsProgress, 0f..1f)
                    setProgress { value ->
                        scope.launch { state.scrollToFraction(value) }
                        true
                    }
                }
        )

        // The visible, draggable handle. Not composed (so not touchable) unless showing.
        if (handleVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, handleYPx()) }
                    .size(HandleTouchSize)
                    .semantics { invisibleToUser() }
                    .systemGestureExclusion()
                    .draggable(
                        enabled = handleShown,
                        orientation = Orientation.Vertical,
                        state = dragState,
                        onDragStarted = { state.startDrag() },
                        onDragStopped = { state.endDrag() }
                    )
                    .testTag(TimelineScreenTags.SCRUBBER_HANDLE),
                contentAlignment = Alignment.CenterEnd
            ) {
                val circleColor = theme.fab
                val chevronColor = theme.fabText
                Spacer(
                    modifier = Modifier
                        .offset(x = HandleHang)
                        .size(HandleDiameter)
                        .graphicsLayer { alpha = handleAlpha }
                        .drawWithCache {
                            // Google Photos style: a circle with an up and a down chevron. The path
                            // and stroke only change with the size, not on every frame.
                            val cx = size.width / 2f
                            val half = size.width * 0.1f
                            val chevrons = Path().apply {
                                // (direction, centre y): the top one points up, the bottom one down.
                                for ((dir, y) in arrayOf(
                                    1f to size.height / 2f - half * 1.6f,
                                    -1f to size.height / 2f + half * 1.6f
                                )) {
                                    moveTo(cx - half, y + dir * half * 0.5f)
                                    lineTo(cx, y - dir * half * 0.5f)
                                    lineTo(cx + half, y + dir * half * 0.5f)
                                }
                            }
                            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            onDrawBehind {
                                drawCircle(color = circleColor)
                                drawPath(chevrons, color = chevronColor, style = stroke)
                            }
                        }
                )
            }
        }
    }
}

/** Centres this element vertically on [centerY] (px, layout phase) however tall it measures. */
private fun Modifier.centeredAtY(centerY: () -> Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(0, (centerY() - placeable.height / 2f).roundToInt())
    }
}
