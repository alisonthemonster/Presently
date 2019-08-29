package journal.gratitude.com.gratitudejournal.ui.entry

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.LiveDataTestUtil
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.threeten.bp.LocalDate

class EntryViewModelTest {

    private lateinit var viewModel: EntryViewModel

    private val today = LocalDate.of(2011, 11, 11)
    private val todayString = today.toString()

    private val repository = mock<EntryRepository>()
    private val application = mock<Application>()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()


}
