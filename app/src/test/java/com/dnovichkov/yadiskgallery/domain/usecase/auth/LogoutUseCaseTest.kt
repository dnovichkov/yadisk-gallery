package com.dnovichkov.yadiskgallery.domain.usecase.auth

import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("LogoutUseCase")
class LogoutUseCaseTest {
    private lateinit var useCase: LogoutUseCase
    private lateinit var repository: FakeAuthRepository

    @BeforeEach
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = LogoutUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {
        @Test
        @DisplayName("should logout successfully")
        fun `should logout successfully`() =
            runTest {
                val result = useCase()

                assertTrue(result.isSuccess)
                assertTrue(repository.logoutCalled)
            }

        @Test
        @DisplayName("should propagate logout errors")
        fun `should propagate logout errors`() =
            runTest {
                repository.logoutError = RuntimeException("Logout failed")

                val result = useCase()

                assertTrue(result.isFailure)
            }
    }

    private class FakeAuthRepository : IAuthRepository {
        var logoutCalled = false
        var logoutError: Throwable? = null

        override fun observeAuthState(): Flow<AuthState> = flowOf(AuthState.NotAuthenticated)

        override suspend fun getAuthState(): AuthState = AuthState.NotAuthenticated

        override suspend fun authenticate(): Result<Unit> = Result.success(Unit)

        override suspend fun handleAuthCallback(authCode: String): Result<UserInfo> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun logout(): Result<Unit> {
            logoutCalled = true
            return logoutError?.let { Result.failure(it) } ?: Result.success(Unit)
        }

        override suspend fun getAccessToken(): String? = null

        override suspend fun refreshToken(): Result<String> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun isTokenValid(): Boolean = false

        override suspend fun setPublicAccess(url: String): Result<Unit> = Result.success(Unit)
    }
}
