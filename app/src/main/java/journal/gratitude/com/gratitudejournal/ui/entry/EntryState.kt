package journal.gratitude.com.gratitudejournal.ui.entry

import android.annotation.SuppressLint
import android.os.Parcelable
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.parcelize.Parcelize
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

data class EntryUiState(
    val date: LocalDate,
    val entryContent: String,
    val isNewEntry: Boolean,
    val numberExistingEntries: Int?,
    val hint: String,
    val quote: String,
    val showQuote: Boolean,
    val promptNumber: Int,
    val promptsList: List<String>,
    val isLoading: Boolean,
    val hasUnsavedChanges: Boolean
) {

    constructor(args: EntryArgs) : this(
        args.date.toLocalDate(),
        "",
        args.isNewEntry,
        args.numberExistingEntries,
        args.firstHint,
        args.quote,
        true,
        0,
        args.prompts,
        !args.isNewEntry,
        false
    )

    val isEmpty = entryContent.isEmpty()
}

sealed interface EntryEffect {
    data class OpenShare(val entryContent: String, val date: LocalDate) : EntryEffect

    data class CopyQuote(val quote: String) : EntryEffect

    data object ShowUnsavedChangesDialog : EntryEffect

    data class EntrySaved(
        val milestoneNumber: Int,
        val shouldTriggerReminderOnboarding: Boolean
    ) : EntryEffect

    data object NavigateBack : EntryEffect
}

enum class EntryHeaderMode {
    TODAY,
    YESTERDAY,
    PAST
}

fun LocalDate.toEntryHeaderMode(today: LocalDate = LocalDate.now()): EntryHeaderMode = when (this) {
    today -> EntryHeaderMode.TODAY
    today.minusDays(1) -> EntryHeaderMode.YESTERDAY
    else -> EntryHeaderMode.PAST
}
