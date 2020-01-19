package journal.gratitude.com.gratitudejournal.ui.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toMonthString
import journal.gratitude.com.gratitudejournal.util.getYearString
import journal.gratitude.com.gratitudejournal.util.toDate
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.android.synthetic.main.calendar_fragment.view.*
import org.threeten.bp.LocalDate
import java.util.*
import android.util.TypedValue


class EntryCalendarView : ConstraintLayout {

    private var monthString = "${Date().toMonthString()} ${Date().getYearString()}"
    private var entryCalendarListener: EntryCalendarListener? = null
    private var writtenDates = emptyList<LocalDate>()

    private lateinit var calendar: CompactCalendarView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        init()
    }

    private fun init() {
        val view = View.inflate(context, R.layout.calendar_fragment, this)

        calendar = view.compactcalendar_view
        calendar.shouldDrawIndicatorsBelowSelectedDays(true)
        view.month_year.text = monthString

        view.compactcalendar_view.setListener(object :
            CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                if (!dateClicked.after(Date())) {
                    entryCalendarListener?.onDateClicked(
                        dateClicked,
                        !writtenDates.contains(dateClicked.toLocalDate()),
                        writtenDates.size
                    )
                }
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                monthString =
                    "${firstDayOfNewMonth.toMonthString()} ${firstDayOfNewMonth.getYearString()}"
                month_year.text = monthString
            }
        })

        view.close_button.setOnClickListener {
            entryCalendarListener?.onCloseClicked()
        }

        view.calendar_screen_background.setOnClickListener {
            entryCalendarListener?.onCloseClicked()
        }
    }

    fun setDayClickedListener(entryCalendarListener: EntryCalendarListener) {
        this.entryCalendarListener = entryCalendarListener
    }

    fun setWrittenDates(dates: List<LocalDate>) {
        calendar.removeAllEvents()
        for (date in dates) {
            calendar.addEvent(Event(getBackgroundColorForTheme(), date.toDate().time))
        }
        writtenDates = dates
    }

    private fun getBackgroundColorForTheme(): Int {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(
            typedValue.data,
            intArrayOf(android.R.attr.windowBackground)
        )
        val color = a.getColor(0, 0)
        a.recycle()

        return color
    }

}

interface EntryCalendarListener {
    fun onDateClicked(date: Date, isNewDate: Boolean, numberOfEntries: Int)

    fun onCloseClicked()
}
