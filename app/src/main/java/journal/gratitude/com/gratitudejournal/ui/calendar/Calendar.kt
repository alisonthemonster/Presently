package journal.gratitude.com.gratitudejournal.ui.calendar

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.presently.ui.PresentlyTheme
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class) // DatePicker
@Composable
fun Calendar(
    writtenDates: ImmutableSet<LocalDate>,
    onDateSelected: (LocalDate, Boolean) -> Unit,
    onCalendarDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState()

    Dialog(onDismissRequest = onCalendarDismissed) {
        Surface(
            color = PresentlyTheme.colors.timelineFab
        ) {
            DatePicker(
                state = datePickerState,
                dateValidator = { utcDateInMills ->
                    val date = Instant.fromEpochMilliseconds(utcDateInMills).toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                    date > today
                }
            )
        }
    }
}
