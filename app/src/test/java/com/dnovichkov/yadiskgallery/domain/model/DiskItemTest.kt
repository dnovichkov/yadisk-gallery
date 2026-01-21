package com.dnovichkov.yadiskgallery.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("DiskItem")
class DiskItemTest {
    @Nested
    @DisplayName("DiskItem.File")
    inner class FileTests {
        @Test
        @DisplayName("should wrap MediaFile correctly")
        fun `should wrap MediaFile correctly`() {
            val mediaFile = createTestMediaFile()
            val diskItem = DiskItem.File(mediaFile)

            assertEquals(mediaFile, diskItem.mediaFile)
            assertEquals(mediaFile.id, diskItem.id)
            assertEquals(mediaFile.name, diskItem.name)
            assertEquals(mediaFile.path, diskItem.path)
        }

        @Test
        @DisplayName("should be instance of DiskItem")
        fun `should be instance of DiskItem`() {
            val diskItem: DiskItem = DiskItem.File(createTestMediaFile())
            assertTrue(diskItem is DiskItem.File)
        }
    }

    @Nested
    @DisplayName("DiskItem.Directory")
    inner class DirectoryTests {
        @Test
        @DisplayName("should wrap Folder correctly")
        fun `should wrap Folder correctly`() {
            val folder = createTestFolder()
            val diskItem = DiskItem.Directory(folder)

            assertEquals(folder, diskItem.folder)
            assertEquals(folder.id, diskItem.id)
            assertEquals(folder.name, diskItem.name)
            assertEquals(folder.path, diskItem.path)
        }

        @Test
        @DisplayName("should be instance of DiskItem")
        fun `should be instance of DiskItem`() {
            val diskItem: DiskItem = DiskItem.Directory(createTestFolder())
            assertTrue(diskItem is DiskItem.Directory)
        }
    }

    @Nested
    @DisplayName("Polymorphism")
    inner class PolymorphismTests {
        @Test
        @DisplayName("should handle mixed list of DiskItems")
        fun `should handle mixed list of DiskItems`() {
            val items: List<DiskItem> =
                listOf(
                    DiskItem.File(createTestMediaFile(id = "file-1", name = "photo.jpg")),
                    DiskItem.Directory(createTestFolder(id = "folder-1", name = "Photos")),
                    DiskItem.File(createTestMediaFile(id = "file-2", name = "video.mp4")),
                )

            assertEquals(3, items.size)
            assertTrue(items[0] is DiskItem.File)
            assertTrue(items[1] is DiskItem.Directory)
            assertTrue(items[2] is DiskItem.File)
        }

        @Test
        @DisplayName("should access common properties through sealed interface")
        fun `should access common properties through sealed interface`() {
            val file: DiskItem = DiskItem.File(createTestMediaFile(id = "f1", name = "test.jpg", path = "/test.jpg"))
            val directory: DiskItem = DiskItem.Directory(createTestFolder(id = "d1", name = "folder", path = "/folder"))

            assertEquals("f1", file.id)
            assertEquals("test.jpg", file.name)
            assertEquals("/test.jpg", file.path)

            assertEquals("d1", directory.id)
            assertEquals("folder", directory.name)
            assertEquals("/folder", directory.path)
        }

        @Test
        @DisplayName("should work with when expression exhaustively")
        fun `should work with when expression exhaustively`() {
            val items =
                listOf(
                    DiskItem.File(createTestMediaFile()),
                    DiskItem.Directory(createTestFolder()),
                )

            val results =
                items.map { item ->
                    when (item) {
                        is DiskItem.File -> "file:${item.mediaFile.name}"
                        is DiskItem.Directory -> "dir:${item.folder.name}"
                    }
                }

            assertEquals("file:test.jpg", results[0])
            assertEquals("dir:TestFolder", results[1])
        }
    }

    private fun createTestMediaFile(
        id: String = "test-id",
        name: String = "test.jpg",
        path: String = "/test.jpg",
        type: MediaType = MediaType.IMAGE,
        mimeType: String = "image/jpeg",
        size: Long = 100L,
    ) = MediaFile(
        id = id,
        name = name,
        path = path,
        type = type,
        mimeType = mimeType,
        size = size,
        createdAt = null,
        modifiedAt = null,
        previewUrl = null,
        md5 = null,
    )

    private fun createTestFolder(
        id: String = "test-id",
        name: String = "TestFolder",
        path: String = "/TestFolder",
        itemsCount: Int? = null,
    ) = Folder(
        id = id,
        name = name,
        path = path,
        itemsCount = itemsCount,
        createdAt = null,
        modifiedAt = null,
    )
}
