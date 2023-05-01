package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.backups.CsvWriter.createCsvString
import org.junit.Test
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

class CreateCsvStringTest {

    @Test
    fun `GIVEN a set of entries WHEN createCsvString is called THEN a csv is created`() {
        val entries = listOf(
            Entry(
                LocalDate.of(2020, 6, 2),
                "Unit tests are great!",
            ),
            Entry(
                LocalDate.of(2020, 6, 3),
                "Unit tests are great!\n\n\n\nSuper great!",
            ),
            Entry(
                LocalDate.of(2020, 6, 4),
                "Unit tests are the \"bomb\"",
            ),
        )
        val actual = createCsvString(entries)
        val expected = "entryDate,entryContent\n" +
            "2020-06-02,Unit tests are great!\n" +
            "2020-06-03,\"Unit tests are great!\n" +
            "\n" +
            "\n" +
            "\n" +
            "Super great!\"\n" +
            "2020-06-04,\"Unit tests are the \"\"bomb\"\"\"\n"

        assertEquals(expected, actual)
    }
}
