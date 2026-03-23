package journal.gratitude.com.gratitudejournal.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PresentlyThemeSpecTest {

    @Test
    fun fromStorageValue_mapsCurrentOriginalPreference() {
        val actual = PresentlyThemeSpec.fromStorageValue("original")

        assertThat(actual).isEqualTo(PresentlyThemeSpec.Original)
    }

    @Test
    fun fromStorageValue_fallsBackToOriginalForUnknownValues() {
        val actual = PresentlyThemeSpec.fromStorageValue("not-a-real-theme")

        assertThat(actual).isEqualTo(PresentlyThemeSpec.Original)
    }
}
