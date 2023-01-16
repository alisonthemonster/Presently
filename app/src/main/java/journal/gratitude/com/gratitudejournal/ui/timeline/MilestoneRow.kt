package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.presently.ui.PresentlyColors
import com.presently.ui.PresentlyTheme

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
        val (titleString, image, timelineLine, timelineDot) = createRefs()

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
                    top.linkTo(titleString.top)
                    bottom.linkTo(titleString.bottom)
                    start.linkTo(timelineLine.start)
                    end.linkTo(timelineLine.end)
                }
                .size(15.dp)
                .clip(CircleShape)
                .background(theme.timelineLine)
        )
        Text(
            modifier = Modifier.constrainAs(titleString) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(timelineLine.end, margin = 14.dp)
                end.linkTo(parent.end, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
            text = "$milestoneNumber days of gratitude",
            style = PresentlyTheme.typography.bodyLarge,
            color = PresentlyTheme.colors.timelineDate
        )
        Image(
            modifier = modifier
                .requiredHeight(80.dp)
                .constrainAs(image) {
                    top.linkTo(titleString.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            painter = painterResource(id = theme.iconResource),
            contentDescription = null,
        )
    }
}
