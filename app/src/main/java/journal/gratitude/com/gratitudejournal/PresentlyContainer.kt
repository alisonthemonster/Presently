package journal.gratitude.com.gratitudejournal

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PresentlyContainer() {
    val navController = rememberAnimatedNavController()

    val viewModel = hiltViewModel<SettingsViewModel>()

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onAppBackgrounded()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (viewModel.shouldAppLock()) {
                    navController.navigate(Screen.Lock.createRoute()) {
                        popUpTo(0) // reset stack
                    }
                }
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    AppNavigation(navController = navController)
}