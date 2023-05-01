package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class OpenCsvDocumentContract : ActivityResultContracts.GetContent() {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = super.createIntent(context, input)
        val mimeTypes = arrayOf("text/comma-separated-values", "text/csv")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        return intent
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
