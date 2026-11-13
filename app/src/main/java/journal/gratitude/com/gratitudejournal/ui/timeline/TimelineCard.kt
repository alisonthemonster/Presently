package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import journal.gratitude.com.gratitudejournal.R

object TimelineCardTags {
    const val CARD = "timeline_card"
    const val TITLE = "timeline_card_title"
    const val BODY = "timeline_card_body"
    const val ACTION = "timeline_card_action"
    const val DISMISS = "timeline_card_dismiss"
}

private class CardColors(val background: Color, val accent: Color, val text: Color)

// The inverted, high-contrast FAB surface for every tone, so the card matches whichever theme is
// active. Tone isn't shown in the color; it only decides whether the card can be dismissed.
@Composable
private fun TimelineCardTone.colors(): CardColors {
    val theme = LocalPresentlyTheme.current
    return CardColors(theme.fab, theme.fabText, theme.fabText)
}

@Composable
internal fun TimelineCard(
    state: TimelineCardRowState,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = state.tone.colors()
    val theme = LocalPresentlyTheme.current
    Box(modifier = modifier.fillMaxWidth()) {
        // The timeline line runs behind the card, so it looks continuous above and below it.
        Box(modifier = Modifier.matchParentSize().clipToBounds()) {
            Box(
                modifier = Modifier
                    .offset(x = 32.dp)
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(theme.timelineLine)
            )
        }
        CardSurface(state, colors, onActionClick, onDismiss)
    }
}

@Composable
private fun CardSurface(
    state: TimelineCardRowState,
    colors: CardColors,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 8.dp)
            .testTag(TimelineCardTags.CARD),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.background)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp, end = 16.dp)) {
            Text(
                text = stringResource(state.title.res, *state.title.args.toTypedArray()),
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag(TimelineCardTags.TITLE)
            )
            Text(
                text = stringResource(state.body.res, *state.body.args.toTypedArray()),
                color = colors.text,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .testTag(TimelineCardTags.BODY)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // No start padding, so the label lines up with the title and body.
                TextButton(
                    onClick = onActionClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.accent),
                    contentPadding = ButtonPadding,
                    modifier = Modifier.testTag(TimelineCardTags.ACTION)
                ) {
                    Text(
                        text = stringResource(state.actionLabel),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (state.isDismissible) {
                    Spacer(modifier = Modifier.width(24.dp))
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.text),
                        contentPadding = ButtonPadding,
                        modifier = Modifier.testTag(TimelineCardTags.DISMISS)
                    ) {
                        Text(text = stringResource(R.string.backup_card_dismiss))
                    }
                }
            }
        }
    }
}

private val ButtonPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
