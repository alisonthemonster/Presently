package journal.gratitude.com.gratitudejournal.util.backups

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.junit.Test
import kotlin.test.assertEquals

class RealCsvParserTest {

    @Test
    fun `GIVEN apache CSVParser WHEN getRecords is called THEN the correctly parsed records are returned`() {
        val csvString =
            "a,b,c,d\n a,b,c,d\na , b , 1 2 \n"
        val apacheParser = CSVParser.parse(csvString, CSVFormat.DEFAULT)
        val realCsvParser = RealCsvParser(apacheParser)

        val actual = realCsvParser.getRecords()
        val expected = listOf(
            CsvRecord(
                arrayOf("a", "b", "c", "d")
            ),
            CsvRecord(
                arrayOf(" a", "b", "c", "d")
            ),
            CsvRecord(
                arrayOf("a ", " b ", " 1 2 ")
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `GIVEN apache CSVParser AND exported gratitude entries with escaped chars WHEN getRecords is called THEN the correctly parsed records are returned`() {
        val csvString =
            "entryDate,entryContent\n" +
                "2020-06-02,Unit tests are great!\n" +
                "2020-06-03,\"Unit tests are great!\n" +
                "\n" +
                "\n" +
                "\n" +
                "Super great!\"\n" +
                "2020-06-04,\"Unit tests are the \"\"bomb\"\"\"\n"
        val apacheParser = CSVParser.parse(csvString, CSVFormat.DEFAULT)
        val realCsvParser = RealCsvParser(apacheParser)

        val actual = realCsvParser.getRecords()
        val expected = listOf(
            CsvRecord(
                arrayOf("entryDate", "entryContent")
            ),
            CsvRecord(
                arrayOf("2020-06-02", "Unit tests are great!")
            ),
            CsvRecord(
                arrayOf("2020-06-03", "Unit tests are great!\n\n\n\nSuper great!")
            ),
            CsvRecord(
                arrayOf("2020-06-04", "Unit tests are the \"bomb\"")
            )
        )

        assertEquals(expected, actual)
    }
}
