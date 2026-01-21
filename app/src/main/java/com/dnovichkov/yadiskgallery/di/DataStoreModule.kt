package com.dnovichkov.yadiskgallery.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for DataStore dependencies.
 *
 * Note: SettingsDataStore and TokenStorage use constructor injection
 * with @Inject and @Singleton annotations, so they are automatically
 * provided by Hilt without explicit @Provides methods.
 *
 * This module is kept for potential future bindings (e.g., interfaces).
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    // SettingsDataStore and TokenStorage are provided via constructor injection
}
