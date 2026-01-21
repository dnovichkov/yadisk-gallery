package com.dnovichkov.yadiskgallery.data.datastore

import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for SettingsDataStore.
 * Note: Full integration tests require Android instrumentation tests.
 */
class SettingsDataStoreTest {
    @Test
    fun `default UserSettings has correct values`() {
        val settings = UserSettings.default()

        assertNull(settings.publicFolderUrl)
        assertNull(settings.rootFolderPath)
        assertFalse(settings.isAuthenticated)
        assertEquals(ViewMode.GRID, settings.viewMode)
        assertEquals(SortOrder.DATE_DESC, settings.sortOrder)
    }

    @Test
    fun `UserSettings copy works correctly`() {
        val original = UserSettings.default()
        val updated =
            original.copy(
                publicFolderUrl = "https://disk.yandex.ru/d/test",
                isAuthenticated = true,
                viewMode = ViewMode.LIST,
            )

        assertEquals("https://disk.yandex.ru/d/test", updated.publicFolderUrl)
        assertTrue(updated.isAuthenticated)
        assertEquals(ViewMode.LIST, updated.viewMode)
        assertEquals(SortOrder.DATE_DESC, updated.sortOrder)
    }

    @Test
    fun `ViewMode enum contains expected values`() {
        val modes = ViewMode.entries

        assertEquals(2, modes.size)
        assertTrue(modes.contains(ViewMode.GRID))
        assertTrue(modes.contains(ViewMode.LIST))
    }

    @Test
    fun `SortOrder enum contains expected values`() {
        val orders = SortOrder.entries

        assertEquals(6, orders.size)
        assertTrue(orders.contains(SortOrder.NAME_ASC))
        assertTrue(orders.contains(SortOrder.NAME_DESC))
        assertTrue(orders.contains(SortOrder.DATE_ASC))
        assertTrue(orders.contains(SortOrder.DATE_DESC))
        assertTrue(orders.contains(SortOrder.SIZE_ASC))
        assertTrue(orders.contains(SortOrder.SIZE_DESC))
    }

    @Test
    fun `ViewMode valueOf works correctly`() {
        assertEquals(ViewMode.GRID, ViewMode.valueOf("GRID"))
        assertEquals(ViewMode.LIST, ViewMode.valueOf("LIST"))
    }

    @Test
    fun `SortOrder valueOf works correctly`() {
        assertEquals(SortOrder.NAME_ASC, SortOrder.valueOf("NAME_ASC"))
        assertEquals(SortOrder.DATE_DESC, SortOrder.valueOf("DATE_DESC"))
    }
}
