package journal.gratitude.com.gratitudejournal.util

import journal.gratitude.com.gratitudejournal.model.Entry
import java.time.LocalDate

/**
 * Returns [list] (newest first) with empty placeholder entries for today and yesterday added
 * when missing. Placeholders are added by date rather than by list position so a future-dated
 * entry at the top of the list can never cause a date to appear twice (the timeline uses the
 * date as a unique LazyColumn key).
 */
fun appendTodayAndYesterday(list: List<Entry>): List<Entry> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val entries = list.distinctBy { it.entryDate }.toMutableList()
    if (entries.none { it.entryDate == today }) {
        entries.add(Entry(today, ""))
    }
    if (entries.none { it.entryDate == yesterday }) {
        entries.add(Entry(yesterday, ""))
    }
    return entries.sortedByDescending { it.entryDate }
}
