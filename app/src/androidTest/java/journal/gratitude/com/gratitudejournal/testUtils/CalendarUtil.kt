package journal.gratitude.com.gratitudejournal.testUtils

import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*


fun clickXY(x: Int, y: Int): ViewAction {
    return GeneralClickAction(
        Tap.SINGLE,
        CoordinatesProvider { view ->
            val screenPos = IntArray(2)
            view?.getLocationOnScreen(screenPos)

            val screenX = (screenPos[0] + x).toFloat()
            val screenY = (screenPos[1] + y).toFloat()

            floatArrayOf(screenX, screenY)
        },
        Press.FINGER,
        InputDevice.SOURCE_MOUSE,
        MotionEvent.BUTTON_PRIMARY
    )
}

fun scroll(startX: Int, startY: Int, endX: Int, endY: Int): ViewAction {
    return GeneralSwipeAction(
        Swipe.FAST,
        CoordinatesProvider { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)

            val screenX = screenPos[0] + startX
            val screenY = screenPos[1] + startY

            floatArrayOf(screenX.toFloat(), screenY.toFloat())
        },
        CoordinatesProvider { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)

            val screenX = screenPos[0] + endX
            val screenY = screenPos[1] + endY

            floatArrayOf(screenX.toFloat(), screenY.toFloat())
        },
        Press.FINGER
    )
}
