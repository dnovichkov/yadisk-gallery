package com.dnovichkov.yadiskgallery.di

import com.dnovichkov.yadiskgallery.data.repository.AuthRepositoryImpl
import com.dnovichkov.yadiskgallery.data.repository.CacheRepositoryImpl
import com.dnovichkov.yadiskgallery.data.repository.FilesRepositoryImpl
import com.dnovichkov.yadiskgallery.data.repository.SettingsRepositoryImpl
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import com.dnovichkov.yadiskgallery.domain.repository.ICacheRepository
import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import com.dnovichkov.yadiskgallery.domain.repository.ISettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): ISettingsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindFilesRepository(impl: FilesRepositoryImpl): IFilesRepository

    @Binds
    @Singleton
    abstract fun bindCacheRepository(impl: CacheRepositoryImpl): ICacheRepository
}
