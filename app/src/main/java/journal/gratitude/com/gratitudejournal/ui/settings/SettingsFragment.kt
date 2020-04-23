package journal.gratitude.com.gratitudejournal.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.crashlytics.android.Crashlytics
import com.dropbox.core.android.Auth
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.android.support.AndroidSupportInjection
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.search.SearchViewModel
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineViewModel
import journal.gratitude.com.gratitudejournal.util.backups.*
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader.Companion.PRESENTLY_BACKUP
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreference
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreferenceFragment
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.timeline_fragment.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preferences, rootKey)

        val privacy = findPreference("privacy_policy")
        privacy.setOnPreferenceClickListener {
            openPrivacyPolicy()
            true
        }
        val terms = findPreference("terms_conditions")
        terms.setOnPreferenceClickListener {
            openTermsAndConditions()
            true
        }
        val faq = findPreference("faq")
        faq.setOnPreferenceClickListener {
            openFaq()
            true
        }
        val theme = findPreference(THEME_PREF)
        theme.setOnPreferenceClickListener {
            openThemes()
            true
        }
        val oss = findPreference("open_source")
        oss.setOnPreferenceClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        }

        val version = findPreference(VERSION_PREF)
        val versionNum = BuildConfig.VERSION_NAME
        version.summary = versionNum
        val dropbox = findPreference(BACKUP_TOKEN)
        val cadencePref = (findPreference(BACKUP_CADENCE) as ListPreference)


        dropbox.setOnPreferenceClickListener {
            val wantsToLogin = preferenceScreen.sharedPreferences.getBoolean(BACKUP_TOKEN, false)
            if (!wantsToLogin) {
                firebaseAnalytics.logEvent(DROPBOX_DEAUTH, null)
                firebaseAnalytics.setUserProperty(DROPBOX_USER, "false")
                lifecycleScope.launch {
                    DropboxUploader.deauthorizeDropboxAccess(context!!)
                }
            } else {
                firebaseAnalytics.logEvent(DROPBOX_AUTH_ATTEMPT, null)
                DropboxUploader.authorizeDropboxAccess(context!!)
            }
            true
        }

        val cadence = preferenceScreen.sharedPreferences.getString(BACKUP_CADENCE, "0")
        val index = when (cadence) {
            "0" -> 0
            "1" -> 1
            else -> 2
        }
        cadencePref.setValueIndex(index)

        val oneTimeExport = findPreference(ONE_TIME_EXPORT_PREF)
        oneTimeExport.setOnPreferenceClickListener {
            exportToCsv()
            true
        }

        val import = findPreference(IMPORT_PREF)
        import.setOnPreferenceClickListener {
            importFromCsv()
            true
        }

        val fingerprint = findPreference(FINGERPRINT)
        val canAuthenticateUsingFingerPrint =
            BiometricManager.from(context!!).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        fingerprint.parent!!.isEnabled = canAuthenticateUsingFingerPrint

    }

    override fun onResume() {
        super.onResume()
        val prefs = preferenceScreen.sharedPreferences

        // Set up a listener whenever a key changes
        prefs.registerOnSharedPreferenceChangeListener(this)

        val accessToken = prefs.getString("access-token", null)
        if (accessToken == "attempted") {
            val token = Auth.getOAuth2Token()
            if (token == null) {
                //user started to auth and didn't succeed
                firebaseAnalytics.logEvent(DROPBOX_AUTH_QUIT, null)
                prefs.edit().putBoolean(BACKUP_TOKEN, false).apply()
                prefs.edit().remove("access-token").apply()
                activity?.recreate()
            } else {
                firebaseAnalytics.logEvent(DROPBOX_AUTH_SUCCESS, null)
                firebaseAnalytics.setUserProperty(DROPBOX_USER, "true")
                prefs.edit().putString("access-token", token).apply()
                createDropboxUploaderWorker("0")
            }
        }
    }

    private fun createDropboxUploaderWorker(cadence: String) {
        WorkManager.getInstance(context!!).cancelAllWorkByTag(PRESENTLY_BACKUP)

        when (cadence) {
            "0" -> {
                //every day
                val uploadWorkRequest =
                    PeriodicWorkRequestBuilder<UploadToCloudWorker>(1, TimeUnit.DAYS)
                        .addTag(PRESENTLY_BACKUP)
                        .build()
                WorkManager.getInstance(context!!).enqueue(uploadWorkRequest)
            }
            "1" -> {
                //every week
                val uploadWorkRequest =
                    PeriodicWorkRequestBuilder<UploadToCloudWorker>(7, TimeUnit.DAYS)
                        .addTag(PRESENTLY_BACKUP)
                        .build()
                WorkManager.getInstance(context!!).enqueue(uploadWorkRequest)
            }
            "2" -> {
                //every change so do an upload now
                val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadToCloudWorker>()
                    .addTag(PRESENTLY_BACKUP)
                    .build()
                WorkManager.getInstance(context!!).enqueue(uploadWorkRequest)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            THEME_PREF -> {
                val theme = sharedPreferences.getString(THEME_PREF, "original")
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, theme)
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, theme)
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "theme")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                activity?.recreate()
            }
            NOTIFS -> {
                val notifsTurnedOn = sharedPreferences.getBoolean(key, true)
                if (notifsTurnedOn) {
                    NotificationScheduler().configureNotifications(context!!)
                    firebaseAnalytics.setUserProperty(HAS_NOTIFICATIONS_TURNED_ON, "true")
                } else {
                    NotificationScheduler().disableNotifications(context!!)
                    firebaseAnalytics.logEvent(CANCELLED_NOTIFS, null)
                    firebaseAnalytics.setUserProperty(HAS_NOTIFICATIONS_TURNED_ON, "false")
                }
            }
            FINGERPRINT -> {
                val biometricsEnabled = sharedPreferences.getBoolean(key, false)
                if (biometricsEnabled) {
                    firebaseAnalytics.logEvent(BIOMETRICS_SELECT, null)
                    firebaseAnalytics.setUserProperty(BIOMETRICS_ENABLED, "true")
                } else {
                    firebaseAnalytics.logEvent(BIOMETRICS_DESELECT, null)
                    firebaseAnalytics.setUserProperty(BIOMETRICS_ENABLED, "false")
                }
            }
            BACKUP_CADENCE -> {
                val cadence =
                    preferenceScreen.sharedPreferences.getString(BACKUP_CADENCE, "0") ?: "0"
                createDropboxUploaderWorker(cadence)
            }
        }
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
            dialogFragment.show(this.fragmentManager!!, "DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            //TODO move constants
            TimelineFragment.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    performExport()
                } else {
                    Toast.makeText(context, R.string.permission_export, Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
            }
        }
    }

    private fun openThemes() {
        firebaseAnalytics.logEvent(OPENED_THEMES, null)
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.settingsFragment) {
            navController.navigate(
                R.id.action_settingsFragment_to_themeFragment
            )
        }
    }

    private fun openTermsAndConditions() {
        firebaseAnalytics.logEvent(OPENED_TERMS_CONDITIONS, null)
        try {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://presently-app.firebaseapp.com/termsconditions.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            Crashlytics.logException(activityNotFoundException)
        }
    }

    private fun openPrivacyPolicy() {
        firebaseAnalytics.logEvent(OPENED_PRIVACY_POLICY, null)

        try {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://presently-app.firebaseapp.com/privacypolicy.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            Crashlytics.logException(activityNotFoundException)
        }
    }

    private fun openFaq() {
        firebaseAnalytics.logEvent(OPENED_FAQ, null)

        try {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://presently-app.firebaseapp.com/faq.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            Crashlytics.logException(activityNotFoundException)
        }
    }

    private fun exportToCsv() {
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                TimelineFragment.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL
            )
        } else {
            performExport()
        }
    }

    private fun performExport() {
        firebaseAnalytics.logEvent(EXPORTED_DATA, null)

        lifecycleScope.launch {
            val entries = viewModel.getEntries()
            when (val csvResult = LocalExporter.exportToCSV(entries)) {
                is CsvError -> exportCallback.onFailure(csvResult.exception)
                is CsvCreated -> exportCallback.onSuccess(csvResult.file)
            }
        }
    }

    private fun importFromCsv() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.import_data_dialog)
                setMessage(R.string.import_data_dialog_message)
                setPositiveButton(R.string.ok) { dialog, id ->
                    selectCSVFile()
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    private fun selectCSVFile() {
        firebaseAnalytics.logEvent(LOOKED_FOR_DATA, null)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/csv|text/comma-separated-values|application/csv"
        val mimeTypes = arrayOf("text/comma-separated-values", "text/csv")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        try {
            startActivityForResult(Intent.createChooser(intent, "Select"),
                TimelineFragment.IMPORT_CSV
            )
        } catch (ex: ActivityNotFoundException) {
            Crashlytics.logException(ex)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    private val exportCallback: ExportCallback = object : ExportCallback {
        override fun onSuccess(file: File) {
            Snackbar.make(view!!, R.string.export_success, Snackbar.LENGTH_LONG)
                .setAction(R.string.open) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        val apkURI = FileProvider.getUriForFile(
                            context!!,
                            context?.applicationContext?.packageName + ".provider", file
                        )
                        intent.setDataAndType(apkURI, "text/csv")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Crashlytics.logException(e)
                        Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
                    }
                }.show()
        }

        override fun onFailure(exception: Exception) {
            Crashlytics.logException(exception)
            Toast.makeText(context, "Error : ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val BACKUP_CATEGORY = "backups_category"
        const val BACKUP_TOKEN = "dropbox_pref"
        const val BACKUP_CADENCE = "dropbox_cadence"
        const val FINGERPRINT = "fingerprint_lock"
        const val NOTIFS = "notif_parent"
        const val NOTIF_PREF_TIME = "pref_time"
        const val THEME_PREF = "current_theme"
        const val VERSION_PREF = "version"
        const val ONE_TIME_EXPORT_PREF = "one_time_export"
        const val IMPORT_PREF = "import_entries"
    }
}
