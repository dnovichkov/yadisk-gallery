package com.dnovichkov.yadiskgallery.presentation.auth

import app.cash.turbine.test
import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import com.dnovichkov.yadiskgallery.domain.usecase.auth.GetAuthStateUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.auth.LogoutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("AuthViewModel")
class AuthViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: IAuthRepository
    private lateinit var getAuthStateUseCase: GetAuthStateUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var viewModel: AuthViewModel

    private val testUserInfo =
        UserInfo(
            uid = "123",
            login = "test@yandex.ru",
            displayName = "Test User",
            avatarUrl = "https://avatars.yandex.net/123",
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk()
        getAuthStateUseCase = mockk()
        logoutUseCase = mockk()

        // Default mock behavior
        every { getAuthStateUseCase.observeAuthState() } returns flowOf(AuthState.NotAuthenticated)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AuthViewModel {
        return AuthViewModel(
            authRepository = authRepository,
            getAuthStateUseCase = getAuthStateUseCase,
            logoutUseCase = logoutUseCase,
        )
    }

    @Nested
    @DisplayName("Initial State")
    inner class InitialState {
        @Test
        @DisplayName("should have default UI state on creation")
        fun shouldHaveDefaultUiStateOnCreation() =
            runTest {
                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value

                assertFalse(state.isAuthenticated)
                assertFalse(state.isLoading)
                assertNull(state.userName)
                assertNull(state.errorMessage)
            }

        @Test
        @DisplayName("should reflect authenticated state when user is logged in")
        fun shouldReflectAuthenticatedState() =
            runTest {
                every { getAuthStateUseCase.observeAuthState() } returns
                    flowOf(AuthState.Authenticated(testUserInfo))

                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertTrue(state.isAuthenticated)
                assertEquals("Test User", state.userName)
            }

        @Test
        @DisplayName("should show loading state during authentication")
        fun shouldShowLoadingDuringAuthentication() =
            runTest {
                every { getAuthStateUseCase.observeAuthState() } returns
                    flowOf(AuthState.Authenticating)

                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertTrue(state.isLoading)
            }
    }

    @Nested
    @DisplayName("Authentication Events")
    inner class AuthenticationEvents {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should emit login requested event")
        fun shouldEmitLoginRequestedEvent() =
            runTest {
                advanceUntilIdle()

                viewModel.events.test {
                    viewModel.onEvent(AuthEvent.LoginRequested)
                    advanceUntilIdle()

                    val event = awaitItem()
                    assertTrue(event is AuthNavigationEvent.NavigateToSettings)
                }
            }

        @Test
        @DisplayName("should handle successful auth result")
        fun shouldHandleSuccessfulAuthResult() =
            runTest {
                advanceUntilIdle()
                coEvery { authRepository.saveToken(any(), any()) } returns Result.success(Unit)

                viewModel.onAuthResultReceived(AuthResult.Success("test_token"))
                advanceUntilIdle()

                coVerify { authRepository.saveToken("test_token", any()) }
            }

        @Test
        @DisplayName("should show error on failed auth result")
        fun shouldShowErrorOnFailedAuthResult() =
            runTest {
                advanceUntilIdle()

                viewModel.onAuthResultReceived(AuthResult.Error("Authentication failed"))
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals("Authentication failed", state.errorMessage)
            }

        @Test
        @DisplayName("should clear loading on cancelled auth")
        fun shouldClearLoadingOnCancelledAuth() =
            runTest {
                advanceUntilIdle()

                viewModel.onAuthResultReceived(AuthResult.Cancelled)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertFalse(state.isLoading)
            }
    }

    @Nested
    @DisplayName("Logout")
    inner class Logout {
        @BeforeEach
        fun setUp() {
            every { getAuthStateUseCase.observeAuthState() } returns
                flowOf(AuthState.Authenticated(testUserInfo))
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should logout user")
        fun shouldLogoutUser() =
            runTest {
                advanceUntilIdle()
                coEvery { logoutUseCase() } returns Result.success(Unit)

                viewModel.onEvent(AuthEvent.LogoutRequested)
                advanceUntilIdle()

                coVerify { logoutUseCase() }
            }

        @Test
        @DisplayName("should emit navigate to settings after logout")
        fun shouldEmitNavigateToSettingsAfterLogout() =
            runTest {
                advanceUntilIdle()
                coEvery { logoutUseCase() } returns Result.success(Unit)

                viewModel.events.test {
                    viewModel.onEvent(AuthEvent.LogoutRequested)
                    advanceUntilIdle()

                    val event = awaitItem()
                    assertTrue(event is AuthNavigationEvent.NavigateToSettings)
                }
            }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandling {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should clear error")
        fun shouldClearError() =
            runTest {
                advanceUntilIdle()

                viewModel.onAuthResultReceived(AuthResult.Error("Some error"))
                advanceUntilIdle()

                viewModel.onEvent(AuthEvent.ClearError)
                advanceUntilIdle()

                assertNull(viewModel.uiState.value.errorMessage)
            }
    }
}
