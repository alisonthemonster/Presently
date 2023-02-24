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
    timelineLogo: Color,
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

    var timelineLogo by mutableStateOf(timelineLogo)
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
        timelineLogo = other.timelineLogo
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
    timelineLogo = Color(0xffdbd1c7),
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
    timelineLogo = Color(0xff697AD6),
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
    timelineLogo = Color(0xffF2A7A1),
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
    timelineLogo = Color(0xffDFA700),
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
    timelineLogo = Color(0xffFFFFFF),
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

val BettyColors = PresentlyColors(
    iconResource = R.drawable.ic_betty,
    timelineBackground = Color(0xffFCDAD1),
    timelineLine = Color(0xffF3A799),
    timelineDate = Color(0xff4B3534),
    timelineContent = Color(0xffA04A32),
    timelineHint = Color(0xffCF988E),
    timelineToolbar = Color(0xffD67770),
    timelineLogo = Color(0xffFCDAD1),
    timelineOnToolbar = Color(0xffFCDAD1),
    timelineFab = Color(0xffD67770),
    timelineOnFab = Color(0xffFCDAD1),
    entryBackground = Color(0xffFCDAD1),
    entryDate = Color(0xffA04A32),
    entryBody = Color(0xffA04A32),
    entryHint = Color(0xffCF988E),
    entryButtonBackground = Color(0xffD67770),
    entryButtonText = Color(0xffFCDAD1),
    entryQuoteText = Color(0xffCF988E),
    debugColor1 = Color(0xff510998)
)

val AutumnColors = PresentlyColors(
    iconResource = R.drawable.ic_autumn_leaves,
    timelineBackground = Color(0xffF4E8DB),
    timelineLine = Color(0xff4B3534),
    timelineDate = Color(0xff4B3534),
    timelineContent = Color(0xff4B3534),
    timelineHint = Color(0xff938C8C),
    timelineToolbar = Color(0xffA98043),
    timelineLogo = Color(0xff000000),
    timelineOnToolbar = Color(0xff000000),
    timelineFab = Color(0xffA98043),
    timelineOnFab = Color(0xff000000),
    entryBackground = Color(0xffF4E8DB),
    entryDate = Color(0xff4B3534),
    entryBody = Color(0xff4B3534),
    entryHint = Color(0xff938C8C),
    entryButtonBackground = Color(0xffA98043),
    entryButtonText = Color(0xff000000),
    entryQuoteText = Color(0xff938C8C),
    debugColor1 = Color(0xff510998)
)

val BraylaColors = PresentlyColors(
    iconResource = R.drawable.ic_brayla,
    timelineBackground = Color(0xff000000),
    timelineLine = Color(0xffFFFFFF),
    timelineDate = Color(0xffFFFFFF),
    timelineContent = Color(0xffFFFFFF),
    timelineHint = Color(0xffA6A6A6),
    timelineToolbar = Color(0xffFFFFFF),
    timelineLogo = Color(0xff000000),
    timelineOnToolbar = Color(0xff000000),
    timelineFab = Color(0xffFFFFFF),
    timelineOnFab = Color(0xff000000),
    entryBackground = Color(0xff000000),
    entryDate = Color(0xffFFFFFF),
    entryBody = Color(0xffFFFFFF),
    entryHint = Color(0xffA6A6A6),
    entryButtonBackground = Color(0xffFFFFFF),
    entryButtonText = Color(0xff000000),
    entryQuoteText = Color(0xffA6A6A6),
    debugColor1 = Color(0xff510998)
)

val MarshaColors = PresentlyColors(
    iconResource = R.drawable.ic_trans_hearts,
    timelineBackground = Color(0xffFFFAFA),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff4E4E4E),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffFFFAFA),
    timelineOnToolbar = Color(0xffFFFAFA),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFFAFA),
    entryBackground = Color(0xffFFFAFA),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff4E4E4E),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffFFFAFA),
    entryQuoteText = Color(0xff4E4E4E),
    debugColor1 = Color(0xff510998)
)

val RemmieColors = PresentlyColors(
    iconResource = R.drawable.ic_rainbow,
    timelineBackground = Color(0xffC6E3F1),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff4E4E4E),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffFFFAFA),
    timelineOnToolbar = Color(0xffFFFAFA),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFFAFA),
    entryBackground = Color(0xffC6E3F1),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff4E4E4E),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xff4E4E4E),
    entryQuoteText = Color(0xff4E4E4E),
    debugColor1 = Color(0xff510998)
)

val AhalyaColors = PresentlyColors(
    iconResource = R.drawable.ic_butterfly,
    timelineBackground = Color(0xffEFCCF4),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff706767),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffEFCCF4),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff706767),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff706767),
    debugColor1 = Color(0xff510998)
)

val DanahColors = PresentlyColors(
    iconResource = R.drawable.ic_danah,
    timelineBackground = Color(0xffFFEBE7),
    timelineLine = Color(0xffFFB1AB),
    timelineDate = Color(0xff342E2E),
    timelineContent = Color(0xff342E2E),
    timelineHint = Color(0xff706767),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffFFEBE7),
    timelineOnToolbar = Color(0xffFFEBE7),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFEBE7),
    entryBackground = Color(0xffFFEBE7),
    entryDate = Color(0xff342E2E),
    entryBody = Color(0xff342E2E),
    entryHint = Color(0xff706767),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffFFEBE7),
    entryQuoteText = Color(0xff706767),
    debugColor1 = Color(0xff510998)
)

val EllenColors = PresentlyColors(
    iconResource = R.drawable.ic_ellen,
    timelineBackground = Color(0xffDFE0B4),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff706767),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffDFE0B4),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff706767),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff706767),
    debugColor1 = Color(0xff510998)
)

val JulieColors = PresentlyColors(
    iconResource = R.drawable.ic_julie,
    timelineBackground = Color(0xffEEE7D4),
    timelineLine = Color(0xff161616),
    timelineDate = Color(0xff161616),
    timelineContent = Color(0xff161616),
    timelineHint = Color(0xff706767),
    timelineToolbar = Color(0xff161616),
    timelineLogo = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffEEE7D4),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff706767),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff706767),
    debugColor1 = Color(0xff510998)
)

val JungleColors = PresentlyColors(
    iconResource = R.drawable.ic_tiger,
    timelineBackground = Color(0xff316346),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xffEAC9AA),
    timelineContent = Color(0xffEAC9AA),
    timelineHint = Color(0xffAC977A),
    timelineToolbar = Color(0xff161616),
    timelineLogo = Color(0xffEAC9AA),
    timelineOnToolbar = Color(0xffEAC9AA),
    timelineFab = Color(0xff161616),
    timelineOnFab = Color(0xffEAC9AA),
    entryBackground = Color(0xff316346),
    entryDate = Color(0xffEAC9AA),
    entryBody = Color(0xffEAC9AA),
    entryHint = Color(0xffAC977A),
    entryButtonBackground = Color(0xff161616),
    entryButtonText = Color(0xffEAC9AA),
    entryQuoteText = Color(0xffAC977A),
    debugColor1 = Color(0xff510998)
)

val Colors = PresentlyColors(
    iconResource = R.drawable.ic_betty,
    timelineBackground = Color(0xff000000),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff000000),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xff000000),
    timelineOnToolbar = Color(0xff000000),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xff000000),
    entryBackground = Color(0xff000000),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff000000),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xff000000),
    entryQuoteText = Color(0xff000000),
    debugColor1 = Color(0xff510998)
)

val colorSchemes = mapOf(
    "Original" to OriginalColors,
    "Calm" to CalmColors,
    "Passion" to PassionColors,
    "Joy" to JoyColors,
    "Boo" to BooColors,
    "Betty" to BettyColors,
    "Autumn" to AutumnColors,
    "Brayla" to BraylaColors,
    "Marsha" to MarshaColors,
    "Remmie" to RemmieColors,
    "Ahalya" to AhalyaColors,
    "Danah" to DanahColors,
    "Ellen" to EllenColors,
    "Julie" to JulieColors,
    "Jungle" to JungleColors,
)