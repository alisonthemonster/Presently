package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.lazy.LazyListState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineScrubberStateTest {

    private val dragStarted = mutableListOf<Unit>()
    private val draggingChanges = mutableListOf<Boolean>()

    private fun newState(rows: Int = 100) = TimelineScrubberState(
        listState = LazyListState(),
        indexProvider = { TimelineScrubberIndex.from(sparseRows(rows)) },
        isListTouched = { false },
        onDragStarted = { dragStarted += Unit },
        onDraggingChanged = { draggingChanges += it },
        performHaptic = {},
        clockMs = { 0L },
        sdkInt = 35
    )

    @Test
    fun startDrag_beginsAtTheScrollPosition_andReportsOnce() {
        val state = newState()
        state.startDrag()

        assertTrue(state.isDragging)
        assertEquals(0f, state.dragFraction!!, 0f)
        assertEquals(1, dragStarted.size)
        assertEquals(listOf(true), draggingChanges)
    }

    @Test
    fun dragBy_movesAndClampsToTheTrack() {
        val state = newState()
        state.startDrag()

        state.dragBy(0.4f)
        assertEquals(0.4f, state.dragFraction!!, 0.0001f)

        state.dragBy(5f)
        assertEquals(1f, state.dragFraction!!, 0f)

        state.dragBy(-5f)
        assertEquals(0f, state.dragFraction!!, 0f)
    }

    @Test
    fun dragBy_withoutADrag_doesNothing() {
        val state = newState()
        state.dragBy(0.5f)
        assertNull(state.dragFraction)
        assertTrue(draggingChanges.isEmpty())
    }

    @Test
    fun endDrag_clearsTheDrag_andReportsOnlyOnce() {
        val state = newState()
        state.startDrag()

        state.endDrag()
        state.endDrag() // e.g. onDragEnd followed by the pointerInput's finally block

        assertFalse(state.isDragging)
        assertEquals(listOf(true, false), draggingChanges)
    }

    @Test
    fun endDrag_withoutADrag_reportsNothing() {
        val state = newState()
        state.endDrag()
        assertTrue(draggingChanges.isEmpty())
    }

    @Test
    fun dragMonthKey_followsTheDrag_andIsNullAfterwards() {
        val state = newState()
        assertNull(state.dragMonthKey)

        val index = TimelineScrubberIndex.from(sparseRows(100))
        state.startDrag()
        state.dragBy(1f)
        assertEquals(index.monthKeyAt(99), state.dragMonthKey)

        state.endDrag()
        assertNull(state.dragMonthKey)
    }
}
