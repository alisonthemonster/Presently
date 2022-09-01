package com.presently.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class PresentlyColors(
    iconResource: Int,
    timelineBackground: Color,
    timelineLine: Color,
    timelineDate: Color,
    timelineContent: Color,
    timelineHint: Color,
    timelineToolbar: Color,
    timelineTitle: Color,
    timelineOnToolbar: Color,
    timelineFab: Color,
    timelineOnFab: Color,
    entryBackground: Color,
    entryDate: Color,
    entryBody: Color,
    entryHint: Color,
    entryButtonBackground: Color,
    entryButtonText: Color,
    entryQuoteText: Color,
    debugColor1: Color
) {
    var iconResource by mutableStateOf(iconResource)
        private set

    var timelineBackground by mutableStateOf(timelineBackground)
        private set

    var timelineLine by mutableStateOf(timelineLine)
        private set

    var timelineDate by mutableStateOf(timelineDate)
        private set

    var timelineContent by mutableStateOf(timelineContent)
        private set

    var timelineHint by mutableStateOf(timelineHint)
        private set

    var timelineToolbar by mutableStateOf(timelineToolbar)
        private set

    var timelineOnToolbar by mutableStateOf(timelineOnToolbar)
        private set

    var timelineTitle by mutableStateOf(timelineTitle)
        private set

    var timelineFab by mutableStateOf(timelineFab)
        private set

    var timelineOnFab by mutableStateOf(timelineOnFab)
        private set

    var entryBackground by mutableStateOf(entryBackground)
        private set

    var entryDate by mutableStateOf(entryDate)
        private set

    var entryBody by mutableStateOf(entryBody)
        private set

    var entryHint by mutableStateOf(entryHint)
        private set

    var entryButtonBackground by mutableStateOf(entryButtonBackground)
        private set

    var entryButtonText by mutableStateOf(entryButtonText)
        private set

    var entryQuoteText by mutableStateOf(entryQuoteText)
        private set

    var debugColor1 by mutableStateOf(debugColor1)
        private set

    fun update(other: PresentlyColors) {
        iconResource = other.iconResource
        timelineBackground = other.timelineBackground
        timelineLine = other.timelineLine
        timelineDate = other.timelineDate
        timelineContent = other.timelineContent
        timelineHint = other.timelineHint
        timelineToolbar = other.timelineToolbar
        timelineOnToolbar = other.timelineOnToolbar
        timelineFab = other.timelineFab
        timelineTitle = other.timelineTitle
        timelineOnFab = other.timelineOnFab
        entryBackground = other.entryBackground
        entryDate = other.entryDate
        entryBody = other.entryBody
        entryHint = other.entryHint
        entryButtonBackground = other.entryButtonBackground
        entryButtonText = other.entryButtonText
        entryQuoteText = other.entryQuoteText
        debugColor1 = other.debugColor1
    }
}

fun String.toPresentlyColors(): PresentlyColors {
    return colorSchemes[this] ?: OriginalColors
}

val OriginalColors = PresentlyColors(
    iconResource = R.drawable.ic_flower,
    timelineBackground = Color(0xffdbd1c7),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff79736a),
    timelineToolbar = Color(0xff000000),
    timelineTitle = Color(0xffdbd1c7),
    timelineOnToolbar = Color(0xffdbd1c7),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffdbd1c7),
    entryBackground = Color(0xffdbd1c7),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff79736a),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xff79736a),
    entryQuoteText = Color(0xff79736a),
    debugColor1 = Color(0xffDFA700)
)


val CalmColors = PresentlyColors(
    iconResource = R.drawable.ic_calm,
    timelineBackground = Color(0xffBBC4FB),
    timelineLine = Color(0xffFFFFFF),
    timelineDate = Color(0xffFFFFFF),
    timelineContent = Color(0xff697AD6),
    timelineHint = Color(0xffE7EBFF),
    timelineToolbar = Color(0xffFFFFFF),
    timelineTitle = Color(0xff697AD6),
    timelineOnToolbar = Color(0xffdbd1c7),
    timelineFab = Color(0xffFFFFFF),
    timelineOnFab = Color(0xffBBC4FB),
    entryBackground = Color(0xffFFFFFF),
    entryDate = Color(0xff7985DB),
    entryBody = Color(0xff7985DB),
    entryHint = Color(0xffBBC4FB),
    entryButtonBackground = Color(0xff7985DB),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xffBBC4FB),
    debugColor1 = Color(0xffDFA700)
)

val PassionColors = PresentlyColors(
    iconResource = R.drawable.ic_passion,
    timelineBackground = Color(0xffF8CDFA),
    timelineLine = Color(0xffFFFFFF),
    timelineDate = Color(0xffFFFFFF),
    timelineContent = Color(0xffE180D3),
    timelineHint = Color(0xffFFE6FF),
    timelineToolbar = Color(0xffFFFFFF),
    timelineTitle = Color(0xffF2A7A1),
    timelineOnToolbar = Color(0xffF2A7A1),
    timelineFab = Color(0xffF2A7A1),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffFFFFFF),
    entryDate = Color(0xffF2A7A1),
    entryBody = Color(0xffE180D3),
    entryHint = Color(0xffFAD8D9),
    entryButtonBackground = Color(0xffE180D3),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xffFAD8D9),
    debugColor1 = Color(0xffDFA700)
)

val JoyColors = PresentlyColors(
    iconResource = R.drawable.ic_joy,
    timelineBackground = Color(0xffB8EEEE),
    timelineLine = Color(0xff77C4CC),
    timelineDate = Color(0xffFFFFFF),
    timelineContent = Color(0xff298582),
    timelineHint = Color(0xff91CFCC),
    timelineToolbar = Color(0xffFFFFFF),
    timelineTitle = Color(0xffDFA700),
    timelineOnToolbar = Color(0xffB8EEEE),
    timelineFab = Color(0xffDFA700),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffFFFFFF),
    entryDate = Color(0xffDFA700),
    entryBody = Color(0xff298582),
    entryHint = Color(0xff91CFCC),
    entryButtonBackground = Color(0xff298582),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff91CFCC),
    debugColor1 = Color(0xff510998)
)

val BooColors = PresentlyColors(
    iconResource = R.drawable.ic_boo,
    timelineBackground = Color(0xffF9B163),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff505050),
    timelineToolbar = Color(0xff000000),
    timelineTitle = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffF9B163),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff505050),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff505050),
    debugColor1 = Color(0xff510998)
)

val colorSchemes = mapOf(
    "Original" to OriginalColors,
    "Calm" to CalmColors,
    "Passion" to PassionColors,
    "Joy" to JoyColors,
    "Boo" to BooColors
)