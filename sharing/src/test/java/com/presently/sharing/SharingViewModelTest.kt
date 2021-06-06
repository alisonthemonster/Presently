package com.presently.sharing

import com.airbnb.mvrx.withState
import com.presently.sharing.view.SharingViewModel
import org.junit.Test
import com.airbnb.mvrx.test.MvRxTestRule
import com.presently.sharing.data.SharingViewDesign
import com.presently.sharing.data.SharingViewState
import org.junit.Assert.*
import org.junit.Rule

class SharingViewModelTest {

    @get:Rule
    val mvrxRule = MvRxTestRule()

    @Test
    fun `GIVEN a sharing viewmodel WHEN a color scheme is selected THEN the state is changed`() {
        val initialState = SharingViewState(
            "content", "March 11, 2021", SharingViewDesign(
                "original", R.color.originalTimelineColor,
                R.color.originalTimelineColor,
                R.color.originalBackgroundColor
            )
        )
        val newDesign = SharingViewDesign(
            "moonlight",
            R.color.moonlightMainTextAndButtonColor,
            R.color.moonlightMainTextAndButtonColor,
            R.color.moonlightBackgroundColor
        )
        val viewModel = SharingViewModel(initialState)
        viewModel.selectColorScheme(newDesign)

        withState (viewModel) {
            assertEquals(it.viewDesign, newDesign)
        }
    }

    @Test
    fun `GIVEN a sharing viewmodel WHEN clickFinish is called THEN the state is updated`() {
        val initialState = SharingViewState(
            "content", "March 11, 2021", SharingViewDesign(
                "original", R.color.originalTimelineColor,
                R.color.originalTimelineColor,
                R.color.originalBackgroundColor
            )
        )
        val viewModel = SharingViewModel(initialState)
        viewModel.clickFinish()

        withState(viewModel) {
            assertTrue(it.clicksShare)
        }
    }

    @Test
    fun `GIVEN a sharing viewmodel WHEN sharingComplete is called THEN the state is updated`() {
        val initialState = SharingViewState(
            "content", "March 11, 2021", SharingViewDesign(
                "original", R.color.originalTimelineColor,
                R.color.originalTimelineColor,
                R.color.originalBackgroundColor,
            ),
            clicksShare = true
        )
        val viewModel = SharingViewModel(initialState)
        viewModel.sharingComplete()

        withState(viewModel) {
            assertFalse(it.clicksShare)
        }
    }
}