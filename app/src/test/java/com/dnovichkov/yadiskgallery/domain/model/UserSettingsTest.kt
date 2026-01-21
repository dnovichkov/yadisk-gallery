package com.dnovichkov.yadiskgallery.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("UserSettings")
class UserSettingsTest {

    @Nested
    @DisplayName("Creation")
    inner class Creation {

        @Test
        @DisplayName("should create UserSettings with all properties")
        fun `should create UserSettings with all properties`() {
            val settings = UserSettings(
                publicFolderUrl = "https://disk.yandex.ru/d/abc123",
                rootFolderPath = "/Photos/2024",
                isAuthenticated = true,
                viewMode = ViewMode.GRID,
                sortOrder = SortOrder.DATE_DESC
            )

            assertEquals("https://disk.yandex.ru/d/abc123", settings.publicFolderUrl)
            assertEquals("/Photos/2024", settings.rootFolderPath)
            assertTrue(settings.isAuthenticated)
            assertEquals(ViewMode.GRID, settings.viewMode)
            assertEquals(SortOrder.DATE_DESC, settings.sortOrder)
        }

        @Test
        @DisplayName("should create UserSettings with null optional fields")
        fun `should create UserSettings with null optional fields`() {
            val settings = UserSettings(
                publicFolderUrl = null,
                rootFolderPath = null,
                isAuthenticated = false,
                viewMode = ViewMode.GRID,
                sortOrder = SortOrder.NAME_ASC
            )

            assertNull(settings.publicFolderUrl)
            assertNull(settings.rootFolderPath)
            assertFalse(settings.isAuthenticated)
        }
    }

    @Nested
    @DisplayName("Default values")
    inner class DefaultValues {

        @Test
        @DisplayName("should have sensible defaults")
        fun `should have sensible defaults`() {
            val settings = UserSettings.default()

            assertNull(settings.publicFolderUrl)
            assertNull(settings.rootFolderPath)
            assertFalse(settings.isAuthenticated)
            assertEquals(ViewMode.GRID, settings.viewMode)
            assertEquals(SortOrder.DATE_DESC, settings.sortOrder)
        }
    }

    @Nested
    @DisplayName("ViewMode")
    inner class ViewModeTests {

        @Test
        @DisplayName("should have GRID mode")
        fun `should have GRID mode`() {
            val settings = createTestSettings(viewMode = ViewMode.GRID)
            assertEquals(ViewMode.GRID, settings.viewMode)
        }

        @Test
        @DisplayName("should have LIST mode")
        fun `should have LIST mode`() {
            val settings = createTestSettings(viewMode = ViewMode.LIST)
            assertEquals(ViewMode.LIST, settings.viewMode)
        }
    }

    @Nested
    @DisplayName("SortOrder")
    inner class SortOrderTests {

        @Test
        @DisplayName("should have NAME_ASC order")
        fun `should have NAME_ASC order`() {
            val settings = createTestSettings(sortOrder = SortOrder.NAME_ASC)
            assertEquals(SortOrder.NAME_ASC, settings.sortOrder)
        }

        @Test
        @DisplayName("should have NAME_DESC order")
        fun `should have NAME_DESC order`() {
            val settings = createTestSettings(sortOrder = SortOrder.NAME_DESC)
            assertEquals(SortOrder.NAME_DESC, settings.sortOrder)
        }

        @Test
        @DisplayName("should have DATE_ASC order")
        fun `should have DATE_ASC order`() {
            val settings = createTestSettings(sortOrder = SortOrder.DATE_ASC)
            assertEquals(SortOrder.DATE_ASC, settings.sortOrder)
        }

        @Test
        @DisplayName("should have DATE_DESC order")
        fun `should have DATE_DESC order`() {
            val settings = createTestSettings(sortOrder = SortOrder.DATE_DESC)
            assertEquals(SortOrder.DATE_DESC, settings.sortOrder)
        }

        @Test
        @DisplayName("should have SIZE_ASC order")
        fun `should have SIZE_ASC order`() {
            val settings = createTestSettings(sortOrder = SortOrder.SIZE_ASC)
            assertEquals(SortOrder.SIZE_ASC, settings.sortOrder)
        }

        @Test
        @DisplayName("should have SIZE_DESC order")
        fun `should have SIZE_DESC order`() {
            val settings = createTestSettings(sortOrder = SortOrder.SIZE_DESC)
            assertEquals(SortOrder.SIZE_DESC, settings.sortOrder)
        }
    }

    @Nested
    @DisplayName("Equality")
    inner class Equality {

        @Test
        @DisplayName("should be equal when all properties match")
        fun `should be equal when all properties match`() {
            val settings1 = UserSettings(
                publicFolderUrl = "url",
                rootFolderPath = "/path",
                isAuthenticated = true,
                viewMode = ViewMode.GRID,
                sortOrder = SortOrder.DATE_DESC
            )

            val settings2 = UserSettings(
                publicFolderUrl = "url",
                rootFolderPath = "/path",
                isAuthenticated = true,
                viewMode = ViewMode.GRID,
                sortOrder = SortOrder.DATE_DESC
            )

            assertEquals(settings1, settings2)
            assertEquals(settings1.hashCode(), settings2.hashCode())
        }
    }

    private fun createTestSettings(
        publicFolderUrl: String? = null,
        rootFolderPath: String? = null,
        isAuthenticated: Boolean = false,
        viewMode: ViewMode = ViewMode.GRID,
        sortOrder: SortOrder = SortOrder.DATE_DESC
    ) = UserSettings(
        publicFolderUrl = publicFolderUrl,
        rootFolderPath = rootFolderPath,
        isAuthenticated = isAuthenticated,
        viewMode = viewMode,
        sortOrder = sortOrder
    )
}
