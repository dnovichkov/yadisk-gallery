package com.dnovichkov.yadiskgallery.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.presentation.settings.components.AuthStatusCard
import com.dnovichkov.yadiskgallery.presentation.settings.components.CacheInfoSection
import com.dnovichkov.yadiskgallery.presentation.settings.components.LogoutButton
import com.dnovichkov.yadiskgallery.presentation.settings.components.LogoutConfirmationDialog
import com.dnovichkov.yadiskgallery.presentation.settings.components.PublicUrlTextField
import com.dnovichkov.yadiskgallery.presentation.settings.components.RootFolderSelector
import com.dnovichkov.yadiskgallery.presentation.settings.components.SortOrderSelector
import com.dnovichkov.yadiskgallery.presentation.settings.components.ViewModeSelector
import com.dnovichkov.yadiskgallery.presentation.settings.components.YandexLoginButton
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Settings screen composable.
 */
@Composable
fun SettingsScreen(
    onNavigateToGallery: () -> Unit,
    onStartYandexLogin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.NavigateToGallery -> onNavigateToGallery()
                is SettingsEvent.StartYandexLogin -> onStartYandexLogin()
                is SettingsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Show snackbar for cache cleared
    LaunchedEffect(uiState.showCacheClearedMessage) {
        if (uiState.showCacheClearedMessage) {
            snackbarHostState.showSnackbar("Cache cleared successfully")
            viewModel.onDismissCacheClearedMessage()
        }
    }

    // Show snackbar for errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onDismissError()
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onPublicUrlChange = viewModel::onPublicUrlChange,
        onSavePublicUrl = viewModel::onSavePublicUrl,
        onClearPublicUrl = viewModel::onClearPublicUrl,
        onViewModeChange = viewModel::onViewModeChange,
        onSortOrderChange = viewModel::onSortOrderChange,
        onRootFolderSelect = { /* TODO: Open folder picker */ },
        onRootFolderClear = { viewModel.onRootFolderChange(null) },
        onLoginClick = viewModel::onLoginClick,
        onConfirmLogin = viewModel::onConfirmLogin,
        onDismissLoginDialog = viewModel::onDismissLoginDialog,
        onLogoutRequest = viewModel::onLogoutRequest,
        onLogoutConfirm = viewModel::onLogoutClick,
        onDismissLogoutConfirmation = viewModel::onDismissLogoutConfirmation,
        onClearCache = viewModel::onClearCache,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onPublicUrlChange: (String) -> Unit,
    onSavePublicUrl: () -> Unit,
    onClearPublicUrl: () -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onRootFolderSelect: () -> Unit,
    onRootFolderClear: () -> Unit,
    onLoginClick: () -> Unit,
    onConfirmLogin: () -> Unit,
    onDismissLoginDialog: () -> Unit,
    onLogoutRequest: () -> Unit,
    onLogoutConfirm: () -> Unit,
    onDismissLogoutConfirmation: () -> Unit,
    onClearCache: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Public folder section
            SectionHeader(text = "Public folder")

            PublicUrlTextField(
                value = uiState.publicUrlInput,
                onValueChange = onPublicUrlChange,
                onSave = onSavePublicUrl,
                onClear = onClearPublicUrl,
                error = uiState.publicUrlError,
                isLoading = uiState.isSaving,
            )

            HorizontalDivider()

            // Authentication section
            SectionHeader(text = "Yandex account")

            AuthStatusCard(authState = uiState.authState)

            if (uiState.authState.isAuthenticated) {
                LogoutButton(
                    onClick = onLogoutRequest,
                    enabled = true,
                )
            } else {
                YandexLoginButton(
                    onClick = onLoginClick,
                    isLoading = uiState.authState is AuthState.Authenticating,
                )
            }

            // Only show these settings if authenticated
            if (uiState.authState.isAuthenticated) {
                HorizontalDivider()

                // Root folder section
                SectionHeader(text = "Browse settings")

                RootFolderSelector(
                    currentPath = uiState.rootFolderPath,
                    onSelectFolder = onRootFolderSelect,
                    onClearFolder = onRootFolderClear,
                )
            }

            HorizontalDivider()

            // Display settings
            SectionHeader(text = "Display")

            ViewModeSelector(
                selectedMode = uiState.viewMode,
                onModeSelected = onViewModeChange,
            )

            Spacer(modifier = Modifier.height(8.dp))

            SortOrderSelector(
                selectedOrder = uiState.sortOrder,
                onOrderSelected = onSortOrderChange,
            )

            HorizontalDivider()

            // Cache section
            SectionHeader(text = "Storage")

            CacheInfoSection(
                cacheSize = uiState.cacheSize,
                onClearCache = onClearCache,
                isClearing = uiState.isClearingCache,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (uiState.showLoginDialog) {
        LoginConfirmationDialog(
            onConfirm = onConfirmLogin,
            onDismiss = onDismissLoginDialog,
        )
    }

    if (uiState.showLogoutConfirmation) {
        LogoutConfirmationDialog(
            onConfirm = onLogoutConfirm,
            onDismiss = onDismissLogoutConfirmation,
        )
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun LoginConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Sign in with Yandex")
        },
        text = {
            Text(text = "You will be redirected to Yandex to sign in and grant access to your Yandex.Disk.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenNotAuthenticatedPreview() {
    YaDiskGalleryTheme {
        SettingsScreenContent(
            uiState =
                SettingsUiState(
                    authState = AuthState.NotAuthenticated,
                    viewMode = ViewMode.GRID,
                    sortOrder = SortOrder.DATE_DESC,
                    cacheSize = 52_428_800L,
                ),
            snackbarHostState = remember { SnackbarHostState() },
            onPublicUrlChange = {},
            onSavePublicUrl = {},
            onClearPublicUrl = {},
            onViewModeChange = {},
            onSortOrderChange = {},
            onRootFolderSelect = {},
            onRootFolderClear = {},
            onLoginClick = {},
            onConfirmLogin = {},
            onDismissLoginDialog = {},
            onLogoutRequest = {},
            onLogoutConfirm = {},
            onDismissLogoutConfirmation = {},
            onClearCache = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenAuthenticatedPreview() {
    YaDiskGalleryTheme {
        SettingsScreenContent(
            uiState =
                SettingsUiState(
                    authState =
                        AuthState.Authenticated(
                            UserInfo(
                                uid = "123",
                                login = "user@yandex.ru",
                                displayName = "Test User",
                                avatarUrl = null,
                            ),
                        ),
                    viewMode = ViewMode.GRID,
                    sortOrder = SortOrder.DATE_DESC,
                    cacheSize = 104_857_600L,
                    rootFolderPath = "/Photos",
                ),
            snackbarHostState = remember { SnackbarHostState() },
            onPublicUrlChange = {},
            onSavePublicUrl = {},
            onClearPublicUrl = {},
            onViewModeChange = {},
            onSortOrderChange = {},
            onRootFolderSelect = {},
            onRootFolderClear = {},
            onLoginClick = {},
            onConfirmLogin = {},
            onDismissLoginDialog = {},
            onLogoutRequest = {},
            onLogoutConfirm = {},
            onDismissLogoutConfirmation = {},
            onClearCache = {},
        )
    }
}
