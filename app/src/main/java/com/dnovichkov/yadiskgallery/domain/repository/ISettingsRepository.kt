package com.dnovichkov.yadiskgallery.domain.repository

import com.dnovichkov.yadiskgallery.domain.model.DomainError
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user settings.
 * Settings are persisted locally using DataStore.
 */
interface ISettingsRepository {

    /**
     * Observes the current user settings.
     * Emits new values whenever settings change.
     *
     * @return Flow of user settings
     */
    fun observeSettings(): Flow<UserSettings>

    /**
     * Gets the current user settings.
     *
     * @return Result containing settings or error
     */
    suspend fun getSettings(): Result<UserSettings>

    /**
     * Saves the public folder URL.
     *
     * @param url The public folder URL (can be null to clear)
     * @return Result indicating success or error
     */
    suspend fun setPublicFolderUrl(url: String?): Result<Unit>

    /**
     * Saves the root folder path.
     *
     * @param path The root folder path (can be null for disk root)
     * @return Result indicating success or error
     */
    suspend fun setRootFolderPath(path: String?): Result<Unit>

    /**
     * Saves the view mode preference.
     *
     * @param viewMode The view mode (GRID or LIST)
     * @return Result indicating success or error
     */
    suspend fun setViewMode(viewMode: ViewMode): Result<Unit>

    /**
     * Saves the sort order preference.
     *
     * @param sortOrder The sort order
     * @return Result indicating success or error
     */
    suspend fun setSortOrder(sortOrder: SortOrder): Result<Unit>

    /**
     * Clears all settings and resets to defaults.
     *
     * @return Result indicating success or error
     */
    suspend fun clearSettings(): Result<Unit>
}

/**
 * Extension function to convert DomainError to Result.failure
 */
fun <T> DomainError.toFailure(): Result<T> = Result.failure(DomainException(this))

/**
 * Exception wrapper for DomainError to use with Kotlin Result.
 */
class DomainException(val error: DomainError) : Exception(error.message, error.cause)
