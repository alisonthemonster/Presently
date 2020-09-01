package journal.gratitude.com.gratitudejournal.util.reminders

import android.content.Context
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.model.NOTIF_TIME
import org.threeten.bp.LocalTime

class TimePreferenceFragment: PreferenceDialogFragmentCompat() {

    private var timePicker: TimePicker? = null

    override fun onCreateDialogView(context: Context): View {
        timePicker = TimePicker(context)
        return timePicker as TimePicker
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        timePicker?.setIs24HourView(false)
        val pref = preference as TimePreference
        timePicker?.hour = pref.hour
        timePicker?.minute = pref.minute
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val pref = preference as TimePreference
            pref.hour = timePicker?.hour!!
            pref.minute = timePicker?.minute!!

            val time = LocalTime.of(pref.hour, pref.minute)

            val firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
            firebaseAnalytics.setUserProperty(NOTIF_TIME, time.toString())

            if (pref.callChangeListener(time)) {
                pref.persistStringValue(time.toString())
                NotificationScheduler().setNotificationTime(requireContext(), time)
            }
        }
    }

}