package com.dnovichkov.yadiskgallery.data.api

import com.dnovichkov.yadiskgallery.data.api.dto.ErrorDto
import com.dnovichkov.yadiskgallery.domain.model.DomainError
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps API exceptions and errors to domain-level errors.
 */
@Singleton
class ErrorMapper
    @Inject
    constructor(
        private val json: Json,
    ) {
        /**
         * Maps a Throwable to a DomainError.
         */
        fun map(throwable: Throwable): DomainError {
            return when (throwable) {
                is HttpException -> mapHttpException(throwable)
                is SocketTimeoutException -> DomainError.Network.Timeout
                is UnknownHostException -> DomainError.Network.NoConnection
                is IOException -> mapIOException(throwable)
                else ->
                    DomainError.Unknown(
                        message = throwable.message ?: "Unknown error occurred",
                        cause = throwable,
                    )
            }
        }

        /**
         * Maps an HTTP exception to appropriate DomainError.
         */
        private fun mapHttpException(exception: HttpException): DomainError {
            val code = exception.code()
            val errorBody = exception.response()?.errorBody()?.string()

            // Try to parse error body as ErrorDto
            val errorDto = errorBody?.let { parseErrorBody(it) }

            return when (code) {
                401 -> DomainError.Auth.Unauthorized
                403 -> mapForbiddenError(errorDto, errorBody)
                404 -> mapNotFoundError(errorDto, errorBody)
                429 ->
                    DomainError.Network.ServerError(
                        code = code,
                        serverMessage = "Too many requests. Please try again later.",
                    )
                in 500..599 ->
                    DomainError.Network.ServerError(
                        code = code,
                        serverMessage = errorDto?.message ?: "Server error",
                    )
                else ->
                    DomainError.Network.ServerError(
                        code = code,
                        serverMessage = errorDto?.message ?: exception.message(),
                    )
            }
        }

        /**
         * Maps 403 Forbidden errors.
         */
        private fun mapForbiddenError(
            errorDto: ErrorDto?,
            errorBody: String?,
        ): DomainError {
            return when (errorDto?.error) {
                ErrorDto.ERROR_FORBIDDEN ->
                    DomainError.Disk.AccessDenied(
                        path = errorDto.description ?: "unknown",
                    )
                ErrorDto.ERROR_QUOTA_EXCEEDED -> DomainError.Disk.QuotaExceeded
                else -> DomainError.Auth.Unauthorized
            }
        }

        /**
         * Maps 404 Not Found errors.
         */
        private fun mapNotFoundError(
            errorDto: ErrorDto?,
            errorBody: String?,
        ): DomainError {
            return when (errorDto?.error) {
                ErrorDto.ERROR_NOT_FOUND,
                ErrorDto.ERROR_PATH_NOT_FOUND,
                ->
                    DomainError.Disk.NotFound(
                        path = errorDto.description ?: "unknown",
                    )
                else -> DomainError.Disk.NotFound(path = "unknown")
            }
        }

        /**
         * Maps IO exceptions.
         */
        private fun mapIOException(exception: IOException): DomainError {
            return when {
                exception.message?.contains("Unable to resolve host") == true ->
                    DomainError.Network.NoConnection
                exception.message?.contains("timeout") == true ->
                    DomainError.Network.Timeout
                else -> DomainError.Network.Unknown(exception)
            }
        }

        /**
         * Attempts to parse error body as ErrorDto.
         */
        private fun parseErrorBody(body: String): ErrorDto? {
            return try {
                json.decodeFromString<ErrorDto>(body)
            } catch (e: Exception) {
                null
            }
        }
    }

/**
 * Extension function to convert Result failure to DomainError.
 */
fun <T> Result<T>.mapError(errorMapper: ErrorMapper): Result<T> {
    return this.onFailure { throwable ->
        return Result.failure(
            Exception(errorMapper.map(throwable).message, throwable),
        )
    }
}
