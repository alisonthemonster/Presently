package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toFullString
import org.threeten.bp.LocalDate
import kotlin.random.Random

@Composable
fun ReadView(
    modifier: Modifier = Modifier,
    date: LocalDate,
    content: String,
    onShareClicked: (date: String, content: String) -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        EntryHeader(date)
        Text(
            text = content,
            style = PresentlyTheme.typography.bodyMedium,
            color = PresentlyTheme.colors.entryBody,
        )
        val quotes = stringArrayResource(id = R.array.inspirations)
        val randomValue: Int = remember { Random.nextInt(quotes.size) }
        Text(
            text = quotes[randomValue],
            style = PresentlyTheme.typography.bodyExtraSmall,
            color = PresentlyTheme.colors.entryQuoteText
        )
        //todo make this have long press to copy to clipboard
        //todo hide this if the user says so
        IconButton(onClick = {
            onShareClicked(
                date.toFullString(),
                content
            )
        }) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.share_your_gratitude),
                tint = PresentlyTheme.colors.entryButtonBackground
            )
        }
    }
}