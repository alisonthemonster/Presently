package journal.gratitude.com.gratitudejournal.util.backups

import org.junit.Assert.*
import org.junit.Test
import java.io.*


class CSVWriterImplTest {

    /**
     * Test routine for converting output to a string.
     *
     * @param args the elements of a line of the cvs file
     * @return a String version
     * @throws IOException if there are problems writing
     */
    @Throws(IOException::class)
    private fun invokeWriter(args: Array<String>): String {
        val sw = StringWriter()
        val csvw = CSVWriterImpl(sw, ',', '\'')
        csvw.writeNext(args)
        return sw.toString()
    }

    @Throws(IOException::class)
    private fun invokeNoEscapeWriter(args: Array<String>): String {
        val sw = StringWriter()
        val csvw = CSVWriterImpl(sw, CSVWriterImpl.DEFAULT_SEPARATOR, '\'', CSVWriterImpl.NO_ESCAPE_CHARACTER)
        csvw.writeNext(args)
        return sw.toString()
    }

    @Test
    fun correctlyParseNullString() {
        val sw = StringWriter()
        val csvw = CSVWriterImpl(sw, ',', '\'')
        csvw.writeNext(null)
        assertEquals(0, sw.toString().length)
    }

    @Test
    fun correctlyParserNullObject() {
        val sw = StringWriter()
        val csvw = CSVWriterImpl(sw, ',', '\'')
        csvw.writeNext(null)
        assertEquals(0, sw.toString().length)
    }

    /**
     * Tests parsing individual lines.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    @Throws(IOException::class)
    fun testParseLine() {

        // test normal case
        val normal = arrayOf("a", "b", "c")
        var output = invokeWriter(normal)
        assertEquals("'a','b','c'\n", output)

        // test quoted commas
        val quoted = arrayOf("a", "b,b,b", "c")
        output = invokeWriter(quoted)
        assertEquals("'a','b,b,b','c'\n", output)

        // test empty elements
        val empty = arrayOf<String>()
        output = invokeWriter(empty)
        assertEquals("\n", output)

        // test multiline quoted
        val multiline = arrayOf("This is a \n multiline entry", "so is \n this")
        output = invokeWriter(multiline)
        assertEquals("'This is a \u0012 multiline entry','so is \u0012 this'\n", output)


        // test quoted line
        val quoteLine = arrayOf("This is a \" multiline entry", "so is \n this")
        output = invokeWriter(quoteLine)
        assertEquals("'This is a \"\" multiline entry','so is \u0012 this'\n", output)

    }

    @Test
    @Throws(IOException::class)
    fun testSpecialCharacters() {
        // test quoted line
        val quoteLine = arrayOf("This is a \r multiline entry", "so is \n this")
        val output = invokeWriter(quoteLine)
        assertEquals("'This is a  multiline entry','so is \u0012 this'\n", output)
    }

    @Test
    @Throws(IOException::class)
    fun parseLineWithBothEscapeAndQuoteChar() {
        // test quoted line
        val quoteLine = arrayOf("This is a 'multiline' entry", "so is \n this")
        val output = invokeWriter(quoteLine)
        assertEquals("'This is a \"'multiline\"' entry','so is \u0012 this'\n", output)
    }

    /**
     * Tests parsing individual lines.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    @Throws(IOException::class)
    fun testParseLineWithNoEscapeChar() {

        // test normal case
        val normal = arrayOf("a", "b", "c")
        var output = invokeNoEscapeWriter(normal)
        assertEquals("'a','b','c'\n", output)

        // test quoted commas
        val quoted = arrayOf("a", "b,b,b", "c")
        output = invokeNoEscapeWriter(quoted)
        assertEquals("'a','b,b,b','c'\n", output)

        // test empty elements
        val empty = arrayOf<String>()
        output = invokeNoEscapeWriter(empty)
        assertEquals("\n", output)

        // test multiline quoted
        val multiline = arrayOf("This is a \n multiline entry", "so is \n this")
        output = invokeNoEscapeWriter(multiline)
        assertEquals("'This is a \u0012 multiline entry','so is \u0012 this'\n", output)

    }

    @Test
    @Throws(IOException::class)
    fun parseLineWithNoEscapeCharAndQuotes() {
        val quoteLine = arrayOf("This is a \" 'multiline' entry", "so is \n this")
        val output = invokeNoEscapeWriter(quoteLine)
        assertEquals("'This is a \" 'multiline' entry','so is \u0012 this'\n", output)
    }

    /**
     * Tests the option of having omitting quotes in the output stream.
     *
     * @throws IOException if bad things happen
     */
    @Test
    @Throws(IOException::class)
    fun testNoQuoteChars() {

        val line = arrayOf("Foo", "Bar", "Baz")
        val sw = StringWriter()
        val csvw = CSVWriterImpl(sw, CSVWriterImpl.DEFAULT_SEPARATOR, CSVWriterImpl.NO_QUOTE_CHARACTER)
        csvw.writeNext(line)
        val result = sw.toString()

        assertEquals("Foo,Bar,Baz\n", result)
    }

    /**
     * Tests the option of having omitting quotes in the output stream.
     *
     * @throws IOException if bad things happen
     */
    @Test
    @Throws(IOException::class)
    fun testNoQuoteCharsAndNoEscapeChars() {

        val line = arrayOf("Foo", "Bar", "Baz")
        val sw = StringWriter()
        val csvw = CSVWriterImpl(
            sw,
            CSVWriterImpl.DEFAULT_SEPARATOR,
            CSVWriterImpl.NO_QUOTE_CHARACTER,
            CSVWriterImpl.NO_ESCAPE_CHARACTER
        )
        csvw.writeNext(line)
        val result = sw.toString()

        assertEquals("Foo,Bar,Baz\n", result)
    }

    /**
     * Test null values.
     *
     * @throws IOException if bad things happen
     */
    @Test
    @Throws(IOException::class)
    fun testEmptyValues() {

        val line = arrayOf<String>("Foo", "", "Bar", "baz")
        val sw = StringWriter()
        val csvw = CSVWriterImpl(sw)
        csvw.writeNext(line)
        val result = sw.toString()

        assertEquals("\"Foo\",\"\",\"Bar\",\"baz\"\n", result)

    }

    @Test
    @Throws(IOException::class)
    fun testStreamFlushing() {

        val WRITE_FILE = "myfile.csv"

        val nextLine = arrayOf("aaaa", "bbbb", "cccc", "dddd")

        val fileWriter = FileWriter(WRITE_FILE)
        val writer = CSVWriterImpl(fileWriter)

        writer.writeNext(nextLine)

        // If this line is not executed, it is not written in the file.
        writer.close()

    }

    @Test
    fun testAlternateEscapeChar() {
        val line = arrayOf("Foo", "bar's")
        val sw = StringWriter()
        val csvw =
            CSVWriterImpl(sw, CSVWriterImpl.DEFAULT_SEPARATOR, CSVWriterImpl.DEFAULT_QUOTE_CHARACTER, '\'')
        csvw.writeNext(line)
        assertEquals("\"Foo\",\"bar''s\"\n", sw.toString())
    }

    @Test
    fun testNoQuotingNoEscaping() {
        val line = arrayOf("\"Foo\",\"Bar\"")
        val sw = StringWriter()
        val csvw = CSVWriterImpl(
            sw,
            CSVWriterImpl.DEFAULT_SEPARATOR,
            CSVWriterImpl.NO_QUOTE_CHARACTER,
            CSVWriterImpl.NO_ESCAPE_CHARACTER
        )
        csvw.writeNext(line)
        assertEquals("\"Foo\",\"Bar\"\n", sw.toString())
    }

    @Test
    fun testNestedQuotes() {
        val data = arrayOf("\"\"", "test")
        val oracle = "\"\"\"\"\"\",\"test\"\n"

        var writer: CSVWriter? = null
        var tempFile: File? = null
        var fwriter: FileWriter? = null

        try {
            tempFile = File.createTempFile("csvWriterTest", ".csv")
            tempFile!!.deleteOnExit()
            fwriter = FileWriter(tempFile)
            writer = CSVWriterImpl(fwriter)
        } catch (e: IOException) {
            fail()
        }

        // write the test data:
        writer!!.writeNext(data)

        try {
            writer.close()
        } catch (e: IOException) {
            fail()
        }

        try {
            // assert that the writer was also closed.
            fwriter!!.flush()
            fail()
        } catch (e: IOException) {
            // we should go through here..
        }

        // read the data and compare.
        var input: FileReader? = null
        try {
            input = FileReader(tempFile)
        } catch (e: FileNotFoundException) {
            fail()
        }

        val fileContents = StringBuilder(CSVWriterImpl.INITIAL_STRING_SIZE)
        try {
            var ch: Int = input!!.read()
            while (ch != -1) {
                fileContents.append(ch.toChar())
                ch = input.read()
            }
            input.close()
        } catch (e: IOException) {
            fail()
        }

        assertTrue(oracle == fileContents.toString())
    }

    @Test
    fun testAlternateLineFeeds() {
        val line = arrayOf("Foo", "Bar", "baz")
        val sw = StringWriter()
        val csvw =
            CSVWriterImpl(sw, CSVWriterImpl.DEFAULT_SEPARATOR, CSVWriterImpl.DEFAULT_QUOTE_CHARACTER, '\n')
        csvw.writeNext(line)
        val result = sw.toString()

        assertTrue(result.endsWith("\n"))
    }
}