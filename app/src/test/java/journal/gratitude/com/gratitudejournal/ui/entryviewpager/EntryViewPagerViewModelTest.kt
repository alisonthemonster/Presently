package journal.gratitude.com.gratitudejournal.ui.entryviewpager

import com.airbnb.mvrx.test.MvRxTestRule
import com.airbnb.mvrx.withState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

class EntryViewPagerViewModelTest {

    private val repository = mock<EntryRepository>()

    @get:Rule
    val mvrxRule = MvRxTestRule()

    @Test
    fun `GIVEN empty list WHEN getEntriesFlow called THEN the list should have Today and Yesterday`() {

        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        val initialState = EntryViewPagerState(EntryViewPagerArgs(LocalDate.now()))

        val mockFlow = flow {
            emit(emptyList<Entry>())
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = EntryViewPagerViewModel(initialState, repository)

        withState(viewModel) {
            assertEquals(it.entriesList.size, 2)
            assertEquals(it.entriesList, listOf(todayEntry, yesterdayEntry))
        }

    }

    @Test
    fun `GIVEN only Today entry WHEN repository getEntriesFlow called THEN the should should have Today and Yesterday`() {

        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        val initialState = EntryViewPagerState(EntryViewPagerArgs(LocalDate.now()))

        val mockFlow = flow {
            emit(listOf(todayEntry))
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = EntryViewPagerViewModel(initialState, repository)

        withState(viewModel) {
            assertEquals(it.entriesList.size, 2)
            assertEquals(it.entriesList, listOf(todayEntry, yesterdayEntry))
        }
    }


    @Test
    fun `GIVEN only Yesterday entry WHEN repository getEntriesFlow called THEN the should should have Today and Yesterday`() {

        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        val initialState = EntryViewPagerState(EntryViewPagerArgs(LocalDate.now()))

        val mockFlow = flow {
            emit(listOf(todayEntry))
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = EntryViewPagerViewModel(initialState, repository)

        withState(viewModel) {
            assertEquals(it.entriesList.size, 2)
            assertEquals(it.entriesList, listOf(todayEntry, yesterdayEntry))
        }
    }

    @Test
    fun `GIVEN 3 entries with Today and Yesterday WHEN repository getEntriesFlow called THEN size should be 3`() {

        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val ereyesterdayEntry = Entry(LocalDate.now().minusDays(2), "")

        val initialState = EntryViewPagerState(EntryViewPagerArgs(LocalDate.now()))

        val mockFlow = flow {
            emit(listOf(todayEntry, yesterdayEntry, ereyesterdayEntry))
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = EntryViewPagerViewModel(initialState, repository)

        withState(viewModel) {
            assertEquals(it.entriesList.size, 3)
            assertEquals(it.entriesList, listOf(todayEntry, yesterdayEntry, ereyesterdayEntry))
        }
    }

    @Test
    fun `GIVEN selected date is 10 days earlier WHEN repository getEntriesFlow called THEN should return data in descending order`() {

        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val ereyesterdayEntry = Entry(LocalDate.now().minusDays(5), "")
        val fifteenDaysEarlierEntry = Entry(LocalDate.now().minusDays(15), "")

        val tenDayEarlierEntry = Entry(LocalDate.now().minusDays(10), "")

        val initialState =
            EntryViewPagerState(EntryViewPagerArgs(tenDayEarlierEntry.entryDate))

        val mockFlow = flow {
            emit(listOf(todayEntry, yesterdayEntry, ereyesterdayEntry, fifteenDaysEarlierEntry))
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = EntryViewPagerViewModel(initialState, repository)

        withState(viewModel) {
            assertEquals(it.entriesList.size, 5)
            assertEquals(
                it.entriesList,
                listOf(
                    todayEntry,
                    yesterdayEntry,
                    ereyesterdayEntry,
                    tenDayEarlierEntry,
                    fifteenDaysEarlierEntry
                )
            )
        }
    }

    @Test
    fun `GIVEN todayEntry with Content WHEN repository getEntriesFlow called THEN state count should be 2`() {

        val todayEntry = Entry(LocalDate.now(), "Some content")

        val initialState =
            EntryViewPagerState(EntryViewPagerArgs(LocalDate.now()))

        val mockFlow = flow {
            emit(listOf(todayEntry))
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = EntryViewPagerViewModel(initialState, repository)

        withState(viewModel) {
            assertEquals(it.numEntries, 1)
        }
    }

    @Test
    fun `GIVEN no entry WHEN repository getEntriesFlow called THEN state count should be 2`() {

        val initialState =
            EntryViewPagerState(EntryViewPagerArgs(LocalDate.now()))

        val mockFlow = flow {
            emit(emptyList<Entry>())
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = EntryViewPagerViewModel(initialState, repository)

        withState(viewModel) {
            assertEquals(it.numEntries, 0)
        }
    }
}