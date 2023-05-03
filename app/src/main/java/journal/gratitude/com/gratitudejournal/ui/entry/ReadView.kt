package journal.gratitude.com.gratitudejournal.ui.entry

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import kotlinx.datetime.LocalDate
import kotlin.random.Random

@Composable
fun ReadView(
    date: LocalDate,
    content: String,
    shouldShowQuote: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("readMode")
    ) {
        EntryHeader(date)
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = content,
            style = PresentlyTheme.typography.bodyMedium,
            color = PresentlyTheme.colors.entryBody
        )
        if (shouldShowQuote) {
            val quotes = stringArrayResource(id = R.array.inspirations)
            val randomValue: Int = remember { Random.nextInt(quotes.size) }
            val quote = quotes[randomValue]
            Text(
                text = quote,
                style = PresentlyTheme.typography.bodyExtraSmall,
                color = PresentlyTheme.colors.entryQuoteText,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString(quote))
                    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
