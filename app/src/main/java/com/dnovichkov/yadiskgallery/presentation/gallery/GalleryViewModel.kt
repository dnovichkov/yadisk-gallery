package com.dnovichkov.yadiskgallery.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.usecase.files.GetFolderContentsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.files.RefreshFilesUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.GetSettingsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.SaveSettingsUseCase
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
 * ViewModel for the gallery screen.
 * Manages folder navigation, pagination, and display settings.
 */
@HiltViewModel
class GalleryViewModel
    @Inject
    constructor(
        private val getFolderContentsUseCase: GetFolderContentsUseCase,
        private val refreshFilesUseCase: RefreshFilesUseCase,
        private val getSettingsUseCase: GetSettingsUseCase,
        private val saveSettingsUseCase: SaveSettingsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(GalleryUiState())
        val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

        private val _navigationEvents = MutableSharedFlow<GalleryNavigationEvent>()
        val navigationEvents: SharedFlow<GalleryNavigationEvent> = _navigationEvents.asSharedFlow()

        init {
            observeSettings()
        }

        private fun observeSettings() {
            viewModelScope.launch {
                getSettingsUseCase.observeSettings().collect { settings ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            viewMode = settings.viewMode,
                            sortOrder = settings.sortOrder,
                        )
                    }
                }
            }
        }

        /**
         * Handles UI events.
         */
        fun onEvent(event: GalleryEvent) {
            when (event) {
                is GalleryEvent.LoadContent -> loadContent()
                is GalleryEvent.Refresh -> refresh()
                is GalleryEvent.LoadMore -> loadMore()
                is GalleryEvent.NavigateToFolder -> navigateToFolder(event.path)
                is GalleryEvent.NavigateToPath -> navigateToPath(event.path)
                is GalleryEvent.FolderClicked -> onFolderClicked(event.folder)
                is GalleryEvent.MediaClicked -> onMediaClicked(event.file)
                is GalleryEvent.OpenMedia -> openMedia(event.item)
                is GalleryEvent.SetViewMode -> changeViewMode(event.viewMode)
                is GalleryEvent.SetSortOrder -> changeSortOrder(event.sortOrder)
                is GalleryEvent.NavigateToSettings -> navigateToSettings()
                is GalleryEvent.Retry -> retry()
                is GalleryEvent.ClearError -> clearError()
            }
        }

        private fun loadContent() {
            val currentState = _uiState.value
            if (currentState.isLoading) return

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                getFolderContentsUseCase(
                    path = currentState.currentPath,
                    offset = 0,
                    limit = PAGE_SIZE,
                    sortOrder = currentState.sortOrder,
                    mediaOnly = false,
                ).onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            items = result.items,
                            isLoading = false,
                            hasMoreItems = result.hasMore,
                            isEmpty = result.items.isEmpty(),
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load content",
                        )
                    }
                }
            }
        }

        private fun refresh() {
            val currentState = _uiState.value
            if (currentState.isRefreshing) return

            viewModelScope.launch {
                _uiState.update { it.copy(isRefreshing = true, error = null) }

                refreshFilesUseCase(currentState.currentPath)
                    .onSuccess {
                        // Reload content after refresh
                        loadContentInternal(clearItems = true)
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                error = error.message ?: "Failed to refresh",
                            )
                        }
                    }
            }
        }

        private fun loadMore() {
            val currentState = _uiState.value
            if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMoreItems) return

            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingMore = true) }

                getFolderContentsUseCase(
                    path = currentState.currentPath,
                    offset = currentState.items.size,
                    limit = PAGE_SIZE,
                    sortOrder = currentState.sortOrder,
                    mediaOnly = false,
                ).onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            items = it.items + result.items,
                            isLoadingMore = false,
                            hasMoreItems = result.hasMore,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = error.message ?: "Failed to load more",
                        )
                    }
                }
            }
        }

        private fun loadContentInternal(clearItems: Boolean = false) {
            val currentState = _uiState.value

            viewModelScope.launch {
                if (clearItems) {
                    _uiState.update { it.copy(items = emptyList()) }
                }

                getFolderContentsUseCase(
                    path = currentState.currentPath,
                    offset = 0,
                    limit = PAGE_SIZE,
                    sortOrder = currentState.sortOrder,
                    mediaOnly = false,
                ).onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            items = result.items,
                            isLoading = false,
                            isRefreshing = false,
                            hasMoreItems = result.hasMore,
                            isEmpty = result.items.isEmpty(),
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "Failed to load content",
                        )
                    }
                }
            }
        }

        private fun navigateToFolder(path: String) {
            _uiState.update {
                it.copy(
                    currentPath = path,
                    items = emptyList(),
                    breadcrumbs = buildBreadcrumbs(path),
                )
            }
            loadContent()
        }

        private fun navigateToPath(path: String?) {
            _uiState.update {
                it.copy(
                    currentPath = path,
                    items = emptyList(),
                    breadcrumbs = buildBreadcrumbs(path),
                )
            }
            loadContent()
        }

        private fun onFolderClicked(folder: DiskItem.Directory) {
            navigateToFolder(folder.folder.path)
        }

        private fun onMediaClicked(file: DiskItem.File) {
            openMedia(file)
        }

        private fun navigateToSettings() {
            viewModelScope.launch {
                _navigationEvents.emit(GalleryNavigationEvent.NavigateToSettings)
            }
        }

        private fun buildBreadcrumbs(path: String?): List<BreadcrumbItem> {
            val breadcrumbs = mutableListOf(BreadcrumbItem("Root", null))

            if (path != null) {
                val parts = path.trimStart('/').split('/')
                var currentPath = ""
                for (part in parts) {
                    if (part.isNotEmpty()) {
                        currentPath = "$currentPath/$part"
                        breadcrumbs.add(BreadcrumbItem(part, currentPath))
                    }
                }
            }

            return breadcrumbs
        }

        private fun openMedia(item: DiskItem.File) {
            val currentState = _uiState.value
            val mediaFiles = currentState.items.filterIsInstance<DiskItem.File>()
            val index = mediaFiles.indexOf(item)

            viewModelScope.launch {
                when (item.mediaFile.type) {
                    MediaType.IMAGE -> {
                        _navigationEvents.emit(
                            GalleryNavigationEvent.NavigateToImageViewer(
                                path = currentState.currentPath ?: "",
                                index = if (index >= 0) index else 0,
                            ),
                        )
                    }
                    MediaType.VIDEO -> {
                        _navigationEvents.emit(
                            GalleryNavigationEvent.NavigateToVideoPlayer(
                                path = item.mediaFile.path,
                            ),
                        )
                    }
                }
            }
        }

        private fun changeViewMode(viewMode: ViewMode) {
            viewModelScope.launch {
                saveSettingsUseCase.setViewMode(viewMode)
            }
        }

        private fun changeSortOrder(sortOrder: SortOrder) {
            viewModelScope.launch {
                saveSettingsUseCase.setSortOrder(sortOrder)
                    .onSuccess {
                        // Reload with new sort order
                        loadContent()
                    }
            }
        }

        private fun retry() {
            _uiState.update { it.copy(error = null) }
            loadContent()
        }

        private fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        companion object {
            private const val PAGE_SIZE = 20
        }
    }
