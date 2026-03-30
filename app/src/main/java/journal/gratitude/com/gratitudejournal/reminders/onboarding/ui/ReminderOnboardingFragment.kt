package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderPermissionSnapshot
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderOnboardingFragment : Fragment() {

    private val viewModel: ReminderOnboardingViewModel by viewModels()

    private var awaitingNotificationSettingsResult = false
    private var awaitingExactAlarmSettingsResult = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onNotificationPermissionDialogResult(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onDismissRequested()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PresentlyTheme(themeSpec = PresentlyThemeSpec.Original) {
                    ReminderOnboardingScreen(
                        state = viewModel.state,
                        onDismiss = viewModel::onDismissRequested,
                        onPrimaryAction = viewModel::onPrimaryActionClicked,
                        onSkipForNow = viewModel::onSkipForNowClicked,
                        onTimeChanged = viewModel::onTimeChanged
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start(buildPermissionSnapshot())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        ReminderOnboardingEffect.RequestNotificationPermission -> {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        ReminderOnboardingEffect.OpenNotificationSettings -> {
                            awaitingNotificationSettingsResult = true
                            startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                                }
                            )
                        }
                        ReminderOnboardingEffect.OpenExactAlarmSettings -> {
                            awaitingExactAlarmSettingsResult = true
                            startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${requireContext().packageName}")
                                }
                            )
                        }
                        ReminderOnboardingEffect.Dismiss -> {
                            parentFragmentManager.popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (awaitingNotificationSettingsResult) {
            awaitingNotificationSettingsResult = false
            viewModel.onNotificationSettingsResult(
                NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
            )
        }

        if (awaitingExactAlarmSettingsResult) {
            awaitingExactAlarmSettingsResult = false
            viewModel.onExactAlarmSettingsResult(isExactAlarmGranted(requireContext()))
        }
    }

    private fun buildPermissionSnapshot(): ReminderPermissionSnapshot {
        val notificationsEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        val canRequestNotificationPermission =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

        return ReminderPermissionSnapshot(
            notificationsEnabled = notificationsEnabled,
            canRequestNotificationPermission = canRequestNotificationPermission,
            exactAlarmGranted = isExactAlarmGranted(requireContext())
        )
    }

    private fun isExactAlarmGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }
}
