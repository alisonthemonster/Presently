package com.presently.presently_local_source

import com.google.common.truth.Truth.assertThat
import com.presently.presently_local_source.database.Converters.dateToTimestamp
import com.presently.presently_local_source.database.Converters.fromTimestamp
import org.junit.Test
import org.threeten.bp.LocalDate

class DateConvertersTest {

    @Test
    fun `GIVEN a string date timestamp WHEN fromTimestamp is called then return the LocalDate`() {
        val expected = LocalDate.of(2021, 11, 11)
        val actual = fromTimestamp("2021-11-11")

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN a null date timestamp WHEN fromTimestamp is called then return null`() {
        val actual = fromTimestamp(null)
        assertThat(actual).isNull()
    }

    @Test
    fun `GIVEN a LocalDate WHEN dateToTimestamp is called then return the string date timestamp`() {
        val expected = "2021-11-11"
        val actual = dateToTimestamp(LocalDate.of(2021, 11, 11))

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN a null localdate WHEN dateToTimestamp is called then return null`() {
        val actual = dateToTimestamp(null)
        assertThat(actual).isNull()
    }
}