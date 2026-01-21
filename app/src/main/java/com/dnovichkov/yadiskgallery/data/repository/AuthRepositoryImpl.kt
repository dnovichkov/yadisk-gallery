package com.dnovichkov.yadiskgallery.data.repository

import com.dnovichkov.yadiskgallery.data.datastore.SettingsDataStore
import com.dnovichkov.yadiskgallery.data.datastore.TokenStorage
import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.DomainError
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.repository.DomainException
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of IAuthRepository.
 * Manages OAuth authentication state and token storage.
 *
 * Note: Actual Yandex Login SDK integration requires Android Activity context
 * and will be handled by a separate wrapper class in the presentation layer.
 * This repository manages the underlying state and token persistence.
 */
@Singleton
class AuthRepositoryImpl
    @Inject
    constructor(
        private val tokenStorage: TokenStorage,
        private val settingsDataStore: SettingsDataStore,
    ) : IAuthRepository {
        private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)

        init {
            // Initialize auth state based on stored tokens
            initializeAuthState()
        }

        private fun initializeAuthState() {
            // Check if we have a stored token or public URL
            // This will be updated asynchronously
        }

        override fun observeAuthState(): Flow<AuthState> {
            return _authState.asStateFlow()
        }

        override suspend fun getAuthState(): AuthState {
            // Update state based on current token
            val token = tokenStorage.getAccessToken()
            val settings = settingsDataStore.settings.first()

            return when {
                token != null && !tokenStorage.isTokenExpired() -> {
                    // We have a valid token, but we don't have user info stored
                    // Return authenticated state with minimal info
                    _authState.value as? AuthState.Authenticated ?: AuthState.NotAuthenticated
                }
                settings.publicFolderUrl != null -> {
                    AuthState.PublicAccess(settings.publicFolderUrl)
                }
                else -> {
                    AuthState.NotAuthenticated
                }
            }
        }

        override suspend fun authenticate(): Result<Unit> {
            // Set state to authenticating
            _authState.update { AuthState.Authenticating }
            // Actual authentication will be triggered from presentation layer
            // using Yandex Login SDK
            return Result.success(Unit)
        }

        override suspend fun handleAuthCallback(authCode: String): Result<UserInfo> {
            return runCatching {
                // In a real implementation, this would:
                // 1. Exchange auth code for tokens using Yandex OAuth API
                // 2. Fetch user info using the access token
                // 3. Store tokens securely

                // For now, we'll simulate the token storage
                // The actual token exchange would happen via the network layer
                throw DomainException(
                    DomainError.Unknown(
                        message = "Auth callback handling requires Yandex OAuth API integration",
                        cause = null,
                    ),
                )
            }
        }

        /**
         * Called from the presentation layer after successful Yandex SDK authentication.
         * Stores the tokens and updates the auth state.
         */
        suspend fun onAuthenticationSuccess(
            accessToken: String,
            refreshToken: String?,
            expiresInSeconds: Long,
            userInfo: UserInfo,
        ) {
            tokenStorage.saveTokenData(accessToken, refreshToken, expiresInSeconds)
            settingsDataStore.setAuthenticated(true)
            _authState.update { AuthState.Authenticated(userInfo) }
        }

        override suspend fun logout(): Result<Unit> {
            return runCatching {
                tokenStorage.clearTokens()
                settingsDataStore.setAuthenticated(false)
                _authState.update { AuthState.NotAuthenticated }
            }
        }

        override suspend fun getAccessToken(): String? {
            return tokenStorage.getAccessToken()
        }

        override suspend fun refreshToken(): Result<String> {
            return runCatching {
                val refreshToken =
                    tokenStorage.getRefreshToken()
                        ?: throw DomainException(DomainError.Auth.Unauthorized)

                // In a real implementation, this would:
                // 1. Call Yandex OAuth API to refresh the token
                // 2. Store the new tokens
                // For now, throw an error indicating this needs API integration
                throw DomainException(
                    DomainError.Unknown(
                        message = "Token refresh requires Yandex OAuth API integration",
                        cause = null,
                    ),
                )
            }
        }

        override suspend fun isTokenValid(): Boolean {
            val token = tokenStorage.getAccessToken()
            return token != null && !tokenStorage.isTokenExpired()
        }

        override suspend fun setPublicAccess(url: String): Result<Unit> {
            return runCatching {
                settingsDataStore.setPublicFolderUrl(url)
                _authState.update { AuthState.PublicAccess(url) }
            }
        }

        /**
         * Updates the authentication state on error.
         */
        fun onAuthenticationError(message: String) {
            _authState.update { AuthState.AuthError(message) }
        }

        /**
         * Restores authentication state from stored tokens.
         * Should be called on app startup.
         */
        suspend fun restoreAuthState() {
            val token = tokenStorage.getAccessToken()
            val settings = settingsDataStore.settings.first()

            _authState.update {
                when {
                    token != null && !tokenStorage.isTokenExpired() -> {
                        // Token exists and is valid - but we need user info
                        // In a real app, we'd fetch user info or store it
                        AuthState.NotAuthenticated // Will be updated when we have user info
                    }
                    settings.publicFolderUrl != null -> {
                        AuthState.PublicAccess(settings.publicFolderUrl)
                    }
                    else -> {
                        AuthState.NotAuthenticated
                    }
                }
            }
        }
    }
