package journal.gratitude.com.gratitudejournal.util.reminders

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import journal.gratitude.com.gratitudejournal.R

class TimePreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    private var time: Int = 0

    init {
        isPersistent = true
        dialogLayoutResource = R.layout.pref_time_dialog
    }

    var hour = 0
    var minute = 0

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val value = getPersistedString(defaultValue.toString())

        hour = parseHour(value)
        minute = parseMinute(value)
    }

    fun persistStringValue(value: String) {
        persistString(value)
    }

    private fun parseHour(value: String): Int {
        val time = value.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Integer.parseInt(time[0])
    }

    private fun parseMinute(value: String): Int {
        val time = value.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Integer.parseInt(time[1])
    }
}
