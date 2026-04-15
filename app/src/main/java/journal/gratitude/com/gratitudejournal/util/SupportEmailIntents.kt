package journal.gratitude.com.gratitudejournal.util

import android.content.Intent
import androidx.core.net.toUri

fun createSupportEmailIntent(
    recipients: Array<String>,
    subject: String,
    body: String
): Intent {
    return Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, recipients)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
}
