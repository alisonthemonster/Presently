package journal.gratitude.com.gratitudejournal.ui.theme

import androidx.annotation.StyleRes
import journal.gratitude.com.gratitudejournal.R

enum class PresentlyThemeSpec(
    val storageValue: String,
    @StyleRes val styleRes: Int
) {
    Original("Original", R.style.Base_AppTheme),
    Sunset("Sunset", R.style.AppTheme_SUNSET),
    Moonlight("Moonlight", R.style.AppTheme_MOONLIGHT),
    Midnight("Midnight", R.style.AppTheme_MIDNIGHT),
    Ivy("Ivy", R.style.AppTheme_IVY),
    Dawn("Dawn", R.style.AppTheme_DAWN),
    Wesley("Wesley", R.style.AppTheme_WESLEY),
    Moss("Moss", R.style.AppTheme_MOSS),
    Clean("Clean", R.style.AppTheme_CLEAN),
    Glacier("Glacier", R.style.AppTheme_GLACIER),
    Gelato("Gelato", R.style.AppTheme_GELATO),
    Waves("Waves", R.style.AppTheme_WAVES),
    Beach("Beach", R.style.AppTheme_BEACH),
    Field("Field", R.style.AppTheme_FIELD),
    Western("Western", R.style.AppTheme_WESTERN),
    Sunlight("Sunlight", R.style.AppTheme_SUNLIGHT),
    Tulip("Tulip", R.style.AppTheme_TULIP),
    Rosie("Rosie", R.style.AppTheme_ROSIE),
    Daisy("Daisy", R.style.AppTheme_DAISY),
    Matisse("Matisse", R.style.AppTheme_MATISSE),
    Clouds("Clouds", R.style.AppTheme_CLOUDS),
    Monstera("Monstera", R.style.AppTheme_MONSTERA),
    Lotus("Lotus", R.style.AppTheme_LOTUS),
    Katie("Katie", R.style.AppTheme_KATIE),
    Brittany("Brittany", R.style.AppTheme_BRITTANY),
    Jungle("Jungle", R.style.AppTheme_JUNGLE),
    Julie("Julie", R.style.AppTheme_JULIE),
    Ellen("Ellen", R.style.AppTheme_ELLEN),
    Danah("Danah", R.style.AppTheme_DANAH),
    Ahalya("Ahalya", R.style.AppTheme_AHALYA),
    Remmie("Rem'mie", R.style.AppTheme_REMMIE),
    Marsha("Marsha", R.style.AppTheme_MARSHA),
    Brayla("Brayla", R.style.AppTheme_BRAYLA),
    Autumn("Autumn", R.style.AppTheme_AUTUMN),
    Betty("Betty", R.style.AppTheme_BETTY),
    Boo("Boo", R.style.AppTheme_BOO),
    Calm("Calm", R.style.AppTheme_CALM),
    Passion("Passion", R.style.AppTheme_PASSION),
    Joy("Joy", R.style.AppTheme_JOY),
    Annalisa("Annalisa", R.style.AppTheme_ANNALISA),
    Celia("Celia", R.style.AppTheme_CELIA),
    Sophia("Sophia", R.style.AppTheme_SOPHIA),
    Emilia("Emilia", R.style.AppTheme_EMILIA),
    Betsy("Betsy", R.style.AppTheme_BETSY),
    Pacific("Pacific", R.style.AppTheme_PACIFIC);

    companion object {
        fun fromStorageValue(value: String?): PresentlyThemeSpec {
            val normalizedValue = value?.trim().orEmpty()

            return entries.firstOrNull {
                it.storageValue.equals(normalizedValue, ignoreCase = true)
            } ?: Original
        }
    }
}
