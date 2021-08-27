package com.presently.presently_local_source.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey
    val entryDate: LocalDate,
    val entryContent: String
)