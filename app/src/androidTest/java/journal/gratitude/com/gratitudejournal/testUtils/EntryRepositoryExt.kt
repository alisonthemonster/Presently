package journal.gratitude.com.gratitudejournal.testUtils

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.threeten.bp.LocalDate

/**
 * A blocking version of TasksRepository.saveTask to minimize the number of times we have to
 * explicitly add <code>runBlocking { ... }</code> in our tests
 */
fun EntryRepository.saveEntryBlocking(entry: Entry) = kotlinx.coroutines.runBlocking {
    this@saveEntryBlocking.addEntry(entry)
}

fun EntryRepository.saveEntriesBlocking(entries: List<Entry>) = kotlinx.coroutines.runBlocking {
    this@saveEntriesBlocking.addEntries(entries)
}

