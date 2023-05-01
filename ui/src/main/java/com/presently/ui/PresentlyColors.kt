package com.presently.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

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

fun Color.isDark(): Boolean {
    return this.luminance() < 0.5
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
    entryButtonText = Color(0xffdbd1c7),
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

val BrittanyColors = PresentlyColors(
    iconResource = R.drawable.ic_brittany,
    timelineBackground = Color(0xffC1DCE4),
    timelineLine = Color(0xff161616),
    timelineDate = Color(0xff161616),
    timelineContent = Color(0xff161616),
    timelineHint = Color(0xff706767),
    timelineToolbar = Color(0xff161616),
    timelineLogo = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff161616),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffC1DCE4),
    entryDate = Color(0xff161616),
    entryBody = Color(0xff161616),
    entryHint = Color(0xff706767),
    entryButtonBackground = Color(0xff161616),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff706767),
    debugColor1 = Color(0xff510998)
)

val KatieColors = PresentlyColors(
    iconResource = R.drawable.ic_katie,
    timelineBackground = Color(0xffFEE9E6),
    timelineLine = Color(0xff3B1011),
    timelineDate = Color(0xff241E1E),
    timelineContent = Color(0xff241E1E),
    timelineHint = Color(0xff706767),
    timelineToolbar = Color(0xffFEC2B8),
    timelineLogo = Color(0xff3B1011),
    timelineOnToolbar = Color(0xff3B1011),
    timelineFab = Color(0xffFEC2B8),
    timelineOnFab = Color(0xff3B1011),
    entryBackground = Color(0xffFEE9E6),
    entryDate = Color(0xff241E1E),
    entryBody = Color(0xff241E1E),
    entryHint = Color(0xff706767),
    entryButtonBackground = Color(0xffFEC2B8),
    entryButtonText = Color(0xff3B1011),
    entryQuoteText = Color(0xff706767),
    debugColor1 = Color(0xff510998)
)

val LotusColors = PresentlyColors(
    iconResource = R.drawable.ic_lotus,
    timelineBackground = Color(0xffF2C1C1),
    timelineLine = Color(0xff4D1616),
    timelineDate = Color(0xff4D1616),
    timelineContent = Color(0xff4D1616),
    timelineHint = Color(0xffA57575),
    timelineToolbar = Color(0xff4D1616),
    timelineLogo = Color(0xffF2C1C1),
    timelineOnToolbar = Color(0xffF2C1C1),
    timelineFab = Color(0xff4D1616),
    timelineOnFab = Color(0xffF2C1C1),
    entryBackground = Color(0xffF2C1C1),
    entryDate = Color(0xff4D1616),
    entryBody = Color(0xff4D1616),
    entryHint = Color(0xffA57575),
    entryButtonBackground = Color(0xff4D1616),
    entryButtonText = Color(0xffF2C1C1),
    entryQuoteText = Color(0xffA57575),
    debugColor1 = Color(0xff510998)
)

val MonsteraColors = PresentlyColors(
    iconResource = R.drawable.ic_monstera,
    timelineBackground = Color(0xffFFFFFF),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff919191),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xffFFFFFF),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff919191),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff919191),
    debugColor1 = Color(0xff510998)
)

val CloudColors = PresentlyColors(
    iconResource = R.drawable.clouds,
    timelineBackground = Color(0xff3EBCE4),
    timelineLine = Color(0xffFFFFFF),
    timelineDate = Color(0xffFFFFFF),
    timelineContent = Color(0xffFFFFFF),
    timelineHint = Color(0xffD5D5D5),
    timelineToolbar = Color(0xffFFFFFF),
    timelineLogo = Color(0xff000000),
    timelineOnToolbar = Color(0xff000000),
    timelineFab = Color(0xffFFFFFF),
    timelineOnFab = Color(0xff000000),
    entryBackground = Color(0xff3EBCE4),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xffD5D5D5),
    entryButtonBackground = Color(0xffFFFFFF),
    entryButtonText = Color(0xff000000),
    entryQuoteText = Color(0xffD5D5D5),
    debugColor1 = Color(0xff510998)
)

val MatisseColors = PresentlyColors(
    iconResource = R.drawable.ic_matisse,
    timelineBackground = Color(0xff7D727B),
    timelineLine = Color(0xff332626),
    timelineDate = Color(0xff332626),
    timelineContent = Color(0xff332626),
    timelineHint = Color(0xffAAA09C),
    timelineToolbar = Color(0xff332626),
    timelineLogo = Color(0xffB8AFB6),
    timelineOnToolbar = Color(0xffB8AFB6),
    timelineFab = Color(0xff332626),
    timelineOnFab = Color(0xffB8AFB6),
    entryBackground = Color(0xff7D727B),
    entryDate = Color(0xff332626),
    entryBody = Color(0xff332626),
    entryHint = Color(0xffAAA09C),
    entryButtonBackground = Color(0xff332626),
    entryButtonText = Color(0xffB8AFB6),
    entryQuoteText = Color(0xffAAA09C),
    debugColor1 = Color(0xff510998)
)

val DaisyColors = PresentlyColors(
    iconResource = R.drawable.daisies,
    timelineBackground = Color(0xffABCFB5),
    timelineLine = Color(0xff373E39),
    timelineDate = Color(0xff373E39),
    timelineContent = Color(0xff373E39),
    timelineHint = Color(0xff7E837F),
    timelineToolbar = Color(0xff373E39),
    timelineLogo = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff373E39),
    timelineOnFab = Color(0xff000000),
    entryBackground = Color(0xffABCFB5),
    entryDate = Color(0xff373E39),
    entryBody = Color(0xff373E39),
    entryHint = Color(0xff7E837F),
    entryButtonBackground = Color(0xff373E39),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff7E837F),
    debugColor1 = Color(0xff510998)
)

val RosieColors = PresentlyColors(
    iconResource = R.drawable.ic_rosie,
    timelineBackground = Color(0xffF1DDD4),
    timelineLine = Color(0xffD26635),
    timelineDate = Color(0xff797979),
    timelineContent = Color(0xff797979),
    timelineHint = Color(0xffAAA09C),
    timelineToolbar = Color(0xffD26635),
    timelineLogo = Color(0xffF1DDD4),
    timelineOnToolbar = Color(0xffF1DDD4),
    timelineFab = Color(0xffD26635),
    timelineOnFab = Color(0xffF1DDD4),
    entryBackground = Color(0xffF1DDD4),
    entryDate = Color(0xff797979),
    entryBody = Color(0xff797979),
    entryHint = Color(0xffAAA09C),
    entryButtonBackground = Color(0xffD26635),
    entryButtonText = Color(0xffF1DDD4),
    entryQuoteText = Color(0xffAAA09C),
    debugColor1 = Color(0xff510998)
)

val TulipColors = PresentlyColors(
    iconResource = R.drawable.ic_tulip,
    timelineBackground = Color(0xffE6C5B6),
    timelineLine = Color(0xff4E603A),
    timelineDate = Color(0xff474747),
    timelineContent = Color(0xff474747),
    timelineHint = Color(0xff837E7C),
    timelineToolbar = Color(0xff4E603A),
    timelineLogo = Color(0xffE6D4CC),
    timelineOnToolbar = Color(0xffE6D4CC),
    timelineFab = Color(0xff4E603A),
    timelineOnFab = Color(0xffE6D4CC),
    entryBackground = Color(0xffE6C5B6),
    entryDate = Color(0xff474747),
    entryBody = Color(0xff474747),
    entryHint = Color(0xff837E7C),
    entryButtonBackground = Color(0xff4E603A),
    entryButtonText = Color(0xffE6D4CC),
    entryQuoteText = Color(0xff837E7C),
    debugColor1 = Color(0xff510998)
)

val SunlightColors = PresentlyColors(
    iconResource = R.drawable.ic_sunshine,
    timelineBackground = Color(0xffFCF8D4),
    timelineLine = Color(0xffF1AF1B),
    timelineDate = Color(0xff797979),
    timelineContent = Color(0xff797979),
    timelineHint = Color(0xffAAA09C),
    timelineToolbar = Color(0xffF1AF1B),
    timelineLogo = Color(0xffFCF8D4),
    timelineOnToolbar = Color(0xffFCF8D4),
    timelineFab = Color(0xffF1AF1B),
    timelineOnFab = Color(0xffFCF8D4),
    entryBackground = Color(0xffFCF8D4),
    entryDate = Color(0xff797979),
    entryBody = Color(0xff797979),
    entryHint = Color(0xffAAA09C),
    entryButtonBackground = Color(0xffF1AF1B),
    entryButtonText = Color(0xffFCF8D4),
    entryQuoteText = Color(0xffAAA09C),
    debugColor1 = Color(0xff510998)
)

val WesternColors = PresentlyColors(
    iconResource = R.drawable.ic_cactus,
    timelineBackground = Color(0xff282828),
    timelineLine = Color(0xffB1977E),
    timelineDate = Color(0xffB1977E),
    timelineContent = Color(0xffB1977E),
    timelineHint = Color(0xff6C5B4B),
    timelineToolbar = Color(0xffB1977E),
    timelineLogo = Color(0xff282828),
    timelineOnToolbar = Color(0xff282828),
    timelineFab = Color(0xffB1977E),
    timelineOnFab = Color(0xff282828),
    entryBackground = Color(0xff282828),
    entryDate = Color(0xffB1977E),
    entryBody = Color(0xffB1977E),
    entryHint = Color(0xff6C5B4B),
    entryButtonBackground = Color(0xffB1977E),
    entryButtonText = Color(0xff282828),
    entryQuoteText = Color(0xff6C5B4B),
    debugColor1 = Color(0xff510998)
)

val FieldColors = PresentlyColors(
    iconResource = R.drawable.ic_field,
    timelineBackground = Color(0xffF0E9D7),
    timelineLine = Color(0xffE1D0B6),
    timelineDate = Color(0xff786C5A),
    timelineContent = Color(0xff786C5A),
    timelineHint = Color(0xffAAA09C),
    timelineToolbar = Color(0xffE1D0B6),
    timelineLogo = Color(0xffF0E9D7),
    timelineOnToolbar = Color(0xffF0E9D7),
    timelineFab = Color(0xffE1D0B6),
    timelineOnFab = Color(0xffF0E9D7),
    entryBackground = Color(0xffF0E9D7),
    entryDate = Color(0xff786C5A),
    entryBody = Color(0xff786C5A),
    entryHint = Color(0xffAAA09C),
    entryButtonBackground = Color(0xffE1D0B6),
    entryButtonText = Color(0xffF0E9D7),
    entryQuoteText = Color(0xffAAA09C),
    debugColor1 = Color(0xff510998)
)

val BeachColors = PresentlyColors(
    iconResource = R.drawable.ic_shell,
    timelineBackground = Color(0xffECE8E4),
    timelineLine = Color(0xffD3C8C2),
    timelineDate = Color(0xff797979),
    timelineContent = Color(0xff797979),
    timelineHint = Color(0xffB9B9B9),
    timelineToolbar = Color(0xffD3C8C2),
    timelineLogo = Color(0xff636363),
    timelineOnToolbar = Color(0xff636363),
    timelineFab = Color(0xffD3C8C2),
    timelineOnFab = Color(0xff636363),
    entryBackground = Color(0xffECE8E4),
    entryDate = Color(0xff797979),
    entryBody = Color(0xff797979),
    entryHint = Color(0xffB9B9B9),
    entryButtonBackground = Color(0xffD3C8C2),
    entryButtonText = Color(0xff636363),
    entryQuoteText = Color(0xffB9B9B9),
    debugColor1 = Color(0xff510998)
)

val WavesColors = PresentlyColors(
    iconResource = R.drawable.ic_wave,
    timelineBackground = Color(0xff96B8D8),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff5F5F5F),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffF9F6EF),
    timelineOnToolbar = Color(0xffF9F6EF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffF9F6EF),
    entryBackground = Color(0xff96B8D8),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff5F5F5F),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffF9F6EF),
    entryQuoteText = Color(0xff5F5F5F),
    debugColor1 = Color(0xff510998)
)

val GelatoColors = PresentlyColors(
    iconResource = R.drawable.ic_flower,
    timelineBackground = Color(0xffC7A8A8),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff5F5F5F),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffEAD9D9),
    timelineOnToolbar = Color(0xffEAD9D9),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffEAD9D9),
    entryBackground = Color(0xffC7A8A8),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff5F5F5F),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffEAD9D9),
    entryQuoteText = Color(0xff5F5F5F),
    debugColor1 = Color(0xff510998)
)

val GlacierColors = PresentlyColors(
    iconResource = R.drawable.ic_cube,
    timelineBackground = Color(0xffA8C7C7),
    timelineLine = Color(0xffFFFFFF),
    timelineDate = Color(0xffFFFFFF),
    timelineContent = Color(0xffFFFFFF),
    timelineHint = Color(0xff5F5F5F),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffF9F6EF),
    timelineOnToolbar = Color(0xffF9F6EF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffF9F6EF),
    entryBackground = Color(0xffA8C7C7),
    entryDate = Color(0xffFFFFFF),
    entryBody = Color(0xffFFFFFF),
    entryHint = Color(0xff5F5F5F),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffF9F6EF),
    entryQuoteText = Color(0xff5F5F5F),
    debugColor1 = Color(0xff510998)
)

val CleanColors = PresentlyColors(
    iconResource = R.drawable.ic_flower,
    timelineBackground = Color(0xffA8C7C7),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff5F5F5F),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffF9F6EF),
    timelineOnToolbar = Color(0xffF9F6EF),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffF9F6EF),
    entryBackground = Color(0xffA8C7C7),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff5F5F5F),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffF9F6EF),
    entryQuoteText = Color(0xff5F5F5F),
    debugColor1 = Color(0xff510998)
)

val MossColors = PresentlyColors(
    iconResource = R.drawable.ic_flower,
    timelineBackground = Color(0xff7F9975),
    timelineLine = Color(0xff000000),
    timelineDate = Color(0xff000000),
    timelineContent = Color(0xff000000),
    timelineHint = Color(0xff3D3D3D),
    timelineToolbar = Color(0xff000000),
    timelineLogo = Color(0xffBCCCB7),
    timelineOnToolbar = Color(0xffBCCCB7),
    timelineFab = Color(0xff000000),
    timelineOnFab = Color(0xffBCCCB7),
    entryBackground = Color(0xff7F9975),
    entryDate = Color(0xff000000),
    entryBody = Color(0xff000000),
    entryHint = Color(0xff3D3D3D),
    entryButtonBackground = Color(0xff000000),
    entryButtonText = Color(0xffBCCCB7),
    entryQuoteText = Color(0xff3D3D3D),
    debugColor1 = Color(0xff510998)
)

val WesleyColors = PresentlyColors(
    iconResource = R.drawable.ic_cube,
    timelineBackground = Color(0xff000000),
    timelineLine = Color(0xffFFFFFF),
    timelineDate = Color(0xffFFFFFF),
    timelineContent = Color(0xffFFFFFF),
    timelineHint = Color(0xff8A878B),
    timelineToolbar = Color(0xffFFFFFF),
    timelineLogo = Color(0xff000000),
    timelineOnToolbar = Color(0xff000000),
    timelineFab = Color(0xffFFFFFF),
    timelineOnFab = Color(0xff000000),
    entryBackground = Color(0xff000000),
    entryDate = Color(0xffFFFFFF),
    entryBody = Color(0xffFFFFFF),
    entryHint = Color(0xff8A878B),
    entryButtonBackground = Color(0xffFFFFFF),
    entryButtonText = Color(0xff000000),
    entryQuoteText = Color(0xff8A878B),
    debugColor1 = Color(0xff510998)
)

val IvyColors = PresentlyColors(
    iconResource = R.drawable.ic_flower,
    timelineBackground = Color(0xff374F4B),
    timelineLine = Color(0xff131515),
    timelineDate = Color(0xff131515),
    timelineContent = Color(0xff131515),
    timelineHint = Color(0xff1D332F),
    timelineToolbar = Color(0xff131515),
    timelineLogo = Color(0xffFFFFFF),
    timelineOnToolbar = Color(0xffFFFFFF),
    timelineFab = Color(0xff131515),
    timelineOnFab = Color(0xffFFFFFF),
    entryBackground = Color(0xff374F4B),
    entryDate = Color(0xff131515),
    entryBody = Color(0xff131515),
    entryHint = Color(0xff1D332F),
    entryButtonBackground = Color(0xff131515),
    entryButtonText = Color(0xffFFFFFF),
    entryQuoteText = Color(0xff1D332F),
    debugColor1 = Color(0xff510998)
)

val MidnightColors = PresentlyColors(
    iconResource = R.drawable.ic_moon,
    timelineBackground = Color(0xff28262C),
    timelineLine = Color(0xff484349),
    timelineDate = Color(0xff8A878B),
    timelineContent = Color(0xff8A878B),
    timelineHint = Color(0xff484349),
    timelineToolbar = Color(0xff484349),
    timelineLogo = Color(0xff7E7B7F),
    timelineOnToolbar = Color(0xff7E7B7F),
    timelineFab = Color(0xff484349),
    timelineOnFab = Color(0xff7E7B7F),
    entryBackground = Color(0xff28262C),
    entryDate = Color(0xff8A878B),
    entryBody = Color(0xff8A878B),
    entryHint = Color(0xff484349),
    entryButtonBackground = Color(0xff484349),
    entryButtonText = Color(0xff7E7B7F),
    entryQuoteText = Color(0xff484349),
    debugColor1 = Color(0xff510998)
)

val DawnColors = PresentlyColors(
    iconResource = R.drawable.ic_sun_icon,
    timelineBackground = Color(0xff4F5D75),
    timelineLine = Color(0xff2D3142),
    timelineDate = Color(0xffBFC0C0),
    timelineContent = Color(0xffBFC0C0),
    timelineHint = Color(0xff8B8C8C),
    timelineToolbar = Color(0xff2D3142),
    timelineLogo = Color(0xffBFC0C0),
    timelineOnToolbar = Color(0xffBFC0C0),
    timelineFab = Color(0xff2D3142),
    timelineOnFab = Color(0xffBFC0C0),
    entryBackground = Color(0xff4F5D75),
    entryDate = Color(0xffBFC0C0),
    entryBody = Color(0xffBFC0C0),
    entryHint = Color(0xff8B8C8C),
    entryButtonBackground = Color(0xff2D3142),
    entryButtonText = Color(0xffBFC0C0),
    entryQuoteText = Color(0xff8B8C8C),
    debugColor1 = Color(0xff510998)
)

val MoonlightColors = PresentlyColors(
    iconResource = R.drawable.ic_moon,
    timelineBackground = Color(0xff091016),
    timelineLine = Color(0xff838383),
    timelineDate = Color(0xff838383),
    timelineContent = Color(0xff838383),
    timelineHint = Color(0xff484349),
    timelineToolbar = Color(0xff838383),
    timelineLogo = Color(0xff091016),
    timelineOnToolbar = Color(0xff091016),
    timelineFab = Color(0xff838383),
    timelineOnFab = Color(0xff091016),
    entryBackground = Color(0xff091016),
    entryDate = Color(0xff838383),
    entryBody = Color(0xff838383),
    entryHint = Color(0xff484349),
    entryButtonBackground = Color(0xff838383),
    entryButtonText = Color(0xff091016),
    entryQuoteText = Color(0xff484349),
    debugColor1 = Color(0xff510998)
)

val SunsetColors = PresentlyColors(
    iconResource = R.drawable.ic_sun_icon,
    timelineBackground = Color(0xff303030),
    timelineLine = Color(0xffA47272),
    timelineDate = Color(0xffA47272),
    timelineContent = Color(0xffA47272),
    timelineHint = Color(0xffA47272),
    timelineToolbar = Color(0xffA47272),
    timelineLogo = Color(0xff303030),
    timelineOnToolbar = Color(0xff303030),
    timelineFab = Color(0xffA47272),
    timelineOnFab = Color(0xff303030),
    entryBackground = Color(0xff303030),
    entryDate = Color(0xffA47272),
    entryBody = Color(0xffA47272),
    entryHint = Color(0xffA47272),
    entryButtonBackground = Color(0xffA47272),
    entryButtonText = Color(0xff303030),
    entryQuoteText = Color(0xffA47272),
    debugColor1 = Color(0xff510998)
)

//todo fix icons that are using text_color attribute

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
    "Brittany" to BrittanyColors,
    "Katie" to KatieColors,
    "Lotus" to LotusColors,
    "Monstera" to MonsteraColors,
    "Clouds" to CloudColors,
    "Matisse" to MatisseColors,
    "Daisy" to DaisyColors,
    "Rosie" to RosieColors,
    "Tulip" to TulipColors,
    "Sunlight" to SunlightColors,
    "Western" to WesternColors,
    "Field" to FieldColors,
    "Beach" to BeachColors,
    "Waves" to WavesColors,
    "Gelato" to GelatoColors,
    "Glacier" to GlacierColors,
    "Clean" to CleanColors,
    "Moss" to MossColors,
    "Wesley" to WesleyColors,
    "Ivy" to IvyColors,
    "Midnight" to MidnightColors,
    "Dawn" to DawnColors,
    "Moonlight" to MoonlightColors,
    "Sunset" to SunsetColors,
)