package journal.gratitude.com.gratitudejournal.ui.milestone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R

@Composable
fun MilestoneCelebration(
    onDismiss: () -> Unit,
    onShareClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MilestoneViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState()

    MilestoneScreen(
        milestoneNumber = state.value.milestoneNumber,
        onDismiss = onDismiss,
        onShareClicked = {
            viewModel.onShareClicked()
            onShareClicked()
        }
    )
}

@Composable
fun MilestoneScreen(
    milestoneNumber: Int,
    onDismiss: () -> Unit,
    onShareClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("milestoneScreen"),
        color = PresentlyTheme.colors.timelineBackground
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onDismiss() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.close),
                    tint = PresentlyTheme.colors.timelineBackground
                )
            }
            Text(
                text = "$milestoneNumber",
                fontSize = 128.sp
            )
            Text(
                text = "$milestoneNumber " + stringResource(R.string.days_of_gratitude),
                style = PresentlyTheme.typography.titleLarge,
                fontSize = 64.sp
            )
            Text(
                text = stringResource(R.string.congrats_milestone)
            )
            Button(
                onClick = { onShareClicked() },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PresentlyTheme.colors.timelineContent,
                    contentColor = PresentlyTheme.colors.timelineBackground
                )
            ) {
                Text(stringResource(R.string.share_your_achievement))
            }
            // todo feature idea -- if user hasn't turned on backups show button to export or to set up auto backup
        }
    }
}
