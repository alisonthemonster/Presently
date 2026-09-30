package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import journal.gratitude.com.gratitudejournal.R
import androidx.preference.R as PreferenceR

/**
 * A [SeekBarPreference] that only stops at fixed increments (e.g. min=50, max=150,
 * seekBarIncrement=25 gives 5 notches: 50/75/100/125/150), instead of the stock
 * continuous drag behavior. Reuses the platform seekbar preference layout/styling.
 *
 * Optionally accepts app:stepLabels="@array/..." to show a word (e.g. "Small") next
 * to the slider instead of the raw persisted number.
 */
class SteppedSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    private val stepLabels: Array<String>? = context.obtainStyledAttributes(
        attrs, R.styleable.SteppedSeekBarPreference
    ).run {
        val labelsResId = getResourceId(R.styleable.SteppedSeekBarPreference_stepLabels, 0)
        recycle()
        if (labelsResId != 0) context.resources.getStringArray(labelsResId) else null
    }

    init {
        stepLabels?.let { labels ->
            check(labels.size == stepCount) {
                "SteppedSeekBarPreference key=\"$key\" has ${labels.size} stepLabels but " +
                    "$stepCount steps (min=$min, max=$max, seekBarIncrement=$seekBarIncrement)"
            }
        }
    }

    private val stepCount: Int
        get() = (max - min) / effectiveIncrement + 1

    private val effectiveIncrement: Int
        get() = seekBarIncrement.takeIf { it > 0 } ?: 1

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val seekBar = holder.findViewById(PreferenceR.id.seekbar) as? SeekBar ?: return
        val valueText = holder.findViewById(PreferenceR.id.seekbar_value) as? TextView

        // Normalize any off-grid persisted value (e.g. from before stepping was added)
        // so the thumb, the label, and the stored value always agree.
        val normalized = steppedValue(value)
        if (normalized != value) {
            value = normalized
        }
        seekBar.progress = normalized - min
        valueText?.text = labelFor(normalized)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val stepped = steppedValue(min + progress)
                if (stepped != min + bar.progress) {
                    bar.progress = stepped - min
                }
                valueText?.text = labelFor(stepped)
            }

            override fun onStartTrackingTouch(bar: SeekBar) {}

            override fun onStopTrackingTouch(bar: SeekBar) {
                value = steppedValue(min + bar.progress)
            }
        })
    }

    private fun steppedValue(rawValue: Int): Int {
        val stepped = min + Math.round((rawValue - min) / effectiveIncrement.toFloat()) * effectiveIncrement
        return stepped.coerceIn(min, max)
    }

    private fun labelFor(rawValue: Int): String {
        val labels = stepLabels ?: return rawValue.toString()
        val stepped = steppedValue(rawValue)
        val index = (stepped - min) / effectiveIncrement
        return labels.getOrNull(index) ?: stepped.toString()
    }
}
