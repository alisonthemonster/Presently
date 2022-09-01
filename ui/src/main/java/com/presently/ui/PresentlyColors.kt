package com.presently.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class PresentlyColors(
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

val colorSchemes = mapOf(
    "Original" to OriginalColors,
    "Calm" to CalmColors
)