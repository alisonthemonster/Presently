package journal.gratitude.com.gratitudejournal.ui.calendar

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import com.presently.ui.PresentlyTheme
import kotlinx.datetime.LocalDate

@Composable
fun Calendar(
    writtenDates: Set<LocalDate>,
    onDateSelected: (LocalDate, Boolean) -> Unit,
    onCalendarDismissed: () -> Unit,
) {
    Dialog(onDismissRequest = onCalendarDismissed) {
        Surface(
            color = PresentlyTheme.colors.timelineFab,
        ) {
            Text(
                text = "Calendar will go here",
                color = PresentlyTheme.colors.timelineOnFab
            )
        }
    }
}