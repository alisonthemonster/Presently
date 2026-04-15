package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.data

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui.NotificationTroubleshooterCheck
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui.NotificationTroubleshooterCheckResult
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui.SupportEmailData
import javax.inject.Inject

open class NotificationTroubleshooterRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    open fun runChecks(): List<NotificationTroubleshooterCheckResult> {
        return buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // The Android 13 runtime permission is separate from the app-wide notifications toggle.
                add(
                    NotificationTroubleshooterCheck.POST_NOTIFICATIONS.resultFor(
                        hasPostNotificationsPermission()
                    )
                )
            }
            add(
                NotificationTroubleshooterCheck.APP_NOTIFICATIONS.resultFor(
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(NotificationTroubleshooterCheck.EXACT_ALARM.resultFor(hasExactAlarmAccess()))
            }
        }
    }

    open fun buildSupportEmailData(): SupportEmailData {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val body = """
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            App version: ${packageInfo.versionName ?: BuildConfig.VERSION_NAME}


            """.trimIndent()

        return SupportEmailData(
            recipients = arrayOf(SUPPORT_EMAIL),
            subject = "Presently notification troubleshooting",
            body = body
        )
    }

    private fun hasPostNotificationsPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasExactAlarmAccess(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val hasUseExactAlarmPermission =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.USE_EXACT_ALARM
                ) == PackageManager.PERMISSION_GRANTED
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return hasUseExactAlarmPermission || alarmManager.canScheduleExactAlarms()
    }

    private companion object {
        private const val SUPPORT_EMAIL = "gratitude.journal.app@gmail.com"
    }
}
