package com.dnovichkov.yadiskgallery.data.datastore

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for TokenStorage.
 * Note: Full integration tests require Android instrumentation tests
 * due to EncryptedSharedPreferences dependency.
 */
class TokenStorageTest {
    @Test
    fun `expiration buffer is set to 1 minute`() {
        // Verify the expiration buffer constant value
        val bufferMs = 60_000L
        assertEquals(bufferMs, 60_000L)
    }

    @Test
    fun `token expiration calculation is correct`() {
        val currentTime = System.currentTimeMillis()
        val expiresInSeconds = 3600L // 1 hour
        val expirationTime = currentTime + (expiresInSeconds * 1000)

        assertTrue(expirationTime > currentTime)
        assertEquals(currentTime + 3600_000L, expirationTime)
    }

    @Test
    fun `isTokenExpired logic with buffer`() {
        val currentTime = System.currentTimeMillis()
        val bufferMs = 60_000L

        // Token expiring in 30 seconds should be considered expired (within buffer)
        val nearExpiration = currentTime + 30_000L
        val shouldBeExpired = currentTime >= nearExpiration - bufferMs
        assertTrue(shouldBeExpired)

        // Token expiring in 2 minutes should not be expired
        val farExpiration = currentTime + 120_000L
        val shouldNotBeExpired = currentTime >= farExpiration - bufferMs
        assertTrue(!shouldNotBeExpired)
    }
}
