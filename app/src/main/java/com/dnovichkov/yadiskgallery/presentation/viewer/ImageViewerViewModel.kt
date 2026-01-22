package com.dnovichkov.yadiskgallery.presentation.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.domain.usecase.files.GetFolderContentsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.files.GetMediaDownloadUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the image viewer screen.
 * Handles image navigation, zoom, and original image loading.
 */
@HiltViewModel
class ImageViewerViewModel
    @Inject
    constructor(
        private val getFolderContentsUseCase: GetFolderContentsUseCase,
        private val getMediaDownloadUrlUseCase: GetMediaDownloadUrlUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ImageViewerUiState())
        val uiState: StateFlow<ImageViewerUiState> = _uiState.asStateFlow()

        private var folderPath: String? = null
        private var publicFolderUrl: String? = null

        /**
         * Handles UI events.
         */
        fun onEvent(event: ImageViewerEvent) {
            when (event) {
                is ImageViewerEvent.LoadImages ->
                    loadImages(event.folderPath, event.initialIndex, event.publicFolderUrl)
                is ImageViewerEvent.NextImage -> navigateToNext()
                is ImageViewerEvent.PreviousImage -> navigateToPrevious()
                is ImageViewerEvent.GoToImage -> goToImage(event.index)
                is ImageViewerEvent.SetZoomLevel -> setZoomLevel(event.zoom)
                is ImageViewerEvent.ToggleDoubleTapZoom -> toggleDoubleTapZoom()
                is ImageViewerEvent.ToggleControls -> toggleControls()
                is ImageViewerEvent.ShowExifInfo -> showExifInfo()
                is ImageViewerEvent.HideExifInfo -> hideExifInfo()
                is ImageViewerEvent.LoadOriginalImage -> loadOriginalImage()
                is ImageViewerEvent.Retry -> retry()
                is ImageViewerEvent.ClearError -> clearError()
            }
        }

        private fun loadImages(
            path: String?,
            initialIndex: Int,
            publicUrl: String? = null,
        ) {
            folderPath = path
            publicFolderUrl = publicUrl
            // Normalize empty/blank string to null for API compatibility
            val normalizedPath = path?.ifBlank { null }
            _uiState.update { it.copy(isLoading = true, error = null) }

            viewModelScope.launch {
                val result =
                    if (publicUrl != null) {
                        getFolderContentsUseCase.getPublicFolderContents(
                            publicUrl = publicUrl,
                            path = normalizedPath,
                            offset = 0,
                            limit = MAX_IMAGES_LOAD,
                        )
                    } else {
                        getFolderContentsUseCase(
                            path = normalizedPath,
                            offset = 0,
                            limit = MAX_IMAGES_LOAD,
                            mediaOnly = true,
                        )
                    }

                result.onSuccess { pagedResult ->
                    val images =
                        pagedResult.items
                            .filterIsInstance<DiskItem.File>()
                            .map { it.mediaFile }
                            .filter { it.type == MediaType.IMAGE }

                    val validIndex = initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0))
                    val currentImage = images.getOrNull(validIndex)

                    _uiState.update {
                        it.copy(
                            images = images,
                            currentIndex = validIndex,
                            isLoading = false,
                            currentImageUrl = currentImage?.previewUrl,
                            originalImageUrl = null,
                            zoomLevel = ImageViewerUiState.MIN_ZOOM,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load images",
                        )
                    }
                }
            }
        }

        private fun navigateToNext() {
            val currentState = _uiState.value
            if (!currentState.hasNextImage) return

            val newIndex = currentState.currentIndex + 1
            updateCurrentIndex(newIndex)
        }

        private fun navigateToPrevious() {
            val currentState = _uiState.value
            if (!currentState.hasPreviousImage) return

            val newIndex = currentState.currentIndex - 1
            updateCurrentIndex(newIndex)
        }

        private fun goToImage(index: Int) {
            val currentState = _uiState.value
            val validIndex = index.coerceIn(0, (currentState.images.size - 1).coerceAtLeast(0))

            if (validIndex != currentState.currentIndex) {
                updateCurrentIndex(validIndex)
            }
        }

        private fun updateCurrentIndex(newIndex: Int) {
            val currentState = _uiState.value
            val newImage = currentState.images.getOrNull(newIndex)

            _uiState.update {
                it.copy(
                    currentIndex = newIndex,
                    currentImageUrl = newImage?.previewUrl,
                    originalImageUrl = null,
                    zoomLevel = ImageViewerUiState.MIN_ZOOM,
                    exifData = null,
                )
            }
        }

        private fun setZoomLevel(zoom: Float) {
            val clampedZoom =
                zoom.coerceIn(
                    ImageViewerUiState.MIN_ZOOM,
                    ImageViewerUiState.MAX_ZOOM,
                )

            _uiState.update { it.copy(zoomLevel = clampedZoom) }
        }

        private fun toggleDoubleTapZoom() {
            val currentState = _uiState.value
            val newZoom =
                if (currentState.zoomLevel > ImageViewerUiState.MIN_ZOOM) {
                    ImageViewerUiState.MIN_ZOOM
                } else {
                    ImageViewerUiState.DOUBLE_TAP_ZOOM
                }

            _uiState.update { it.copy(zoomLevel = newZoom) }
        }

        private fun toggleControls() {
            _uiState.update { it.copy(showControls = !it.showControls) }
        }

        private fun showExifInfo() {
            _uiState.update { it.copy(showExifSheet = true) }
        }

        private fun hideExifInfo() {
            _uiState.update { it.copy(showExifSheet = false) }
        }

        private fun loadOriginalImage() {
            val currentState = _uiState.value
            val currentImage = currentState.currentImage ?: return

            if (currentState.isLoadingOriginal || currentState.originalImageUrl != null) return

            _uiState.update { it.copy(isLoadingOriginal = true) }

            viewModelScope.launch {
                val result =
                    if (publicFolderUrl != null) {
                        getMediaDownloadUrlUseCase.getPublicDownloadUrl(
                            publicUrl = publicFolderUrl!!,
                            path = currentImage.path,
                        )
                    } else {
                        getMediaDownloadUrlUseCase(currentImage.path)
                    }

                result
                    .onSuccess { url ->
                        _uiState.update {
                            it.copy(
                                originalImageUrl = url,
                                isLoadingOriginal = false,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoadingOriginal = false,
                                error = error.message ?: "Failed to load original image",
                            )
                        }
                    }
            }
        }

        private fun retry() {
            _uiState.update { it.copy(error = null) }
            loadImages(folderPath, _uiState.value.currentIndex, publicFolderUrl)
        }

        private fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        /**
         * Preload adjacent images for smoother swiping.
         */
        fun preloadAdjacentImages(): List<MediaFile> {
            val currentState = _uiState.value
            val result = mutableListOf<MediaFile>()

            // Preload previous image
            if (currentState.hasPreviousImage) {
                currentState.images.getOrNull(currentState.currentIndex - 1)?.let {
                    result.add(it)
                }
            }

            // Preload next image
            if (currentState.hasNextImage) {
                currentState.images.getOrNull(currentState.currentIndex + 1)?.let {
                    result.add(it)
                }
            }

            return result
        }

        companion object {
            private const val MAX_IMAGES_LOAD = 1000
        }
    }
