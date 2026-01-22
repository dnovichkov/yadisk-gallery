package com.dnovichkov.yadiskgallery.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import com.dnovichkov.yadiskgallery.domain.usecase.auth.GetAuthStateUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing authentication state and operations.
 */
@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val authRepository: IAuthRepository,
        private val getAuthStateUseCase: GetAuthStateUseCase,
        private val logoutUseCase: LogoutUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AuthUiState())
        val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<AuthNavigationEvent>()
        val events: SharedFlow<AuthNavigationEvent> = _events.asSharedFlow()

        init {
            observeAuthState()
        }

        private fun observeAuthState() {
            viewModelScope.launch {
                getAuthStateUseCase.observeAuthState().collect { authState ->
                    _uiState.update { currentState ->
                        when (authState) {
                            is AuthState.Authenticated ->
                                currentState.copy(
                                    isAuthenticated = true,
                                    isLoading = false,
                                    userName = authState.userInfo.displayName,
                                    errorMessage = null,
                                )
                            is AuthState.Authenticating ->
                                currentState.copy(
                                    isLoading = true,
                                )
                            is AuthState.NotAuthenticated ->
                                currentState.copy(
                                    isAuthenticated = false,
                                    isLoading = false,
                                    userName = null,
                                )
                            is AuthState.AuthError ->
                                currentState.copy(
                                    isAuthenticated = false,
                                    isLoading = false,
                                    errorMessage = authState.message,
                                )
                            is AuthState.PublicAccess ->
                                currentState.copy(
                                    isAuthenticated = false,
                                    isLoading = false,
                                    userName = null,
                                )
                        }
                    }
                }
            }
        }

        /**
         * Handles UI events from the authentication screen.
         */
        fun onEvent(event: AuthEvent) {
            when (event) {
                is AuthEvent.LoginRequested -> handleLoginRequest()
                is AuthEvent.LogoutRequested -> handleLogout()
                is AuthEvent.AuthResultReceived -> onAuthResultReceived(event.result)
                is AuthEvent.ClearError -> clearError()
            }
        }

        /**
         * Handles the authentication result from Yandex SDK.
         * This method should be called from the Activity after receiving
         * the result from the Yandex Login SDK.
         */
        fun onAuthResultReceived(result: AuthResult) {
            viewModelScope.launch {
                when (result) {
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = true) }
                        // Yandex SDK 3.x provides token directly, save it
                        // Default token expiration is 1 year (31536000 seconds)
                        authRepository.saveToken(result.token, DEFAULT_TOKEN_EXPIRATION_SECONDS)
                            .onSuccess {
                                _events.emit(AuthNavigationEvent.NavigateToGallery)
                            }
                            .onFailure { error ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = error.message ?: "Authentication failed",
                                    )
                                }
                            }
                    }
                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message,
                            )
                        }
                    }
                    is AuthResult.Cancelled -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }

        companion object {
            private const val DEFAULT_TOKEN_EXPIRATION_SECONDS = 31536000L // 1 year
        }

        private fun handleLoginRequest() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                _events.emit(AuthNavigationEvent.NavigateToSettings)
            }
        }

        private fun handleLogout() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                logoutUseCase()
                    .onSuccess {
                        _events.emit(AuthNavigationEvent.NavigateToSettings)
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Logout failed",
                            )
                        }
                    }
            }
        }

        private fun clearError() {
            _uiState.update { it.copy(errorMessage = null) }
        }
    }
