package journal.gratitude.com.gratitudejournal.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

@Composable
fun SearchResult(
    modifier: Modifier = Modifier,
    result: Entry,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    Column(
        modifier = modifier.clickable {
            //todo log analytics
            //analytics.recordEvent(CLICKED_SEARCH_ITEM)
            onEntryClicked(result.entryDate)
        },
    ) {
        Text(
            text = result.entryDate.toStringWithDayOfWeek(),
            style = PresentlyTheme.typography.bodyLarge,
            color = PresentlyTheme.colors.timelineDate
        )
        Text(
            text = result.entryContent,
            style = PresentlyTheme.typography.bodyMedium,
            color = PresentlyTheme.colors.timelineContent
        )
    }
}