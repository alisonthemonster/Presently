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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Theme


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
        val view = inflater.inflate(R.layout.fragment_theme, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.layoutManager = GridLayoutManager(context, 3)
            view.adapter = adapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        val themes: List<Theme> = listOf(
                Theme("Original",
                        ContextCompat.getColor(context!!, R.color.colorAccent),
                        ContextCompat.getColor(context!!, R.color.backgroundColor),
                        ContextCompat.getColor(context!!, R.color.colorAccent),
                        R.drawable.ic_flower),
                Theme("Glacier",
                        ContextCompat.getColor(context!!, R.color.glacierToolbarColor),
                        ContextCompat.getColor(context!!, R.color.glacierBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.glacierMainTextAndButtonColor),
                        R.drawable.ic_cube),
                Theme("Midnight",
                        ContextCompat.getColor(context!!, R.color.midnightToolbarColor),
                        ContextCompat.getColor(context!!, R.color.midnightBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.midnightMainTextAndButtonColor),
                        R.drawable.ic_moon),
                Theme("Waves",
                        ContextCompat.getColor(context!!, R.color.wavesToolbarColor),
                        ContextCompat.getColor(context!!, R.color.wavesBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.wavesMainTextAndButtonColor),
                        R.drawable.ic_wave
                ),
                Theme("Sunset",
                        ContextCompat.getColor(context!!, R.color.sunsetToolbarColor),
                        ContextCompat.getColor(context!!, R.color.sunsetBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.sunsetAndroidWidgetColor),
                        R.drawable.ic_sun_icon),

                Theme("Moss",
                        ContextCompat.getColor(context!!, R.color.mossToolbarColor),
                        ContextCompat.getColor(context!!, R.color.mossBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.mossMainTextAndButtonColor),
                        R.drawable.ic_flower
                ),
                Theme("Wesley",
                        ContextCompat.getColor(context!!, R.color.wesleyToolbarColor),
                        ContextCompat.getColor(context!!, R.color.wesleyBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.wesleyMainTextAndButtonColor),
                        R.drawable.ic_cube
                ),
                Theme("Moonlight",
                        ContextCompat.getColor(context!!, R.color.moonlightToolbarColor),
                        ContextCompat.getColor(context!!, R.color.moonlightBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.moonlightMainTextAndButtonColor),
                        R.drawable.ic_moon
                ),
                Theme("Dawn",
                        ContextCompat.getColor(context!!, R.color.dawnToolbarColor),
                        ContextCompat.getColor(context!!, R.color.dawnBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.dawnMainTextAndButtonColor),
                        R.drawable.ic_sun_icon
                ),
                Theme("Ivy",
                        ContextCompat.getColor(context!!, R.color.ivyToolbarColor),
                        ContextCompat.getColor(context!!, R.color.ivyBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.ivyMainTextAndButtonColor),
                        R.drawable.ic_flower
                ),
                Theme("Clean",
                        ContextCompat.getColor(context!!, R.color.cleanToolbarColor),
                        ContextCompat.getColor(context!!, R.color.cleanBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.cleanMainTextAndButtonColor),
                        R.drawable.ic_flower
                ),
                Theme("Gelato",
                        ContextCompat.getColor(context!!, R.color.gelatoToolbarColor),
                        ContextCompat.getColor(context!!, R.color.gelatoBackgroundColor),
                        ContextCompat.getColor(context!!, R.color.gelatoMainTextAndButtonColor),
                        R.drawable.ic_flower
                )
        )
        adapter.addData(themes)
    }

    interface OnThemeSelectedListener {
        fun onThemeSelected(theme: String)
    }

}
