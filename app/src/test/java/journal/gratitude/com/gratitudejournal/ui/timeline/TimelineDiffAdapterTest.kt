package journal.gratitude.com.gratitudejournal.ui.timeline

import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineEntry
import org.junit.Test
import org.threeten.bp.LocalDate

class TimelineDiffAdapterTest {

    @Test
    fun `GIVEN two identical timeline items WHEN areItemsTheSame is called THEN return true`() {
        val oldItem = TimelineEntry(LocalDate.now(), "")
        val newItem = oldItem

        val actual = TimelineAdapter.TimelineDiffUtil().areItemsTheSame(oldItem, newItem)
        assertThat(actual).isTrue()
    }

    @Test
    fun `GIVEN two same content timeline items WHEN areContentsTheSame is called THEN return true`() {
        val oldItem = TimelineEntry(LocalDate.now(), "hello")
        val newItem = TimelineEntry(LocalDate.now(), "edited hello!")

        val actual = TimelineAdapter.TimelineDiffUtil().areContentsTheSame(oldItem, newItem)
        assertThat(actual).isTrue()
    }

    @Test
    fun `GIVEN two different timeline items WHEN areContentsTheSame is called THEN return false`() {
        val oldItem = TimelineEntry(LocalDate.now(), "hello")
        val newItem = TimelineEntry(LocalDate.now().minusDays(1), "hello")

        val actual = TimelineAdapter.TimelineDiffUtil().areContentsTheSame(oldItem, newItem)
        assertThat(actual).isFalse()
    }

    @Test
    fun `GIVEN a milestone and a timeline item WHEN areContentsTheSame is called THEN return false`() {
        val oldItem = TimelineEntry(LocalDate.now(), "hello")
        val newItem = Milestone(5, "Five")

        val actual = TimelineAdapter.TimelineDiffUtil().areContentsTheSame(oldItem, newItem)
        assertThat(actual).isFalse()
    }

    @Test
    fun `GIVEN two same content milestone items WHEN areContentsTheSame is called THEN return true`() {
        val oldItem = Milestone(5, "five")
        val newItem = Milestone(5, "Five")

        val actual = TimelineAdapter.TimelineDiffUtil().areContentsTheSame(oldItem, newItem)
        assertThat(actual).isTrue()
    }
}