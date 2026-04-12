package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@AndroidEntryPoint
class NotificationTroubleshooterFragment : Fragment() {

    @Inject lateinit var crashReporter: CrashReporter

    private val viewModel: NotificationTroubleshooterViewModel by viewModels()

    private var awaitingExternalSettingsResult = false

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PresentlyTheme {
                    NotificationTroubleshooterScreen(
                        state = viewModel.state,
                        onClose = { parentFragmentManager.popBackStack() },
                        onFixItClicked = viewModel::onFixItClicked,
                        onContactSupportClicked = viewModel::onContactSupportClicked
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadChecks()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        NotificationTroubleshooterEffect.OpenAppNotificationSettings -> {
                            awaitingExternalSettingsResult = true
                            openIntent(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                                }
                            )
                        }

                        NotificationTroubleshooterEffect.OpenExactAlarmSettings -> {
                            awaitingExternalSettingsResult = true
                            //todo what are we doing in the onboarding flow for this?
                            openIntent(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = "package:${requireContext().packageName}".toUri()
                                }
                            )
                        }

                        NotificationTroubleshooterEffect.OpenBatteryOptimizationSettings -> {
                            awaitingExternalSettingsResult = true
                            //todo can this be more precise?
                            openIntent(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }

                        is NotificationTroubleshooterEffect.ContactSupport -> {
                            //todo let's use the code from the other contact and make it resuable
                            openIntent(
                                Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:".toUri()
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(effect.emailData.recipient))
                                    putExtra(Intent.EXTRA_SUBJECT, effect.emailData.subject)
                                    putExtra(Intent.EXTRA_TEXT, effect.emailData.body)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (awaitingExternalSettingsResult) {
            awaitingExternalSettingsResult = false
            viewModel.loadChecks()
        }
    }

    private fun openIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            crashReporter.logHandledException(activityNotFoundException)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

}
