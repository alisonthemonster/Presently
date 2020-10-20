package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class OpenCsvDocumentContract : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<out String>): Intent {
        val intent = super.createIntent(context, input)
        intent.type = "text/csv|text/comma-separated-values|application/csv"
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
