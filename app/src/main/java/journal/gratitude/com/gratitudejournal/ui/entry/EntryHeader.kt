package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

@Composable
fun EntryHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Column() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        Text(
            text = when (date) {
                today -> stringResource(R.string.today)
                today.minus(1, DateTimeUnit.DAY) -> stringResource(R.string.yesterday)
                else -> date.toStringWithDayOfWeek()
            },
            style = PresentlyTheme.typography.titleLarge,
            color = PresentlyTheme.colors.entryDate
        )
        Text(
            text = if (date == today) {
                stringResource(R.string.iam)
            } else {
                stringResource(
                    R.string.iwas
                )
            },
            style = PresentlyTheme.typography.titleLarge,
            color = PresentlyTheme.colors.entryDate
        )
    }
}
