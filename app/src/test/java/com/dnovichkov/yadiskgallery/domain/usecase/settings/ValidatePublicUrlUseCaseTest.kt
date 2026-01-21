package com.dnovichkov.yadiskgallery.domain.usecase.settings

import com.dnovichkov.yadiskgallery.domain.model.DomainError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ValidatePublicUrlUseCase")
class ValidatePublicUrlUseCaseTest {

    private lateinit var useCase: ValidatePublicUrlUseCase

    @BeforeEach
    fun setUp() {
        useCase = ValidatePublicUrlUseCase()
    }

    @Nested
    @DisplayName("Valid URLs")
    inner class ValidUrlTests {

        @Test
        @DisplayName("should accept disk.yandex.ru/d/ URL")
        fun `should accept disk yandex ru d URL`() {
            val url = "https://disk.yandex.ru/d/abc123XYZ"

            val result = useCase(url)

            assertTrue(result.isSuccess)
            assertEquals(url, result.getOrNull())
        }

        @Test
        @DisplayName("should accept disk.yandex.com/d/ URL")
        fun `should accept disk yandex com d URL`() {
            val url = "https://disk.yandex.com/d/abc123XYZ"

            val result = useCase(url)

            assertTrue(result.isSuccess)
        }

        @Test
        @DisplayName("should accept yadi.sk short URL")
        fun `should accept yadi sk short URL`() {
            val url = "https://yadi.sk/d/abc123XYZ"

            val result = useCase(url)

            assertTrue(result.isSuccess)
        }

        @Test
        @DisplayName("should accept URL without https prefix")
        fun `should accept URL without https prefix`() {
            val url = "disk.yandex.ru/d/abc123XYZ"

            val result = useCase(url)

            assertTrue(result.isSuccess)
            assertEquals("https://disk.yandex.ru/d/abc123XYZ", result.getOrNull())
        }

        @Test
        @DisplayName("should accept URL with http prefix and convert to https")
        fun `should accept URL with http prefix and convert to https`() {
            val url = "http://disk.yandex.ru/d/abc123XYZ"

            val result = useCase(url)

            assertTrue(result.isSuccess)
            assertEquals("https://disk.yandex.ru/d/abc123XYZ", result.getOrNull())
        }

        @Test
        @DisplayName("should trim whitespace from URL")
        fun `should trim whitespace from URL`() {
            val url = "  https://disk.yandex.ru/d/abc123  "

            val result = useCase(url)

            assertTrue(result.isSuccess)
            assertEquals("https://disk.yandex.ru/d/abc123", result.getOrNull())
        }
    }

    @Nested
    @DisplayName("Invalid URLs")
    inner class InvalidUrlTests {

        @Test
        @DisplayName("should reject empty URL")
        fun `should reject empty URL`() {
            val url = ""

            val result = useCase(url)

            assertTrue(result.isFailure)
            val error = (result.exceptionOrNull() as? ValidatePublicUrlUseCase.ValidationException)?.error
            assertTrue(error is DomainError.Validation.EmptyField)
        }

        @Test
        @DisplayName("should reject blank URL")
        fun `should reject blank URL`() {
            val url = "   "

            val result = useCase(url)

            assertTrue(result.isFailure)
            val error = (result.exceptionOrNull() as? ValidatePublicUrlUseCase.ValidationException)?.error
            assertTrue(error is DomainError.Validation.EmptyField)
        }

        @Test
        @DisplayName("should reject non-Yandex URL")
        fun `should reject non-Yandex URL`() {
            val url = "https://google.com/drive/folder123"

            val result = useCase(url)

            assertTrue(result.isFailure)
            val error = (result.exceptionOrNull() as? ValidatePublicUrlUseCase.ValidationException)?.error
            assertTrue(error is DomainError.Disk.InvalidPublicUrl)
        }

        @Test
        @DisplayName("should reject Yandex Disk URL without /d/ path")
        fun `should reject Yandex Disk URL without d path`() {
            val url = "https://disk.yandex.ru/client/disk"

            val result = useCase(url)

            assertTrue(result.isFailure)
            val error = (result.exceptionOrNull() as? ValidatePublicUrlUseCase.ValidationException)?.error
            assertTrue(error is DomainError.Disk.InvalidPublicUrl)
        }

        @Test
        @DisplayName("should reject malformed URL")
        fun `should reject malformed URL`() {
            val url = "not a valid url at all"

            val result = useCase(url)

            assertTrue(result.isFailure)
        }

        @Test
        @DisplayName("should reject URL with invalid characters")
        fun `should reject URL with invalid characters`() {
            val url = "https://disk.yandex.ru/d/<script>alert('xss')</script>"

            val result = useCase(url)

            assertTrue(result.isFailure)
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("should accept URL with query parameters")
        fun `should accept URL with query parameters`() {
            val url = "https://disk.yandex.ru/d/abc123?view=large"

            val result = useCase(url)

            assertTrue(result.isSuccess)
        }

        @Test
        @DisplayName("should accept URL with path after hash")
        fun `should accept URL with path after hash`() {
            val url = "https://disk.yandex.ru/d/abc123/subfolder"

            val result = useCase(url)

            assertTrue(result.isSuccess)
        }
    }
}
