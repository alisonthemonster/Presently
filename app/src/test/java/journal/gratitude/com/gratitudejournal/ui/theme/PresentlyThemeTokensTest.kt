package journal.gratitude.com.gratitudejournal.ui.theme

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PresentlyThemeTokensTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun resolvePresentlyThemeTokens_usesThemeSpecStyleValues() {
        val tokens = context.resolvePresentlyThemeTokens(PresentlyThemeSpec.Sunset)

        assertThat(tokens.timelineIconRes).isEqualTo(R.drawable.ic_sun_icon)
        assertThat(tokens.timelineBackground).isEqualTo(colorOf(R.color.sunsetBackgroundColor))
        assertThat(tokens.toolbar).isEqualTo(colorOf(R.color.sunsetToolbarColor))
        assertThat(tokens.toolbarItem).isEqualTo(colorOf(R.color.sunsetToolbarItemColor))
        assertThat(tokens.timelineBody).isEqualTo(colorOf(R.color.sunsetPrimaryColor))
        assertThat(tokens.highlight).isEqualTo(colorOf(R.color.sunsetAndroidWidgetColor))
    }

    @Test
    fun resolvePresentlyThemeTokens_withoutExplicitSpec_usesCurrentContextTheme() {
        val themedContext = ContextThemeWrapper(context, PresentlyThemeSpec.Midnight.styleRes)

        val tokens = themedContext.resolvePresentlyThemeTokens()

        assertThat(tokens.timelineIconRes).isEqualTo(R.drawable.ic_moon)
        assertThat(tokens.timelineBackground).isEqualTo(colorOf(R.color.midnightBackgroundColor))
        assertThat(tokens.toolbar).isEqualTo(colorOf(R.color.midnightToolbarColor))
        assertThat(tokens.entryBody).isEqualTo(colorOf(R.color.midnightMainTextAndButtonColor))
    }

    private fun colorOf(colorRes: Int): Color {
        return Color(ContextCompat.getColor(context, colorRes))
    }
}
