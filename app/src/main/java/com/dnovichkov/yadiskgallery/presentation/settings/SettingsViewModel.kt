package com.dnovichkov.yadiskgallery.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.usecase.auth.GetAuthStateUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.auth.LogoutUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.cache.ClearCacheUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.cache.GetCacheSizeUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.GetSettingsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.SaveSettingsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.ValidatePublicUrlUseCase
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
 * ViewModel for the Settings screen.
 * Manages user settings, authentication state, and cache operations.
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val getSettingsUseCase: GetSettingsUseCase,
        private val saveSettingsUseCase: SaveSettingsUseCase,
        private val validatePublicUrlUseCase: ValidatePublicUrlUseCase,
        private val getAuthStateUseCase: GetAuthStateUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val getCacheSizeUseCase: GetCacheSizeUseCase,
        private val clearCacheUseCase: ClearCacheUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState.Initial)
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<SettingsEvent>()
        val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

        init {
            observeSettings()
            observeAuthState()
            observeCacheSize()
        }

        private fun observeSettings() {
            viewModelScope.launch {
                getSettingsUseCase.observeSettings().collect { settings ->
                    _uiState.update { state ->
                        state.copy(
                            publicFolderUrl = settings.publicFolderUrl,
                            publicUrlInput = settings.publicFolderUrl ?: state.publicUrlInput,
                            rootFolderPath = settings.rootFolderPath,
                            viewMode = settings.viewMode,
                            sortOrder = settings.sortOrder,
                        )
                    }
                }
            }
        }

        private fun observeAuthState() {
            viewModelScope.launch {
                getAuthStateUseCase.observeAuthState().collect { authState ->
                    _uiState.update { state ->
                        state.copy(authState = authState)
                    }
                }
            }
        }

        private fun observeCacheSize() {
            viewModelScope.launch {
                getCacheSizeUseCase.observeCacheSize().collect { size ->
                    _uiState.update { state ->
                        state.copy(cacheSize = size)
                    }
                }
            }
        }

        /**
         * Called when the user changes the public URL input.
         */
        fun onPublicUrlChange(url: String) {
            _uiState.update { state ->
                state.copy(
                    publicUrlInput = url,
                    publicUrlError = null,
                )
            }
        }

        /**
         * Called when the user wants to save the public URL.
         */
        fun onSavePublicUrl() {
            val url = _uiState.value.publicUrlInput

            if (url.isBlank()) {
                _uiState.update { it.copy(publicUrlError = "URL cannot be empty") }
                return
            }

            val validationResult = validatePublicUrlUseCase(url)

            validationResult.fold(
                onSuccess = { validatedUrl ->
                    viewModelScope.launch {
                        _uiState.update { it.copy(isSaving = true) }

                        saveSettingsUseCase.setPublicFolderUrl(validatedUrl).fold(
                            onSuccess = {
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        publicUrlError = null,
                                    )
                                }
                                _events.emit(SettingsEvent.NavigateToGallery)
                            },
                            onFailure = { error ->
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        error = error.message ?: "Failed to save URL",
                                    )
                                }
                            },
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage =
                        when (error) {
                            is ValidatePublicUrlUseCase.ValidationException -> error.error.message
                            else -> error.message ?: "Invalid URL"
                        }
                    _uiState.update { it.copy(publicUrlError = errorMessage) }
                },
            )
        }

        /**
         * Called when the user wants to clear the public URL.
         */
        fun onClearPublicUrl() {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }

                saveSettingsUseCase.setPublicFolderUrl(null).fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                publicUrlInput = "",
                                publicUrlError = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = error.message ?: "Failed to clear URL",
                            )
                        }
                    },
                )
            }
        }

        /**
         * Called when the user changes the view mode.
         */
        fun onViewModeChange(viewMode: ViewMode) {
            viewModelScope.launch {
                saveSettingsUseCase.setViewMode(viewMode).onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to save view mode")
                    }
                }
            }
        }

        /**
         * Called when the user changes the sort order.
         */
        fun onSortOrderChange(sortOrder: SortOrder) {
            viewModelScope.launch {
                saveSettingsUseCase.setSortOrder(sortOrder).onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to save sort order")
                    }
                }
            }
        }

        /**
         * Called when the user changes the root folder path.
         */
        fun onRootFolderChange(path: String?) {
            viewModelScope.launch {
                saveSettingsUseCase.setRootFolderPath(path).onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to save root folder")
                    }
                }
            }
        }

        /**
         * Called when the user clicks the login button.
         */
        fun onLoginClick() {
            _uiState.update { it.copy(showLoginDialog = true) }
        }

        /**
         * Called when the user confirms the login.
         */
        fun onConfirmLogin() {
            _uiState.update { it.copy(showLoginDialog = false) }
            viewModelScope.launch {
                _events.emit(SettingsEvent.StartYandexLogin)
            }
        }

        /**
         * Called when the login dialog is dismissed.
         */
        fun onDismissLoginDialog() {
            _uiState.update { it.copy(showLoginDialog = false) }
        }

        /**
         * Called when the user requests to logout.
         */
        fun onLogoutRequest() {
            _uiState.update { it.copy(showLogoutConfirmation = true) }
        }

        /**
         * Called when the user confirms logout.
         */
        fun onLogoutClick() {
            _uiState.update { it.copy(showLogoutConfirmation = false) }
            viewModelScope.launch {
                logoutUseCase().onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to logout")
                    }
                }
            }
        }

        /**
         * Called when the logout confirmation dialog is dismissed.
         */
        fun onDismissLogoutConfirmation() {
            _uiState.update { it.copy(showLogoutConfirmation = false) }
        }

        /**
         * Called when the user wants to clear the cache.
         */
        fun onClearCache() {
            viewModelScope.launch {
                _uiState.update { it.copy(isClearingCache = true) }

                clearCacheUseCase().fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isClearingCache = false,
                                showCacheClearedMessage = true,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isClearingCache = false,
                                error = error.message ?: "Failed to clear cache",
                            )
                        }
                    },
                )
            }
        }

        /**
         * Called to dismiss the cache cleared message.
         */
        fun onDismissCacheClearedMessage() {
            _uiState.update { it.copy(showCacheClearedMessage = false) }
        }

        /**
         * Called to dismiss the error message.
         */
        fun onDismissError() {
            _uiState.update { it.copy(error = null) }
        }
    }
