package journal.gratitude.com.gratitudejournal.ui.timeline

import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineScrubberIndex.Companion.NO_MONTH
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineScrubberMath.HapticBoundary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class TimelineScrubberMathTest {

    // ---- fraction <-> index ----

    @Test
    fun fractionToIndex_mapsEndsAndMiddle() {
        assertEquals(0, TimelineScrubberMath.fractionToIndex(0f, 101))
        assertEquals(50, TimelineScrubberMath.fractionToIndex(0.5f, 101))
        assertEquals(100, TimelineScrubberMath.fractionToIndex(1f, 101))
    }

    @Test
    fun fractionToIndex_clampsBeyondEnds() {
        assertEquals(0, TimelineScrubberMath.fractionToIndex(-3f, 50))
        assertEquals(49, TimelineScrubberMath.fractionToIndex(7f, 50))
        assertEquals(0, TimelineScrubberMath.fractionToIndex(Float.NaN, 50))
    }

    @Test
    fun fractionIndex_oneItemAndEmpty() {
        assertEquals(0, TimelineScrubberMath.fractionToIndex(0.9f, 1))
        assertEquals(0, TimelineScrubberMath.fractionToIndex(0.9f, 0))
        assertEquals(0f, TimelineScrubberMath.indexToFraction(0, 1), 0f)
        assertEquals(0f, TimelineScrubberMath.indexToFraction(0, 0), 0f)
    }

    @Test
    fun indexToFraction_roundTripsEveryIndex() {
        for (size in listOf(2, 3, 30, 365, 4000)) {
            for (i in 0 until size) {
                val f = TimelineScrubberMath.indexToFraction(i, size)
                assertEquals("size=$size i=$i", i, TimelineScrubberMath.fractionToIndex(f, size))
            }
        }
        assertEquals(1f, TimelineScrubberMath.indexToFraction(99, 100), 0f)
        assertEquals(1f, TimelineScrubberMath.indexToFraction(500, 100), 0f)
    }

    // ---- scroll fraction ----

    @Test
    fun scrollFraction_usesFractionalOffsetIntoFirstRow() {
        // row 10 of 101, half way scrolled through it
        assertEquals(0.105f, TimelineScrubberMath.scrollFraction(10, 50, 100, 101, true), 1e-5f)
        // different row heights give the same progress for the same relative offset
        assertEquals(0.105f, TimelineScrubberMath.scrollFraction(10, 250, 500, 101, true), 1e-5f)
    }

    @Test
    fun scrollFraction_isMonotonicAcrossRowBoundary() {
        val endOfRow = TimelineScrubberMath.scrollFraction(10, 299, 300, 101, true)
        val startOfNext = TimelineScrubberMath.scrollFraction(11, 0, 80, 101, true)
        assertTrue(startOfNext >= endOfRow)
        assertEquals(0.11f, startOfNext, 1e-5f)
    }

    @Test
    fun scrollFraction_atTopEndAndDegenerate() {
        assertEquals(0f, TimelineScrubberMath.scrollFraction(0, 0, 100, 50, true), 0f)
        assertEquals(1f, TimelineScrubberMath.scrollFraction(45, 20, 100, 50, false), 1f)
        assertEquals(0f, TimelineScrubberMath.scrollFraction(0, 0, 0, 0, false), 0f)
        assertEquals(0f, TimelineScrubberMath.scrollFraction(0, 0, 100, 1, false), 0f)
        assertEquals(0f, TimelineScrubberMath.scrollFraction(0, 0, 0, 50, true), 0f)
    }

    // ---- month lookup ----

    @Test
    fun index_emptyList() {
        val index = TimelineScrubberIndex.from(emptyList())
        assertEquals(0, index.size)
        assertEquals(NO_MONTH, index.monthKeyAt(0))
        assertNull(index.yearMonthAt(0))
        assertTrue(index.yearTicks.isEmpty())
    }

    @Test
    fun index_onlyPlaceholderRows() {
        val today = LocalDate.of(2026, 3, 28)
        val rows = listOf(
            entryRow(today, content = ""),
            entryRow(today.minusDays(1), content = "")
        )
        val index = TimelineScrubberIndex.from(rows)
        assertEquals(YearMonth.of(2026, 3), index.yearMonthAt(0))
        assertEquals(YearMonth.of(2026, 3), index.yearMonthAt(1))
        assertEquals(listOf(YearTick(2026, 0)), index.yearTicks)
    }

    @Test
    fun index_milestoneUsesNearestEntryBelow() {
        val rows = listOf(
            milestoneRow(100),
            entryRow(LocalDate.of(2026, 3, 1)),
            entryRow(LocalDate.of(2026, 2, 28)),
            milestoneRow(50),
            entryRow(LocalDate.of(2025, 12, 31))
        )
        val index = TimelineScrubberIndex.from(rows)
        assertEquals(YearMonth.of(2026, 3), index.yearMonthAt(0))
        assertEquals(YearMonth.of(2025, 12), index.yearMonthAt(3))
    }

    @Test
    fun index_milestoneWithNoEntryBelowFallsBackToAbove() {
        val rows = listOf(entryRow(LocalDate.of(2026, 3, 1)), milestoneRow(1))
        assertEquals(YearMonth.of(2026, 3), TimelineScrubberIndex.from(rows).yearMonthAt(1))
    }

    @Test
    fun index_noEntryRowsAtAllMeansNoMonth() {
        val index = TimelineScrubberIndex.from(listOf(milestoneRow(1), milestoneRow(2)))
        assertEquals(NO_MONTH, index.monthKeyAt(0))
        assertNull(index.yearMonthAt(1))
        assertTrue(index.yearTicks.isEmpty())
    }

    @Test
    fun index_futureDatedRowsAtTop() {
        val rows = listOf(
            entryRow(LocalDate.of(2027, 1, 5)),
            entryRow(LocalDate.of(2026, 3, 28)),
            entryRow(LocalDate.of(2026, 3, 27))
        )
        val index = TimelineScrubberIndex.from(rows)
        assertEquals(YearMonth.of(2027, 1), index.yearMonthAt(0))
        assertEquals(listOf(YearTick(2027, 0), YearTick(2026, 1)), index.yearTicks)
    }

    @Test
    fun index_sparseDatesFollowRowsNotDays() {
        val rows = listOf(
            entryRow(LocalDate.of(2026, 3, 1)),
            entryRow(LocalDate.of(2024, 1, 1))
        )
        val index = TimelineScrubberIndex.from(rows)
        // the track mid point is one of two rows, not "halfway through time"
        assertEquals(YearMonth.of(2026, 3), index.yearMonthAt(TimelineScrubberMath.fractionToIndex(0.4f, 2)))
        assertEquals(YearMonth.of(2024, 1), index.yearMonthAt(TimelineScrubberMath.fractionToIndex(0.6f, 2)))
    }

    @Test
    fun index_outOfRangeLookupIsClamped() {
        val index = TimelineScrubberIndex.from(dailyRows(3))
        assertEquals(index.monthKeyAt(2), index.monthKeyAt(99))
        assertEquals(index.monthKeyAt(0), index.monthKeyAt(-5))
    }

    // ---- year ticks ----

    @Test
    fun yearTicks_singleYearHasOneTick() {
        val index = TimelineScrubberIndex.from(dailyRows(40))
        assertEquals(listOf(YearTick(2026, 0)), index.yearTicks)
    }

    @Test
    fun yearTicks_severalYearsAtFirstRowOfEach() {
        val rows = listOf(
            entryRow(LocalDate.of(2026, 3, 1)),
            entryRow(LocalDate.of(2026, 1, 1)),
            entryRow(LocalDate.of(2025, 12, 31)),
            entryRow(LocalDate.of(2025, 6, 1)),
            entryRow(LocalDate.of(2024, 2, 1))
        )
        assertEquals(
            listOf(YearTick(2026, 0), YearTick(2025, 2), YearTick(2024, 4)),
            TimelineScrubberIndex.from(rows).yearTicks
        )
    }

    @Test
    fun yearTicks_ignoreMilestoneRows() {
        val rows = listOf(
            milestoneRow(100),
            entryRow(LocalDate.of(2026, 3, 1)),
            milestoneRow(50),
            entryRow(LocalDate.of(2025, 3, 1))
        )
        assertEquals(
            listOf(YearTick(2026, 1), YearTick(2025, 3)),
            TimelineScrubberIndex.from(rows).yearTicks
        )
    }

    @Test
    fun yearTicks_gapYearHasNoTick() {
        val rows = listOf(entryRow(LocalDate.of(2026, 3, 1)), entryRow(LocalDate.of(2023, 3, 1)))
        assertEquals(
            listOf(2026, 2023),
            TimelineScrubberIndex.from(rows).yearTicks.map { it.year }
        )
    }

    @Test
    fun placeYearTicks_positionsByRowIndex() {
        val ticks = listOf(YearTick(2026, 0), YearTick(2025, 50), YearTick(2024, 100))
        val placed = TimelineScrubberMath.placeYearTicks(ticks, size = 101, travelDp = 600f, minGapDp = 20f)
        assertEquals(listOf(0f, 0.5f, 1f), placed.map { it.fraction })
        assertTrue(placed.all { it.labelled })
    }

    @Test
    fun placeYearTicks_thinsCloseLabelsButKeepsNewestAndOldest() {
        // 10 years, one row each: 9 gaps over 90dp = 10dp apart, closer than 20dp
        val ticks = (0..9).map { YearTick(2026 - it, it) }
        val placed = TimelineScrubberMath.placeYearTicks(ticks, size = 10, travelDp = 90f, minGapDp = 20f)
        assertEquals(10, placed.size)
        assertTrue(placed.first().labelled)
        assertTrue(placed.last().labelled)
        val labelledY = placed.filter { it.labelled }.map { it.fraction * 90f }
        labelledY.zipWithNext().forEach { (a, b) -> assertTrue("labels overlap: $a $b", b - a >= 20f) }
        assertTrue(placed.count { it.labelled } < 10)
    }

    @Test
    fun placeYearTicks_oldestLabelledEvenWhenNearAMiddleLabel() {
        // ticks at 0, 85, 100 (of 100dp): the 85 one is within 20dp of the oldest, so it yields.
        val ticks = listOf(YearTick(3, 0), YearTick(2, 85), YearTick(1, 100))
        val placed = TimelineScrubberMath.placeYearTicks(ticks, size = 101, travelDp = 100f, minGapDp = 20f)
        assertEquals(listOf(true, false, true), placed.map { it.labelled })
    }

    @Test
    fun placeYearTicks_singleAndEmpty() {
        assertTrue(TimelineScrubberMath.placeYearTicks(emptyList(), 0, 100f, 20f).isEmpty())
        val single = TimelineScrubberMath.placeYearTicks(listOf(YearTick(2026, 0)), 40, 100f, 20f)
        assertEquals(1, single.size)
        assertTrue(single[0].labelled)
    }

    // ---- haptics ----

    private fun key(year: Int, month: Int) = year * 12 + month - 1

    @Test
    fun crossedBoundary_month() {
        assertTrue(TimelineScrubberMath.crossedBoundary(key(2026, 3), key(2026, 2), HapticBoundary.MONTH))
        assertTrue(TimelineScrubberMath.crossedBoundary(key(2025, 12), key(2026, 1), HapticBoundary.MONTH))
        assertFalse(TimelineScrubberMath.crossedBoundary(key(2026, 3), key(2026, 3), HapticBoundary.MONTH))
    }

    @Test
    fun crossedBoundary_movingBackwardsAlsoCounts() {
        assertTrue(TimelineScrubberMath.crossedBoundary(key(2026, 2), key(2026, 3), HapticBoundary.MONTH))
    }

    @Test
    fun crossedBoundary_yearModeIgnoresMonthChanges() {
        assertFalse(TimelineScrubberMath.crossedBoundary(key(2026, 3), key(2026, 2), HapticBoundary.YEAR))
        assertTrue(TimelineScrubberMath.crossedBoundary(key(2026, 1), key(2025, 12), HapticBoundary.YEAR))
    }

    @Test
    fun crossedBoundary_noMonthNeverCrosses() {
        assertFalse(TimelineScrubberMath.crossedBoundary(NO_MONTH, key(2026, 3), HapticBoundary.MONTH))
        assertFalse(TimelineScrubberMath.crossedBoundary(key(2026, 3), NO_MONTH, HapticBoundary.MONTH))
    }

    @Test
    fun shouldPerformHaptic_throttles() {
        assertTrue(TimelineScrubberMath.shouldPerformHaptic(null, 0L, 40L))
        assertFalse(TimelineScrubberMath.shouldPerformHaptic(1000L, 1039L, 40L))
        assertTrue(TimelineScrubberMath.shouldPerformHaptic(1000L, 1040L, 40L))
    }
}
