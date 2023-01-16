package journal.gratitude.com.gratitudejournal.ui.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//todo style this dialog

@Composable
fun MilestoneDialog(
    modifier: Modifier = Modifier,
    milestoneNumber: Int,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = "Omg congrats")
        },
        text = {
            Text("You have $milestoneNumber entries bro!")
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("This is the Confirm Button")
            }
        },
    )
}