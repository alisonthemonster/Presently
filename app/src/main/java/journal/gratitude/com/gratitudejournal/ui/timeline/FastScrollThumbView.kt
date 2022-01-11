package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toLocalDate

class FastScrollThumbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.indicatorFastScrollerThumbStyle
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr
), ItemSelectedCallback {

    private val isSetup: Boolean get() = (fastScrollTrack != null)
    private var fastScrollTrack: FastScrollTrack? = null

    private val thumbView: ViewGroup
    private val textView: TextView

    private val thumbAnimation: SpringAnimation

    init {
        LayoutInflater.from(context).inflate(R.layout.view_scroller_thumb, this, true)
        thumbView = findViewById(R.id.thumb_container)
        thumbView.visibility = View.GONE //only show when item has been selected
        textView = thumbView.findViewById(R.id.date)

        thumbAnimation = SpringAnimation(thumbView, DynamicAnimation.TRANSLATION_Y).apply {
            spring = SpringForce().apply {
                dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            }
        }

    }

    override fun onItemSelected(item: String, indicatorCenterY: Int) {
        val thumbTargetY = indicatorCenterY.toFloat() - (thumbView.measuredHeight / 2)
        thumbAnimation.animateToFinalPosition(thumbTargetY)

        val date = item.toLocalDate()
        val monthYear = date.toMonthYearString()
        textView.text = monthYear
    }

    fun setupWithFastScroller(fastScrollTrack: FastScrollTrack) {
        check(!isSetup) { "Only set this view's FastScrollerView once!" }
        this.fastScrollTrack = fastScrollTrack

        fastScrollTrack.itemSelectedCallback = this
        fastScrollTrack.onItemIndicatorTouched = { isTouched ->
            if (isTouched) {
                thumbView.visibility = View.VISIBLE
            } else {
                thumbView.visibility = View.GONE
            }
            isActivated = isTouched
        }
    }
}