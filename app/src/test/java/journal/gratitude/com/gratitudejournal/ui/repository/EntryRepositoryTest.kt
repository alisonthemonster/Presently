package journal.gratitude.com.gratitudejournal.ui.repository

import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepositoryImpl
import journal.gratitude.com.gratitudejournal.room.EntryDao
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class EntryRepositoryTest {

    private val entryDao = mock<EntryDao>()
    private lateinit var repository: EntryRepository

    @Before
    fun before() {
        whenever(entryDao.searchAllEntries(any())).thenReturn(mock())

        repository = EntryRepositoryImpl(entryDao)
    }

    @Test
    fun getEntry_CallsDaoOnce() {
        repository.getEntry(LocalDate.now())

        verify(entryDao, times(1)).getEntry(any())
    }

    @Test
    fun getEntry_CallsDaoWithRightDate() {
        val expectedDate = LocalDate.now()
        repository.getEntry(expectedDate)

        verify(entryDao).getEntry(expectedDate)
    }

    @Test
    fun getEntries_CallsDaoOnce() {
        repository.getAllEntries()

        verify(entryDao, times(1)).getEntries()
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

        verify(entryDao, times(1)).searchAllEntries(any())
    }

    @Test
    fun searchEntries_callsDaoWithCorrectQuery() {
        val query = "Howdy!"
        val expectedQuery = "*$query*"
        repository.searchEntries(query)

        verify(entryDao).searchAllEntries(expectedQuery)
    }

    //Fixes crash with FTS and quotation marks
    @Test
    fun searchEntries_removesQuotesFromQuery() {
        val query = "\"Howdy!\""
        val expectedQuery = "*Howdy!*"
        repository.searchEntries(query)

        verify(entryDao).searchAllEntries(expectedQuery)
    }

    @Test
    fun getWrittenDates_callsDaoGetWrittenDates() {
        repository.getWrittenDates()

        verify(entryDao, times(1)).getWrittenDates()
    }
}