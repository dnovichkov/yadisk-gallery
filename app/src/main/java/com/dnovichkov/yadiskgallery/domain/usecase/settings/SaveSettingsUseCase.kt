package com.dnovichkov.yadiskgallery.domain.usecase.settings

import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.repository.ISettingsRepository
import javax.inject.Inject

/**
 * Use case for saving user settings.
 */
class SaveSettingsUseCase
    @Inject
    constructor(
        private val settingsRepository: ISettingsRepository,
    ) {
        /**
         * Saves the public folder URL.
         *
         * @param url The public folder URL (can be null to clear)
         * @return Result indicating success or error
         */
        suspend fun setPublicFolderUrl(url: String?): Result<Unit> {
            return settingsRepository.setPublicFolderUrl(url)
        }

        /**
         * Saves the root folder path.
         *
         * @param path The root folder path (can be null for disk root)
         * @return Result indicating success or error
         */
        suspend fun setRootFolderPath(path: String?): Result<Unit> {
            return settingsRepository.setRootFolderPath(path)
        }

        /**
         * Saves the view mode preference.
         *
         * @param viewMode The view mode (GRID or LIST)
         * @return Result indicating success or error
         */
        suspend fun setViewMode(viewMode: ViewMode): Result<Unit> {
            return settingsRepository.setViewMode(viewMode)
        }

        /**
         * Saves the sort order preference.
         *
         * @param sortOrder The sort order
         * @return Result indicating success or error
         */
        suspend fun setSortOrder(sortOrder: SortOrder): Result<Unit> {
            return settingsRepository.setSortOrder(sortOrder)
        }

        /**
         * Clears all settings and resets to defaults.
         *
         * @return Result indicating success or error
         */
        suspend fun clearSettings(): Result<Unit> {
            return settingsRepository.clearSettings()
        }
    }
