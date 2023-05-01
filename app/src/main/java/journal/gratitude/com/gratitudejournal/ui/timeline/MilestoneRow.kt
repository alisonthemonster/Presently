package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.presently.ui.PresentlyColors
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R

@Composable
fun MilestoneRow(
    modifier: Modifier = Modifier,
    theme: PresentlyColors,
    milestoneNumber: Int
) {
    ConstraintLayout(
        modifier = modifier
            .requiredHeightIn(min = 100.dp)
            .fillMaxWidth()
    ) {
        val (milestoneContent, timelineLine, timelineDot) = createRefs()

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
                    top.linkTo(milestoneContent.top)
                    bottom.linkTo(milestoneContent.bottom)
                    start.linkTo(timelineLine.start)
                    end.linkTo(timelineLine.end)
                }
                .size(15.dp)
                .clip(CircleShape)
                .background(theme.timelineLine)
        )
        MilestoneContent(
            modifier = Modifier.constrainAs(milestoneContent) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(timelineLine.end, margin = 14.dp)
                end.linkTo(parent.end, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
            milestoneNumber = milestoneNumber,
            iconResource = theme.iconResource
        )
    }
}

@Composable
fun MilestoneContent(
    modifier: Modifier = Modifier,
    milestoneNumber: Int,
    iconResource: Int,
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(15.dp))
            .background(color = PresentlyTheme.colors.timelineFab)
    ) {
        Image(
            modifier = modifier
                .height(80.dp)
                .padding(8.dp),
            painter = painterResource(id = iconResource),
            contentDescription = null,
        )
        Column(
            modifier = modifier.height(80.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$milestoneNumber",
                style = PresentlyTheme.typography.bodyLarge,
                color = PresentlyTheme.colors.timelineOnFab
            )
            Text(
                text = stringResource(R.string.days_of),
                style = PresentlyTheme.typography.bodyLarge,
                color = PresentlyTheme.colors.timelineOnFab
            )
        }
    }
}