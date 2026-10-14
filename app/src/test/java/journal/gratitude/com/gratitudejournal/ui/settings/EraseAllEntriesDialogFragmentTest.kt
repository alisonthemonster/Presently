package journal.gratitude.com.gratitudejournal.ui.settings

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EraseAllEntriesDialogFragmentTest {

    private val word = "BORRAR"

    @Test
    fun matches_localizedWord_ignoringCaseAndWhitespace() {
        assertTrue(EraseAllEntriesDialogFragment.matchesConfirmWord("borrar", word))
        assertTrue(EraseAllEntriesDialogFragment.matchesConfirmWord("  BORRAR ", word))
    }

    @Test
    fun matches_englishWord_evenWhenLocalizedWordDiffers() {
        assertTrue(EraseAllEntriesDialogFragment.matchesConfirmWord("erase", word))
    }

    @Test
    fun doesNotMatch_emptyNullOrPartial() {
        assertFalse(EraseAllEntriesDialogFragment.matchesConfirmWord(null, word))
        assertFalse(EraseAllEntriesDialogFragment.matchesConfirmWord("", word))
        assertFalse(EraseAllEntriesDialogFragment.matchesConfirmWord("BORR", word))
    }

    @Test
    fun matches_turkishDottedCapitalI() {
        assertTrue(EraseAllEntriesDialogFragment.matchesConfirmWord("sil", "S\u0130L"))
    }
}
