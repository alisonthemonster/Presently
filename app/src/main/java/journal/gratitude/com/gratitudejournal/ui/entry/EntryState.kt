package journal.gratitude.com.gratitudejournal.ui.entry

import android.annotation.SuppressLint
import android.os.Parcelable
import com.airbnb.mvrx.MavericksState
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

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