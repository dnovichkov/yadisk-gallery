package com.dnovichkov.yadiskgallery.data.api.dto

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ResourceDto")
class ResourceDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Nested
    @DisplayName("Deserialization")
    inner class DeserializationTests {

        @Test
        @DisplayName("should deserialize file resource")
        fun `should deserialize file resource`() {
            val jsonString = """
                {
                    "name": "photo.jpg",
                    "path": "disk:/Photos/photo.jpg",
                    "type": "file",
                    "mime_type": "image/jpeg",
                    "size": 1024,
                    "created": "2024-01-15T10:30:00+00:00",
                    "modified": "2024-01-15T10:30:00+00:00",
                    "resource_id": "123456789",
                    "md5": "abc123def456",
                    "preview": "https://downloader.disk.yandex.ru/preview/..."
                }
            """.trimIndent()

            val resource = json.decodeFromString<ResourceDto>(jsonString)

            assertEquals("photo.jpg", resource.name)
            assertEquals("disk:/Photos/photo.jpg", resource.path)
            assertEquals("file", resource.type)
            assertEquals("image/jpeg", resource.mimeType)
            assertEquals(1024L, resource.size)
            assertEquals("123456789", resource.resourceId)
            assertEquals("abc123def456", resource.md5)
            assertEquals("https://downloader.disk.yandex.ru/preview/...", resource.preview)
        }

        @Test
        @DisplayName("should deserialize folder resource")
        fun `should deserialize folder resource`() {
            val jsonString = """
                {
                    "name": "Photos",
                    "path": "disk:/Photos",
                    "type": "dir",
                    "created": "2024-01-10T08:00:00+00:00",
                    "modified": "2024-01-15T12:00:00+00:00",
                    "resource_id": "folder123"
                }
            """.trimIndent()

            val resource = json.decodeFromString<ResourceDto>(jsonString)

            assertEquals("Photos", resource.name)
            assertEquals("disk:/Photos", resource.path)
            assertEquals("dir", resource.type)
            assertEquals("folder123", resource.resourceId)
            assertNull(resource.mimeType)
            assertNull(resource.size)
            assertNull(resource.md5)
            assertNull(resource.preview)
        }

        @Test
        @DisplayName("should deserialize resource with embedded items")
        fun `should deserialize resource with embedded items`() {
            val jsonString = """
                {
                    "name": "Photos",
                    "path": "disk:/Photos",
                    "type": "dir",
                    "resource_id": "folder123",
                    "_embedded": {
                        "items": [
                            {
                                "name": "photo1.jpg",
                                "path": "disk:/Photos/photo1.jpg",
                                "type": "file",
                                "resource_id": "file1"
                            },
                            {
                                "name": "Subfolder",
                                "path": "disk:/Photos/Subfolder",
                                "type": "dir",
                                "resource_id": "folder2"
                            }
                        ],
                        "offset": 0,
                        "limit": 20,
                        "total": 2
                    }
                }
            """.trimIndent()

            val resource = json.decodeFromString<ResourceDto>(jsonString)

            assertEquals("Photos", resource.name)
            assertEquals(2, resource.embedded?.items?.size)
            assertEquals("photo1.jpg", resource.embedded?.items?.get(0)?.name)
            assertEquals("Subfolder", resource.embedded?.items?.get(1)?.name)
            assertEquals(0, resource.embedded?.offset)
            assertEquals(20, resource.embedded?.limit)
            assertEquals(2, resource.embedded?.total)
        }

        @Test
        @DisplayName("should handle missing optional fields")
        fun `should handle missing optional fields`() {
            val jsonString = """
                {
                    "name": "file.txt",
                    "path": "disk:/file.txt",
                    "type": "file"
                }
            """.trimIndent()

            val resource = json.decodeFromString<ResourceDto>(jsonString)

            assertEquals("file.txt", resource.name)
            assertNull(resource.resourceId)
            assertNull(resource.mimeType)
            assertNull(resource.size)
            assertNull(resource.created)
            assertNull(resource.modified)
            assertNull(resource.md5)
            assertNull(resource.preview)
            assertNull(resource.embedded)
        }

        @Test
        @DisplayName("should deserialize video file")
        fun `should deserialize video file`() {
            val jsonString = """
                {
                    "name": "video.mp4",
                    "path": "disk:/Videos/video.mp4",
                    "type": "file",
                    "mime_type": "video/mp4",
                    "size": 10485760,
                    "resource_id": "video123",
                    "preview": "https://downloader.disk.yandex.ru/preview/video..."
                }
            """.trimIndent()

            val resource = json.decodeFromString<ResourceDto>(jsonString)

            assertEquals("video.mp4", resource.name)
            assertEquals("video/mp4", resource.mimeType)
            assertEquals(10485760L, resource.size)
        }
    }

    @Nested
    @DisplayName("Serialization")
    inner class SerializationTests {

        @Test
        @DisplayName("should serialize resource to JSON")
        fun `should serialize resource to JSON`() {
            val resource = ResourceDto(
                name = "test.jpg",
                path = "disk:/test.jpg",
                type = "file",
                mimeType = "image/jpeg",
                size = 500L,
                resourceId = "res123"
            )

            val jsonString = json.encodeToString(ResourceDto.serializer(), resource)

            val decoded = json.decodeFromString<ResourceDto>(jsonString)
            assertEquals(resource.name, decoded.name)
            assertEquals(resource.path, decoded.path)
            assertEquals(resource.type, decoded.type)
        }
    }
}
