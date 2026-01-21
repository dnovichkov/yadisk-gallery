package com.dnovichkov.yadiskgallery.domain.usecase.settings

import com.dnovichkov.yadiskgallery.domain.model.DomainError
import javax.inject.Inject

/**
 * Use case for validating Yandex.Disk public folder URLs.
 */
class ValidatePublicUrlUseCase
    @Inject
    constructor() {
        /**
         * Validates and normalizes a Yandex.Disk public folder URL.
         *
         * @param url The URL to validate
         * @return Result containing normalized URL or validation error
         */
        operator fun invoke(url: String): Result<String> {
            val trimmedUrl = url.trim()

            // Check for empty URL
            if (trimmedUrl.isBlank()) {
                return Result.failure(
                    ValidationException(DomainError.Validation.EmptyField("publicFolderUrl")),
                )
            }

            // Normalize URL
            val normalizedUrl = normalizeUrl(trimmedUrl)

            // Validate URL format
            if (!isValidYandexDiskUrl(normalizedUrl)) {
                return Result.failure(
                    ValidationException(DomainError.Disk.InvalidPublicUrl(trimmedUrl)),
                )
            }

            // Check for potentially malicious content
            if (containsInvalidCharacters(normalizedUrl)) {
                return Result.failure(
                    ValidationException(DomainError.Validation.InvalidUrl(trimmedUrl)),
                )
            }

            return Result.success(normalizedUrl)
        }

        private fun normalizeUrl(url: String): String {
            var normalized = url

            // Add https:// if no protocol
            if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
                normalized = "https://$normalized"
            }

            // Convert http to https
            if (normalized.startsWith("http://")) {
                normalized = normalized.replaceFirst("http://", "https://")
            }

            return normalized
        }

        private fun isValidYandexDiskUrl(url: String): Boolean {
            // Valid Yandex.Disk public folder URL patterns:
            // - https://disk.yandex.ru/d/...
            // - https://disk.yandex.com/d/...
            // - https://yadi.sk/d/...
            val validPatterns =
                listOf(
                    Regex("""^https://disk\.yandex\.ru/d/[a-zA-Z0-9_-]+.*$"""),
                    Regex("""^https://disk\.yandex\.com/d/[a-zA-Z0-9_-]+.*$"""),
                    Regex("""^https://yadi\.sk/d/[a-zA-Z0-9_-]+.*$"""),
                )

            return validPatterns.any { it.matches(url) }
        }

        private fun containsInvalidCharacters(url: String): Boolean {
            // Check for potentially dangerous characters/patterns
            val dangerousPatterns =
                listOf(
                    "<script",
                    "javascript:",
                    "data:",
                    "<img",
                    "onerror",
                    "onclick",
                )

            val lowerUrl = url.lowercase()
            return dangerousPatterns.any { lowerUrl.contains(it) }
        }

        /**
         * Exception wrapper for validation errors.
         */
        class ValidationException(val error: DomainError) : Exception(error.message)
    }
