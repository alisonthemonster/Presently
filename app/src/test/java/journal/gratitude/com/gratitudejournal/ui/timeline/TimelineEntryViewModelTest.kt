package journal.gratitude.com.gratitudejournal.ui.timeline

import android.view.View
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineAdapter
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineEntryViewModel
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDate

class TimelineEntryViewModelTest {

    var onClickWasCalled = false
    var clickedDateActual = LocalDate.MAX
    var isNewEntryActual = true
    var numEntriesActual = -1
    private val onClickListener = object : OnClickListener {
        override fun onClick(
            view: View,
            clickedDate: LocalDate,
            isNewEntry: Boolean,
            numEntries: Int
        ) {
            onClickWasCalled = true
            clickedDateActual = clickedDate
            isNewEntryActual = isNewEntry
            numEntriesActual = numEntries
        }

    }
    private val date = LocalDate.of(2011, 11, 11)
    private val content = "hiiiiiiii"
    private val numEntries = 1
    private val viewModel = TimelineEntryViewModel(
        Entry(date, content),
        false,
        numEntries,
        false,
        10,
        onClickListener
    )

    @Test
    fun onClick_callsClickListener() {
        onClickWasCalled = false
        clickedDateActual = LocalDate.MAX
        isNewEntryActual = true
        numEntriesActual = -1
        viewModel.onClick(mock())

        assertThat(onClickWasCalled).isTrue()
        assertThat(clickedDateActual).isEqualTo(date)
        assertThat(numEntriesActual).isEqualTo(1)
        assertThat(isNewEntryActual).isFalse()
    }

    @Test
    fun onClick_callsClickListener_withEmptyContent() {
        onClickWasCalled = false
        clickedDateActual = LocalDate.MAX
        isNewEntryActual = false
        numEntriesActual = -1
        val viewModel = TimelineEntryViewModel(
            Entry(date, ""),
            false,
            numEntries,
            false,
            10,
            onClickListener
        )
        viewModel.onClick(mock())

        assertThat(onClickWasCalled).isTrue()

        assertThat(clickedDateActual).isEqualTo(date)
        assertThat(numEntriesActual).isEqualTo(1)
        assertThat(isNewEntryActual).isTrue()
    }

    @Test
    fun isCurrentDate_currentDate_true() {
        val today = LocalDate.now()
        val viewModel = TimelineEntryViewModel(
            Entry(today, content),
            false,
            numEntries,
            false,
            10,
            onClickListener
        )

        val actual = viewModel.isCurrentDate()

        assertThat(actual).isTrue()
    }

    @Test
    fun isCurrentDate_notCurrentDate_false() {
        val actual = viewModel.isCurrentDate()

        assertThat(actual).isFalse()
    }

    @Test
    fun isEmptyState_emptyState_visible() {
        val content = ""
        val viewModel = TimelineEntryViewModel(
            Entry(date, content),
            false,
            numEntries,
            false,
            10,
            onClickListener
        )

        val actual = viewModel.isEmptyState()
        val expected = View.VISIBLE

        assertEquals(expected, actual)
    }

    @Test
    fun isEmptyState_notEmptyState_gone() {
        val actual = viewModel.isEmptyState()
        val expected = View.GONE

        assertEquals(expected, actual)
    }

    @Test
    fun isTailVisible_notLastItem_gone() {
        val actual = viewModel.isTailVisible()

        assertThat(actual).isFalse()
    }


    @Test
    fun isTailVisible_LastItem_visible() {
        val viewModel = TimelineEntryViewModel(
            Entry(date, content),
            true,
            numEntries,
            false,
            10,
            onClickListener
        )

        val actual = viewModel.isTailVisible()

        assertThat(actual).isTrue()
    }

    @Test
    fun getContent_returns_content() {
        val actual = viewModel.content

        assertEquals(content, actual)
    }

    @Test
    fun getDayString_showDayOfWeekFalse_noDayOfWeek() {
        val actual = viewModel.dateString()
        val expected = "November 11, 2011"

        assertEquals(expected, actual)
    }

    @Test
    fun getDayString_showDayOfWeekTrue_dayOfWeek() {
        val viewModel = TimelineEntryViewModel(
            Entry(date, content),
            false,
            numEntries,
            true,
            10,
            onClickListener
        )

        val actual = viewModel.dateString()
        val expected = "Friday, November 11, 2011"

        assertEquals(expected, actual)
    }
}