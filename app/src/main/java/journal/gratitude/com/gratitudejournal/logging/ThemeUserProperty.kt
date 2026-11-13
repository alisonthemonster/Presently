package journal.gratitude.com.gratitudejournal.logging

import journal.gratitude.com.gratitudejournal.model.THEME
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings

/**
 * The theme the user currently has selected, as the stored theme name (the default is
 * `original`). Set as a user property so any other analytics can be segmented by it, which also
 * covers users who picked a theme before this was tracked.
 *
 * Call at launch and whenever the theme changes. Changes are handled in [RealPresentlySettings].
 */
internal fun AnalyticsLogger.updateThemeProperty(settings: PresentlySettings) {
    setUserProperty(THEME, settings.getCurrentTheme())
}
