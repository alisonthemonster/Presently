package com.presently.coverage_example

import org.junit.Test

import org.junit.Assert.*

class MyUnitTest {
    @Test
    fun mathWorks() {
        val actual = ClassToTest().mathIsGreat()
        val expected = 8
        assertEquals(expected, actual)
    }

//    @Test
//    fun wordsWork() {
//        val actual = ClassToTest().wordsAreCool()
//        val expected = "So cool"
//        assertEquals(expected, actual)
//    }
}