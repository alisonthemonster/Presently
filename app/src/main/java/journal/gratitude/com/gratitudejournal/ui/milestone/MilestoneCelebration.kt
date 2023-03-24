package journal.gratitude.com.gratitudejournal.ui.milestone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme

@Composable
fun MilestoneCelebration(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val viewModel = hiltViewModel<MilestoneViewModel>()
    val state = viewModel.state.collectAsState()

    PresentlyTheme(
        selectedTheme = viewModel.getSelectedTheme()
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = PresentlyTheme.colors.entryBackground
        ) {
            Column() {
                Text(text = "Omg congrats")
                Text("You have ${state.value.milestoneNumber} entries bro!")
                Button(onClick = { onDismiss() }) {
                    Text("This is the Confirm Button")
                }
            }
        }
    }
}