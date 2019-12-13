package journal.gratitude.com.gratitudejournal.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import journal.gratitude.com.gratitudejournal.R

inline fun <reified F : Fragment> launchFragmentScenario(
    bundle: Bundle?, fragment: F, navController: NavController
): FragmentScenario<F> {
    return launchFragmentInContainer(bundle, R.style.Theme_AppCompat) {
        fragment.also { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { lifeCycleOwner ->
                if (lifeCycleOwner != null) {
                    // The fragment’s view has just been created
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }
    }
}