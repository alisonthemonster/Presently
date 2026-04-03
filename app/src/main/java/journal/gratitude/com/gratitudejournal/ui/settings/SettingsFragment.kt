package journal.gratitude.com.gratitudejournal.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.provider.Settings.EXTRA_APP_PACKAGE
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dropbox.core.android.Auth
import com.dropbox.core.android.AuthActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.settings.model.*
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.convertCsvToEntries
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.exportEntriesToCsvFile
import journal.gratitude.com.gratitudejournal.util.backups.RealCsvParser
import journal.gratitude.com.gratitudejournal.util.backups.UploadToCloudWorker
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader.Companion.PRESENTLY_BACKUP
import journal.gratitude.com.gratitudejournal.util.AppLocaleManager
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreference
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreferenceFragment
import journal.gratitude.com.gratitudejournal.ui.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.ui.security.APP_LOCK_BIOMETRIC_AUTHENTICATORS
import journal.gratitude.com.gratitudejournal.ui.security.BIOMETRIC_SOURCE_SETTINGS
import journal.gratitude.com.gratitudejournal.ui.security.BiometricTelemetry
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeFragment
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.backups.RealUploader.Companion.BACKUP_NOTIFICATION_ID
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.threeten.bp.LocalDateTime
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, DialogPreference.TargetFragment {

    @Inject lateinit var repository: EntryRepository
    @Inject lateinit var settings: PresentlySettings
    @Inject lateinit var analytics: AnalyticsLogger
    @Inject lateinit var crashReporter: CrashReporter

    private var awaitingNotificationSettingsResult = false
    private var awaitingExactAlarmSettingsResult = false

    private val notificationPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                settings.setNotificationsEnabled(true)
                NotificationScheduler().configureNotifications(requireContext(), settings)
            } else {
                settings.setNotificationsEnabled(false)
                findPreference<SwitchPreference>(NOTIFS)?.isChecked = false
            }
            refreshReminderPreferences()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(
                    top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                    bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        val window = requireActivity().window
        window.statusBarColor = Color.TRANSPARENT
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.timelineBackgroundColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preferences, rootKey)

        //region App Information
        val faq = findPreference<Preference>(getString(R.string.key_faq))
        faq?.setOnPreferenceClickListener {
            openFaq()
            true
        }
        val share = findPreference<Preference>(getString(R.string.key_share_app))

        // Handle icon issues for android versions < 23
        if(Build.VERSION.SDK_INT <= 23) {
            context?.getColor(R.color.text_color)?.let { share?.icon?.setTint(it) }
            val lang = findPreference<Preference>(APP_LANGUAGE)
            context?.getColor(R.color.text_color)?.let { lang?.icon?.setTint(it) }
        }

        share?.setOnPreferenceClickListener {
            openShareApp()
            true
        }

        val privacy = findPreference<Preference>(getString(R.string.key_privacy_policy))
        privacy?.setOnPreferenceClickListener {
            openPrivacyPolicy()
            true
        }
        val terms = findPreference<Preference>(getString(R.string.key_terms_conditions))
        terms?.setOnPreferenceClickListener {
            openTermsAndConditions()
            true
        }
        val oss = findPreference<Preference>(getString(R.string.key_open_source))
        oss?.setOnPreferenceClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        }
        val version = findPreference<Preference>(VERSION_PREF)
        val versionNum = BuildConfig.VERSION_NAME
        version?.summary = versionNum
        //endregion

        val theme = findPreference<Preference>(THEME_PREF)
        theme?.setOnPreferenceClickListener {
            openThemes()
            true
        }

        val dropbox = findPreference<Preference>(BACKUP_TOKEN)
        val cadencePref = (findPreference<Preference>(BACKUP_CADENCE) as ListPreference)

        dropbox?.setOnPreferenceClickListener {
            val wantsToLogin = preferenceScreen.sharedPreferences?.getBoolean(BACKUP_TOKEN, false) ?: false
            if (!wantsToLogin) {
                analytics.recordEvent(DROPBOX_DEAUTH)
                lifecycleScope.launch {
                    DropboxUploader.deauthorizeDropboxAccess(requireContext(), settings)
                }
            } else {
                analytics.recordEvent(DROPBOX_AUTH_ATTEMPT)
                try {
                    val appKey = BuildConfig.DROPBOX_APP_KEY
                    val manifestCheckPassed =
                        AuthActivity.checkAppBeforeAuth(requireContext(), appKey, false)
                    if (!manifestCheckPassed) {
                        val exception = IllegalStateException(
                            "Dropbox manifest/auth precheck failed before auth start"
                        )
                        crashReporter.logHandledException(exception)
                        Toast.makeText(context, R.string.dropbox_auth_failed, Toast.LENGTH_SHORT).show()
                        return@setOnPreferenceClickListener true
                    }
                    DropboxUploader.authorizeDropboxAccess(requireContext(), settings)
                } catch (exception: Exception) {
                    crashReporter.logHandledException(exception)
                    Toast.makeText(context, R.string.dropbox_auth_failed, Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        val cadence = settings.getAutomaticBackupCadence()
        val index = cadence.index
        cadencePref.setValueIndex(index)
        val oneTimeExport = findPreference<Preference>(ONE_TIME_EXPORT_PREF)
        oneTimeExport?.setOnPreferenceClickListener {
            createFileOnDevice()
            true
        }

        val import = findPreference<Preference>(IMPORT_PREF)
        import?.setOnPreferenceClickListener {
            importFromCsv()
            true
        }

        val fingerprint = findPreference<Preference>(FINGERPRINT)
        val biometricStatus = BiometricManager.from(requireContext())
            .canAuthenticate(APP_LOCK_BIOMETRIC_AUTHENTICATORS)
        analytics.recordEvent(
            BIOMETRICS_AVAILABILITY_CHECKED,
            BiometricTelemetry.availabilityDetails(biometricStatus, BIOMETRIC_SOURCE_SETTINGS)
        )
        val canAuthenticateUsingFingerPrint =
            biometricStatus == BiometricManager.BIOMETRIC_SUCCESS
        fingerprint?.parent!!.isEnabled = canAuthenticateUsingFingerPrint

        findPreference<SwitchPreference>(EXACT_ALARMS)?.apply {
            isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            setOnPreferenceChangeListener { _, _ ->
                openExactAlarmPermissionSettings()
                false
            }
        }

        refreshReminderPreferences()
    }

    override fun onResume() {
        super.onResume()
        val prefs = preferenceScreen.sharedPreferences

        // Set up a listener whenever a key changes
        prefs?.registerOnSharedPreferenceChangeListener(this)
        syncReminderPreferencesWithSystemState()
        refreshReminderPreferences()

        // If we just resumed after launching the Dropbox activity
        if (settings.wasDropboxAuthInitiated()) {
            val token = Auth.getDbxCredential() //get token from Dropbox Auth activity
            if (token == null) {
                //user started to auth and didn't succeed
                val exception = IllegalStateException(
                    "Dropbox auth resumed without a credential. data=${activity?.intent?.data}"
                )
                crashReporter.logHandledException(exception)
                settings.markDropboxAuthAsCancelled()
                Toast.makeText(context, R.string.dropbox_auth_failed, Toast.LENGTH_SHORT).show()
                activity?.recreate()
            } else {
                settings.setAccessToken(token)
                createDropboxUploaderWorker(settings.getAutomaticBackupCadence())
                cancelDropboxFailureNotifications() //now that user has auth'd cancel any notifs about previous failure
            }
        }
    }

    private fun refreshReminderPreferences() {
        val hasDisabledAlarmReminders = settings.hasUserDisabledAlarmReminders(requireContext())
        val hasDisabledSystemNotifications = !NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        val remindersEnabled = settings.hasEnabledNotifications() && !hasDisabledSystemNotifications
        val notifs = findPreference<SwitchPreference>(NOTIFS)
        val prefTime = findPreference<Preference>(NOTIF_PREF_TIME)
        val exactAlarms = findPreference<SwitchPreference>(EXACT_ALARMS)

        notifs?.isChecked = remindersEnabled
        notifs?.isEnabled = true
        prefTime?.isEnabled = remindersEnabled
        exactAlarms?.isEnabled = remindersEnabled
        exactAlarms?.isChecked = !hasDisabledAlarmReminders
    }

    private fun syncReminderPreferencesWithSystemState() {
        val notificationsEnabledInSystem =
            NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()

        if (!notificationsEnabledInSystem && settings.hasEnabledNotifications()) {
            settings.setNotificationsEnabled(false)
            NotificationScheduler().disableNotifications(requireContext())
        }

        if (awaitingNotificationSettingsResult) {
            awaitingNotificationSettingsResult = false
            settings.setNotificationsEnabled(notificationsEnabledInSystem)
            if (notificationsEnabledInSystem) {
                NotificationScheduler().configureNotifications(requireContext(), settings)
            } else {
                NotificationScheduler().disableNotifications(requireContext())
            }
        }

        if (awaitingExactAlarmSettingsResult && settings.hasEnabledNotifications()) {
            awaitingExactAlarmSettingsResult = false
            NotificationScheduler().configureNotifications(requireContext(), settings)
        } else {
            awaitingExactAlarmSettingsResult = false
        }
    }

    private fun openNotificationPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            val shouldRequestInApp =
                !settings.hasRequestedNotificationPermission() ||
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

            if (shouldRequestInApp) {
                settings.markNotificationPermissionRequested()
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }

            awaitingNotificationSettingsResult = true
            Intent().apply {
                action = ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(EXTRA_APP_PACKAGE, requireContext().packageName)
            }.also {
                startActivity(it)
            }
            return
        }

        awaitingNotificationSettingsResult = true
        Intent().apply {
            action = ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(EXTRA_APP_PACKAGE, requireContext().packageName)
        }.also {
            startActivity(it)
        }
    }

    private fun openExactAlarmPermissionSettings() {
        awaitingExactAlarmSettingsResult = true
        Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }.also {
            startActivity(it)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NOTIFS -> {
                val notifsTurnedOn = settings.hasEnabledNotifications()
                if (notifsTurnedOn) {
                    if (
                        hasNotificationPermission() &&
                        NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
                    ) {
                        NotificationScheduler().configureNotifications(requireContext(), settings)
                        refreshReminderPreferences()
                    } else {
                        openNotificationPermissionSettings()
                    }
                } else {
                    NotificationScheduler().disableNotifications(requireContext())
                    analytics.recordEvent(CANCELLED_NOTIFS)
                }
            }
            FINGERPRINT -> {
                val biometricsEnabled = settings.isBiometricsEnabled()
                if (biometricsEnabled) {
                    analytics.recordEvent(BIOMETRICS_SELECT)
                } else {
                    analytics.recordEvent(BIOMETRICS_DESELECT)
                }
            }
            BACKUP_CADENCE -> {
                //todo test cadence works properly with dropbox
                val cadence = settings.getAutomaticBackupCadence()
                analytics.recordSelectEvent(cadence.string, "cadence")
                createDropboxUploaderWorker(cadence)
            }
            APP_LANGUAGE -> {
                val language = settings.getLocale()
                updateLanguage(language)
            }
            ANALYTICS_OPT_IN_PREF -> {
                val isOptedIn = settings.isOptedIntoAnalytics()
                if (isOptedIn) {
                    analytics.optIntoAnalytics()
                    Toast.makeText(context, R.string.analytics_opt_in_success, Toast.LENGTH_SHORT).show()
                } else {
                    analytics.optOutOfAnalytics()
                    Toast.makeText(context, R.string.analytics_opt_out_success, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateLanguage(language: String) {
        analytics.recordSelectEvent(language, "language")
        val locales = AppLocaleManager.localeListFor(language)
        if (AppCompatDelegate.getApplicationLocales() != locales) {
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    private fun createDropboxUploaderWorker(cadence: BackupCadence) {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(PRESENTLY_BACKUP)

        when (cadence) {
            BackupCadence.DAILY -> {
                val uploadWorkRequest =
                    PeriodicWorkRequestBuilder<UploadToCloudWorker>(1, TimeUnit.DAYS)
                        .addTag(PRESENTLY_BACKUP)
                        .build()
                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
            }
            BackupCadence.WEEKLY -> {
                val uploadWorkRequest =
                    PeriodicWorkRequestBuilder<UploadToCloudWorker>(7, TimeUnit.DAYS)
                        .addTag(PRESENTLY_BACKUP)
                        .build()
                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
            }
            BackupCadence.EVERY_CHANGE -> {
                //every change so do an upload now
                val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadToCloudWorker>()
                    .addTag(PRESENTLY_BACKUP)
                    .build()
                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
            }
        }
    }

    private fun cancelDropboxFailureNotifications() {
        val notificationManager = NotificationManagerCompat.from(requireContext())
        notificationManager.cancel(BACKUP_NOTIFICATION_ID)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is TimePreference) {
            dialogFragment =
                TimePreferenceFragment()
            val bundle = Bundle(1)
            bundle.putString("key", preference.getKey())
            dialogFragment.setArguments(bundle)
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, "DIALOG")
        } else {
                val dialogFragment =
                        CustomListPrefDialogFragCompat.newInstance(preference.key)
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null);

        }
    }

    private fun openThemes() {
        analytics.recordEvent(OPENED_THEMES)

        val fragment = ThemeFragment()
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(SETTINGS_TO_THEME)
            .commit()
    }

    private fun openTermsAndConditions() {
        analytics.recordEvent(OPENED_TERMS_CONDITIONS)
        try {
            val browserIntent =
                Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://presently-app.firebaseapp.com/termsconditions.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            crashReporter.logHandledException(activityNotFoundException)
        }
    }

    private fun openShareApp() {
        analytics.recordEvent(OPENED_SHARE_APP)

        try {
            val appName = getString(R.string.app_name)
            val textIntent = Intent(Intent.ACTION_SEND)
            textIntent.type = "text/plain"
            textIntent.putExtra(Intent.EXTRA_SUBJECT, appName)

            val appPackageName = context?.packageName
            val shareApp = getString(R.string.share_app_text)
            val shareText =
                "$shareApp https://play.google.com/store/apps/details?id=$appPackageName"
            textIntent.putExtra(Intent.EXTRA_TEXT, shareText)

            val chooserIntent = Intent.createChooser(textIntent, appName)
            startActivity(chooserIntent);
        } catch (exception: Exception) {
            crashReporter.logHandledException(exception)
        }
    }

    private fun openPrivacyPolicy() {
        analytics.recordEvent(OPENED_PRIVACY_POLICY)

        try {
            val browserIntent =
                Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://presently-app.firebaseapp.com/privacypolicy.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            crashReporter.logHandledException(activityNotFoundException)
        }
    }

    private fun openFaq() {
        analytics.recordEvent(OPENED_FAQ)

        try {
            val browserIntent =
                Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://presently-app.firebaseapp.com/faq.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            crashReporter.logHandledException(activityNotFoundException)
        }
    }

    /**
     * Warns the user about importing a CSV file via an alert dialog
     * */
    private fun importFromCsv() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.import_data_dialog)
                setMessage(R.string.import_data_dialog_message)
                setPositiveButton(R.string.ok) { _, _ ->
                    selectCSVFile()
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    /**
     * Result contract for activity result to read from the backup CSV file
     * */
    private val readCsvResultContact =
        registerForActivityResult(OpenCsvDocumentContract()) { uri: Uri? ->
            if (uri == null) {
                crashReporter.logHandledException(NullPointerException("URI was null when receiving file"))
                Toast.makeText(context, R.string.file_not_csv, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            try {
                val contentResolver = requireContext().contentResolver
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers do not offer persistable permissions for one-time access.
            } catch (exception: IllegalArgumentException) {
                crashReporter.logHandledException(exception)
            }

            try {
                requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                    importFromCsv(inputStream)
                } ?: run {
                    crashReporter.logHandledException(
                        NullPointerException("inputStream is null, uri: $uri")
                    )
                    Toast.makeText(context, R.string.error_parsing, Toast.LENGTH_SHORT).show()
                }
            } catch (exception: Exception) {
                analytics.recordEvent(IMPORTING_BACKUP_ERROR)
                crashReporter.logHandledException(
                    IllegalStateException("Unable to open selected CSV uri: $uri", exception)
                )
                Toast.makeText(context, R.string.error_parsing, Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Opens the chooser to allow the user to select their CSV file.
     * */
    private fun selectCSVFile() {
        analytics.recordEvent(LOOKED_FOR_DATA)
        try {
            readCsvResultContact.launch(OpenCsvDocumentContract.mimeTypes)
        } catch (ex: ActivityNotFoundException) {
            crashReporter.logHandledException(ex)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Takes the input stream, converts it to a list of entries, saves it to the
     * DB and lets the user know the result.
     * */
    private fun importFromCsv(inputStream: InputStream) {
        try {
            val parser = CSVParser.parse(
                    inputStream, Charset.defaultCharset(),
                    CSVFormat.DEFAULT
            )
            val realCsvParser = RealCsvParser(parser)
            val entries = convertCsvToEntries(realCsvParser)
            lifecycleScope.launch {
                repository.addEntries(entries)
                analytics.recordEvent(IMPORTED_DATA_SUCCESS)
                parentFragmentManager.popBackStack()
            }

            //TODO move this hardcoded string to strings.xml
            Toast.makeText(context, "Imported successfully!", Toast.LENGTH_SHORT).show()
        } catch (exception: Exception) {
            analytics.recordEvent(IMPORTING_BACKUP_ERROR)
            crashReporter.logHandledException(exception)

            Toast.makeText(context, R.string.error_parsing, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Result contract for activity result to create backup the CSV file
     * */
    private val saveCsvResultContact =
        registerForActivityResult(CreateCsvDocumentContract()) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    val csvResult = exportEntriesToCsvFile(
                            requireContext(),
                            uri,
                            repository.getEntries()
                    )
                    when (csvResult) {
                        is CsvUriError -> exportCallback.onFailure(csvResult.exception)
                        is CsvUriCreated -> exportCallback.onSuccess(csvResult.uri)
                    }
                }
            } else {
                crashReporter.logHandledException(NullPointerException("URI was null after user selected file location"))
                Toast.makeText(
                        context,
                        R.string.error_creating_csv_file,
                        Toast.LENGTH_SHORT
                ).show()
            }

        }

    /**
     * Opens the Storage Access Framework and lets the user select where they want to
     * export the CSV file.
     * */
    private fun createFileOnDevice() {
        val date = LocalDateTime.now().withNano(0).toString().replace(':', '-')
        val fileName = "PresentlyBackup$date.csv"
        saveCsvResultContact.launch(fileName)
    }

    private val exportCallback: ExportCallback = object : ExportCallback {
        override fun onSuccess(uri: Uri) {
            Snackbar.make(view!!, R.string.export_success, Snackbar.LENGTH_LONG)
                .setAction(R.string.open) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "text/csv")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        crashReporter.logHandledException(e)
                        Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT)
                            .show()
                    }
                }.show()
        }

        override fun onFailure(exception: Exception) {
            crashReporter.logHandledException(exception)
            Toast.makeText(
                    context,
                    "Error : ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    companion object {
        const val BACKUP_TOKEN = "dropbox_pref"
        const val SETTINGS_TO_THEME = "SETTINGS_TO_THEME"
    }
}

interface ExportCallback {
    fun onSuccess(file: Uri)

    fun onFailure(exception: Exception)
}
