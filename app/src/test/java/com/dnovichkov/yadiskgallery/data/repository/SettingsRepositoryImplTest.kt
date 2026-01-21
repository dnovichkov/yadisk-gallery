package com.dnovichkov.yadiskgallery.data.repository

import app.cash.turbine.test
import com.dnovichkov.yadiskgallery.data.datastore.SettingsDataStore
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SettingsRepositoryImplTest {

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: SettingsRepositoryImpl

    @BeforeEach
    fun setup() {
        settingsDataStore = mockk(relaxed = true)
        repository = SettingsRepositoryImpl(settingsDataStore)
    }

    @Test
    fun `observeSettings returns flow from data store`() = runTest {
        val settings = UserSettings.default()
        every { settingsDataStore.settings } returns flowOf(settings)

        repository.observeSettings().test {
            assertEquals(settings, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getSettings returns current settings`() = runTest {
        val settings = UserSettings.default()
        every { settingsDataStore.settings } returns flowOf(settings)

        val result = repository.getSettings()

        assertTrue(result.isSuccess)
        assertEquals(settings, result.getOrNull())
    }

    @Test
    fun `setPublicFolderUrl delegates to data store`() = runTest {
        val url = "https://disk.yandex.ru/d/test"
        coEvery { settingsDataStore.setPublicFolderUrl(url) } returns Unit

        val result = repository.setPublicFolderUrl(url)

        assertTrue(result.isSuccess)
        coVerify { settingsDataStore.setPublicFolderUrl(url) }
    }

    @Test
    fun `setPublicFolderUrl with null clears url`() = runTest {
        coEvery { settingsDataStore.setPublicFolderUrl(null) } returns Unit

        val result = repository.setPublicFolderUrl(null)

        assertTrue(result.isSuccess)
        coVerify { settingsDataStore.setPublicFolderUrl(null) }
    }

    @Test
    fun `setRootFolderPath delegates to data store`() = runTest {
        val path = "/Photos/2024"
        coEvery { settingsDataStore.setRootFolderPath(path) } returns Unit

        val result = repository.setRootFolderPath(path)

        assertTrue(result.isSuccess)
        coVerify { settingsDataStore.setRootFolderPath(path) }
    }

    @Test
    fun `setViewMode delegates to data store`() = runTest {
        coEvery { settingsDataStore.setViewMode(ViewMode.LIST) } returns Unit

        val result = repository.setViewMode(ViewMode.LIST)

        assertTrue(result.isSuccess)
        coVerify { settingsDataStore.setViewMode(ViewMode.LIST) }
    }

    @Test
    fun `setSortOrder delegates to data store`() = runTest {
        coEvery { settingsDataStore.setSortOrder(SortOrder.NAME_ASC) } returns Unit

        val result = repository.setSortOrder(SortOrder.NAME_ASC)

        assertTrue(result.isSuccess)
        coVerify { settingsDataStore.setSortOrder(SortOrder.NAME_ASC) }
    }

    @Test
    fun `clearSettings delegates to data store`() = runTest {
        coEvery { settingsDataStore.clear() } returns Unit

        val result = repository.clearSettings()

        assertTrue(result.isSuccess)
        coVerify { settingsDataStore.clear() }
    }

    @Test
    fun `setPublicFolderUrl returns failure on exception`() = runTest {
        val exception = RuntimeException("DataStore error")
        coEvery { settingsDataStore.setPublicFolderUrl(any()) } throws exception

        val result = repository.setPublicFolderUrl("url")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
