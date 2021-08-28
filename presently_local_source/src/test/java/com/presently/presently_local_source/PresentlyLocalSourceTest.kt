package com.presently.presently_local_source


import androidx.paging.InitialPagingSource
import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import com.presently.presently_local_source.database.EntryDao
import com.presently.presently_local_source.model.Entry
import com.presently.presently_local_source.model.EntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.threeten.bp.LocalDate
import kotlin.test.Test

class PresentlyLocalSourceTest {

    var getEntryWasCalled = false
    var getEntryDate = LocalDate.now()
    var getEntriesWasCalled = false
    var getEntriesFlowWasCalled = false
    var insertEntryWasCalled = false
    var deleteWasCalled = false
    var insertedEntry = EntryEntity(LocalDate.now(), "test")
    var insertEntriesWasCalled = false
    var insertedEntries = emptyList<EntryEntity>()
    var searchQuery = ""
    var getWrittenDatesWasCalled = false

    private val entryDao = object : EntryDao {
        override fun getEntriesFlow(): Flow<List<EntryEntity>> {
            getEntriesFlowWasCalled = true
            return flowOf(emptyList())
        }

        override suspend fun getEntries(): List<EntryEntity> {
            getEntriesWasCalled = true
            return emptyList()
        }

        override suspend fun getWrittenDates(): List<LocalDate> {
            getWrittenDatesWasCalled = true
            return emptyList()
        }

        override suspend fun getEntry(date: LocalDate): EntryEntity?  {
            getEntryWasCalled = true
            getEntryDate = date
            return null
        }

        override suspend fun delete(entry: EntryEntity) {
            deleteWasCalled = true
        }

        override fun searchAllEntries(query: String): PagingSource<Int, EntryEntity> {
            searchQuery = query
            return InitialPagingSource()
        }

        override suspend fun insertEntry(entry: EntryEntity) {
            insertedEntry = entry
            insertEntryWasCalled = true
        }

        override suspend fun insertEntries(entries: List<EntryEntity>) {
            insertEntriesWasCalled = true
            insertedEntries = entries
        }
    }


    @Test
    fun getEntry_CallsDaoOnce() = runBlockingTest {
        getEntryWasCalled = false
        val localSource = RealPresentlyLocalSource(entryDao)
        localSource.getEntry(LocalDate.now())

        assertThat(getEntryWasCalled).isTrue()
    }

    @Test
    fun getEntry_CallsDaoWithRightDate() = runBlockingTest {
        getEntryDate = LocalDate.now()
        val expectedDate = LocalDate.of(2021, 2, 2)

        val localSource = RealPresentlyLocalSource(entryDao)
        localSource.getEntry(expectedDate)

        assertThat(getEntryDate).isEqualTo(expectedDate)
    }

    @Test
    fun getEntries_CallsDaoOnce() {
        getEntriesWasCalled = false
        val localSource = RealPresentlyLocalSource(entryDao)
        runBlocking {
            localSource.getEntries()
        }
        assertThat(getEntriesWasCalled).isTrue()
    }

    @Test
    fun getEntriesFlow_CallsDaoOnce() {
        getEntriesFlowWasCalled = false
        val localSource = RealPresentlyLocalSource(entryDao)
        runBlocking {
            localSource.getEntriesFlow()
        }
        assertThat(getEntriesFlowWasCalled).isTrue()
    }

    @Test
    fun addEntry_CallsDaoOnce() {
        insertEntryWasCalled = false
        val localSource = RealPresentlyLocalSource(entryDao)
        runBlocking {
            localSource.addEntry(Entry(LocalDate.now(), "Henlo!"))
        }

        assertThat(insertEntryWasCalled).isTrue()
    }

    @Test
    fun addEntryEmpty_CallsDaoDelete() {
        deleteWasCalled = false
        val localSource = RealPresentlyLocalSource(entryDao)
        runBlocking {
            localSource.addEntry(Entry(LocalDate.now(), ""))
        }

        assertThat(deleteWasCalled).isTrue()

    }

    @Test
    fun addEntry_CallsDaoWithCorrectEntry() {
        insertedEntry = EntryEntity(LocalDate.now(), "test")
        val localSource = RealPresentlyLocalSource(entryDao)
        val expectedEntry = Entry(LocalDate.now(), "Hello!")
        runBlocking {
            localSource.addEntry(expectedEntry)
        }

        assertThat(insertedEntry).isEqualTo(EntryEntity(expectedEntry.entryDate, expectedEntry.entryContent))
    }

    @Test
    fun addEntries_CallsDaoWithCorrectEntry() = runBlockingTest {
        insertEntriesWasCalled = false
        insertedEntries = emptyList()
        val localSource = RealPresentlyLocalSource(entryDao)
        val expectedEntry = listOf(Entry(LocalDate.now(), "Hello!"))
        val expectedEntity = expectedEntry.map { EntryEntity(it.entryDate, it.entryContent) }

        localSource.addEntries(expectedEntry)

        assertThat(insertEntriesWasCalled).isTrue()
        assertThat(insertedEntries).isEqualTo(expectedEntity)
    }

    @Test
    fun searchEntries_callsDaoWithCorrectQuery() {
        searchQuery = ""
        val localSource = RealPresentlyLocalSource(entryDao)
        val query = "Howdy!"
        val expectedQuery = "*$query*"
        localSource.searchEntries(query)

        assertThat(searchQuery).isEqualTo(expectedQuery)
    }

    //Fixes crash with FTS and quotation marks
    @Test
    fun searchEntries_removesQuotesFromQuery() {
        searchQuery = ""
        val localSource = RealPresentlyLocalSource(entryDao)
        val query = "\"Howdy!\""
        val expectedQuery = "*Howdy!*"
        localSource.searchEntries(query)

        assertThat(searchQuery).isEqualTo(expectedQuery)
    }

    @Test
    fun getWrittenDates_callsDaoGetWrittenDates() = runBlockingTest {
        getWrittenDatesWasCalled = false
        val localSource = RealPresentlyLocalSource(entryDao)
        localSource.getWrittenDates()

        assertThat(getWrittenDatesWasCalled).isTrue()
    }
}