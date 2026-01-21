package com.dnovichkov.yadiskgallery.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("PagedResult")
class PagedResultTest {
    @Nested
    @DisplayName("Creation")
    inner class Creation {
        @Test
        @DisplayName("should create PagedResult with all properties")
        fun `should create PagedResult with all properties`() {
            val items = listOf("item1", "item2", "item3")

            val result =
                PagedResult(
                    items = items,
                    offset = 0,
                    limit = 20,
                    total = 100,
                    hasMore = true,
                )

            assertEquals(items, result.items)
            assertEquals(0, result.offset)
            assertEquals(20, result.limit)
            assertEquals(100, result.total)
            assertTrue(result.hasMore)
        }

        @Test
        @DisplayName("should create PagedResult with no more items")
        fun `should create PagedResult with no more items`() {
            val items = listOf("item1", "item2")

            val result =
                PagedResult(
                    items = items,
                    offset = 80,
                    limit = 20,
                    total = 82,
                    hasMore = false,
                )

            assertEquals(2, result.items.size)
            assertEquals(80, result.offset)
            assertFalse(result.hasMore)
        }

        @Test
        @DisplayName("should create empty PagedResult")
        fun `should create empty PagedResult`() {
            val result =
                PagedResult<String>(
                    items = emptyList(),
                    offset = 0,
                    limit = 20,
                    total = 0,
                    hasMore = false,
                )

            assertTrue(result.items.isEmpty())
            assertEquals(0, result.total)
            assertFalse(result.hasMore)
        }

        @Test
        @DisplayName("should create PagedResult with null total")
        fun `should create PagedResult with null total`() {
            val result =
                PagedResult(
                    items = listOf("item"),
                    offset = 0,
                    limit = 20,
                    total = null,
                    hasMore = true,
                )

            assertNull(result.total)
            assertTrue(result.hasMore)
        }
    }

    @Nested
    @DisplayName("Generic Type")
    inner class GenericType {
        @Test
        @DisplayName("should work with DiskItem type")
        fun `should work with DiskItem type`() {
            val mediaFile =
                MediaFile(
                    id = "1",
                    name = "photo.jpg",
                    path = "/photo.jpg",
                    type = MediaType.IMAGE,
                    mimeType = "image/jpeg",
                    size = 100L,
                    createdAt = null,
                    modifiedAt = null,
                    previewUrl = null,
                    md5 = null,
                )
            val folder =
                Folder(
                    id = "2",
                    name = "folder",
                    path = "/folder",
                    itemsCount = 5,
                    createdAt = null,
                    modifiedAt = null,
                )

            val items =
                listOf(
                    DiskItem.File(mediaFile),
                    DiskItem.Directory(folder),
                )

            val result =
                PagedResult(
                    items = items,
                    offset = 0,
                    limit = 20,
                    total = 2,
                    hasMore = false,
                )

            assertEquals(2, result.items.size)
            assertTrue(result.items[0] is DiskItem.File)
            assertTrue(result.items[1] is DiskItem.Directory)
        }

        @Test
        @DisplayName("should work with MediaFile type")
        fun `should work with MediaFile type`() {
            val files =
                listOf(
                    createTestMediaFile(id = "1"),
                    createTestMediaFile(id = "2"),
                )

            val result =
                PagedResult(
                    items = files,
                    offset = 0,
                    limit = 20,
                    total = 2,
                    hasMore = false,
                )

            assertEquals(2, result.items.size)
            assertEquals("1", result.items[0].id)
            assertEquals("2", result.items[1].id)
        }
    }

    @Nested
    @DisplayName("Pagination Calculations")
    inner class PaginationCalculations {
        @Test
        @DisplayName("should calculate next offset correctly")
        fun `should calculate next offset correctly`() {
            val result =
                PagedResult(
                    items = (1..20).toList(),
                    offset = 0,
                    limit = 20,
                    total = 100,
                    hasMore = true,
                )

            assertEquals(20, result.nextOffset)
        }

        @Test
        @DisplayName("should return correct isEmpty value")
        fun `should return correct isEmpty value`() {
            val emptyResult =
                PagedResult<String>(
                    items = emptyList(),
                    offset = 0,
                    limit = 20,
                    total = 0,
                    hasMore = false,
                )

            val nonEmptyResult =
                PagedResult(
                    items = listOf("item"),
                    offset = 0,
                    limit = 20,
                    total = 1,
                    hasMore = false,
                )

            assertTrue(emptyResult.isEmpty)
            assertFalse(nonEmptyResult.isEmpty)
        }

        @Test
        @DisplayName("should return correct isNotEmpty value")
        fun `should return correct isNotEmpty value`() {
            val result =
                PagedResult(
                    items = listOf("item"),
                    offset = 0,
                    limit = 20,
                    total = 1,
                    hasMore = false,
                )

            assertTrue(result.isNotEmpty)
        }
    }

    @Nested
    @DisplayName("Equality")
    inner class Equality {
        @Test
        @DisplayName("should be equal when all properties match")
        fun `should be equal when all properties match`() {
            val items = listOf("a", "b")

            val result1 =
                PagedResult(
                    items = items,
                    offset = 0,
                    limit = 20,
                    total = 2,
                    hasMore = false,
                )

            val result2 =
                PagedResult(
                    items = items,
                    offset = 0,
                    limit = 20,
                    total = 2,
                    hasMore = false,
                )

            assertEquals(result1, result2)
            assertEquals(result1.hashCode(), result2.hashCode())
        }
    }

    private fun createTestMediaFile(id: String) =
        MediaFile(
            id = id,
            name = "test.jpg",
            path = "/test.jpg",
            type = MediaType.IMAGE,
            mimeType = "image/jpeg",
            size = 100L,
            createdAt = null,
            modifiedAt = null,
            previewUrl = null,
            md5 = null,
        )
}
