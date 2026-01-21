package com.dnovichkov.yadiskgallery.domain.usecase.auth

import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetAuthStateUseCase")
class GetAuthStateUseCaseTest {
    private lateinit var useCase: GetAuthStateUseCase
    private lateinit var repository: FakeAuthRepository

    @BeforeEach
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = GetAuthStateUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {
        @Test
        @DisplayName("should return NotAuthenticated state")
        fun `should return NotAuthenticated state`() =
            runTest {
                repository.currentAuthState = AuthState.NotAuthenticated

                val result = useCase()

                assertEquals(AuthState.NotAuthenticated, result)
            }

        @Test
        @DisplayName("should return Authenticated state")
        fun `should return Authenticated state`() =
            runTest {
                val userInfo = UserInfo("123", "user", "User", null)
                repository.currentAuthState = AuthState.Authenticated(userInfo)

                val result = useCase()

                assertTrue(result is AuthState.Authenticated)
                assertEquals(userInfo, (result as AuthState.Authenticated).userInfo)
            }

        @Test
        @DisplayName("should return PublicAccess state")
        fun `should return PublicAccess state`() =
            runTest {
                repository.currentAuthState = AuthState.PublicAccess("https://url")

                val result = useCase()

                assertTrue(result is AuthState.PublicAccess)
                assertEquals("https://url", (result as AuthState.PublicAccess).publicUrl)
            }
    }

    @Nested
    @DisplayName("observeAuthState()")
    inner class ObserveAuthStateTests {
        @Test
        @DisplayName("should observe auth state changes")
        fun `should observe auth state changes`() =
            runTest {
                repository.authStateFlow = flowOf(AuthState.Authenticating)

                val result = useCase.observeAuthState().first()

                assertEquals(AuthState.Authenticating, result)
            }
    }

    @Nested
    @DisplayName("isAuthenticated()")
    inner class IsAuthenticatedTests {
        @Test
        @DisplayName("should return true when authenticated")
        fun `should return true when authenticated`() =
            runTest {
                val userInfo = UserInfo("123", "user", null, null)
                repository.currentAuthState = AuthState.Authenticated(userInfo)

                val result = useCase.isAuthenticated()

                assertTrue(result)
            }

        @Test
        @DisplayName("should return false when not authenticated")
        fun `should return false when not authenticated`() =
            runTest {
                repository.currentAuthState = AuthState.NotAuthenticated

                val result = useCase.isAuthenticated()

                assertEquals(false, result)
            }

        @Test
        @DisplayName("should return false for public access")
        fun `should return false for public access`() =
            runTest {
                repository.currentAuthState = AuthState.PublicAccess("url")

                val result = useCase.isAuthenticated()

                assertEquals(false, result)
            }
    }

    private class FakeAuthRepository : IAuthRepository {
        var currentAuthState: AuthState = AuthState.NotAuthenticated
        var authStateFlow: Flow<AuthState> = flowOf(AuthState.NotAuthenticated)

        override fun observeAuthState(): Flow<AuthState> = authStateFlow

        override suspend fun getAuthState(): AuthState = currentAuthState

        override suspend fun authenticate(): Result<Unit> = Result.success(Unit)

        override suspend fun handleAuthCallback(authCode: String): Result<UserInfo> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun getAccessToken(): String? = null

        override suspend fun refreshToken(): Result<String> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun isTokenValid(): Boolean = false

        override suspend fun setPublicAccess(url: String): Result<Unit> = Result.success(Unit)
    }
}
