package journal.gratitude.com.gratitudejournal

import android.content.Context
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.text.SpannableStringBuilder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.util.CustomTypefaceSpan
import kotlinx.android.synthetic.main.container_activity.*

class ContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

}
