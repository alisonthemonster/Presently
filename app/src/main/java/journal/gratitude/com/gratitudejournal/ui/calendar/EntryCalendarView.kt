package journal.gratitude.com.gratitudejournal.ui.calendar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.getMonthString
import journal.gratitude.com.gratitudejournal.util.getYearString
import journal.gratitude.com.gratitudejournal.util.toDate
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.android.synthetic.main.calendar_fragment.view.*
import org.threeten.bp.LocalDate
import java.util.*

class EntryCalendarView : ConstraintLayout {

    private var monthString = "${Date().getMonthString()} ${Date().getYearString()}"
    private var dayClickedListener: DayClickedListener? = null
    private var writtenDates = mutableListOf<LocalDate>()

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
        view.month_year.text = monthString

        view.compactcalendar_view.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                dayClickedListener?.onDateClicked(dateClicked, !writtenDates.contains(dateClicked.toLocalDate()), writtenDates.size)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                monthString = "${firstDayOfNewMonth.getMonthString()} ${firstDayOfNewMonth.getYearString()}"
                month_year.text = monthString
            }
        })
    }

    fun setDayClickedListener(dayClickedListener: DayClickedListener) {
        this.dayClickedListener = dayClickedListener
    }

    fun setWrittenDates(dates: List<LocalDate>) {
       for (date in dates) {
           calendar.addEvent(Event(Color.WHITE, date.toDate().time)) //TODO this event date might be wrong
       }
    }

    //click close
    //click date


    fun setDate(date: Date) {
        monthString = "${date.getMonthString()} ${date.getYearString()}"
        compactcalendar_view.setCurrentDate(date)
    }

}

interface DayClickedListener {
    fun onDateClicked(date: Date, isNewDate: Boolean, numberOfEntries: Int)
}

interface CalendarClosedListener {
    fun onCalendarClosed(date: Date)
}
