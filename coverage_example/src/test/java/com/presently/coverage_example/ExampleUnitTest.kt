package com.presently.coverage_example

import org.junit.Test

import org.junit.Assert.*

class MyUnitTest {
    @Test
    fun mathWorks() {
        val actual = KotlinClass().mathIsGreat()
        val expected = 8
        assertEquals(expected, actual)
    }

    @Test
    fun wordsWork() {
        val actual = KotlinClass().wordsAreCool()
        val expected = "So cool"
        assertEquals(expected, actual)
    }

    @Test
    fun blahTest() {
        val actual = KotlinClass().blah()
        val expected = 10
        assertEquals(expected, actual)
    }

    @Test
    fun javaTest() {
        val actual = JavaClass().thisReturnsTrue()
        val expected = true
        assertEquals(expected, actual)
    }
}