package journal.gratitude.com.gratitudejournal.ui.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.YearMonth
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TimelineScrubberLabelsTest {

    private val march2026 = YearMonth.of(2026, 3)

    @Test
    fun english_isMonthNameThenYear() {
        assertEquals("March 2026", TimelineScrubberLabels(Locale.US).monthYear(march2026))
    }

    @Test
    fun german_usesLocalMonthName() {
        assertEquals("März 2026", TimelineScrubberLabels(Locale.GERMANY).monthYear(march2026))
    }

    @Test
    fun japanese_usesNonLatinScript() {
        val text = TimelineScrubberLabels(Locale.JAPAN).monthYear(march2026)
        assertTrue(text, text.contains("2026") && text.contains("3") && text.contains("月"))
    }

    @Test
    fun arabic_usesArabicMonthName() {
        val text = TimelineScrubberLabels(Locale.forLanguageTag("ar")).monthYear(march2026)
        assertTrue(text, text.contains("مارس"))
    }

    @Test
    fun monthKey_roundTripsAndRejectsNoMonth() {
        val labels = TimelineScrubberLabels(Locale.US)
        assertEquals("March 2026", labels.monthYear(2026 * 12 + 2))
        assertNull(labels.monthYear(TimelineScrubberIndex.NO_MONTH))
    }
}
