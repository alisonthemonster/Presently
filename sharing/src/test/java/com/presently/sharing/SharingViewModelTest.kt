package com.presently.sharing

import com.airbnb.mvrx.withState
import com.presently.sharing.view.SharingViewModel
import org.junit.Test
import com.airbnb.mvrx.test.MvRxTestRule
import com.presently.logging.AnalyticsLogger
import com.presently.sharing.data.SharingViewDesign
import com.presently.sharing.data.SharingViewState
import org.junit.Assert.*
import org.junit.Rule

class SharingViewModelTest {

    @get:Rule
    val mvrxRule = MvRxTestRule()

    private val analyticsLogger = object : AnalyticsLogger {
        override fun recordEvent(event: String) {}

        override fun recordEvent(event: String, details: Map<String, Any>) {}

        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

        override fun recordEntryAdded(numEntries: Int) {}

        override fun recordView(viewName: String) {}
    }

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
        val viewModel = SharingViewModel(initialState, analyticsLogger)
        viewModel.selectColorScheme(newDesign)

        withState(viewModel) {
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
        val viewModel = SharingViewModel(initialState, analyticsLogger)
        viewModel.clickFinish()

        withState(viewModel) {
            assertTrue(it.clicksShare)
        }
    }

    @Test
    fun `GIVEN a sharing viewmodel WHEN clickFinish is called THEN an analytics event is logged`() {
        var recordEventWasCalled = false
        var eventString = ""
        val analyticsLogger = object : AnalyticsLogger {
            override fun recordEvent(event: String) {
                recordEventWasCalled = true
                eventString = event
            }

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int) {}

            override fun recordView(viewName: String) {}
        }

        val initialState = SharingViewState(
            "content", "March 11, 2021", SharingViewDesign(
                "original", R.color.originalTimelineColor,
                R.color.originalTimelineColor,
                R.color.originalBackgroundColor
            )
        )
        val viewModel = SharingViewModel(initialState, analyticsLogger)
        viewModel.clickFinish()

        assert(recordEventWasCalled)
        assert(eventString == "sharedImage")
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
        val viewModel = SharingViewModel(initialState, analyticsLogger)
        viewModel.sharingComplete()

        withState(viewModel) {
            assertFalse(it.clicksShare)
        }
    }

    @Test
    fun `GIVEN a sharing view model WHEN setContents is called THEN the state changes`() {
        val initialState = SharingViewState(
            "",
            "",
            SharingViewDesign(
                "original", R.color.originalTimelineColor,
                R.color.originalTimelineColor,
                R.color.originalBackgroundColor,
            ),
            clicksShare = true
        )
        val viewModel = SharingViewModel(initialState, analyticsLogger)
        viewModel.setContents("dateString", "content")
        withState(viewModel) {
            assert(it.dateString == "dateString")
            assert(it.content == "content")
        }
    }

    @Test
    fun `GIVEN a sharing view model WHEN setContents is called THEN log an analytics event`() {
        var recordEventWasCalled = false
        var eventString = ""
        val analyticsLogger = object : AnalyticsLogger {
            override fun recordEvent(event: String) {
                recordEventWasCalled = true
                eventString = event
            }

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int) {}

            override fun recordView(viewName: String) {}
        }

        val initialState = SharingViewState(
            "",
            "",
            SharingViewDesign(
                "original", R.color.originalTimelineColor,
                R.color.originalTimelineColor,
                R.color.originalBackgroundColor,
            ),
            clicksShare = true
        )
        val viewModel = SharingViewModel(initialState, analyticsLogger)
        viewModel.setContents("dateString", "content")


        assert(recordEventWasCalled)
        assert(eventString == "viewedShareScreen")
    }
}