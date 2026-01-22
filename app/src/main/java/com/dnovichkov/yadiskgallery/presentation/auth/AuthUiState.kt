package com.dnovichkov.yadiskgallery.presentation.auth

/**
 * UI state for authentication screen.
 */
data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val userName: String? = null,
    val errorMessage: String? = null,
)

/**
 * Events that can be triggered from the authentication UI.
 */
sealed class AuthEvent {
    /**
     * User requested to start the login flow.
     */
    data object LoginRequested : AuthEvent()

    /**
     * User requested to logout.
     */
    data object LogoutRequested : AuthEvent()

    /**
     * Authentication result received from Yandex SDK.
     */
    data class AuthResultReceived(val result: AuthResult) : AuthEvent()

    /**
     * Clear any displayed error message.
     */
    data object ClearError : AuthEvent()
}

/**
 * Navigation events from authentication flow.
 */
sealed class AuthNavigationEvent {
    /**
     * Navigate to main gallery screen after successful login.
     */
    data object NavigateToGallery : AuthNavigationEvent()

    /**
     * Navigate back to settings after logout.
     */
    data object NavigateToSettings : AuthNavigationEvent()
}
