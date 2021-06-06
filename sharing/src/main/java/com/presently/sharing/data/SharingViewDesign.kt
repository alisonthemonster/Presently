package com.presently.sharing.data

import com.presently.sharing.R

data class SharingViewDesign(
    val id: String,
    val headerTextColor: Int,
    val bodyTextColor: Int,
    val backgroundColor: Int
)

//TODO add analytics events

val designs = listOf(
    SharingViewDesign(
        "original",
        R.color.originalTimelineColor,
        R.color.originalTimelineColor,
        R.color.originalBackgroundColor,
    ),
    SharingViewDesign(
        "moonlight",
        R.color.moonlightMainTextAndButtonColor,
        R.color.moonlightMainTextAndButtonColor,
        R.color.moonlightBackgroundColor
    ),
    SharingViewDesign(
        "daisy",
        R.color.daisyMainTextAndButtonColor,
        R.color.daisyMainTextAndButtonColor,
        R.color.daisyBackgroundColor
    ),
    SharingViewDesign(
        "sunlight",
        R.color.sunlightTimelineColor,
        R.color.sunlightDateTextEntryScreenTextColor,
        R.color.sunlightBackgroundColor
    ),
    SharingViewDesign(
        "rosie",
        R.color.rosieMainTextAndButtonColor,
        R.color.rosieHintQuoteTextColor,
        R.color.rosieBackgroundColor
    ),
    SharingViewDesign(
        "dawn",
        R.color.dawnMainTextAndButtonColor,
        R.color.dawnMainTextAndButtonColor,
        R.color.dawnBackgroundColor
    ),
    SharingViewDesign(
        "katie",
        R.color.katieTimelineColor,
        R.color.katieDateTextEntryScreenTextColor,
        R.color.katieBackgroundColor
    ),
    SharingViewDesign(
        "brittany",
        R.color.brittanyDateTextEntryScreenTextColor,
        R.color.brittanyDateTextEntryScreenTextColor,
        R.color.brittanyBackgroundColor
    ),
    SharingViewDesign(
        "matisse",
        R.color.matisseMainTextAndButtonColor,
        R.color.matisseMainTextAndButtonColor,
        R.color.matisseBackgroundColor
    ),
)