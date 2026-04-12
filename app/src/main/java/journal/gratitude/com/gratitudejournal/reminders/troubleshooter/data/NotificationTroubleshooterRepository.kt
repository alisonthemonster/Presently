package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.data

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui.NotificationTroubleshooterCheck
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui.NotificationTroubleshooterCheckResult
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui.SupportEmailData
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import javax.inject.Inject

open class NotificationTroubleshooterRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: PresentlySettings
) {

    open fun runChecks(): List<NotificationTroubleshooterCheckResult> {
        return listOf(
            //todo what is the difference between POST_NOTIFICATIONS and APP_NOTIFICATIONS
            NotificationTroubleshooterCheck.POST_NOTIFICATIONS.resultFor(
                hasPostNotificationsPermission()
            ),
            NotificationTroubleshooterCheck.APP_NOTIFICATIONS.resultFor(
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            ),
            NotificationTroubleshooterCheck.EXACT_ALARM.resultFor(hasExactAlarmAccess()),
            NotificationTroubleshooterCheck.BATTERY_OPTIMIZATION.resultFor(
                isIgnoringBatteryOptimizations()
            ),
        )
    }

    open fun buildSupportEmailData(): SupportEmailData {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val body = """
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            App version: ${packageInfo.versionName ?: BuildConfig.VERSION_NAME}


            """.trimIndent()

        return SupportEmailData(
            recipient = SUPPORT_EMAIL,
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

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private companion object {
        private const val SUPPORT_EMAIL = "gratitude.journal.app@gmail.com"
    }
}
