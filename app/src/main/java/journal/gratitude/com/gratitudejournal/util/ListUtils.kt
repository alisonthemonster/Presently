package journal.gratitude.com.gratitudejournal.util

import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDate

fun appendTodayAndYesterday(list: List<Entry>): List<Entry> {
    var entries: List<Entry>
    val today = LocalDate.now()
    val yesterday = LocalDate.now().minusDays(1)
    when {
        list.isEmpty() -> {
            entries = listOf<Entry>(
                Entry(today, ""),
                Entry(yesterday, "")
            )
        }
        list.size < 2 -> {
            //user has only ever written one day
            val newList = mutableListOf<Entry>()
            newList.addAll(list)
            if (list[0].entryDate != today) {
                newList.add(0, Entry(today, ""))
            }
            if (list[0].entryDate != yesterday) {
                newList.add(1, Entry(yesterday, ""))
            }
            entries = newList
        }
        else -> {
            val latest = list[0]
            val newList = mutableListOf<Entry>()
            newList.addAll(list)
            if (latest.entryDate != today) {
                newList.add(0, Entry(today, ""))
            }
            if (newList[1].entryDate != yesterday) {
                newList.add(1, Entry(yesterday, ""))
            }
            entries = newList
        }
    }

    return entries
}