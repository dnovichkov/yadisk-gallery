package com.dnovichkov.yadiskgallery.domain.model

/**
 * Represents the authentication state of the application.
 */
sealed class AuthState {
    /**
     * Whether the user is authenticated with Yandex account.
     */
    abstract val isAuthenticated: Boolean

    /**
     * User is not authenticated and no authentication is in progress.
     */
    data object NotAuthenticated : AuthState() {
        override val isAuthenticated: Boolean = false
    }

    /**
     * Authentication is in progress.
     */
    data object Authenticating : AuthState() {
        override val isAuthenticated: Boolean = false
    }

    /**
     * User is authenticated with Yandex account.
     *
     * @property userInfo Information about the authenticated user
     */
    data class Authenticated(val userInfo: UserInfo) : AuthState() {
        override val isAuthenticated: Boolean = true
    }

    /**
     * User is accessing a public folder without authentication.
     *
     * @property publicUrl URL of the public folder being accessed
     */
    data class PublicAccess(val publicUrl: String) : AuthState() {
        override val isAuthenticated: Boolean = false
    }

    /**
     * Authentication failed with an error.
     *
     * @property message Error message describing the failure
     */
    data class AuthError(val message: String) : AuthState() {
        override val isAuthenticated: Boolean = false
    }
}

/**
 * Information about the authenticated Yandex user.
 *
 * @property uid Unique user identifier
 * @property login User's login (email)
 * @property displayName User's display name (can be null)
 * @property avatarUrl URL of the user's avatar image (can be null)
 */
data class UserInfo(
    val uid: String,
    val login: String,
    val displayName: String?,
    val avatarUrl: String?,
)
