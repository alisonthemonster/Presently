package journal.gratitude.com.gratitudejournal.ui.entry

sealed class EntryEvent {
    object OnHintClicked: EntryEvent()
    object OnSaveClicked: EntryEvent()
    data class OnTextChanged(val newText: String): EntryEvent()
}