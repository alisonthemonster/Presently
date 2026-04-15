package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.util.createSupportEmailIntent
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                awaitingExternalSettingsResult = true
                                openIntent(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = "package:${requireContext().packageName}".toUri()
                                    }
                                )
                            }
                        }

                        is NotificationTroubleshooterEffect.ContactSupport -> {
                            openIntent(
                                createSupportEmailIntent(
                                    recipients = effect.emailData.recipients,
                                    subject = effect.emailData.subject,
                                    body = effect.emailData.body
                                )
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
