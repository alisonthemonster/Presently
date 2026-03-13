package journal.gratitude.com.gratitudejournal.testUtils


import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.facebook.testing.screenshot.ScreenshotRunner
import dagger.hilt.android.testing.HiltTestApplication
import journal.gratitude.com.gratitudejournal.util.reminders.AlarmPermissionReceiver
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationResetReceiver

/**
 * A custom [AndroidJUnitRunner] used to set up the Screenshot Runner, and update coverage filenames
 * after tests complete.
 */
class AppCustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication_Application::class.java.name, context)
    }

    override fun onCreate(args: Bundle) {
        ScreenshotRunner.onCreate(this, args)
        super.onCreate(args)
        setReminderReceiversEnabled(enabled = false)
    }

    override fun finish(resultCode: Int, results: Bundle) {
        try {
            setReminderReceiversEnabled(enabled = true)
        } finally {
            FileRenamer.write(this)
            ScreenshotRunner.onDestroy()
            super.finish(resultCode, results)
        }
    }

    private fun setReminderReceiversEnabled(enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        val packageManager = targetContext.packageManager
        listOf(
            NotificationResetReceiver::class.java,
            AlarmPermissionReceiver::class.java
        ).forEach { receiverClass ->
            packageManager.setComponentEnabledSetting(
                ComponentName(targetContext, receiverClass),
                state,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
