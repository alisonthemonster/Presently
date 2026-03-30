package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_ACCEPTED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_DISMISSED
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyFontFamilies
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import javax.inject.Inject

@AndroidEntryPoint
class DayOneDialogFragment : DialogFragment() {

    @Inject lateinit var analytics: AnalyticsLogger
    @Inject lateinit var themeSpecProvider: ReminderPromptThemeSpecProvider

    private var openedFullOnboarding = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, R.style.MilestoneDialog)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val themeSpec = themeSpecProvider.get()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PresentlyTheme(themeSpec = themeSpec) {
                    ReminderOnboardingPromptSheet(
                        onPrimaryAction = ::openReminderOnboarding
                    )
                }
            }
        }
    }

    private fun openReminderOnboarding() {
        openedFullOnboarding = true
        analytics.recordEvent(REMINDER_ONBOARDING_PROMPT_ACCEPTED)
        val fragmentManager = parentFragmentManager
        dismissNow()
        fragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, ReminderOnboardingFragment())
            .addToBackStack(TAG_REMINDER_ONBOARDING)
            .commit()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        if (!openedFullOnboarding) {
            analytics.recordEvent(REMINDER_ONBOARDING_PROMPT_DISMISSED)
        }
    }

    companion object {
        const val TAG = "ReminderOnboardingPromptBottomSheet"
        private const val TAG_REMINDER_ONBOARDING = "TIMELINE_TO_REMINDER_ONBOARDING"
    }
}

class ReminderPromptThemeSpecProvider @Inject constructor(
    private val settings: journal.gratitude.com.gratitudejournal.settings.PresentlySettings
) {
    fun get(): PresentlyThemeSpec {
        return PresentlyThemeSpec.fromStorageValue(settings.getCurrentTheme())
    }
}

@Composable
private fun ReminderOnboardingPromptSheet(
    onPrimaryAction: () -> Unit
) {
    val tokens = LocalPresentlyTheme.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = tokens.timelineBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.timelineBackground)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            LogoAnimation()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.reminder_prompt_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = PresentlyFontFamilies.accent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp
                    ),
                    color = tokens.timelineHeader,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.reminder_prompt_body),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = PresentlyFontFamilies.body,
                        lineHeight = 24.sp
                    ),
                    color = tokens.timelineBody.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                onClick = onPrimaryAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.timelineHeader,
                    contentColor = tokens.timelineBackground
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.reminder_prompt_cta),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = PresentlyFontFamilies.body,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun LogoAnimation() {
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.logo_animated))
    val progress = animateLottieCompositionAsState(
        composition = composition.value
    )

    LottieAnimation(
        composition = composition.value,
        progress = { progress.value },
        modifier = Modifier.size(120.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ReminderOnboardingPromptSheetPreview() {
    PresentlyTheme {
        ReminderOnboardingPromptSheet(onPrimaryAction = {})
    }
}
