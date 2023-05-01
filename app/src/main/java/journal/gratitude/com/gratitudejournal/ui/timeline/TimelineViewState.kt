package journal.gratitude.com.gratitudejournal.ui.timeline

import com.presently.settings.AuthenticationState
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.threeten.bp.LocalDate

data class TimelineViewState(
    val timelineItems: ImmutableList<TimelineItem> = persistentListOf(),
    val datesWritten: Set<LocalDate> = emptySet(),
    val shouldShowDayOfWeek: Boolean = false,
    val numberOfLinesPerRow: Int = 10,
    val authenticationState: AuthenticationState = AuthenticationState.UNKNOWN
)
