package journal.gratitude.com.gratitudejournal.ui.entry

import android.annotation.SuppressLint
import android.os.Parcelable
import com.airbnb.mvrx.MavericksState
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDate

data class EntryViewState(
    val date: LocalDate = LocalDate.now(),
    val content: String = "",
    val isNewEntry: Boolean = true,
    val hasUserEdits: Boolean = false,
    val promptNumber: Int = -1,
    val saveState: SaveState = SaveState(),
) {
    val shouldShowHintButton = content.isEmpty()
}

data class SaveState(
    val entryCount: Int = -1,
) {
    val isSaved = entryCount != -1
    val milestoneWasReached = isMilestone(entryCount)
}

@SuppressLint("ParcelCreator")
@Parcelize
data class EntryArgs(
    val date: String,
    val isNewEntry: Boolean,
    val numberExistingEntries: Int?,
    val quote: String,
    val firstHint: String,
    val prompts: List<String>
) : Parcelable

data class EntryState(
    val date: LocalDate,
    val entryContent: String,
    val isNewEntry: Boolean,
    val numberExistingEntries: Int?,
    val hint: String,
    val quote: String,
    val hasUserEdits: Boolean,
    val promptNumber: Int,
    val promptsList: List<String>,
    val isSaved: Boolean,
    val milestoneNumber: Int = 0
) : MavericksState {

    constructor(args: EntryArgs) : this(
        args.date.toLocalDate(),
        // space will stop the hint from showing while the entry is fetched from db
        if (args.isNewEntry) "" else " ",
        args.isNewEntry,
        args.numberExistingEntries,
        args.firstHint,
        args.quote,
        false,
        0,
        args.prompts,
        false
    )

    val isEmpty = entryContent.isEmpty()

}