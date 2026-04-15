package journal.gratitude.com.gratitudejournal.ui.calendar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.platform.LocalContext
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureAcrossThemes
import journal.gratitude.com.gratitudejournal.testUtils.captureInTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.threeten.bp.LocalDate
import java.time.DayOfWeek
import java.util.Locale


/**
 * To record screenshot tests: ./gradlew :app:recordRoborazziDebug --tests 'journal.gratitude.com.gratitudejournal.ui.calendar.EntryCalendarScreenshotTest'
 * */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class EntryCalendarScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun calendar_withEntries_allThemes() {
        composeRule.captureAcrossThemes(screen = "calendar", scenario = "with-entries") { _ ->
            EntryCalendar(
                writtenDates = listOf(
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 5),
                    LocalDate.of(2026, 4, 10),
                    LocalDate.of(2026, 4, 11),
                    LocalDate.of(2026, 3, 28),
                ),
                firstDayOfWeek = DayOfWeek.MONDAY,
                onDateClick = { _, _, _ -> },
                onCloseClick = {}
            )
        }
    }

    @Test
    fun calendar_sundayFirst_allThemes() {
        composeRule.captureInTheme(
            screen = "calendar",
            scenario = "sunday-first",
            themeSpec = PresentlyThemeSpec.Original
        ) {
            EntryCalendar(
                writtenDates = listOf(
                    LocalDate.of(2026, 4, 5),
                    LocalDate.of(2026, 4, 10),
                ),
                firstDayOfWeek = DayOfWeek.SUNDAY,
                onDateClick = { _, _, _ -> },
                onCloseClick = {}
            )
        }
    }

    @Test
    fun calendar_localeArabic_originalTheme() {
        withDefaultLocale(Locale.forLanguageTag("ar")) {
            composeRule.captureInTheme(
                screen = "calendar",
                scenario = "locale-arabic",
                themeSpec = PresentlyThemeSpec.Original
            ) {
                WithLocaleContext(locale = Locale.forLanguageTag("ar")) {
                    EntryCalendar(
                        writtenDates = listOf(
                            LocalDate.of(2026, 4, 1),
                            LocalDate.of(2026, 4, 5),
                            LocalDate.of(2026, 4, 10),
                        ),
                        firstDayOfWeek = DayOfWeek.MONDAY,
                        onDateClick = { _, _, _ -> },
                        onCloseClick = {}
                    )
                }
            }
        }
    }

    private fun withDefaultLocale(locale: Locale, block: () -> Unit) {
        val previousLocale = Locale.getDefault()
        Locale.setDefault(locale)
        try {
            block()
        } finally {
            Locale.setDefault(previousLocale)
        }
    }

    @Composable
    private fun WithLocaleContext(
        locale: Locale,
        content: @Composable () -> Unit
    ) {
        val baseContext = LocalContext.current
        val localizedContext = remember(baseContext, locale) {
            val configuration = Configuration(baseContext.resources.configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            baseContext.createConfigurationContext(configuration)
        }

        CompositionLocalProvider(LocalContext provides localizedContext) {
            content()
        }
    }
}
