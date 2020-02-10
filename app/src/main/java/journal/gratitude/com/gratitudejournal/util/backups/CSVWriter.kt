package journal.gratitude.com.gratitudejournal.util.backups

import java.io.IOException
import java.io.PrintWriter
import java.io.Writer

interface CSVWriter {
    fun writeNext(nextLine: Array<String>?)

    fun flush()

    fun close()
}

/**
 * Constructs CSVWriter with supplied separator, quote char, escape char and line ending.
 *
 * @param writer
 * the writer to an underlying CSV source.
 * @param separator
 * the delimiter to use for separating entries
 * @param quotechar
 * the character to use for quoted elements
 * @param escapechar
 * the character to use for escaping quotechars or escapechars
 * @param lineEnd
 * the line feed terminator to use
 */
class CSVWriterImpl(writer: Writer,
                    private val separator: Char = DEFAULT_SEPARATOR,
                    private val quotechar: Char = DEFAULT_QUOTE_CHARACTER,
                    private val escapechar: Char = DEFAULT_ESCAPE_CHARACTER,
                    private val lineEnd: String = DEFAULT_LINE_END
): CSVWriter {

    private val pw: PrintWriter = PrintWriter(writer)


    /**
     * Writes the next line to the file.
     *
     * @param nextLine
     * a string array with each comma-separated element as a separate
     * entry.
     */
    override fun writeNext(nextLine: Array<String>?) {
        if (nextLine == null)
            return

        val sb = StringBuffer()
        for (i in nextLine.indices) {

            if (i != 0) {
                sb.append(separator)
            }

            val nextElement = nextLine[i]
            if (quotechar != NO_QUOTE_CHARACTER)
                sb.append(quotechar)
            for (element in nextElement) {
                if (escapechar != NO_ESCAPE_CHARACTER && element == quotechar) {
                    sb.append(escapechar).append(element)
                } else if (escapechar != NO_ESCAPE_CHARACTER && element == escapechar) {
                    sb.append(escapechar).append(element)
                } else if (element == '\n') {
                    sb.append('\u0012')
                } else if ( element == '\r'){
                    //skip adding this character
                }
                else {
                    sb.append(element)
                }
            }
            if (quotechar != NO_QUOTE_CHARACTER)
                sb.append(quotechar)
        }

        sb.append(lineEnd)
        pw.write(sb.toString())
    }

    /**
     * Flush underlying stream to writer.
     *
     * @throws IOException if bad things happen
     */
    @Throws(IOException::class)
    override fun flush() {
        pw.flush()
    }

    /**
     * Close the underlying stream writer flushing any buffered content.
     *
     * @throws IOException if bad things happen
     */
    @Throws(IOException::class)
    override fun close() {
        pw.flush()
        pw.close()
    }

    companion object {

        /** The character used for escaping quotes.  */
        val DEFAULT_ESCAPE_CHARACTER = '"'

        /** The default separator to use if none is supplied to the constructor.  */
        val DEFAULT_SEPARATOR = ','

        /**
         * The default quote character to use if none is supplied to the
         * constructor.
         */
        val DEFAULT_QUOTE_CHARACTER = '"'

        /** The quote constant to use when you wish to suppress all quoting.  */
        val NO_QUOTE_CHARACTER = '\u0000'

        /** The escape constant to use when you wish to suppress all escaping.  */
        val NO_ESCAPE_CHARACTER = '\u0000'

        /** Default line terminator uses platform encoding.  */
        val DEFAULT_LINE_END = "\n"
    }

}
