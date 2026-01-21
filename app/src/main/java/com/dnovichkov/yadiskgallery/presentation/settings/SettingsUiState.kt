package com.dnovichkov.yadiskgallery.presentation.settings

import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.ViewMode

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    /** Current public folder URL from saved settings */
    val publicFolderUrl: String? = null,
    /** User input for public folder URL (may differ from saved) */
    val publicUrlInput: String = "",
    /** Validation error for public URL input */
    val publicUrlError: String? = null,
    /** Root folder path for browsing */
    val rootFolderPath: String? = null,
    /** Current authentication state */
    val authState: AuthState = AuthState.NotAuthenticated,
    /** Gallery view mode (grid or list) */
    val viewMode: ViewMode = ViewMode.GRID,
    /** Sort order for files */
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    /** Cache size in bytes */
    val cacheSize: Long = 0L,
    /** Whether a loading operation is in progress */
    val isLoading: Boolean = false,
    /** Whether saving is in progress */
    val isSaving: Boolean = false,
    /** General error message */
    val error: String? = null,
    /** Whether to show the Yandex login dialog */
    val showLoginDialog: Boolean = false,
    /** Whether to show the logout confirmation dialog */
    val showLogoutConfirmation: Boolean = false,
    /** Whether to show the cache cleared success message */
    val showCacheClearedMessage: Boolean = false,
    /** Whether clearing cache is in progress */
    val isClearingCache: Boolean = false,
) {
    companion object {
        /**
         * Initial state when the screen is first created.
         */
        val Initial = SettingsUiState()
    }
}

/**
 * One-time events from the Settings screen.
 */
sealed class SettingsEvent {
    /**
     * Navigate to the gallery screen.
     */
    data object NavigateToGallery : SettingsEvent()

    /**
     * Start Yandex OAuth login flow.
     */
    data object StartYandexLogin : SettingsEvent()

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : SettingsEvent()
}
