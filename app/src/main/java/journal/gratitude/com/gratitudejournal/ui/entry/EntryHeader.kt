package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

@Composable
fun EntryHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Column() {
        Text(
            text = when (date) {
                LocalDate.now() -> stringResource(R.string.today)
                LocalDate.now().minusDays(1) -> stringResource(R.string.yesterday)
                else -> date.toStringWithDayOfWeek()
            },
            style = PresentlyTheme.typography.titleLarge,
            color = PresentlyTheme.colors.entryDate
        )
        Text(
            text = if (date == LocalDate.now()) {
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
