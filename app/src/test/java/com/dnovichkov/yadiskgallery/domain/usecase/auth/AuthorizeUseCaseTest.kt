package com.dnovichkov.yadiskgallery.domain.usecase.auth

import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("AuthorizeUseCase")
class AuthorizeUseCaseTest {

    private lateinit var useCase: AuthorizeUseCase
    private lateinit var repository: FakeAuthRepository

    @BeforeEach
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = AuthorizeUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {

        @Test
        @DisplayName("should initiate authentication")
        fun `should initiate authentication`() = runTest {
            val result = useCase()

            assertTrue(result.isSuccess)
            assertTrue(repository.authenticateCalled)
        }

        @Test
        @DisplayName("should propagate authentication errors")
        fun `should propagate authentication errors`() = runTest {
            repository.authenticateError = RuntimeException("Auth failed")

            val result = useCase()

            assertTrue(result.isFailure)
            assertEquals("Auth failed", result.exceptionOrNull()?.message)
        }
    }

    @Nested
    @DisplayName("handleCallback()")
    inner class HandleCallbackTests {

        @Test
        @DisplayName("should handle auth callback successfully")
        fun `should handle auth callback successfully`() = runTest {
            val expectedUser = UserInfo(
                uid = "123",
                login = "user@yandex.ru",
                displayName = "Test User",
                avatarUrl = "https://avatar.url"
            )
            repository.callbackUserInfo = expectedUser

            val result = useCase.handleCallback("auth_code_123")

            assertTrue(result.isSuccess)
            assertEquals(expectedUser, result.getOrNull())
            assertEquals("auth_code_123", repository.lastAuthCode)
        }

        @Test
        @DisplayName("should propagate callback errors")
        fun `should propagate callback errors`() = runTest {
            repository.callbackError = RuntimeException("Invalid code")

            val result = useCase.handleCallback("invalid_code")

            assertTrue(result.isFailure)
        }
    }

    @Nested
    @DisplayName("setPublicAccess()")
    inner class SetPublicAccessTests {

        @Test
        @DisplayName("should set public access mode")
        fun `should set public access mode`() = runTest {
            val url = "https://disk.yandex.ru/d/abc123"

            val result = useCase.setPublicAccess(url)

            assertTrue(result.isSuccess)
            assertEquals(url, repository.lastPublicUrl)
        }

        @Test
        @DisplayName("should propagate errors")
        fun `should propagate errors`() = runTest {
            repository.publicAccessError = RuntimeException("Invalid URL")

            val result = useCase.setPublicAccess("invalid")

            assertTrue(result.isFailure)
        }
    }

    private class FakeAuthRepository : IAuthRepository {
        var authenticateCalled = false
        var authenticateError: Throwable? = null
        var callbackUserInfo: UserInfo? = null
        var callbackError: Throwable? = null
        var lastAuthCode: String? = null
        var lastPublicUrl: String? = null
        var publicAccessError: Throwable? = null

        override fun observeAuthState(): Flow<AuthState> = flowOf(AuthState.NotAuthenticated)
        override suspend fun getAuthState(): AuthState = AuthState.NotAuthenticated

        override suspend fun authenticate(): Result<Unit> {
            authenticateCalled = true
            return authenticateError?.let { Result.failure(it) } ?: Result.success(Unit)
        }

        override suspend fun handleAuthCallback(authCode: String): Result<UserInfo> {
            lastAuthCode = authCode
            return callbackError?.let { Result.failure(it) }
                ?: callbackUserInfo?.let { Result.success(it) }
                ?: Result.failure(RuntimeException("No user info set"))
        }

        override suspend fun logout(): Result<Unit> = Result.success(Unit)
        override suspend fun getAccessToken(): String? = null
        override suspend fun refreshToken(): Result<String> = Result.failure(RuntimeException("Not implemented"))
        override suspend fun isTokenValid(): Boolean = false

        override suspend fun setPublicAccess(url: String): Result<Unit> {
            lastPublicUrl = url
            return publicAccessError?.let { Result.failure(it) } ?: Result.success(Unit)
        }
    }
}
