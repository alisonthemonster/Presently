package journal.gratitude.com.gratitudejournal.ui.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import journal.gratitude.com.gratitudejournal.R

/**
 * Asks the user to type a confirmation word before permanently erasing every entry.
 *
 * The localized word is shown in the dialog; the English word is always accepted too.
 * Reports a confirmation to the parent fragment via [REQUEST_KEY] and does nothing else, so
 * the caller decides what "erase" means.
 */
class EraseAllEntriesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val confirmWord = getString(R.string.erase_all_confirm_word)
        val content = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_erase_all_entries, null)
        val input = content.findViewById<EditText>(R.id.erase_confirm_input)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.erase_all_dialog_title)
            .setMessage(getString(R.string.erase_all_dialog_message, confirmWord))
            .setView(content)
            .setPositiveButton(R.string.erase_all_confirm_button) { _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf())
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        // Evaluated on show so restored text (after rotation) sets the right initial state.
        dialog.setOnShowListener {
            val confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            fun updateButton(typed: CharSequence?) {
                confirmButton.isEnabled = matchesConfirmWord(typed, confirmWord)
            }
            updateButton(input.text)
            input.addTextChangedListener { updateButton(it) }
        }
        return dialog
    }

    companion object {
        const val TAG = "EraseAllEntriesDialog"
        const val REQUEST_KEY = "erase_all_entries_confirmed"
        private const val ENGLISH_CONFIRM_WORD = "ERASE"

        internal fun matchesConfirmWord(typed: CharSequence?, localizedWord: String): Boolean {
            val text = typed?.toString().orEmpty().trim()
            return text.equals(localizedWord, ignoreCase = true) ||
                text.equals(ENGLISH_CONFIRM_WORD, ignoreCase = true)
        }
    }
}
