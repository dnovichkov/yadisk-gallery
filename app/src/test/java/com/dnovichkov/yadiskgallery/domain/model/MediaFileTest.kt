package com.dnovichkov.yadiskgallery.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("MediaFile")
class MediaFileTest {

    @Nested
    @DisplayName("Creation")
    inner class Creation {

        @Test
        @DisplayName("should create MediaFile with all properties")
        fun `should create MediaFile with all properties`() {
            val createdAt = Instant.now()
            val modifiedAt = Instant.now()

            val mediaFile = MediaFile(
                id = "file-123",
                name = "photo.jpg",
                path = "/photos/photo.jpg",
                type = MediaType.IMAGE,
                mimeType = "image/jpeg",
                size = 1024L,
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                previewUrl = "https://example.com/preview.jpg",
                md5 = "abc123def456"
            )

            assertEquals("file-123", mediaFile.id)
            assertEquals("photo.jpg", mediaFile.name)
            assertEquals("/photos/photo.jpg", mediaFile.path)
            assertEquals(MediaType.IMAGE, mediaFile.type)
            assertEquals("image/jpeg", mediaFile.mimeType)
            assertEquals(1024L, mediaFile.size)
            assertEquals(createdAt, mediaFile.createdAt)
            assertEquals(modifiedAt, mediaFile.modifiedAt)
            assertEquals("https://example.com/preview.jpg", mediaFile.previewUrl)
            assertEquals("abc123def456", mediaFile.md5)
        }

        @Test
        @DisplayName("should create MediaFile with optional fields as null")
        fun `should create MediaFile with optional fields as null`() {
            val mediaFile = MediaFile(
                id = "file-456",
                name = "video.mp4",
                path = "/videos/video.mp4",
                type = MediaType.VIDEO,
                mimeType = "video/mp4",
                size = 2048L,
                createdAt = null,
                modifiedAt = null,
                previewUrl = null,
                md5 = null
            )

            assertEquals("file-456", mediaFile.id)
            assertEquals(null, mediaFile.createdAt)
            assertEquals(null, mediaFile.modifiedAt)
            assertEquals(null, mediaFile.previewUrl)
            assertEquals(null, mediaFile.md5)
        }
    }

    @Nested
    @DisplayName("Equality")
    inner class Equality {

        @Test
        @DisplayName("should be equal when all properties match")
        fun `should be equal when all properties match`() {
            val timestamp = Instant.now()

            val file1 = MediaFile(
                id = "file-1",
                name = "test.jpg",
                path = "/test.jpg",
                type = MediaType.IMAGE,
                mimeType = "image/jpeg",
                size = 100L,
                createdAt = timestamp,
                modifiedAt = timestamp,
                previewUrl = "url",
                md5 = "hash"
            )

            val file2 = MediaFile(
                id = "file-1",
                name = "test.jpg",
                path = "/test.jpg",
                type = MediaType.IMAGE,
                mimeType = "image/jpeg",
                size = 100L,
                createdAt = timestamp,
                modifiedAt = timestamp,
                previewUrl = "url",
                md5 = "hash"
            )

            assertEquals(file1, file2)
            assertEquals(file1.hashCode(), file2.hashCode())
        }

        @Test
        @DisplayName("should not be equal when id differs")
        fun `should not be equal when id differs`() {
            val file1 = createTestMediaFile(id = "file-1")
            val file2 = createTestMediaFile(id = "file-2")

            assertNotEquals(file1, file2)
        }
    }

    @Nested
    @DisplayName("MediaType")
    inner class MediaTypeTests {

        @Test
        @DisplayName("should have IMAGE type")
        fun `should have IMAGE type`() {
            val file = createTestMediaFile(type = MediaType.IMAGE)
            assertEquals(MediaType.IMAGE, file.type)
        }

        @Test
        @DisplayName("should have VIDEO type")
        fun `should have VIDEO type`() {
            val file = createTestMediaFile(type = MediaType.VIDEO)
            assertEquals(MediaType.VIDEO, file.type)
        }
    }

    private fun createTestMediaFile(
        id: String = "test-id",
        name: String = "test.jpg",
        path: String = "/test.jpg",
        type: MediaType = MediaType.IMAGE,
        mimeType: String = "image/jpeg",
        size: Long = 100L
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
        md5 = null
    )
}
