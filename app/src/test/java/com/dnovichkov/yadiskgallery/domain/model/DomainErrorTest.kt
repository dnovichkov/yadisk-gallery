package com.dnovichkov.yadiskgallery.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException

@DisplayName("DomainError")
class DomainErrorTest {
    @Nested
    @DisplayName("Network errors")
    inner class NetworkErrors {
        @Test
        @DisplayName("should create NoConnection error")
        fun `should create NoConnection error`() {
            val error = DomainError.Network.NoConnection

            assertEquals("No internet connection", error.message)
            assertNull(error.cause)
        }

        @Test
        @DisplayName("should create Timeout error")
        fun `should create Timeout error`() {
            val error = DomainError.Network.Timeout

            assertEquals("Request timed out", error.message)
        }

        @Test
        @DisplayName("should create ServerError with code")
        fun `should create ServerError with code`() {
            val error = DomainError.Network.ServerError(code = 500, serverMessage = "Internal Server Error")

            assertEquals(500, error.code)
            assertEquals("Internal Server Error", error.serverMessage)
            assertEquals("Server error: 500 - Internal Server Error", error.message)
        }

        @Test
        @DisplayName("should create Unknown network error with cause")
        fun `should create Unknown network error with cause`() {
            val cause = IOException("Connection reset")
            val error = DomainError.Network.Unknown(cause)

            assertEquals(cause, error.cause)
            assertEquals("Network error: Connection reset", error.message)
        }
    }

    @Nested
    @DisplayName("Auth errors")
    inner class AuthErrors {
        @Test
        @DisplayName("should create Unauthorized error")
        fun `should create Unauthorized error`() {
            val error = DomainError.Auth.Unauthorized

            assertEquals("Authentication required", error.message)
        }

        @Test
        @DisplayName("should create TokenExpired error")
        fun `should create TokenExpired error`() {
            val error = DomainError.Auth.TokenExpired

            assertEquals("Session expired. Please login again", error.message)
        }

        @Test
        @DisplayName("should create InvalidCredentials error")
        fun `should create InvalidCredentials error`() {
            val error = DomainError.Auth.InvalidCredentials

            assertEquals("Invalid credentials", error.message)
        }

        @Test
        @DisplayName("should create AuthCancelled error")
        fun `should create AuthCancelled error`() {
            val error = DomainError.Auth.AuthCancelled

            assertEquals("Authentication cancelled", error.message)
        }
    }

    @Nested
    @DisplayName("Disk errors")
    inner class DiskErrors {
        @Test
        @DisplayName("should create NotFound error")
        fun `should create NotFound error`() {
            val error = DomainError.Disk.NotFound(path = "/photos/deleted.jpg")

            assertEquals("/photos/deleted.jpg", error.path)
            assertEquals("Resource not found: /photos/deleted.jpg", error.message)
        }

        @Test
        @DisplayName("should create AccessDenied error")
        fun `should create AccessDenied error`() {
            val error = DomainError.Disk.AccessDenied(path = "/private/secret.txt")

            assertEquals("/private/secret.txt", error.path)
            assertEquals("Access denied: /private/secret.txt", error.message)
        }

        @Test
        @DisplayName("should create QuotaExceeded error")
        fun `should create QuotaExceeded error`() {
            val error = DomainError.Disk.QuotaExceeded

            assertEquals("Disk quota exceeded", error.message)
        }

        @Test
        @DisplayName("should create InvalidPublicUrl error")
        fun `should create InvalidPublicUrl error`() {
            val error = DomainError.Disk.InvalidPublicUrl(url = "invalid-url")

            assertEquals("invalid-url", error.url)
            assertEquals("Invalid public folder URL: invalid-url", error.message)
        }

        @Test
        @DisplayName("should create PublicLinkExpired error")
        fun `should create PublicLinkExpired error`() {
            val error = DomainError.Disk.PublicLinkExpired

            assertEquals("Public link has expired", error.message)
        }
    }

    @Nested
    @DisplayName("Cache errors")
    inner class CacheErrors {
        @Test
        @DisplayName("should create ReadError")
        fun `should create ReadError`() {
            val cause = IOException("Disk read error")
            val error = DomainError.Cache.ReadError(cause)

            assertEquals(cause, error.cause)
            assertEquals("Failed to read from cache: Disk read error", error.message)
        }

        @Test
        @DisplayName("should create WriteError")
        fun `should create WriteError`() {
            val cause = IOException("Disk full")
            val error = DomainError.Cache.WriteError(cause)

            assertEquals(cause, error.cause)
            assertEquals("Failed to write to cache: Disk full", error.message)
        }
    }

    @Nested
    @DisplayName("Validation errors")
    inner class ValidationErrors {
        @Test
        @DisplayName("should create InvalidUrl error")
        fun `should create InvalidUrl error`() {
            val error = DomainError.Validation.InvalidUrl(url = "not-a-url")

            assertEquals("not-a-url", error.url)
            assertEquals("Invalid URL format: not-a-url", error.message)
        }

        @Test
        @DisplayName("should create EmptyField error")
        fun `should create EmptyField error`() {
            val error = DomainError.Validation.EmptyField(fieldName = "publicUrl")

            assertEquals("publicUrl", error.fieldName)
            assertEquals("Field cannot be empty: publicUrl", error.message)
        }
    }

    @Nested
    @DisplayName("Unknown error")
    inner class UnknownErrorTests {
        @Test
        @DisplayName("should create Unknown error with message and cause")
        fun `should create Unknown error with message and cause`() {
            val cause = RuntimeException("Unexpected error")
            val error = DomainError.Unknown(message = "Something went wrong", cause = cause)

            assertEquals("Something went wrong", error.message)
            assertEquals(cause, error.cause)
        }

        @Test
        @DisplayName("should create Unknown error without cause")
        fun `should create Unknown error without cause`() {
            val error = DomainError.Unknown(message = "Unknown error occurred", cause = null)

            assertEquals("Unknown error occurred", error.message)
            assertNull(error.cause)
        }
    }

    @Nested
    @DisplayName("Polymorphism")
    inner class PolymorphismTests {
        @Test
        @DisplayName("should handle all error types with when expression")
        fun `should handle all error types with when expression`() {
            val errors: List<DomainError> =
                listOf(
                    DomainError.Network.NoConnection,
                    DomainError.Auth.Unauthorized,
                    DomainError.Disk.NotFound("/path"),
                    DomainError.Cache.ReadError(IOException("error")),
                    DomainError.Validation.EmptyField("field"),
                    DomainError.Unknown("error", null),
                )

            val categories =
                errors.map { error ->
                    when (error) {
                        is DomainError.Network -> "network"
                        is DomainError.Auth -> "auth"
                        is DomainError.Disk -> "disk"
                        is DomainError.Cache -> "cache"
                        is DomainError.Validation -> "validation"
                        is DomainError.Unknown -> "unknown"
                    }
                }

            assertEquals(listOf("network", "auth", "disk", "cache", "validation", "unknown"), categories)
        }

        @Test
        @DisplayName("should access common message property")
        fun `should access common message property`() {
            val errors: List<DomainError> =
                listOf(
                    DomainError.Network.NoConnection,
                    DomainError.Auth.Unauthorized,
                    DomainError.Disk.QuotaExceeded,
                )

            assertTrue(errors.all { it.message.isNotBlank() })
        }
    }
}
