package com.presently.sharing

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.facebook.testing.screenshot.ScreenshotRunner
import com.presently.testing.FileRenamer

/**
 * A custom [AndroidJUnitRunner] used to set up the Screenshot Runner
 * and update coverage filenames after tests complete.
 */
class CustomTestRunner : AndroidJUnitRunner() {

    override fun onCreate(args: Bundle) {
        ScreenshotRunner.onCreate(this, args)
        super.onCreate(args)
    }

    override fun finish(resultCode: Int, results: Bundle) {
        FileRenamer.write(this)
        ScreenshotRunner.onDestroy()
        super.finish(resultCode, results)
    }
}