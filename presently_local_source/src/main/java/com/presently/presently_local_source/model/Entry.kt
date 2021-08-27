package com.presently.presently_local_source.model

import org.threeten.bp.LocalDate

data class Entry(
    val entryDate: LocalDate,
    val entryContent: String
)