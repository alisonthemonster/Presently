package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class OpenCsvDocumentContract : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        val intent = super.createIntent(context, input)
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        return intent.apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
    }

    companion object {
        val mimeTypes = arrayOf(
            "text/csv",
            "text/comma-separated-values",
            "application/csv",
            "text/plain"
        )
    }
}


class CreateCsvDocumentContract : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = super.createIntent(context, input)
        return intent.apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
        }
    }
}
