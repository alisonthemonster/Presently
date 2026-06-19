package journal.gratitude.com.gratitudejournal.widget

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuoteParsingTest {

    @Test
    fun `GIVEN quote with author on last line WHEN parseQuote THEN splits on last newline`() {
        val (quote, author) = parseQuote("\"Be grateful.\"\nMaya Angelou")
        assertThat(quote).isEqualTo("\"Be grateful.\"")
        assertThat(author).isEqualTo("Maya Angelou")
    }

    @Test
    fun `GIVEN multi-line quote WHEN parseQuote THEN only last newline separates author`() {
        val raw = "\"Line one\nLine two\nLine three\"\nSome Author"
        val (quote, author) = parseQuote(raw)
        assertThat(quote).isEqualTo("\"Line one\nLine two\nLine three\"")
        assertThat(author).isEqualTo("Some Author")
    }

    @Test
    fun `GIVEN quote with no newline WHEN parseQuote THEN author is empty`() {
        val (quote, author) = parseQuote("Just a quote with no author")
        assertThat(quote).isEqualTo("Just a quote with no author")
        assertThat(author).isEmpty()
    }

    @Test
    fun `GIVEN quote with surrounding whitespace WHEN parseQuote THEN trims both parts`() {
        val (quote, author) = parseQuote("  \"Quote text\"  \n  Author Name  ")
        assertThat(quote).isEqualTo("\"Quote text\"")
        assertThat(author).isEqualTo("Author Name")
    }

    @Test
    fun `GIVEN empty string WHEN parseQuote THEN both parts empty`() {
        val (quote, author) = parseQuote("")
        assertThat(quote).isEmpty()
        assertThat(author).isEmpty()
    }

    @Test
    fun `GIVEN dayOfYear within range WHEN selectQuoteIndex THEN returns modulo`() {
        assertThat(selectQuoteIndex(dayOfYear = 0, total = 100)).isEqualTo(0)
        assertThat(selectQuoteIndex(dayOfYear = 42, total = 100)).isEqualTo(42)
        assertThat(selectQuoteIndex(dayOfYear = 99, total = 100)).isEqualTo(99)
    }

    @Test
    fun `GIVEN dayOfYear larger than total WHEN selectQuoteIndex THEN wraps around`() {
        assertThat(selectQuoteIndex(dayOfYear = 100, total = 100)).isEqualTo(0)
        assertThat(selectQuoteIndex(dayOfYear = 365, total = 100)).isEqualTo(65)
    }

    @Test
    fun `GIVEN total of one quote WHEN selectQuoteIndex THEN always returns zero`() {
        assertThat(selectQuoteIndex(dayOfYear = 0, total = 1)).isEqualTo(0)
        assertThat(selectQuoteIndex(dayOfYear = 365, total = 1)).isEqualTo(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN total of zero WHEN selectQuoteIndex THEN throws`() {
        selectQuoteIndex(dayOfYear = 5, total = 0)
    }
}
