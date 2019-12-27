package journal.gratitude.com.gratitudejournal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.threeten.bp.LocalDate
import java.util.LinkedHashMap

class FakeEntryRepository : EntryRepository {

    //mock of database
    var entriesDatabase: LinkedHashMap<LocalDate, Entry> = LinkedHashMap()

    override fun getEntry(date: LocalDate): LiveData<Entry> {
        val liveData = MutableLiveData<Entry>()

        val entry = entriesDatabase[date] ?: Entry(date, "")

        liveData.value = entry

        return liveData
    }

    override fun getAllEntries(): LiveData<List<Entry>> {
        val liveData = MutableLiveData<List<Entry>>()
        val list = mutableListOf<Entry>()
        entriesDatabase.forEach { (date, entry) -> list.add(entry) }

        liveData.value = list

        return liveData
    }

    override fun getWrittenDates(): LiveData<List<LocalDate>> {
        val liveData = MutableLiveData<List<LocalDate>>()
        liveData.value = entriesDatabase.keys.toList()

        return liveData
    }

    override suspend fun addEntry(entry: Entry) {
        entriesDatabase[entry.entryDate] = entry
    }

    override suspend fun addEntries(entries: List<Entry>) {
        for (entry in entries) {
            entriesDatabase[entry.entryDate] = entry
        }
    }

    override fun searchEntries(query: String): LiveData<PagedList<Entry>> {
        val liveData = MutableLiveData<PagedList<Entry>>()

        //TODO

        return liveData
    }

}