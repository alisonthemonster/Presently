package com.presently.sharing

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.facebook.testing.screenshot.ScreenshotRunner
import com.presently.testing.FileRenamer
import dagger.hilt.android.testing.HiltTestApplication

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

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}