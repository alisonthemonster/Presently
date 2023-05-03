package journal.gratitude.com.gratitudejournal.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.model.Entry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.datetime.LocalDate

@RunWith(AndroidJUnit4::class)
class EntryDaoTest {

    private lateinit var database: EntryDatabase
    private lateinit var entryDao: EntryDao

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EntryDatabase::class.java
        ).allowMainThreadQueries().build()
        entryDao = database.entryDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeEntryAndReadInList() = runTest {
        val date = LocalDate(2012, 1, 1)
        val expectedEntry = Entry(date, "Test content")
        entryDao.insertEntry(expectedEntry)

        val actualEntry = entryDao.getEntry(date)
        assertEquals(expectedEntry, actualEntry)
    }

    @Test
    fun writeMultipleEntries() {
        entryDao.insertEntries(mockEntriesSorted)

        val actualEntry = entryDao.getEntries()
        assertEquals(mockEntriesSorted, actualEntry)
    }

    @Test
    fun getEntriesReturnsEntries() = runTest {
        entryDao.insertEntry(entryTwo)
        entryDao.insertEntry(entryThree)
        entryDao.insertEntry(entryOne)

        val actualEntry =
            entryDao.getEntries()
        assertEquals(mockEntriesSorted, actualEntry)
    }

    @Test
    fun deleteEntryRemovesEntry() = runTest {
        entryDao.insertEntries(mockEntriesSorted)
        entryDao.delete(entryTwo)

        val actualEntry = entryDao.getEntries()
        assertEquals(listOf(entryOne, entryThree), actualEntry)
    }

    private val entryOne = Entry(LocalDate(2013, 1, 1), "Test content")
    private val entryTwo = Entry(LocalDate(2012, 1, 1), "Test content1")
    private val entryThree = Entry(LocalDate(2011, 1, 1), "Test content2")

    private val mockEntriesSorted = listOf(entryOne, entryTwo, entryThree)
}
