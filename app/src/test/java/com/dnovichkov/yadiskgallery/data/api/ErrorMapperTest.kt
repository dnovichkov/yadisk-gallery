package com.dnovichkov.yadiskgallery.data.api

import com.dnovichkov.yadiskgallery.domain.model.DomainError
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {
    private lateinit var json: Json
    private lateinit var errorMapper: ErrorMapper

    @BeforeEach
    fun setup() {
        json = Json { ignoreUnknownKeys = true }
        errorMapper = ErrorMapper(json)
    }

    @Nested
    @DisplayName("Network Errors")
    inner class NetworkErrorsTests {
        @Test
        fun `map SocketTimeoutException to Timeout error`() {
            val error = SocketTimeoutException("Connection timed out")
            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.Timeout)
        }

        @Test
        fun `map UnknownHostException to NoConnection error`() {
            val error = UnknownHostException("Unable to resolve host")
            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.NoConnection)
        }

        @Test
        fun `map IOException with timeout message to Timeout error`() {
            val error = IOException("Socket timeout")
            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.Timeout)
        }

        @Test
        fun `map IOException with resolve host message to NoConnection error`() {
            val error = IOException("Unable to resolve host")
            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.NoConnection)
        }

        @Test
        fun `map generic IOException to Unknown network error`() {
            val error = IOException("Some IO error")
            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.Unknown)
        }
    }

    @Nested
    @DisplayName("HTTP Errors")
    inner class HttpErrorsTests {
        @Test
        fun `map 401 Unauthorized to Auth Unauthorized error`() {
            val response =
                Response.error<Any>(
                    401,
                    "Unauthorized".toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Auth.Unauthorized)
        }

        @Test
        fun `map 403 Forbidden to Auth Unauthorized error by default`() {
            val response =
                Response.error<Any>(
                    403,
                    "Forbidden".toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Auth.Unauthorized)
        }

        @Test
        fun `map 404 Not Found to Disk NotFound error`() {
            val response =
                Response.error<Any>(
                    404,
                    "Not Found".toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Disk.NotFound)
        }

        @Test
        fun `map 429 Too Many Requests to ServerError`() {
            val response =
                Response.error<Any>(
                    429,
                    "Too Many Requests".toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.ServerError)
            assertEquals(429, (result as DomainError.Network.ServerError).code)
        }

        @Test
        fun `map 500 Server Error to ServerError`() {
            val response =
                Response.error<Any>(
                    500,
                    "Internal Server Error".toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.ServerError)
            assertEquals(500, (result as DomainError.Network.ServerError).code)
        }

        @Test
        fun `map 503 Service Unavailable to ServerError`() {
            val response =
                Response.error<Any>(
                    503,
                    "Service Unavailable".toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Network.ServerError)
            assertEquals(503, (result as DomainError.Network.ServerError).code)
        }
    }

    @Nested
    @DisplayName("HTTP Errors with JSON body")
    inner class HttpErrorsWithJsonBodyTests {
        @Test
        fun `map 404 with DiskNotFoundError JSON to NotFound error`() {
            val errorBody =
                """{"error":"DiskNotFoundError","message":"Resource not found","description":"/path/to/file"}"""
            val response =
                Response.error<Any>(
                    404,
                    errorBody.toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Disk.NotFound)
        }

        @Test
        fun `map 403 with DiskQuotaExceededError JSON to QuotaExceeded error`() {
            val errorBody =
                buildString {
                    append("""{"error":"DiskQuotaExceededError",""")
                    append(""""message":"Quota exceeded","description":"Storage limit reached"}""")
                }
            val response =
                Response.error<Any>(
                    403,
                    errorBody.toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Disk.QuotaExceeded)
        }

        @Test
        fun `map 403 with DiskForbiddenError JSON to AccessDenied error`() {
            val errorBody =
                """{"error":"DiskForbiddenError","message":"Access denied","description":"/private/folder"}"""
            val response =
                Response.error<Any>(
                    403,
                    errorBody.toResponseBody(null),
                )
            val error = HttpException(response)

            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Disk.AccessDenied)
        }
    }

    @Nested
    @DisplayName("Unknown Errors")
    inner class UnknownErrorsTests {
        @Test
        fun `map RuntimeException to Unknown error`() {
            val error = RuntimeException("Something went wrong")
            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Unknown)
            assertEquals("Something went wrong", result.message)
        }

        @Test
        fun `map exception with null message to Unknown error with default message`() {
            val error = RuntimeException()
            val result = errorMapper.map(error)

            assertTrue(result is DomainError.Unknown)
            assertEquals("Unknown error occurred", result.message)
        }
    }
}
