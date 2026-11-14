package journal.gratitude.com.gratitudejournal.ui.timeline

import android.os.Build
import android.os.SystemClock
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Everything the timeline scrubber *does*, separate from how it is drawn: where the handle is, what
 * is being dragged, when the handle and the month bubble are shown, and the haptics.
 *
 * Drag state has one owner: [startDrag], [dragBy] and [endDrag]. The "a drag started / ended"
 * callbacks fire from those calls, synchronously, so nothing else needs to watch for them.
 * Time-based behaviour (auto-hide, bubble linger, haptics) lives in [run], which the composable
 * launches once.
 *
 * Plain constructor arguments (no Compose locals) so this can be unit tested on the JVM.
 */
@Stable
internal class TimelineScrubberState(
    private val listState: LazyListState,
    private val indexProvider: () -> TimelineScrubberIndex,
    private val isListTouched: () -> Boolean,
    private val onDragStarted: () -> Unit,
    private val onDraggingChanged: (Boolean) -> Unit,
    private val performHaptic: () -> Unit,
    private val clockMs: () -> Long = SystemClock::elapsedRealtime,
    private val sdkInt: Int = Build.VERSION.SDK_INT
) {
    /** Track fraction under the finger (0f = top), or null when not dragging. */
    var dragFraction: Float? by mutableStateOf(null)
        private set

    val isDragging: Boolean get() = dragFraction != null

    /** Handle position while not dragging, from where the list is scrolled to. */
    val scrollFraction: Float by derivedStateOf {
        val layoutInfo = listState.layoutInfo
        TimelineScrubberMath.scrollFraction(
            firstVisibleIndex = listState.firstVisibleItemIndex,
            firstItemOffsetPx = listState.firstVisibleItemScrollOffset,
            firstItemSizePx = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0,
            size = indexProvider().size,
            canScrollForward = listState.canScrollForward
        )
    }

    /** Where the handle is drawn: under the finger while dragging, else following the list. */
    val handleFraction: Float get() = dragFraction ?: scrollFraction

    /** Month under the finger while dragging (null when not dragging). Changes only on month change. */
    val dragMonthKey: Int? by derivedStateOf {
        dragFraction?.let {
            val index = indexProvider()
            index.monthKeyAt(TimelineScrubberMath.fractionToIndex(it, index.size))
        }
    }

    /** Month of the first visible row, for accessibility while not dragging. */
    val scrollMonthKey: Int by derivedStateOf { indexProvider().monthKeyAt(listState.firstVisibleItemIndex) }

    /** Last month seen while dragging, so the bubble keeps its text while it fades out. */
    var lastDragMonthKey: Int by mutableIntStateOf(TimelineScrubberIndex.NO_MONTH)
        private set

    /** True while scrolling, touched or dragged, and for [SCRUBBER_HIDE_DELAY_MS] after. */
    var isHandleActive: Boolean by mutableStateOf(false)
        private set

    /** Bubble and year labels: while dragging, plus [SCRUBBER_OVERLAY_LINGER_MS] after. */
    var isOverlayActive: Boolean by mutableStateOf(false)
        private set

    fun startDrag() {
        dragFraction = scrollFraction
        onDragStarted()
        onDraggingChanged(true)
    }

    /** Moves the drag by [deltaFraction] of the track, clamped to the track. */
    fun dragBy(deltaFraction: Float) {
        val current = dragFraction ?: return
        dragFraction = (current + deltaFraction).coerceIn(0f, 1f)
    }

    /** Safe to call any number of times: only the first call after [startDrag] has an effect. */
    fun endDrag() {
        if (dragFraction == null) return
        dragFraction = null
        onDraggingChanged(false)
    }

    /** Jumps the list to the row at [fraction] of the track (used by accessibility actions). */
    suspend fun scrollToFraction(fraction: Float) {
        listState.scrollToItem(TimelineScrubberMath.fractionToIndex(fraction, indexProvider().size))
    }

    /** Runs the time-based behaviour until cancelled. */
    suspend fun run() = coroutineScope {
        launch { followDrag() }
        launch { rememberLastDragMonth() }
        launch { tickOnMonthChange() }
        launch { autoHideHandle() }
        launch { lingerOverlay() }
    }

    /** Dragging scrolls the list instantly (no animation, no fling). */
    private suspend fun followDrag() {
        snapshotFlow {
            dragFraction?.let { TimelineScrubberMath.fractionToIndex(it, indexProvider().size) }
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collectLatest { listState.scrollToItem(it) }
    }

    private suspend fun rememberLastDragMonth() {
        snapshotFlow { dragMonthKey }.collect { key -> key?.let { lastDragMonthKey = it } }
    }

    /** Haptic tick when the dragged position crosses a month (or year) boundary. API 34+ only. */
    private suspend fun tickOnMonthChange() {
        var previous: Int? = null
        var lastHapticMs: Long? = null
        snapshotFlow { dragMonthKey }.collect { key ->
            val before = previous
            previous = key
            if (key == null || before == null) return@collect
            if (sdkInt < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return@collect
            if (!TimelineScrubberMath.crossedBoundary(before, key, SCRUBBER_HAPTIC_BOUNDARY)) return@collect
            val now = clockMs()
            if (TimelineScrubberMath.shouldPerformHaptic(lastHapticMs, now, SCRUBBER_HAPTIC_MIN_INTERVAL_MS)) {
                lastHapticMs = now
                performHaptic()
            }
        }
    }

    private suspend fun autoHideHandle() {
        snapshotFlow { listState.isScrollInProgress || isListTouched() || isDragging }
            .collectLatest { active ->
                if (active) {
                    isHandleActive = true
                } else {
                    delay(SCRUBBER_HIDE_DELAY_MS)
                    isHandleActive = false
                }
            }
    }

    private suspend fun lingerOverlay() {
        snapshotFlow { isDragging }.collectLatest { dragging ->
            if (dragging) {
                isOverlayActive = true
            } else {
                delay(SCRUBBER_OVERLAY_LINGER_MS)
                isOverlayActive = false
            }
        }
    }
}

/**
 * Creates the [TimelineScrubberState] for [listState] and keeps it running. If the scrubber leaves
 * composition mid-drag (e.g. the list shrinks below the threshold) the drag is ended, so the
 * caller's [onDraggingChanged] always gets its `false`.
 */
@Composable
internal fun rememberTimelineScrubberState(
    listState: LazyListState,
    index: TimelineScrubberIndex,
    isListTouched: () -> Boolean,
    onDragStarted: () -> Unit,
    onDraggingChanged: (Boolean) -> Unit
): TimelineScrubberState {
    val haptics = LocalHapticFeedback.current
    val currentIndex by rememberUpdatedState(index)
    val currentIsListTouched by rememberUpdatedState(isListTouched)
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)
    val currentOnDraggingChanged by rememberUpdatedState(onDraggingChanged)
    val currentHaptics by rememberUpdatedState(haptics)

    val state = remember(listState) {
        TimelineScrubberState(
            listState = listState,
            indexProvider = { currentIndex },
            isListTouched = { currentIsListTouched() },
            onDragStarted = { currentOnDragStarted() },
            onDraggingChanged = { currentOnDraggingChanged(it) },
            performHaptic = { currentHaptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
        )
    }
    LaunchedEffect(state) { state.run() }
    DisposableEffect(state) { onDispose { state.endDrag() } }
    return state
}
