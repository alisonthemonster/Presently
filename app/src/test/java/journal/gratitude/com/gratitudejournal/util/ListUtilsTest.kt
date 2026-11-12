package journal.gratitude.com.gratitudejournal.util

import journal.gratitude.com.gratitudejournal.model.Entry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ListUtilsTest {
    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)

    @Test
    fun emptyListGetsTodayAndYesterday() {
        assertEquals(listOf(today, yesterday), appendTodayAndYesterday(emptyList()).map { it.entryDate })
    }

    @Test
    fun futureEntryAboveYesterdayDoesNotDuplicateDates() {
        val list = listOf(
            Entry(today.plusDays(1), "future"),
            Entry(yesterday, "y"),
            Entry(today.minusDays(5), "old")
        )
        val dates = appendTodayAndYesterday(list).map { it.entryDate }
        assertEquals(dates.distinct(), dates)
        assertEquals(listOf(today.plusDays(1), today, yesterday, today.minusDays(5)), dates)
    }

    @Test
    fun futureEntryAboveTodayDoesNotDuplicateDates() {
        val list = listOf(Entry(today.plusDays(1), "f"), Entry(today, "t"))
        val dates = appendTodayAndYesterday(list).map { it.entryDate }
        assertEquals(listOf(today.plusDays(1), today, yesterday), dates)
    }
}
