package com.dnovichkov.yadiskgallery.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

/**
 * DataStore wrapper for user settings persistence.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    private object Keys {
        val PUBLIC_FOLDER_URL = stringPreferencesKey("public_folder_url")
        val ROOT_FOLDER_PATH = stringPreferencesKey("root_folder_path")
        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }

    /**
     * Observe user settings as a Flow.
     */
    val settings: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            publicFolderUrl = preferences[Keys.PUBLIC_FOLDER_URL],
            rootFolderPath = preferences[Keys.ROOT_FOLDER_PATH],
            isAuthenticated = preferences[Keys.IS_AUTHENTICATED] ?: false,
            viewMode = preferences[Keys.VIEW_MODE]?.let { ViewMode.valueOf(it) } ?: ViewMode.GRID,
            sortOrder = preferences[Keys.SORT_ORDER]?.let { SortOrder.valueOf(it) }
                ?: SortOrder.DATE_DESC
        )
    }

    /**
     * Save user settings.
     */
    suspend fun saveSettings(settings: UserSettings) {
        dataStore.edit { preferences ->
            if (settings.publicFolderUrl != null) {
                preferences[Keys.PUBLIC_FOLDER_URL] = settings.publicFolderUrl
            } else {
                preferences.remove(Keys.PUBLIC_FOLDER_URL)
            }

            if (settings.rootFolderPath != null) {
                preferences[Keys.ROOT_FOLDER_PATH] = settings.rootFolderPath
            } else {
                preferences.remove(Keys.ROOT_FOLDER_PATH)
            }

            preferences[Keys.IS_AUTHENTICATED] = settings.isAuthenticated
            preferences[Keys.VIEW_MODE] = settings.viewMode.name
            preferences[Keys.SORT_ORDER] = settings.sortOrder.name
        }
    }

    /**
     * Update public folder URL.
     */
    suspend fun setPublicFolderUrl(url: String?) {
        dataStore.edit { preferences ->
            if (url != null) {
                preferences[Keys.PUBLIC_FOLDER_URL] = url
            } else {
                preferences.remove(Keys.PUBLIC_FOLDER_URL)
            }
        }
    }

    /**
     * Update root folder path.
     */
    suspend fun setRootFolderPath(path: String?) {
        dataStore.edit { preferences ->
            if (path != null) {
                preferences[Keys.ROOT_FOLDER_PATH] = path
            } else {
                preferences.remove(Keys.ROOT_FOLDER_PATH)
            }
        }
    }

    /**
     * Update authentication status.
     */
    suspend fun setAuthenticated(isAuthenticated: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_AUTHENTICATED] = isAuthenticated
        }
    }

    /**
     * Update view mode.
     */
    suspend fun setViewMode(viewMode: ViewMode) {
        dataStore.edit { preferences ->
            preferences[Keys.VIEW_MODE] = viewMode.name
        }
    }

    /**
     * Update sort order.
     */
    suspend fun setSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[Keys.SORT_ORDER] = sortOrder.name
        }
    }

    /**
     * Clear all settings.
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
