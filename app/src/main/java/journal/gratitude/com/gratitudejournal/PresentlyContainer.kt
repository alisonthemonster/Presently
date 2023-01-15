package journal.gratitude.com.gratitudejournal

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PresentlyContainer() {
    val navController = rememberAnimatedNavController()

    AppNavigation(navController = navController)
}