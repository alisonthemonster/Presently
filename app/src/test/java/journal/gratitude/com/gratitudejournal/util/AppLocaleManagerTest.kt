package journal.gratitude.com.gratitudejournal.util

import androidx.core.os.LocaleListCompat
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.settings.model.NO_LANG_PREF
import org.junit.Test

class AppLocaleManagerTest {

    @Test
    fun `localeListFor returns empty locales when no app language is selected`() {
        val localeList = AppLocaleManager.localeListFor(NO_LANG_PREF)

        assertThat(localeList).isEqualTo(LocaleListCompat.getEmptyLocaleList())
    }

    @Test
    fun `localeListFor returns empty locales when preference is null`() {
        val localeList = AppLocaleManager.localeListFor(null)

        assertThat(localeList).isEqualTo(LocaleListCompat.getEmptyLocaleList())
    }

    @Test
    fun `localeListFor returns selected locale tags`() {
        val localeList = AppLocaleManager.localeListFor("pt-BR")

        assertThat(localeList.toLanguageTags()).isEqualTo("pt-BR")
    }
}
