package com.presently.presently_local_source.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "entriesFts")
@Fts4(contentEntity = EntryEntity::class)
data class FtsEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Int,
    val entryDate: LocalDate,
    val entryContent: String
)