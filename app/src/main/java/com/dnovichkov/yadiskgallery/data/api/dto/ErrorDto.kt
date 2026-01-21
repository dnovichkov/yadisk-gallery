package com.dnovichkov.yadiskgallery.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing an error response from Yandex.Disk API.
 */
@Serializable
data class ErrorDto(
    @SerialName("error")
    val error: String,
    @SerialName("message")
    val message: String,
    @SerialName("description")
    val description: String? = null,
) {
    companion object {
        // Common error codes from Yandex.Disk API
        const val ERROR_UNAUTHORIZED = "UnauthorizedError"
        const val ERROR_NOT_FOUND = "DiskNotFoundError"
        const val ERROR_PATH_NOT_FOUND = "DiskPathDoesntExistsError"
        const val ERROR_FORBIDDEN = "DiskForbiddenError"
        const val ERROR_QUOTA_EXCEEDED = "DiskQuotaExceededError"
        const val ERROR_SERVICE_UNAVAILABLE = "DiskServiceUnavailableError"
        const val ERROR_RESOURCE_ALREADY_EXISTS = "DiskResourceAlreadyExistsError"
        const val ERROR_TOO_MANY_REQUESTS = "TooManyRequestsError"
    }
}
