package com.dnovichkov.yadiskgallery.domain.repository

import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Handles Yandex OAuth authentication and token management.
 */
interface IAuthRepository {

    /**
     * Observes the current authentication state.
     * Emits new values whenever auth state changes.
     *
     * @return Flow of authentication state
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Gets the current authentication state.
     *
     * @return Current authentication state
     */
    suspend fun getAuthState(): AuthState

    /**
     * Initiates OAuth authentication with Yandex.
     * This should launch the Yandex Login SDK flow.
     *
     * @return Result containing success or error
     */
    suspend fun authenticate(): Result<Unit>

    /**
     * Handles the OAuth callback with authorization code.
     * Exchanges the code for access token.
     *
     * @param authCode Authorization code from OAuth callback
     * @return Result containing user info or error
     */
    suspend fun handleAuthCallback(authCode: String): Result<UserInfo>

    /**
     * Logs out the current user.
     * Clears stored tokens and resets auth state.
     *
     * @return Result indicating success or error
     */
    suspend fun logout(): Result<Unit>

    /**
     * Gets the current access token if available.
     * Returns null if not authenticated.
     *
     * @return Access token or null
     */
    suspend fun getAccessToken(): String?

    /**
     * Refreshes the access token using the refresh token.
     *
     * @return Result containing new access token or error
     */
    suspend fun refreshToken(): Result<String>

    /**
     * Checks if the current token is valid and not expired.
     *
     * @return true if token is valid, false otherwise
     */
    suspend fun isTokenValid(): Boolean

    /**
     * Sets the public folder URL for unauthenticated access.
     *
     * @param url The public folder URL
     * @return Result indicating success or error
     */
    suspend fun setPublicAccess(url: String): Result<Unit>
}
