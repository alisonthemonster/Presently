package journal.gratitude.com.gratitudejournal.widget

internal fun parseQuote(raw: String): Pair<String, String> {
    val idx = raw.lastIndexOf('\n')
    return if (idx >= 0) {
        raw.substring(0, idx).trim() to raw.substring(idx + 1).trim()
    } else {
        raw.trim() to ""
    }
}

internal fun selectQuoteIndex(dayOfYear: Int, total: Int): Int {
    require(total > 0) { "total must be positive" }
    return ((dayOfYear % total) + total) % total
}
