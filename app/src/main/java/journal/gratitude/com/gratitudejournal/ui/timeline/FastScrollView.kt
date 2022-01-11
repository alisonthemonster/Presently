package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDate
import kotlin.math.min

//convert this into a list of dates that scales to fit the space somehow?
    //if less than x items dont show scrubber (return early)
    //no ui except maybe the years?
    //calculate the pixels available and use math to figure out where each header would be??


//every day written gets a point in the total area
    //for each item
        //place at += totalAvailableSize/totalNumEntries
        //scrolls to the exact item but the thumb says the month and year

//todo integrate into coordinator layout somehow to also hide fab and to hide/show scrubber when scrolling?

//todo how to have selected item change when user manually scrolls through?

class FastScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.indicatorFastScrollerStyle,
    defStyleRes: Int = R.style.Widget_IndicatorFastScroll_FastScroller
) : LinearLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {

    private val adapterDataObserver: RecyclerView.AdapterDataObserver = createAdapterDataObserver()

    private var isUpdateItemIndicatorsPosted = false

    //these all get provided by the client
    private var recyclerView: RecyclerView? = null
    private var adapter: TimelineAdapter? = null
        set(value) {
            field?.unregisterAdapterDataObserver(adapterDataObserver)
            field = value
            value?.let { newAdapter ->
                newAdapter.registerAdapterDataObserver(adapterDataObserver)
                postUpdateItemIndicators()
            }
        }

    //these are used to connect with a thumb view
    var itemSelectedCallback: ItemSelectedCallback? = null
    var onItemIndicatorTouched: ((Boolean) -> Unit)? = null

    //the actual items that will be in the scrubber view
        //string is what the item will show and the int is the position in the timeline list
    private val scrubberItemsData: MutableList<Pair<String, Int>> = ArrayList()
    private val scrubberItems: List<String>
        get() = scrubberItemsData.map { it.first }


    private val isSetup: Boolean get() = (recyclerView != null)
    private var lastSelectedPosition: Int? = null

    private val pressedTextColor: Int = ContextCompat.getColor(context, R.color.joyFabColor)
    private val textColor: Int = ContextCompat.getColor(context, R.color.joyTimelineBodyColor)

    init {
        isFocusableInTouchMode = true
        isClickable = true
        orientation = VERTICAL
        gravity = Gravity.CENTER
    }

    //sets up the tracking of the adapter
    fun setRecyclerView(
        recyclerView: RecyclerView
    ) {
        check(!isSetup) { "Only set this view's RecyclerView once!" }

        //set the recyclerview
        this.recyclerView = recyclerView

        //set the adapter
        this.adapter = (recyclerView.adapter as TimelineAdapter).also {
            updateItemIndicators()
        }

        //todo what is this used for?
        recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            // RecyclerView#setAdapter calls requestLayout, so this can detect adapter changes
            if (recyclerView.adapter !== adapter) {
                adapter = recyclerView.adapter as TimelineAdapter
            }
        }
    }

    //posts an update
    private fun postUpdateItemIndicators() {
        if (!isUpdateItemIndicatorsPosted) {
            isUpdateItemIndicatorsPosted = true
            post {
                if (recyclerView!!.run { isAttachedToWindow && adapter != null }) {
                    updateItemIndicators()
                }
                isUpdateItemIndicatorsPosted = false
            }
        }
    }

    //updates the items
    private fun updateItemIndicators() {
        //clear current items
        scrubberItemsData.clear()

        //recreate the scrubber items list and put it into scrubberItemsData
        createScrubberItemList().toCollection(scrubberItemsData)

        //bind views
        bindItemViews()
    }


    //takes the items in the adapter and creates a list of entries with their actual posiiton in the list
        //this is necessary because we have milestones we need to not count in the scrubber scaling calculations
    private fun createScrubberItemList(): List<Pair<String, Int>> {
        val adapter = recyclerView!!.adapter as TimelineAdapter
        //removes all the milestones
        return (0 until adapter.itemCount).mapNotNull { position ->
            val item = adapter.getItemForPosition(position)
            val entry = if (item is Entry) item else null
            entry?.let { entry.entryDate.toString() to position }
        }
    }

    //iterates through the items and creates the views to go in the linear layout
    private fun bindItemViews() {
        //reset the linear layout
        removeAllViews()

        if (scrubberItemsData.isEmpty()) return

        val views = ArrayList<View>() //todo since we aren't combining icons with text maybe we dont need a list of views?

        //the reddit version has both icons and text and it batches all the text items next to each other in a row, binds the icon, and then continues
        //since this is only working with text we can just put all the text into one textview
        views.add(createTextViewFromList(scrubberItems))

        //add each view to the linearlayout
        views.forEach(::addView)
    }

    //combines a list of strings to make one big text view
        //the tag will allow the touch listener to figure out which item its touching
    private fun createTextViewFromList(scrubberItems: List<String>): View {
        val textView = LayoutInflater.from(context).inflate(R.layout.scrubber_track, this, false)

        //todo we need to create a view here that uses the same math as in onTouchEvent to put

        return textView.apply {
            tag = scrubberItems //using the tag so that we can later look through the items in this textview
        }
    }

    //handles the scrolling when this view is touched
        //calculates which date in the view the user is touching
    override fun onTouchEvent(event: MotionEvent): Boolean {

        //if user lifts their finger off the view
        if (event.actionMasked in intArrayOf(MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL)) {
            isPressed = false
            clearSelectedItem()
            onItemIndicatorTouched?.invoke(false)
            return false
        }

        var consumed = false
        val touchY = event.y.toInt()
        // todo this is weird because we have a linearlayout but we only ever put the textview in it lols
        children.forEach { view ->
            //find the view in the children that has the touch happening to it
            if (view.containsY(touchY)) {
                //TODO this logic is close!!
                @Suppress("UNCHECKED_CAST")
                val dates = view.tag as List<String> //all of the dates in the timeline

                val textIndicatorsTouchY = touchY - view.top
                val textLineHeight = view.height / dates.size //the height of each date
                val touchedIndicatorIndex = min(
                    textIndicatorsTouchY / textLineHeight,
                    dates.lastIndex
                )
                val touchedDate = dates[touchedIndicatorIndex]

                val centerY = view.y.toInt() + (textLineHeight / 2) + (touchedIndicatorIndex * textLineHeight)
                selectItemIndicator(touchedDate, centerY, view, textLine = touchedIndicatorIndex)

                consumed = true
            }
        }
        isPressed = consumed
        onItemIndicatorTouched?.invoke(consumed) //so the companion view can be shown
        return consumed
    }

        // scrolls the list!
        // makes haptic feedback
        // highlights the currently touched item visually
    private fun selectItemIndicator(touchedItem: String, centerY: Int, view: View, textLine: Int) {
        //look up in our list the touched date to find its position in the entire timeline
        val positionOfTouchedItem = scrubberItemsData.first { it.first == touchedItem }.let { it.second }
        if (positionOfTouchedItem != lastSelectedPosition) {
            //we're on a new item so clear the old one and update
            clearSelectedItem()
            lastSelectedPosition = positionOfTouchedItem
            scrollToPosition(positionOfTouchedItem)
        }

        performHapticFeedback(
            // Semantically, dragging across the indicators is similar to moving a text handle
            if (Build.VERSION.SDK_INT >= 27) {
                HapticFeedbackConstants.TEXT_HANDLE_MOVE
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
        )

        //todo move a lil mini scrubber here somehow

        // callback
       itemSelectedCallback?.onItemSelected(touchedItem, centerY)
    }

    // resets so there is no currently selected item in the view
        // clears the styling for every item
    private fun clearSelectedItem() {
        lastSelectedPosition = null
        children.filterIsInstance<TextView>().forEach {
            TextColorUtil.clearHighlight(it)
        }
    }

    //todo how to make it scroll so its at the top of the window
    private fun scrollToPosition(position: Int) {
        recyclerView!!.apply {
            stopScroll()
            scrollToPosition(position)
        }
    }


    //listens to changes in the adapter and triggers the items indicators to be updated
    private fun createAdapterDataObserver(): RecyclerView.AdapterDataObserver {
        return object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                postUpdateItemIndicators()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
                onChanged()

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
                onChanged()

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
                onChanged()

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) =
                onChanged()
        }
    }


}

fun View.containsY(y: Int) = y in (top until bottom)

fun View.throwIfMissingAttrs(@StyleRes styleRes: Int, block: () -> Unit) {
    try {
        block()
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(
            "This ${this::class.java.simpleName} is missing an attribute. " +
                    "Add it to its style, or make the style inherit from " +
                    "${resources.getResourceName(styleRes)}.",
            e
        )
    }
}


interface ItemSelectedCallback {
    fun onItemSelected(
        item: String,
        indicatorCenterY: Int
    )
}