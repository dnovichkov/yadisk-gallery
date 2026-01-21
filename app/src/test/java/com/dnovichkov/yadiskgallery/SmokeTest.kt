package com.dnovichkov.yadiskgallery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Smoke tests to verify that the test infrastructure is working correctly.
 * These tests should always pass if JUnit 5 is properly configured.
 */
@DisplayName("Smoke Tests")
class SmokeTest {
    @Test
    @DisplayName("JUnit 5 is properly configured")
    fun `junit5 should be properly configured`() {
        assertTrue(true, "JUnit 5 should be working")
    }

    @Test
    @DisplayName("Basic assertion works")
    fun `basic assertion should work`() {
        val expected = 4
        val actual = 2 + 2
        assertEquals(expected, actual, "Basic math should work")
    }

    @Test
    @DisplayName("Kotlin features work in tests")
    fun `kotlin features should work in tests`() {
        val list = listOf(1, 2, 3)
        val doubled = list.map { it * 2 }
        assertEquals(listOf(2, 4, 6), doubled)
    }
}
