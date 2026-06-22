package journal.gratitude.com.gratitudejournal.ui.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepositoryImpl
import journal.gratitude.com.gratitudejournal.room.EntryDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.threeten.bp.LocalDate
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryRepositoryTest {

    private val entryDao = mock<EntryDao>()
    private lateinit var repository: EntryRepository

    @Before
    fun before() {
        whenever(entryDao.searchAllEntries(any())).thenReturn(TestEntryPagingSource())

        repository = EntryRepositoryImpl(entryDao)
    }

    @Test
    fun getEntry_CallsDaoOnce() = runTest {
        repository.getEntry(LocalDate.now())

        verify(entryDao, times(1)).getEntry(any())
    }

    @Test
    fun getEntry_CallsDaoWithRightDate() = runTest {
        val expectedDate = LocalDate.now()
        repository.getEntry(expectedDate)

        verify(entryDao).getEntry(expectedDate)
    }

    @Test
    fun getEntries_CallsDaoOnce() {
        runBlocking {
            // Will be launched in the mainThreadSurrogate dispatcher
            repository.getEntries()
        }
        verify(entryDao, times(1)).getEntries()
    }

    @Test
    fun getEntriesFlow_CallsDaoOnce() {
        runBlocking {
            // Will be launched in the mainThreadSurrogate dispatcher
            repository.getEntriesFlow()
        }
        verify(entryDao, times(1)).getEntriesFlow()
    }

    @Test
    fun addEntry_CallsDaoOnce() {
        runBlocking {
            repository.addEntry(Entry(LocalDate.now(), "Henlo!"))

        }

        verify(entryDao, times(1)).insertEntry(any())
    }

    @Test
    fun addEntryEmpty_CallsDaoDelete() {
        runBlocking {
            repository.addEntry(Entry(LocalDate.now(), ""))

        }

        verify(entryDao, times(1)).delete(any())
    }

    @Test
    fun addEntry_CallsDaoWithCorrectEntry() {
        val expectedEntry = Entry(LocalDate.now(), "Hello!")
        runBlocking {
            repository.addEntry(expectedEntry)

        }

        verify(entryDao).insertEntry(expectedEntry)
    }

    @Test
    fun addEntries_CallsDaoWithCorrectEntry() {
        val expectedEntry = listOf(Entry(LocalDate.now(), "Hello!"))
        runBlocking {
            repository.addEntries(expectedEntry)

        }

        verify(entryDao).insertEntries(expectedEntry)
    }

    @Test
    fun searchEntries_callsDaoSearch() {
        repository.searchEntries("Howdy!")

        verify(entryDao, never()).searchAllEntries(any())
    }

    @Test
    fun searchEntries_collectingResultsCallsDaoSearch() = runTest {
        repository.searchEntries("Howdy!").take(1).toList()

        verify(entryDao, times(1)).searchAllEntries(any())
    }

    @Test
    fun searchEntries_collectingResultsCallsDaoWithCorrectQuery() = runTest {
        val query = "Howdy!"
        val expectedQuery = "*$query*"
        repository.searchEntries(query).take(1).toList()

        verify(entryDao).searchAllEntries(expectedQuery)
    }

    //Fixes crash with FTS and quotation marks
    @Test
    fun searchEntries_removesQuotesFromQuery() = runTest {
        val query = "\"Howdy!\""
        val expectedQuery = "*Howdy!*"
        repository.searchEntries(query).take(1).toList()

        verify(entryDao).searchAllEntries(expectedQuery)
    }

    @Test
    fun searchEntries_invalidatingPagingSourceCreatesANewOne() = runTest {
        val firstPagingSource = TestEntryPagingSource()
        val secondPagingSource = TestEntryPagingSource()
        whenever(entryDao.searchAllEntries(any())).thenReturn(firstPagingSource, secondPagingSource)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.searchEntries("Howdy!").take(2).toList()
        }

        advanceUntilIdle()
        verify(entryDao, times(1)).searchAllEntries("*Howdy!*")

        firstPagingSource.invalidate()
        advanceUntilIdle()

        verify(entryDao, times(2)).searchAllEntries("*Howdy!*")
        collectJob.cancel()
    }

    @Test
    fun getWrittenDates_callsDaoGetWrittenDates() {
        repository.getWrittenDates()

        verify(entryDao, times(1)).getWrittenDates()
    }

    @Test
    fun getRandomEntry_CallsDaoOnce() = runTest {
        repository.getRandomEntry()

        verify(entryDao, times(1)).getRandomEntry()
    }

    private class TestEntryPagingSource : PagingSource<Int, Entry>() {
        override fun getRefreshKey(state: PagingState<Int, Entry>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Entry> {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }
    }
}
