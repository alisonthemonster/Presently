package com.presently.sharing.data

import android.annotation.SuppressLint
import android.os.Parcelable
import com.airbnb.mvrx.MavericksState
import com.presently.sharing.R
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class SharingArgs(val content: String, val date: String) : Parcelable

data class SharingViewState(
    val content: String,
    val dateString: String,
    val viewDesign: SharingViewDesign = SharingViewDesign(
        "original",
        R.color.originalTimelineColor,
        R.color.originalTimelineColor,
        R.color.originalBackgroundColor,
    ),
    val clicksShare: Boolean = false,
    val hasError: Boolean = false
) : MavericksState {

    constructor(args: SharingArgs) : this(content = args.content, dateString = args.date)
}