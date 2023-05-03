package journal.gratitude.com.gratitudejournal.ui.repository

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepositoryImpl
import journal.gratitude.com.gratitudejournal.room.EntryDao
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.test.Test

class EntryRepositoryTest {

    private val entryDao = mock<EntryDao>()
    private val repository: EntryRepository = EntryRepositoryImpl(entryDao)

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    @Test
    fun getEntry_CallsDaoOnce() = runTest {
        repository.getEntry(today)

        verify(entryDao, times(1)).getEntry(any())
    }

    @Test
    fun getEntry_CallsDaoWithRightDate() = runTest {
        val expectedDate = today
        repository.getEntry(expectedDate)

        verify(entryDao).getEntry(expectedDate)
    }

    @Test
    fun getEntries_CallsDaoOnce() = runTest {
        repository.getEntries()

        verify(entryDao, times(1)).getEntries()
    }

    @Test
    fun getEntriesFlow_CallsDaoOnce() = runTest {
        repository.getEntriesFlow()

        verify(entryDao, times(1)).getEntriesFlow()
    }

    @Test
    fun addEntry_CallsDaoOnce() = runTest {
        repository.addEntry(Entry(today, "Henlo!"))

        verify(entryDao, times(1)).insertEntry(any())
    }

    @Test
    fun addEntryEmpty_CallsDaoDelete() = runTest {
        repository.addEntry(Entry(today, ""))

        verify(entryDao, times(1)).delete(any())
    }

    @Test
    fun addEntry_CallsDaoWithCorrectEntry() = runTest {
        val expectedEntry = Entry(today, "Hello!")
        repository.addEntry(expectedEntry)

        verify(entryDao).insertEntry(expectedEntry)
    }

    @Test
    fun addEntries_CallsDaoWithCorrectEntry() = runTest {
        val expectedEntry = listOf(Entry(today, "Hello!"))
        repository.addEntries(expectedEntry)

        verify(entryDao).insertEntries(expectedEntry)
    }

    @Test
    fun search_callsDaoSearch() = runTest {
        repository.search("Howdy!")

        verify(entryDao, times(1)).search("*Howdy!*")
    }

    // Fixes crash with FTS and quotation marks
    @Test
    fun searchEntries_removesQuotesFromQuery() = runTest {
        val query = "\"Howdy!\""
        val expectedQuery = "*Howdy!*"
        repository.search(query)

        verify(entryDao).search(expectedQuery)
    }
}
