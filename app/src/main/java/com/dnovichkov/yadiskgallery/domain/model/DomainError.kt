package com.dnovichkov.yadiskgallery.domain.model

/**
 * Sealed class hierarchy representing all possible domain errors in the application.
 * Provides type-safe error handling without relying on exceptions.
 */
sealed class DomainError {

    /**
     * Human-readable error message.
     */
    abstract val message: String

    /**
     * Optional underlying cause of the error.
     */
    open val cause: Throwable? = null

    /**
     * Network-related errors.
     */
    sealed class Network : DomainError() {

        /**
         * No internet connection available.
         */
        data object NoConnection : Network() {
            override val message: String = "No internet connection"
        }

        /**
         * Request timed out.
         */
        data object Timeout : Network() {
            override val message: String = "Request timed out"
        }

        /**
         * Server returned an error response.
         *
         * @property code HTTP status code
         * @property serverMessage Error message from server
         */
        data class ServerError(val code: Int, val serverMessage: String) : Network() {
            override val message: String = "Server error: $code - $serverMessage"
        }

        /**
         * Unknown network error.
         *
         * @property cause The underlying exception
         */
        data class Unknown(override val cause: Throwable) : Network() {
            override val message: String = "Network error: ${cause.message}"
        }
    }

    /**
     * Authentication-related errors.
     */
    sealed class Auth : DomainError() {

        /**
         * User is not authenticated.
         */
        data object Unauthorized : Auth() {
            override val message: String = "Authentication required"
        }

        /**
         * Access token has expired.
         */
        data object TokenExpired : Auth() {
            override val message: String = "Session expired. Please login again"
        }

        /**
         * Invalid credentials provided.
         */
        data object InvalidCredentials : Auth() {
            override val message: String = "Invalid credentials"
        }

        /**
         * User cancelled the authentication flow.
         */
        data object AuthCancelled : Auth() {
            override val message: String = "Authentication cancelled"
        }
    }

    /**
     * Yandex.Disk specific errors.
     */
    sealed class Disk : DomainError() {

        /**
         * Resource not found on disk.
         *
         * @property path Path to the resource that was not found
         */
        data class NotFound(val path: String) : Disk() {
            override val message: String = "Resource not found: $path"
        }

        /**
         * Access to resource is denied.
         *
         * @property path Path to the resource
         */
        data class AccessDenied(val path: String) : Disk() {
            override val message: String = "Access denied: $path"
        }

        /**
         * Disk storage quota exceeded.
         */
        data object QuotaExceeded : Disk() {
            override val message: String = "Disk quota exceeded"
        }

        /**
         * Invalid public folder URL format.
         *
         * @property url The invalid URL
         */
        data class InvalidPublicUrl(val url: String) : Disk() {
            override val message: String = "Invalid public folder URL: $url"
        }

        /**
         * Public link has expired or been revoked.
         */
        data object PublicLinkExpired : Disk() {
            override val message: String = "Public link has expired"
        }
    }

    /**
     * Local cache errors.
     */
    sealed class Cache : DomainError() {

        /**
         * Failed to read from cache.
         *
         * @property cause The underlying exception
         */
        data class ReadError(override val cause: Throwable) : Cache() {
            override val message: String = "Failed to read from cache: ${cause.message}"
        }

        /**
         * Failed to write to cache.
         *
         * @property cause The underlying exception
         */
        data class WriteError(override val cause: Throwable) : Cache() {
            override val message: String = "Failed to write to cache: ${cause.message}"
        }
    }

    /**
     * Input validation errors.
     */
    sealed class Validation : DomainError() {

        /**
         * Invalid URL format.
         *
         * @property url The invalid URL
         */
        data class InvalidUrl(val url: String) : Validation() {
            override val message: String = "Invalid URL format: $url"
        }

        /**
         * Required field is empty.
         *
         * @property fieldName Name of the empty field
         */
        data class EmptyField(val fieldName: String) : Validation() {
            override val message: String = "Field cannot be empty: $fieldName"
        }
    }

    /**
     * Unknown or unexpected error.
     *
     * @property message Error message
     * @property cause Optional underlying cause
     */
    data class Unknown(
        override val message: String,
        override val cause: Throwable?
    ) : DomainError()
}
