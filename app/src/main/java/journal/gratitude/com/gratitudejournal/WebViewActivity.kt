package journal.gratitude.com.gratitudejournal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val WEBVIEW_PATH = "WEBVIEW_PATH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webviewPath = intent.getStringExtra(WEBVIEW_PATH)

        webview.loadUrl(webviewPath)
    }
}