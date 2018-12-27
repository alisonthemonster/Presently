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
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, EntryFragment.newInstance())
                    .commitNow()
        }

        setUpBottomNavigation()
    }

    private fun setUpBottomNavigation() {
        val font = Typeface.createFromAsset(assets, "fonts/Larsseit-Medium.ttf")
        val typefaceSpan = CustomTypefaceSpan("", font)

        for (i in 0 until bottom_navigation.menu.size()) {
            val menuItem = bottom_navigation.menu.getItem(i)
            val spannableTitle = SpannableStringBuilder(menuItem.title)
            spannableTitle.setSpan(typefaceSpan, 0, spannableTitle.length, 0)
            menuItem.title = spannableTitle
        }

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.timeline_button -> {
                    supportFragmentManager!!
                            .beginTransaction()
                            .replace(R.id.content_frame, TimelineFragment.newInstance(), "Blerg")
                            .commitAllowingStateLoss()
                    true
                }
                R.id.entry_button -> {
                    supportFragmentManager!!
                            .beginTransaction()
                            .replace(R.id.content_frame, EntryFragment.newInstance(), "Blerg")
                            .commitAllowingStateLoss()
                    true
                }
                R.id.account_button -> {
                    // TODO
                    true
                }
                else -> true
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

}
