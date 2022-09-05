package journal.gratitude.com.gratitudejournal.ui.entryviewpager

import android.annotation.SuppressLint
import android.os.Parcelable
import com.airbnb.mvrx.MavericksState
import journal.gratitude.com.gratitudejournal.model.Entry
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDate

@SuppressLint("ParcelCreator")
@Parcelize
data class EntryViewPagerArgs(
    val date: LocalDate,
    val numEntries: Int?,
    val isNewEntry: Boolean,
) : Parcelable

data class EntryViewPagerState(
    val selectedDate: LocalDate,
    val numEntries: Int?,
    val entriesList: List<Entry>
) : MavericksState {

    constructor(
        args: EntryViewPagerArgs
    ) : this(
        selectedDate = args.date,
        numEntries = args.numEntries,
        entriesList = emptyList()
    )
}