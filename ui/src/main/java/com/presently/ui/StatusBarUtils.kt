package com.presently.ui

import android.view.View
import android.view.Window
import androidx.core.graphics.ColorUtils

fun setStatusBarColorsForBackground(window: Window, backgroundColor: Int) {
    if (!isBackgroundDark(backgroundColor)) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        window.decorView.systemUiVisibility = 0
    }
}

private fun isBackgroundDark(color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) < 0.5
}