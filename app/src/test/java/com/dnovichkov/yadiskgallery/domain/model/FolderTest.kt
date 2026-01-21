package com.dnovichkov.yadiskgallery.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("Folder")
class FolderTest {

    @Nested
    @DisplayName("Creation")
    inner class Creation {

        @Test
        @DisplayName("should create Folder with all properties")
        fun `should create Folder with all properties`() {
            val createdAt = Instant.now()
            val modifiedAt = Instant.now()

            val folder = Folder(
                id = "folder-123",
                name = "Photos",
                path = "/Photos",
                itemsCount = 42,
                createdAt = createdAt,
                modifiedAt = modifiedAt
            )

            assertEquals("folder-123", folder.id)
            assertEquals("Photos", folder.name)
            assertEquals("/Photos", folder.path)
            assertEquals(42, folder.itemsCount)
            assertEquals(createdAt, folder.createdAt)
            assertEquals(modifiedAt, folder.modifiedAt)
        }

        @Test
        @DisplayName("should create Folder with optional fields as null")
        fun `should create Folder with optional fields as null`() {
            val folder = Folder(
                id = "folder-456",
                name = "Videos",
                path = "/Videos",
                itemsCount = null,
                createdAt = null,
                modifiedAt = null
            )

            assertEquals("folder-456", folder.id)
            assertEquals(null, folder.itemsCount)
            assertEquals(null, folder.createdAt)
            assertEquals(null, folder.modifiedAt)
        }

        @Test
        @DisplayName("should create Folder with zero items")
        fun `should create Folder with zero items`() {
            val folder = Folder(
                id = "empty-folder",
                name = "Empty",
                path = "/Empty",
                itemsCount = 0,
                createdAt = null,
                modifiedAt = null
            )

            assertEquals(0, folder.itemsCount)
        }
    }

    @Nested
    @DisplayName("Equality")
    inner class Equality {

        @Test
        @DisplayName("should be equal when all properties match")
        fun `should be equal when all properties match`() {
            val timestamp = Instant.now()

            val folder1 = Folder(
                id = "folder-1",
                name = "Test",
                path = "/Test",
                itemsCount = 10,
                createdAt = timestamp,
                modifiedAt = timestamp
            )

            val folder2 = Folder(
                id = "folder-1",
                name = "Test",
                path = "/Test",
                itemsCount = 10,
                createdAt = timestamp,
                modifiedAt = timestamp
            )

            assertEquals(folder1, folder2)
            assertEquals(folder1.hashCode(), folder2.hashCode())
        }

        @Test
        @DisplayName("should not be equal when id differs")
        fun `should not be equal when id differs`() {
            val folder1 = createTestFolder(id = "folder-1")
            val folder2 = createTestFolder(id = "folder-2")

            assertNotEquals(folder1, folder2)
        }

        @Test
        @DisplayName("should not be equal when path differs")
        fun `should not be equal when path differs`() {
            val folder1 = createTestFolder(path = "/path1")
            val folder2 = createTestFolder(path = "/path2")

            assertNotEquals(folder1, folder2)
        }
    }

    private fun createTestFolder(
        id: String = "test-id",
        name: String = "TestFolder",
        path: String = "/TestFolder",
        itemsCount: Int? = null
    ) = Folder(
        id = id,
        name = name,
        path = path,
        itemsCount = itemsCount,
        createdAt = null,
        modifiedAt = null
    )
}
