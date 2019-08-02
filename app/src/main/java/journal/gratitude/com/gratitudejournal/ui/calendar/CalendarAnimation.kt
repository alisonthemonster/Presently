package journal.gratitude.com.gratitudejournal.ui.calendar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CalendarAnimation(private val fabView: FloatingActionButton, private val calendarView: EntryCalendarView) {

    fun openCalendar() {

        //move fab to center
        val deltaX = calendarView.pivotX - fabView.x - fabView.width/2
        val deltaY = calendarView.pivotY - fabView.y - fabView.height/2

        val translateX = ObjectAnimator.ofFloat(fabView, View.TRANSLATION_X, deltaX)
        val translateY = ObjectAnimator.ofFloat(fabView, View.TRANSLATION_Y, deltaY)
        val moveFabAnimation = AnimatorSet()
        moveFabAnimation.playTogether(translateX, translateY)

        //make calendar visible
        //grow calendar to full size
        val scaleUpX = ObjectAnimator.ofFloat(calendarView, View.SCALE_X, 0f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(calendarView, View.SCALE_Y, 0f, 1f)
        val growCalAnimation = AnimatorSet()
        growCalAnimation.playTogether(scaleUpX, scaleUpY)
        growCalAnimation.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                calendarView.visibility = View.VISIBLE
            }
        })

        val openAnimation = AnimatorSet()
        openAnimation.playSequentially(moveFabAnimation, growCalAnimation)
        openAnimation.start()
    }

    fun closeCalendar() {
        //shrink calendar
        //make invisible
        val scaleDownX = ObjectAnimator.ofFloat(calendarView, View.SCALE_X, 0f)
        val scaleDownY = ObjectAnimator.ofFloat(calendarView, View.SCALE_Y, 0f)
        val shrinkCalAnimation = AnimatorSet()
        shrinkCalAnimation.playTogether(scaleDownX, scaleDownY)
        shrinkCalAnimation.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                calendarView.visibility = View.INVISIBLE
            }
        })

        //move fab to bottom corner
        val translateX = ObjectAnimator.ofFloat(fabView, View.TRANSLATION_X, 0f)
        val translateY = ObjectAnimator.ofFloat(fabView, View.TRANSLATION_Y, 0f)
        val moveFabAnimation = AnimatorSet()
        moveFabAnimation.playTogether(translateX, translateY)

        val closeAnimation = AnimatorSet()
        closeAnimation.playSequentially(shrinkCalAnimation, moveFabAnimation)
        closeAnimation.start()
    }

}