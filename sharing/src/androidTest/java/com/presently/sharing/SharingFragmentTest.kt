package com.presently.sharing

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.mocking.mockVariants
import com.airbnb.mvrx.test.MvRxTestRule
import com.facebook.testing.screenshot.Screenshot
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.presently.sharing.view.SharingFragment
import com.presently.sharing.view.SharingFragmentArgs
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//TODO fix screenshot bot integration so that we actually use these screenshots

@RunWith(AndroidJUnit4::class)
class SharingFragmentTest {

    @get:Rule
    val mvrxRule = MvRxTestRule()

    //iterates through the Mavericks mocks defined in SharingFragment.provideMocks
    //and takes screenshots of each
    @Test
    fun testSharingViewStates() {
        val mockBehavior = MockBehavior(
            initialStateMocking = MockBehavior.InitialStateMocking.Full,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
        )
        val variants = mockVariants<SharingFragment>()
        val fragments = variants.map { mockedViewProvider ->
            mockedViewProvider.createView(mockBehavior)
        }
        for ((testNum, fragment) in fragments.withIndex()) {
            launchFragmentInContainer { fragment.viewInstance }
            onView(withId(android.R.id.content)).perform(ScreenshotAction("test--${testNum}"))
        }
    }

    @Test
    fun clickingThemeOptionChangesPreview() {
        //launch fragment
        launchFragmentInContainer<SharingFragment>(
            themeResId = R.style.Base_AppTheme
        )

        //click second item in recycler view
        onView(withId(R.id.design_list))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, ViewActions.click()))

        //screenshot
        onView(withId(android.R.id.content)).perform(ScreenshotAction("sharing--changed-theme"))
    }

    @Test
    fun clickingDoneIconOpensShareSheet() {
        Intents.init()

        //launch fragment
        val bundle = SharingFragmentArgs(
            "content", "May 5th, 2021")

        launchFragmentInContainer<SharingFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = bundle.toBundle()
        )

        val intent = Intent()
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(anyIntent()).respondWith(intentResult)

        //click done button
        onView(withId(R.id.check_mark)).perform(ViewActions.click())

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(Intent.EXTRA_TITLE, "Share your gratitude")
            )
        )
        Intents.release()
    }

    @Test
    fun clickingBackButtonNavigatesBack() {
        val mockNavController = mock<NavController>()

        //launch fragment
        val bundle = SharingFragmentArgs(
            "content", "May 5th, 2021")

        val scenario = launchFragmentInContainer<SharingFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = bundle.toBundle()
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        //click back button
        onView(withId(R.id.back_icon)).perform(ViewActions.click())

        verify(mockNavController).navigateUp()
    }
}

class ScreenshotAction(val name: String) : ViewAction {
    override fun getConstraints(): Matcher<View> = CoreMatchers.any(View::class.java)

    override fun getDescription() = "Record screenshot of View"

    override fun perform(uiController: UiController, view: View) {
        Screenshot.snap(view)
            .setName(name)
            .record();
    }
}

class TestActivity : Activity()