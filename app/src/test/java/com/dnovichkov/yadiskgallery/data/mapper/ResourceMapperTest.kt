package com.dnovichkov.yadiskgallery.data.mapper

import com.dnovichkov.yadiskgallery.data.api.dto.EmbeddedResourcesDto
import com.dnovichkov.yadiskgallery.data.api.dto.ResourceDto
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ResourceMapper")
class ResourceMapperTest {

    private val mapper = ResourceMapper()

    @Nested
    @DisplayName("toMediaFile()")
    inner class ToMediaFileTests {

        @Test
        @DisplayName("should map image file correctly")
        fun `should map image file correctly`() {
            val dto = ResourceDto(
                name = "photo.jpg",
                path = "disk:/Photos/photo.jpg",
                type = "file",
                resourceId = "res123",
                mimeType = "image/jpeg",
                size = 1024L,
                created = "2024-01-15T10:30:00+00:00",
                modified = "2024-01-15T12:00:00+00:00",
                md5 = "abc123",
                preview = "https://preview.url"
            )

            val mediaFile = mapper.toMediaFile(dto)

            assertEquals("res123", mediaFile.id)
            assertEquals("photo.jpg", mediaFile.name)
            assertEquals("disk:/Photos/photo.jpg", mediaFile.path)
            assertEquals(MediaType.IMAGE, mediaFile.type)
            assertEquals("image/jpeg", mediaFile.mimeType)
            assertEquals(1024L, mediaFile.size)
            assertEquals("abc123", mediaFile.md5)
            assertEquals("https://preview.url", mediaFile.previewUrl)
            assertNotNull(mediaFile.createdAt)
            assertNotNull(mediaFile.modifiedAt)
        }

        @Test
        @DisplayName("should map video file correctly")
        fun `should map video file correctly`() {
            val dto = ResourceDto(
                name = "video.mp4",
                path = "disk:/Videos/video.mp4",
                type = "file",
                resourceId = "vid123",
                mimeType = "video/mp4",
                size = 10485760L
            )

            val mediaFile = mapper.toMediaFile(dto)

            assertEquals(MediaType.VIDEO, mediaFile.type)
            assertEquals("video/mp4", mediaFile.mimeType)
        }

        @Test
        @DisplayName("should detect media type from mime type")
        fun `should detect media type from mime type`() {
            val imageTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp", "image/heic")
            val videoTypes = listOf("video/mp4", "video/quicktime", "video/x-msvideo", "video/x-matroska")

            imageTypes.forEach { mimeType ->
                val dto = createFileDto(mimeType = mimeType)
                val mediaFile = mapper.toMediaFile(dto)
                assertEquals(MediaType.IMAGE, mediaFile.type, "Failed for $mimeType")
            }

            videoTypes.forEach { mimeType ->
                val dto = createFileDto(mimeType = mimeType)
                val mediaFile = mapper.toMediaFile(dto)
                assertEquals(MediaType.VIDEO, mediaFile.type, "Failed for $mimeType")
            }
        }

        @Test
        @DisplayName("should use path as id when resourceId is null")
        fun `should use path as id when resourceId is null`() {
            val dto = ResourceDto(
                name = "file.jpg",
                path = "disk:/file.jpg",
                type = "file",
                resourceId = null,
                mimeType = "image/jpeg",
                size = 100L
            )

            val mediaFile = mapper.toMediaFile(dto)

            assertEquals("disk:/file.jpg", mediaFile.id)
        }

        @Test
        @DisplayName("should handle null optional fields")
        fun `should handle null optional fields`() {
            val dto = ResourceDto(
                name = "file.jpg",
                path = "disk:/file.jpg",
                type = "file",
                mimeType = "image/jpeg",
                size = 100L
            )

            val mediaFile = mapper.toMediaFile(dto)

            assertNull(mediaFile.createdAt)
            assertNull(mediaFile.modifiedAt)
            assertNull(mediaFile.md5)
            assertNull(mediaFile.previewUrl)
        }
    }

    @Nested
    @DisplayName("toFolder()")
    inner class ToFolderTests {

        @Test
        @DisplayName("should map folder correctly")
        fun `should map folder correctly`() {
            val dto = ResourceDto(
                name = "Photos",
                path = "disk:/Photos",
                type = "dir",
                resourceId = "folder123",
                created = "2024-01-10T08:00:00+00:00",
                modified = "2024-01-15T12:00:00+00:00",
                embedded = EmbeddedResourcesDto(
                    items = emptyList(),
                    offset = 0,
                    limit = 20,
                    total = 42
                )
            )

            val folder = mapper.toFolder(dto)

            assertEquals("folder123", folder.id)
            assertEquals("Photos", folder.name)
            assertEquals("disk:/Photos", folder.path)
            assertEquals(42, folder.itemsCount)
            assertNotNull(folder.createdAt)
            assertNotNull(folder.modifiedAt)
        }

        @Test
        @DisplayName("should handle folder without embedded data")
        fun `should handle folder without embedded data`() {
            val dto = ResourceDto(
                name = "Empty",
                path = "disk:/Empty",
                type = "dir",
                resourceId = "folder456"
            )

            val folder = mapper.toFolder(dto)

            assertEquals("folder456", folder.id)
            assertNull(folder.itemsCount)
        }
    }

    @Nested
    @DisplayName("toDiskItem()")
    inner class ToDiskItemTests {

        @Test
        @DisplayName("should map file to DiskItem.File")
        fun `should map file to DiskItem File`() {
            val dto = ResourceDto(
                name = "photo.jpg",
                path = "disk:/photo.jpg",
                type = "file",
                resourceId = "file123",
                mimeType = "image/jpeg",
                size = 100L
            )

            val diskItem = mapper.toDiskItem(dto)

            assertTrue(diskItem is DiskItem.File)
            assertEquals("file123", diskItem.id)
        }

        @Test
        @DisplayName("should map directory to DiskItem.Directory")
        fun `should map directory to DiskItem Directory`() {
            val dto = ResourceDto(
                name = "Folder",
                path = "disk:/Folder",
                type = "dir",
                resourceId = "folder123"
            )

            val diskItem = mapper.toDiskItem(dto)

            assertTrue(diskItem is DiskItem.Directory)
            assertEquals("folder123", diskItem.id)
        }
    }

    @Nested
    @DisplayName("toDiskItems()")
    inner class ToDiskItemsTests {

        @Test
        @DisplayName("should map list of resources")
        fun `should map list of resources`() {
            val dtos = listOf(
                ResourceDto("photo.jpg", "disk:/photo.jpg", "file", "f1", "image/jpeg", 100L),
                ResourceDto("Folder", "disk:/Folder", "dir", "d1"),
                ResourceDto("video.mp4", "disk:/video.mp4", "file", "f2", "video/mp4", 1000L)
            )

            val items = mapper.toDiskItems(dtos)

            assertEquals(3, items.size)
            assertTrue(items[0] is DiskItem.File)
            assertTrue(items[1] is DiskItem.Directory)
            assertTrue(items[2] is DiskItem.File)
        }

        @Test
        @DisplayName("should filter out unsupported types when mediaOnly is true")
        fun `should filter out unsupported types when mediaOnly is true`() {
            val dtos = listOf(
                ResourceDto("photo.jpg", "disk:/photo.jpg", "file", "f1", "image/jpeg", 100L),
                ResourceDto("doc.pdf", "disk:/doc.pdf", "file", "f2", "application/pdf", 500L),
                ResourceDto("video.mp4", "disk:/video.mp4", "file", "f3", "video/mp4", 1000L)
            )

            val items = mapper.toDiskItems(dtos, mediaOnly = true)

            assertEquals(2, items.size)
        }
    }

    private fun createFileDto(
        name: String = "file",
        path: String = "disk:/file",
        mimeType: String = "image/jpeg",
        size: Long = 100L
    ) = ResourceDto(
        name = name,
        path = path,
        type = "file",
        mimeType = mimeType,
        size = size
    )
}
