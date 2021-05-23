package journal.gratitude.com.gratitudejournal.ui.entry

import com.airbnb.mvrx.MavericksState
import org.threeten.bp.LocalDate

data class EntryState(
    val date: LocalDate,
    val entryContent: String,
    val hint: String?,
    val quote: String,
    val hasUserEdits: Boolean,
    val promptNumber: Int,
    val promptsList: List<String>,
    val isSaved: Boolean
) : MavericksState {
    val isEmpty = entryContent.isEmpty()
}