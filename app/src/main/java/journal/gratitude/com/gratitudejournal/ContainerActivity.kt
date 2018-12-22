package journal.gratitude.com.gratitudejournal

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import journal.gratitude.com.gratitudejournal.ui.entry.Entry

class ContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, Entry.newInstance())
                .commitNow()
        }
    }

}
