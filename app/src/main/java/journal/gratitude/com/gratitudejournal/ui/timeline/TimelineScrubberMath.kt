package journal.gratitude.com.gratitudejournal.ui.timeline

import android.text.format.DateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pure (non-Compose) logic for the timeline scrubber. Kept independent of Compose so it can be unit
 * tested on the JVM and reused (e.g. by a future "Look back" hub that needs index -> date lookups).
 *
 * The list is sparse (only written days plus placeholders), so everything here is **index based**:
 * a track fraction maps to a row index, never to a point in calendar time.
 */

/** The first row (index in the list, newest first) that belongs to [year]. */
internal data class YearTick(val year: Int, val index: Int)

/** A [YearTick] positioned on the track, with whether its label is drawn. */
internal data class YearTickPlacement(
    val year: Int,
    val index: Int,
    /** 0f = top of the track, 1f = bottom. */
    val fraction: Float,
    val labelled: Boolean
)

/**
 * Per-row month and per-year data, computed once for a given list of rows (`remember(items)`),
 * so lookups while dragging are O(1) and allocation free.
 */
internal class TimelineScrubberIndex private constructor(
    val size: Int,
    private val monthKeys: IntArray,
    val yearTicks: List<YearTick>
) {
    /** Month key (`year * 12 + month - 1`) of the row at [index], or [NO_MONTH]. */
    fun monthKeyAt(index: Int): Int =
        if (size == 0) NO_MONTH else monthKeys[index.coerceIn(0, size - 1)]

    fun yearMonthAt(index: Int): YearMonth? = monthKeyToYearMonth(monthKeyAt(index))

    companion object {
        const val NO_MONTH = -1

        fun monthKeyToYearMonth(key: Int): YearMonth? =
            if (key < 0) null else YearMonth.of(key / 12, key % 12 + 1)

        fun from(items: List<TimelineRowState>): TimelineScrubberIndex {
            val size = items.size
            val keys = IntArray(size) { NO_MONTH }
            val ticks = ArrayList<YearTick>()
            var previousYear: Int? = null

            for (i in 0 until size) {
                val row = items[i] as? TimelineEntryRowState ?: continue
                val date = row.date
                keys[i] = date.year * 12 + date.monthValue - 1
                if (date.year != previousYear) {
                    ticks.add(YearTick(year = date.year, index = i))
                    previousYear = date.year
                }
            }

            // Milestone rows have no date: borrow the nearest entry row below (the one that
            // reached the milestone), falling back to the nearest one above.
            var below = NO_MONTH
            for (i in size - 1 downTo 0) {
                if (keys[i] != NO_MONTH) below = keys[i] else keys[i] = below
            }
            var above = NO_MONTH
            for (i in 0 until size) {
                if (keys[i] != NO_MONTH) above = keys[i] else keys[i] = above
            }

            return TimelineScrubberIndex(size, keys, ticks)
        }
    }
}

internal object TimelineScrubberMath {

    /** Track fraction to row index in `[0, size - 1]`, clamped at both ends. */
    fun fractionToIndex(fraction: Float, size: Int): Int {
        if (size <= 1) return 0
        val f = if (fraction.isNaN()) 0f else fraction.coerceIn(0f, 1f)
        return (f * (size - 1)).roundToInt().coerceIn(0, size - 1)
    }

    /** Row index to track fraction in `[0f, 1f]` (inverse of [fractionToIndex]). */
    fun indexToFraction(index: Int, size: Int): Float {
        if (size <= 1) return 0f
        return (index.coerceIn(0, size - 1).toFloat() / (size - 1))
    }

    /**
     * Handle fraction while *not* dragging, from the first visible row plus the fractional offset
     * into it. Using the fraction through the first row keeps the handle smooth even though rows
     * have different heights. When the list can't scroll any further the handle rests at the
     * bottom of the track.
     */
    fun scrollFraction(
        firstVisibleIndex: Int,
        firstItemOffsetPx: Int,
        firstItemSizePx: Int,
        size: Int,
        canScrollForward: Boolean
    ): Float {
        if (size <= 1) return 0f
        if (!canScrollForward && firstVisibleIndex > 0) return 1f
        val within = if (firstItemSizePx > 0) {
            (firstItemOffsetPx.toFloat() / firstItemSizePx).coerceIn(0f, 1f)
        } else {
            0f
        }
        return ((firstVisibleIndex + within) / (size - 1)).coerceIn(0f, 1f)
    }

    enum class HapticBoundary { MONTH, YEAR }

    /** Whether moving from month key [previous] to [current] crosses a haptic boundary. */
    fun crossedBoundary(previous: Int, current: Int, boundary: HapticBoundary): Boolean {
        if (previous == TimelineScrubberIndex.NO_MONTH || current == TimelineScrubberIndex.NO_MONTH) {
            return false
        }
        return when (boundary) {
            HapticBoundary.MONTH -> previous != current
            HapticBoundary.YEAR -> previous / 12 != current / 12
        }
    }

    /** At most one haptic per [minIntervalMs]. */
    fun shouldPerformHaptic(lastHapticMs: Long?, nowMs: Long, minIntervalMs: Long): Boolean =
        lastHapticMs == null || nowMs - lastHapticMs >= minIntervalMs

    /**
     * Positions the year ticks on a track whose handle-centre travel is [travelDp] long, and decides
     * which get a label. The newest and oldest year are always labelled; others are skipped when
     * closer than [minGapDp] to a label already drawn (or to the oldest year's label). The tick
     * itself is always drawn. Two labels never overlap (in the degenerate case where the newest and
     * oldest labels can't both fit, the oldest label is dropped).
     */
    fun placeYearTicks(
        ticks: List<YearTick>,
        size: Int,
        travelDp: Float,
        minGapDp: Float
    ): List<YearTickPlacement> {
        if (ticks.isEmpty()) return emptyList()
        val y = FloatArray(ticks.size) { indexToFraction(ticks[it].index, size) * travelDp }
        val labelled = BooleanArray(ticks.size)
        val last = ticks.size - 1
        labelled[0] = true
        var lastLabelY = y[0]
        for (i in 1 until last) {
            if (y[i] - lastLabelY >= minGapDp && y[last] - y[i] >= minGapDp) {
                labelled[i] = true
                lastLabelY = y[i]
            }
        }
        if (last > 0 && y[last] - lastLabelY >= minGapDp) labelled[last] = true

        return ticks.mapIndexed { i, t ->
            YearTickPlacement(t.year, t.index, indexToFraction(t.index, size), labelled[i])
        }
    }
}

/** Formats the bubble text ("March 2026") for a locale. */
internal class TimelineScrubberLabels(locale: Locale) {
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "yMMMM"), locale)

    fun monthYear(yearMonth: YearMonth): String = formatter.format(yearMonth)

    fun monthYear(monthKey: Int): String? =
        TimelineScrubberIndex.monthKeyToYearMonth(monthKey)?.let(::monthYear)

    fun year(year: Int): String = year.toString()
}
