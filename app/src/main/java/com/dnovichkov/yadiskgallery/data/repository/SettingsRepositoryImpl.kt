package com.dnovichkov.yadiskgallery.data.repository

import com.dnovichkov.yadiskgallery.data.datastore.SettingsDataStore
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ISettingsRepository using DataStore.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ISettingsRepository {

    override fun observeSettings(): Flow<UserSettings> {
        return settingsDataStore.settings
    }

    override suspend fun getSettings(): Result<UserSettings> {
        return runCatching {
            settingsDataStore.settings.first()
        }
    }

    override suspend fun setPublicFolderUrl(url: String?): Result<Unit> {
        return runCatching {
            settingsDataStore.setPublicFolderUrl(url)
        }
    }

    override suspend fun setRootFolderPath(path: String?): Result<Unit> {
        return runCatching {
            settingsDataStore.setRootFolderPath(path)
        }
    }

    override suspend fun setViewMode(viewMode: ViewMode): Result<Unit> {
        return runCatching {
            settingsDataStore.setViewMode(viewMode)
        }
    }

    override suspend fun setSortOrder(sortOrder: SortOrder): Result<Unit> {
        return runCatching {
            settingsDataStore.setSortOrder(sortOrder)
        }
    }

    override suspend fun clearSettings(): Result<Unit> {
        return runCatching {
            settingsDataStore.clear()
        }
    }
}
