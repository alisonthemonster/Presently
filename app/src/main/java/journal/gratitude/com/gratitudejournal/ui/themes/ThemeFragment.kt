package journal.gratitude.com.gratitudejournal.ui.themes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.NOTIF_TIME
import journal.gratitude.com.gratitudejournal.model.THEME
import journal.gratitude.com.gratitudejournal.model.Theme
import kotlinx.android.synthetic.main.fragment_theme.*


class ThemeFragment : Fragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var listener = object : OnThemeSelectedListener {
        override fun onThemeSelected(theme: String) {
            PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString("current_theme", theme)
                .apply()
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, theme)
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, theme)
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "theme")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            firebaseAnalytics.setUserProperty(THEME, theme)
            findNavController().navigateUp()
            activity?.recreate()
        }
    }

    private val adapter = ThemeListAdapter(listener)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        val themeList: List<Theme> = listOf(
            Theme(
                "Original",
                ContextCompat.getColor(context!!, R.color.colorAccent),
                ContextCompat.getColor(context!!, R.color.backgroundColor),
                ContextCompat.getColor(context!!, R.color.backgroundColor),
                ContextCompat.getColor(context!!, R.color.colorAccent),
                R.drawable.ic_flower
            ),
            Theme(
                "Dawn",
                ContextCompat.getColor(context!!, R.color.dawnToolbarColor),
                ContextCompat.getColor(context!!, R.color.dawnBackgroundColor),
                ContextCompat.getColor(context!!, R.color.dawnToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.dawnMainTextAndButtonColor),
                R.drawable.ic_sun_icon
            ),
            Theme(
                "Waves",
                ContextCompat.getColor(context!!, R.color.wavesToolbarColor),
                ContextCompat.getColor(context!!, R.color.wavesBackgroundColor),
                ContextCompat.getColor(context!!, R.color.wavesToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.wavesMainTextAndButtonColor),
                R.drawable.ic_wave
            ),
            Theme(
                "Tulip",
                ContextCompat.getColor(context!!, R.color.tulipToolbarColor),
                ContextCompat.getColor(context!!, R.color.tulipBackgroundColor),
                ContextCompat.getColor(context!!, R.color.tulipToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.tulipMainTextAndButtonColor),
                R.drawable.ic_tulip,
                true
            ),
            Theme(
                "Western",
                ContextCompat.getColor(context!!, R.color.westernToolbarColor),
                ContextCompat.getColor(context!!, R.color.westernBackgroundColor),
                ContextCompat.getColor(context!!, R.color.westernToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.westernMainTextAndButtonColor),
                R.drawable.ic_cactus
            ),
            Theme(
                "Beach",
                ContextCompat.getColor(context!!, R.color.beachToolbarColor),
                ContextCompat.getColor(context!!, R.color.beachBackgroundColor),
                ContextCompat.getColor(context!!, R.color.beachToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.beachMainTextAndButtonColor),
                R.drawable.ic_shell
            ),
            Theme(
                "Midnight",
                ContextCompat.getColor(context!!, R.color.midnightToolbarColor),
                ContextCompat.getColor(context!!, R.color.midnightBackgroundColor),
                ContextCompat.getColor(context!!, R.color.midnightToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.midnightMainTextAndButtonColor),
                R.drawable.ic_moon
            ),
            Theme(
                "Wesley",
                ContextCompat.getColor(context!!, R.color.wesleyToolbarColor),
                ContextCompat.getColor(context!!, R.color.wesleyBackgroundColor),
                ContextCompat.getColor(context!!, R.color.wesleyToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.wesleyMainTextAndButtonColor),
                R.drawable.ic_cube
            ),
            Theme(
                "Sunlight",
                ContextCompat.getColor(context!!, R.color.sunlightToolbarColor),
                ContextCompat.getColor(context!!, R.color.sunlightBackgroundColor),
                ContextCompat.getColor(context!!, R.color.sunlightToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.sunlightMainTextAndButtonColor),
                R.drawable.ic_sunshine,
                true
            ),
            Theme(
                "Sunset",
                ContextCompat.getColor(context!!, R.color.sunsetToolbarColor),
                ContextCompat.getColor(context!!, R.color.sunsetBackgroundColor),
                ContextCompat.getColor(context!!, R.color.sunsetToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.sunsetAndroidWidgetColor),
                R.drawable.ic_sun_icon
            ),
            Theme(
                "Field",
                ContextCompat.getColor(context!!, R.color.fieldToolbarColor),
                ContextCompat.getColor(context!!, R.color.fieldBackgroundColor),
                ContextCompat.getColor(context!!, R.color.fieldToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.fieldMainTextAndButtonColor),
                R.drawable.ic_field
            ),
            Theme(
                "Rosie",
                ContextCompat.getColor(context!!, R.color.rosieToolbarColor),
                ContextCompat.getColor(context!!, R.color.rosieBackgroundColor),
                ContextCompat.getColor(context!!, R.color.rosieToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.rosieMainTextAndButtonColor),
                R.drawable.ic_rosie,
                true
            ),
            Theme(
                "Glacier",
                ContextCompat.getColor(context!!, R.color.glacierToolbarColor),
                ContextCompat.getColor(context!!, R.color.glacierBackgroundColor),
                ContextCompat.getColor(context!!, R.color.glacierToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.glacierMainTextAndButtonColor),
                R.drawable.ic_cube
            ),
            Theme(
                "Moonlight",
                ContextCompat.getColor(context!!, R.color.moonlightToolbarColor),
                ContextCompat.getColor(context!!, R.color.moonlightBackgroundColor),
                ContextCompat.getColor(context!!, R.color.moonlightToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.moonlightMainTextAndButtonColor),
                R.drawable.ic_moon
            ),
            Theme(
                "Ivy",
                ContextCompat.getColor(context!!, R.color.ivyToolbarColor),
                ContextCompat.getColor(context!!, R.color.ivyBackgroundColor),
                ContextCompat.getColor(context!!, R.color.ivyToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.ivyMainTextAndButtonColor),
                R.drawable.ic_flower
            ),
            Theme(
                "Moss",
                ContextCompat.getColor(context!!, R.color.mossToolbarColor),
                ContextCompat.getColor(context!!, R.color.mossBackgroundColor),
                ContextCompat.getColor(context!!, R.color.mossToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.mossMainTextAndButtonColor),
                R.drawable.ic_flower
            ),
            Theme(
                "Gelato",
                ContextCompat.getColor(context!!, R.color.gelatoToolbarColor),
                ContextCompat.getColor(context!!, R.color.gelatoBackgroundColor),
                ContextCompat.getColor(context!!, R.color.gelatoToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.gelatoMainTextAndButtonColor),
                R.drawable.ic_flower
            ),
            Theme(
                "Clean",
                ContextCompat.getColor(context!!, R.color.cleanToolbarColor),
                ContextCompat.getColor(context!!, R.color.cleanBackgroundColor),
                ContextCompat.getColor(context!!, R.color.cleanToolbarItemColor),
                ContextCompat.getColor(context!!, R.color.cleanMainTextAndButtonColor),
                R.drawable.ic_flower
            )
        )

        adapter.addData(themeList)
        // Set the adapter
        themes.layoutManager = GridLayoutManager(context, 3)
        themes.adapter = adapter

        back_icon.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    interface OnThemeSelectedListener {
        fun onThemeSelected(theme: String)
    }

}
