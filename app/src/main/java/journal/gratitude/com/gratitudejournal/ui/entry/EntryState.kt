package journal.gratitude.com.gratitudejournal.ui.entry

import com.airbnb.mvrx.MavericksState
import org.threeten.bp.LocalDate
import java.util.Queue


data class EntryState(
    val date: LocalDate,
    val entryContent: String,
    val hint: String,
    val quote: String,
    val hasUserEdits: Boolean,
    val prompts: Queue<String>
) : MavericksState {
    val isEmpty = entryContent.isEmpty()
}