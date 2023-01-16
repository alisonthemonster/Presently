package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import journal.gratitude.com.gratitudejournal.R
import com.presently.ui.PresentlyColors
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

@Composable
fun TimelineRow1(
    modifier: Modifier = Modifier,
    theme: PresentlyColors,
    entryDate: LocalDate,
    entryContent: String,
    isLastEntry: Boolean = false,
    onEntryClicked: (date: LocalDate) -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
            .requiredHeightIn(min = 100.dp)
            .clickable { onEntryClicked(entryDate) }
    ) {
        val (dateString, entryString, image, timelineLine, timelineDot, timelineEndDot) = createRefs()

        val content = entryContent.ifEmpty {
            if (entryDate == LocalDate.now()) {
                stringResource(R.string.what_are_you_thankful_for)
            } else {
                stringResource(R.string.what_were_you_thankful_for)
            }
        }
        Box(
            modifier = Modifier
                .constrainAs(timelineLine) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                    height = Dimension.fillToConstraints
                }
                .width(5.dp)
                .clip(RectangleShape)
                .background(theme.timelineLine)
        )
        Box(
            modifier = Modifier
                .constrainAs(timelineDot) {
                    top.linkTo(dateString.top)
                    bottom.linkTo(dateString.bottom)
                    start.linkTo(timelineLine.start)
                    end.linkTo(timelineLine.end)
                }
                .size(15.dp)
                .clip(CircleShape)
                .background(theme.timelineLine)
        )
        Text(
            modifier = Modifier.constrainAs(dateString) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(timelineLine.end, margin = 14.dp)
                end.linkTo(parent.end, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
            text = entryDate.toStringWithDayOfWeek(),
            style = PresentlyTheme.typography.bodyLarge,
            color = PresentlyTheme.colors.timelineDate
        )
        Text(
            modifier = Modifier.constrainAs(entryString) {
                top.linkTo(dateString.bottom)
                start.linkTo(dateString.start)
                end.linkTo(parent.end, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
            text = content,
            style = PresentlyTheme.typography.bodyMedium,
            color = if (entryContent.isEmpty()) PresentlyTheme.colors.timelineHint else PresentlyTheme.colors.timelineContent,
        )
        if (isLastEntry) {
            Image(
                modifier = modifier
                    .requiredHeight(80.dp)
                    .constrainAs(image) {
                        top.linkTo(entryString.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                painter = painterResource(id = theme.iconResource),
                contentDescription = null,
            )
            Box(
                modifier = Modifier
                    .constrainAs(timelineEndDot) {
                        bottom.linkTo(timelineLine.bottom)
                        start.linkTo(timelineLine.start)
                        end.linkTo(timelineLine.end)
                    }
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(theme.timelineLine)
            )
        }
    }
}

