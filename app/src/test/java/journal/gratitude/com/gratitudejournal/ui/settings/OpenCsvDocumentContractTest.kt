package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OpenCsvDocumentContractTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun createIntent_usesOpenDocumentWithCsvMimeTypes() {
        val contract = OpenCsvDocumentContract()

        val intent = contract.createIntent(context, OpenCsvDocumentContract.mimeTypes)

        assertThat(intent.action).isEqualTo(Intent.ACTION_OPEN_DOCUMENT)
        assertThat(intent.categories).contains(Intent.CATEGORY_OPENABLE)
        assertThat(intent.type).isEqualTo("*/*")
        assertThat(intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)?.toList())
            .containsExactlyElementsIn(OpenCsvDocumentContract.mimeTypes.toList())
    }
}
