package journal.gratitude.com.gratitudejournal.ui

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airbnb.mvrx.asMavericksArgs
import com.presently.testing.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.saveEntriesBlocking
import journal.gratitude.com.gratitudejournal.testUtils.saveEntryBlocking
import journal.gratitude.com.gratitudejournal.ui.entryviewpager.EntryViewPagerArgs
import journal.gratitude.com.gratitudejournal.ui.entryviewpager.EntryViewPagerFragment
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EntryViewPagerFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun entryViewPagerFragment_showsViewPager() {
        val date = LocalDate.now()

        val mockEntry = Entry(date, "test content")
        repository.saveEntryBlocking(mockEntry)

        val args = EntryViewPagerArgs(date)

        launchFragmentInHiltContainer<EntryViewPagerFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.view_pager)).check(matches(isDisplayed()))
    }

    @Test
    fun entryViewPagerFragment_twoEntries_viewPagerShowTwoPages() {
        val today = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)

        val mockEntry1 = Entry(today, "test content")
        val mockEntry2 = Entry(yesterday, "test content")
        repository.saveEntriesBlocking(listOf(mockEntry1, mockEntry2))

        val args = EntryViewPagerArgs(today)

        launchFragmentInHiltContainer<EntryViewPagerFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.date)).check(matches(withText("Today")))
        onView(withId(R.id.thankful_for)).check(matches(withText("I am grateful for")))
    }


    @Test
    fun entryViewPagerFragment_twoEntries_yesterdayDateSelection_viewPagerShowYesterdayPage() {
        val today = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)

        val mockEntry1 = Entry(today, "test content")
        val mockEntry2 = Entry(yesterday, "test content")
        repository.saveEntriesBlocking(listOf(mockEntry1, mockEntry2))

        val args = EntryViewPagerArgs(yesterday)

        launchFragmentInHiltContainer<EntryViewPagerFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.date)).check(matches(withText("Yesterday")))
        onView(withId(R.id.thankful_for)).check(matches(withText("I was grateful for")))
    }


    @Test
    fun entryViewPagerFragment_twoEntries_onPageSwipeRight() {
        val today = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)

        val mockEntry1 = Entry(today, "test content")
        val mockEntry2 = Entry(yesterday, "test content")
        repository.saveEntriesBlocking(listOf(mockEntry1, mockEntry2))

        val args = EntryViewPagerArgs(yesterday)

        launchFragmentInHiltContainer<EntryViewPagerFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withId(R.id.date)).check(matches(withText("Yesterday")))
        onView(withId(R.id.thankful_for)).check(matches(withText("I was grateful for")))

        onView(withId(R.id.view_pager)).perform(ViewActions.swipeRight())

        onView(withText("Today")).check(matches(isDisplayed()))
        onView(withText("I am grateful for")).check(matches(isDisplayed()))
    }

    @Test
    fun entryViewPagerFragment_twoEntries_onPageSwipeLeft() {
        val today = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)

        val mockEntry1 = Entry(today, "test content")
        val mockEntry2 = Entry(yesterday, "test content")
        repository.saveEntriesBlocking(listOf(mockEntry1, mockEntry2))

        val args = EntryViewPagerArgs(today)

        launchFragmentInHiltContainer<EntryViewPagerFragment>(
            themeResId = R.style.Base_AppTheme,
            fragmentArgs = args.asMavericksArgs()
        )

        onView(withText("Today")).check(matches(isDisplayed()))
        onView(withText("I am grateful for")).check(matches(isDisplayed()))

        onView(withId(R.id.view_pager)).perform(ViewActions.swipeLeft())

        onView(withText("Yesterday")).check(matches(withText("Yesterday")))
        onView(withText("I was grateful for")).check(matches(withText("I was grateful for")))

    }

}

