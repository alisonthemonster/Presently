package journal.gratitude.com.gratitudejournal.util.backups

import org.apache.commons.csv.CSVParser

interface CsvParser {
    fun getRecords(): List<CsvRecord>
}

class RealCsvParser(private val parser: CSVParser) : CsvParser {
    override fun getRecords(): List<CsvRecord> {
        val result = mutableListOf<CsvRecord>()
        val records = parser.records
        for (record in records) {
            val columns = mutableListOf<String>()
            for (columnNum in 0 until record.size()) {
                columns.add(record.get(columnNum))
            }
            val csvRecord = CsvRecord(columns.toTypedArray())
            result.add(csvRecord)
        }
        return result
    }
}

data class CsvRecord(val values: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CsvRecord

        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }
}
