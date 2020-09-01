package journal.gratitude.com.gratitudejournal.util.backups

class CSVReaderTest {

//    lateinit var csvr: CSVReaderImpl
//
//    /**
//     * Setup the test.
//     */
//    @Before
//    @Throws(Exception::class)
//    fun setUp() {
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//        sb.append("a,b,c").append("\n")   // standard case
//        sb.append("a,\"b,b,b\",c").append("\n")  // quoted elements
//        sb.append(",,").append("\n") // empty elements
//        sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n")
//        sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n") // Test quoted quote chars
//        sb.append("\"\"\"\"\"\",\"test\"\n") // """""","test"  representing:  "", test
//        sb.append("\"a\nb\",b,\"\nd\",e\n")
//        csvr = CSVReaderImpl(StringReader(sb.toString()))
//    }
//
//
//    /**
//     * Tests iterating over a reader.
//     *
//     * @throws IOException if the reader fails.
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testParseLine() {
//
//        // test normal case
//        var nextLine = csvr.readNext()
//        assertEquals("a", nextLine!![0])
//        assertEquals("b", nextLine[1])
//        assertEquals("c", nextLine[2])
//
//        // test quoted commas
//        nextLine = csvr.readNext()
//        assertEquals("a", nextLine!![0])
//        assertEquals("b,b,b", nextLine[1])
//        assertEquals("c", nextLine[2])
//
//        // test empty elements
//        nextLine = csvr.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        // test multiline quoted
//        nextLine = csvr.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        // test quoted quote chars
//        nextLine = csvr.readNext()
//        assertEquals("Glen \"The Man\" Smith", nextLine!![0])
//
//        nextLine = csvr.readNext()
//        assertEquals("\"\"", nextLine!![0]) // check the tricky situation
//        assertEquals("test", nextLine[1]) // make sure we didn't ruin the next field..
//
//        nextLine = csvr.readNext()
//        assertEquals(4, nextLine!!.size.toLong())
//
//        //test end of stream
//        assertNull(csvr.readNext())
//
//    }
//
//    @Test
//    @Throws(IOException::class)
//    fun testParseLineStrictQuote() {
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//        sb.append("a,b,c").append("\n")   // standard case
//        sb.append("a,\"b,b,b\",c").append("\n")  // quoted elements
//        sb.append(",,").append("\n") // empty elements
//        sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n")
//        sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n") // Test quoted quote chars
//        sb.append("\"\"\"\"\"\",\"test\"\n") // """""","test"  representing:  "", test
//        sb.append("\"a\nb\",b,\"\nd\",e\n")
//        csvr = CSVReaderImpl(StringReader(sb.toString()), ',', '\"')
//
//        // test normal case
//        var nextLine = csvr.readNext()
//        assertEquals("", nextLine!![0])
//        assertEquals("", nextLine[1])
//        assertEquals("", nextLine[2])
//
//        // test quoted commas
//        nextLine = csvr.readNext()
//        assertEquals("", nextLine!![0])
//        assertEquals("b,b,b", nextLine[1])
//        assertEquals("", nextLine[2])
//
//        // test empty elements
//        nextLine = csvr.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        // test multiline quoted
//        nextLine = csvr.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        // test quoted quote chars
//        nextLine = csvr.readNext()
//        assertEquals("Glen \"The Man\" Smith", nextLine!![0])
//
//        nextLine = csvr.readNext()
//        assertTrue(nextLine!![0] == "\"\"") // check the tricky situation
//        assertTrue(nextLine[1] == "test") // make sure we didn't ruin the next field..
//
//        nextLine = csvr.readNext()
//        assertEquals(4, nextLine!!.size.toLong())
//        assertEquals("a\nb", nextLine[0])
//        assertEquals("", nextLine[1])
//        assertEquals("\nd", nextLine[2])
//        assertEquals("", nextLine[3])
//
//        //test end of stream
//        assertNull(csvr.readNext())
//    }
//
//
//    /**
//     * Tests constructors with optional delimiters and optional quote char.
//     *
//     * @throws IOException if the reader fails.
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testOptionalConstructors() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//        sb.append("a\tb\tc").append("\n")   // tab separated case
//        sb.append("a\t'b\tb\tb'\tc").append("\n")  // single quoted elements
//        val c = CSVReaderImpl(StringReader(sb.toString()), '\t', '\'')
//
//        var nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//    }
//
//    @Test
//    @Throws(IOException::class)
//    fun parseQuotedStringWithDefinedSeperator() {
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//        sb.append("a\tb\tc").append("\n")   // tab separated case
//
//        val c = CSVReaderImpl(StringReader(sb.toString()), '\t')
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//    }
//
//    /**
//     * Tests option to skip the first few lines of a file.
//     *
//     * @throws IOException if bad things happen
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testSkippingLines() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//        sb.append("Skip this line\t with tab").append("\n")   // should skip this
//        sb.append("And this line too").append("\n")   // and this
//        sb.append("a\t'b\tb\tb'\tc").append("\n")  // single quoted elements
//        val c = CSVReaderImpl(StringReader(sb.toString()), '\t', '\'', 2)
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine[0])
//    }
//
//
//    /**
//     * Tests option to skip the first few lines of a file.
//     *
//     * @throws IOException if bad things happen
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testSkippingLinesWithDifferentEscape() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//        sb.append("Skip this line?t with tab").append("\n")   // should skip this
//        sb.append("And this line too").append("\n")   // and this
//        sb.append("a\t'b\tb\tb'\t'c'").append("\n")  // single quoted elements
//        val c = CSVReaderImpl(StringReader(sb.toString()), '\t', '\'', 2)
//
//        val nextLine = c.readNext()
//
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine!![0])
//        assertEquals("c", nextLine!![2])
//    }
//
//    /**
//     * Test a normal non quoted line with three elements
//     *
//     * @throws IOException
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testNormalParsedLine() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("a,1234567,c").append("\n")// a,1234,c
//
//        val c = CSVReaderImpl(StringReader(sb.toString()))
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine[0])
//        assertEquals("1234567", nextLine[1])
//        assertEquals("c", nextLine[2])
//
//    }
//
//
//    /**
//     * Same as testADoubleQuoteAsDataElement but I changed the quotechar to a
//     * single quote.
//     *
//     * @throws IOException
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testASingleQuoteAsDataElement() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("a,'''',c").append("\n")// a,',c
//
//        val c = CSVReaderImpl(StringReader(sb.toString()), ',', '\'')
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine[0])
//        assertEquals(1, nextLine[1].length.toLong())
//        assertEquals("\'", nextLine[1])
//        assertEquals("c", nextLine[2])
//
//    }
//
//    /**
//     * Same as testADoubleQuoteAsDataElement but I changed the quotechar to a
//     * single quote.  Also the middle field is empty.
//     *
//     * @throws IOException
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testASingleQuoteAsDataElementWithEmptyField() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("a,'',c").append("\n")// a,,c
//
//        val c = CSVReaderImpl(StringReader(sb.toString()), ',', '\'')
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine!![0])
//        assertEquals(0, nextLine!![1].length.toLong())
//        assertEquals("", nextLine!![1])
//        assertEquals("c", nextLine!![2])
//
//    }
//
//    @Test
//    @Throws(IOException::class)
//    fun testSpacesAtEndOfString() {
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("\"a\",\"b\",\"c\"   ")
//
//        val c = CSVReaderImpl(
//            StringReader(sb.toString()),
//            CSVReaderImpl.DEFAULT_SEPARATOR,
//            CSVReaderImpl.DEFAULT_QUOTE_CHARACTER)
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine[0])
//        assertEquals("b", nextLine[1])
//        assertEquals("c", nextLine[2])
//    }
//
//
//    @Test
//    @Throws(IOException::class)
//    fun testEscapedQuote() {
//
//        val sb = StringBuffer()
//
//        sb.append("a,\"123\\\"4567\",c").append("\n")// a,123"4",c
//
//        val c = CSVReaderImpl(StringReader(sb.toString()))
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("123\"4567", nextLine[1])
//
//    }
//
//    @Test
//    @Throws(IOException::class)
//    fun testEscapedEscape() {
//
//        val sb = StringBuffer()
//
//        sb.append("a,\"123\\\\4567\",c").append("\n")// a,123"4",c
//
//        val c = CSVReaderImpl(StringReader(sb.toString()))
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("123\\4567", nextLine[1])
//
//    }
//
//
//    /**
//     * Test a line where one of the elements is two single quotes and the
//     * quote character is the default double quote.  The expected result is two
//     * single quotes.
//     *
//     * @throws IOException
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testSingleQuoteWhenDoubleQuoteIsQuoteChar() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("a,'',c").append("\n")// a,'',c
//
//        val c = CSVReaderImpl(StringReader(sb.toString()))
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine!![0])
//        assertEquals(2, nextLine!![1].length.toLong())
//        assertEquals("''", nextLine!![1])
//        assertEquals("c", nextLine!![2])
//
//    }
//
//    /**
//     * Test a normal line with three elements and all elements are quoted
//     *
//     * @throws IOException
//     */
//    @Test
//    @Throws(IOException::class)
//    fun testQuotedParsedLine() {
//
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("\"a\",\"1234567\",\"c\"").append("\n") // "a","1234567","c"
//
//        val c = CSVReaderImpl(
//            StringReader(sb.toString()),
//            CSVReaderImpl.DEFAULT_SEPARATOR,
//            CSVReaderImpl.DEFAULT_QUOTE_CHARACTER)
//
//        val nextLine = c.readNext()
//        assertEquals(3, nextLine!!.size.toLong())
//
//        assertEquals("a", nextLine[0])
//        assertEquals(1, nextLine[0].length.toLong())
//
//        assertEquals("1234567", nextLine[1])
//        assertEquals("c", nextLine[2])
//
//    }
//
//    @Test
//    @Throws(IOException::class)
//    fun testIssue2992134OutOfPlaceQuotes() {
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"")
//
//        val c = CSVReaderImpl(StringReader(sb.toString()))
//
//        val nextLine = c.readNext()
//
//        assertEquals("a", nextLine!![0])
//        assertEquals("b", nextLine[1])
//        assertEquals("c", nextLine[2])
//        assertEquals("ddd\"eee", nextLine[3])
//    }
//
//    @Test(expected = UnsupportedOperationException::class)
//    fun quoteAndEscapeMustBeDifferent() {
//        val sb = StringBuilder(CSVReaderImpl.INITIAL_READ_SIZE)
//
//        sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"")
//
//        val c = CSVReaderImpl(
//            StringReader(sb.toString()),
//            CSVReaderImpl.DEFAULT_SEPARATOR,
//            CSVReaderImpl.DEFAULT_QUOTE_CHARACTER,
//            CSVReaderImpl.DEFAULT_SKIP_LINES
//        )
//    }
//
//
//    @Test
//    @Throws(IOException::class)
//    fun canCloseReader() {
//        csvr.close()
//    }

}