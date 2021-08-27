package com.presently.presently_local_source


import com.nhaarman.mockitokotlin2.*
import com.presently.presently_local_source.database.EntryDao
import com.presently.presently_local_source.model.Entry
import com.presently.presently_local_source.model.EntryEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.threeten.bp.LocalDate
import kotlin.test.Test

class PresentlyLocalSourceTest {

    private val entryDao = mock<EntryDao>()
    private lateinit var localSource: PresentlyLocalSource

    @Before
    fun before() {
        whenever(entryDao.searchAllEntries(any())).thenReturn(mock())

        localSource = RealPresentlyLocalSource(entryDao)
    }

    @Test
    fun getEntry_CallsDaoOnce() = runBlockingTest {
        localSource.getEntry(LocalDate.now())

        verify(entryDao, times(1)).getEntry(any())
    }

    @Test
    fun getEntry_CallsDaoWithRightDate() = runBlockingTest {
        val expectedDate = LocalDate.now()
        localSource.getEntry(expectedDate)

        verify(entryDao).getEntry(expectedDate)
    }

    @Test
    fun getEntries_CallsDaoOnce() {
        runBlocking {
            localSource.getEntries()
        }
        verify(entryDao, times(1)).getEntries()
    }

    @Test
    fun getEntriesFlow_CallsDaoOnce() {
        runBlocking {
            localSource.getEntriesFlow()
        }
        verify(entryDao, times(1)).getEntriesFlow()
    }

    @Test
    fun addEntry_CallsDaoOnce() {
        runBlocking {
            localSource.addEntry(Entry(LocalDate.now(), "Henlo!"))
        }

        verify(entryDao, times(1)).insertEntry(any())
    }

    @Test
    fun addEntryEmpty_CallsDaoDelete() {
        runBlocking {
            localSource.addEntry(Entry(LocalDate.now(), ""))
        }

        verify(entryDao, times(1)).delete(any())
    }

    @Test
    fun addEntry_CallsDaoWithCorrectEntry() {
        val expectedEntry = Entry(LocalDate.now(), "Hello!")
        runBlocking {
            localSource.addEntry(expectedEntry)
        }

        verify(entryDao).insertEntry(EntryEntity(expectedEntry.entryDate, expectedEntry.entryContent))
    }

    @Test
    fun addEntries_CallsDaoWithCorrectEntry() {
        val expectedEntry = listOf(Entry(LocalDate.now(), "Hello!"))
        val expectedEntity = expectedEntry.map { EntryEntity(it.entryDate, it.entryContent) }
        runBlocking {
            localSource.addEntries(expectedEntry)
        }

        verify(entryDao).insertEntries(expectedEntity)
    }

    @Test
    fun searchEntries_callsDaoSearch() {
        localSource.searchEntries("Howdy!")

        verify(entryDao, times(1)).searchAllEntries(any())
    }

    @Test
    fun searchEntries_callsDaoWithCorrectQuery() {
        val query = "Howdy!"
        val expectedQuery = "*$query*"
        localSource.searchEntries(query)

        verify(entryDao).searchAllEntries(expectedQuery)
    }

    //Fixes crash with FTS and quotation marks
    @Test
    fun searchEntries_removesQuotesFromQuery() {
        val query = "\"Howdy!\""
        val expectedQuery = "*Howdy!*"
        localSource.searchEntries(query)

        verify(entryDao).searchAllEntries(expectedQuery)
    }

    @Test
    fun getWrittenDates_callsDaoGetWrittenDates() = runBlockingTest {
        localSource.getWrittenDates()

        verify(entryDao, times(1)).getWrittenDates()
    }
}