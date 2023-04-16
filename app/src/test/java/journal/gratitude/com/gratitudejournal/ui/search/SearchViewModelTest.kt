package journal.gratitude.com.gratitudejournal.ui.search

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.presently.logging.AnalyticsLogger
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchViewModelTest {

    private val repository = mock<EntryRepository>()
    private val settings = mock<PresentlySettings>()
    private val analytics = mock<AnalyticsLogger>()


    @Test
    fun `GIVEN a SearchViewModel WHEN search is called THEN update the state after debouncing`() = runTest {
        val viewModel = SearchViewModel(repository, settings, analytics)
        whenever(repository.search(any())).thenReturn(emptyList())

        viewModel.search("my searchQuery")

        assertThat(viewModel.state.value.query).isEqualTo("my searchQuery")
        verify(repository).search("my searchQuery")
    }

    @Test
    fun `GIVEN a SearchViewModel WHEN getSelectedTheme is called THEN fetch the theme`()  {
        val viewModel = SearchViewModel(repository, settings, analytics)
        val expected = "MyTheme"
        whenever(settings.getCurrentTheme()).thenReturn(expected)

        val actual = viewModel.getSelectedTheme()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN a SearchViewModel WHEN logScreenView is called THEN log an analytics event`()  {
        val viewModel = SearchViewModel(repository, settings, analytics)

        viewModel.logScreenView()

        verify(analytics).recordView("Search")
    }

}