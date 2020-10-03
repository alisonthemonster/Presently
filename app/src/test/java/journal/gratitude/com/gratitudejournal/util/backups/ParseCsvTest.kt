package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.convertCsvToEntries
import org.junit.Test
import org.threeten.bp.LocalDate
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParseCsvTest {

    @Test
    fun `GIVEN no records from parser WHENEVER parseCsvString is called THEN exception is thrown`() {
        val parser: CsvParser = object : CsvParser {
            override fun getRecords(): List<CsvRecord> {
                return emptyList()
            }
        }

        assertFailsWith<IOException> {
            convertCsvToEntries(parser)
        }
    }

    @Test
    fun `GIVEN bad header row WHENEVER parseCsvString is called THEN exception is thrown`() {
        val parser: CsvParser = object : CsvParser {
            override fun getRecords(): List<CsvRecord> {
                return listOf(CsvRecord(arrayOf("bad header!")))
            }
        }

        assertFailsWith<IOException> {
            convertCsvToEntries(parser)
        }
    }

    @Test
    fun `GIVEN wrong header row WHENEVER parseCsvString is called THEN exception is thrown`() {
        val parser: CsvParser = object : CsvParser {
            override fun getRecords(): List<CsvRecord> {
                return listOf(CsvRecord(arrayOf("date", "content")))
            }
        }

        assertFailsWith<IOException> {
            convertCsvToEntries(parser)
        }
    }

    @Test
    fun `GIVEN good csv file WHENEVER parseCsvString is called THEN correct Entry list is returned`() {
        val parser: CsvParser = object : CsvParser {
            override fun getRecords(): List<CsvRecord> {
                return listOf(
                    CsvRecord(arrayOf("entryDate", "entryContent")),
                    CsvRecord(arrayOf("2020-06-14", "Unit tests are great!"))
                )
            }
        }

        val expected = listOf(
            Entry(
                LocalDate.of(2020, 6, 14),
                "Unit tests are great!"
            )
        )
        val actual = convertCsvToEntries(parser)

        assertEquals(expected, actual)
    }

    @Test
    fun `GIVEN csv file with extra column WHENEVER parseCsvString is called THEN correct Entry list is returned`() {
        val parser: CsvParser = object : CsvParser {
            override fun getRecords(): List<CsvRecord> {
                return listOf(
                    CsvRecord(arrayOf("entryDate", "entryContent")),
                    CsvRecord(arrayOf("2020-06-14", "Unit tests are great!", "they catch errors!"))
                )
            }
        }

        assertFailsWith<IOException> {
            convertCsvToEntries(parser)
        }
    }

    @Test
    fun `GIVEN csv file with empty content WHENEVER parseCsvString is called THEN correct Entry list is returned`() {
        val parser: CsvParser = object : CsvParser {
            override fun getRecords(): List<CsvRecord> {
                return listOf(
                    CsvRecord(arrayOf("entryDate", "entryContent")),
                    CsvRecord(arrayOf("2020-06-02", "Unit tests are great!")),
                    CsvRecord(arrayOf("2020-06-14", ""))
                )
            }
        }

        val expected = listOf(
            Entry(
                LocalDate.of(2020, 6, 2),
                "Unit tests are great!"
            )
        )
        val actual = convertCsvToEntries(parser)

        assertEquals(expected, actual)
    }

}