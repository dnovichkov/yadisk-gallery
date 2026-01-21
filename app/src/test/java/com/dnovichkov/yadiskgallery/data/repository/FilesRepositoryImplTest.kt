package com.dnovichkov.yadiskgallery.data.repository

import com.dnovichkov.yadiskgallery.data.api.YandexDiskApi
import com.dnovichkov.yadiskgallery.data.api.dto.DownloadLinkDto
import com.dnovichkov.yadiskgallery.data.api.dto.EmbeddedResourcesDto
import com.dnovichkov.yadiskgallery.data.api.dto.PublicResourceDto
import com.dnovichkov.yadiskgallery.data.api.dto.ResourceDto
import com.dnovichkov.yadiskgallery.data.cache.dao.CacheMetadataDao
import com.dnovichkov.yadiskgallery.data.cache.dao.FolderDao
import com.dnovichkov.yadiskgallery.data.cache.dao.MediaDao
import com.dnovichkov.yadiskgallery.data.mapper.ResourceMapper
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.repository.PreviewSize
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

class FilesRepositoryImplTest {

    private lateinit var api: YandexDiskApi
    private lateinit var mediaDao: MediaDao
    private lateinit var folderDao: FolderDao
    private lateinit var cacheMetadataDao: CacheMetadataDao
    private lateinit var resourceMapper: ResourceMapper
    private lateinit var repository: FilesRepositoryImpl

    @BeforeEach
    fun setup() {
        api = mockk(relaxed = true)
        mediaDao = mockk(relaxed = true)
        folderDao = mockk(relaxed = true)
        cacheMetadataDao = mockk(relaxed = true)
        resourceMapper = ResourceMapper()

        repository = FilesRepositoryImpl(
            api = api,
            mediaDao = mediaDao,
            folderDao = folderDao,
            cacheMetadataDao = cacheMetadataDao,
            resourceMapper = resourceMapper
        )
    }

    @Test
    fun `getFolderContents fetches from API when cache is stale`() = runTest {
        val resourceDto = createFolderResourceDto()
        coEvery { cacheMetadataDao.getByFolderPath("/") } returns null
        coEvery {
            api.getResource(
                path = any(),
                fields = any(),
                limit = any(),
                offset = any(),
                previewSize = any(),
                previewCrop = any(),
                sort = any()
            )
        } returns Response.success(resourceDto)

        val result = repository.getFolderContents(
            path = null,
            offset = 0,
            limit = 20,
            sortOrder = SortOrder.DATE_DESC
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getDownloadUrl returns download URL from API`() = runTest {
        val downloadUrl = "https://downloader.disk.yandex.ru/file/123"
        coEvery { api.getDownloadLink(any()) } returns
            Response.success(DownloadLinkDto(href = downloadUrl, method = "GET"))

        val result = repository.getDownloadUrl("/Photos/image.jpg")

        assertTrue(result.isSuccess)
        assertEquals(downloadUrl, result.getOrNull())
    }

    @Test
    fun `getDownloadUrl returns failure on API error`() = runTest {
        coEvery { api.getDownloadLink(any()) } returns
            Response.error(404, "Not found".toResponseBody())

        val result = repository.getDownloadUrl("/nonexistent.jpg")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPublicFolderContents returns items from public URL`() = runTest {
        val publicResource = createPublicResourceDto()
        coEvery {
            api.getPublicResource(
                publicKey = any(),
                path = any(),
                limit = any(),
                offset = any(),
                previewSize = any(),
                previewCrop = any(),
                sort = any()
            )
        } returns Response.success(publicResource)

        val result = repository.getPublicFolderContents(
            publicUrl = "https://disk.yandex.ru/d/test",
            path = null,
            offset = 0,
            limit = 20
        )

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.items?.size)
    }

    @Test
    fun `getPublicDownloadUrl returns download URL`() = runTest {
        val downloadUrl = "https://downloader.disk.yandex.ru/public/123"
        coEvery { api.getPublicDownloadLink(any(), any()) } returns
            Response.success(DownloadLinkDto(href = downloadUrl, method = "GET"))

        val result = repository.getPublicDownloadUrl(
            publicUrl = "https://disk.yandex.ru/d/test",
            path = "/image.jpg"
        )

        assertTrue(result.isSuccess)
        assertEquals(downloadUrl, result.getOrNull())
    }

    @Test
    fun `refreshFolder invalidates cache and fetches fresh data`() = runTest {
        val resourceDto = createFolderResourceDto()
        coEvery {
            api.getResource(
                path = any(),
                fields = any(),
                limit = any(),
                offset = any(),
                previewSize = any(),
                previewCrop = any(),
                sort = any()
            )
        } returns Response.success(resourceDto)

        val result = repository.refreshFolder("/Photos")

        assertTrue(result.isSuccess)
        coVerify { cacheMetadataDao.deleteByFolderPath("/Photos") }
        coVerify { folderDao.deleteByParentPath("/Photos") }
        coVerify { mediaDao.deleteByParentPath("/Photos") }
    }

    @Test
    fun `getPreviewUrl requests correct preview size`() = runTest {
        val resourceDto = ResourceDto(
            name = "test.jpg",
            path = "/test.jpg",
            type = "file",
            mimeType = "image/jpeg",
            preview = "https://preview.disk.yandex.ru/L/123"
        )
        coEvery {
            api.getResource(
                path = any(),
                fields = any(),
                limit = any(),
                offset = any(),
                previewSize = any(),
                previewCrop = any(),
                sort = any()
            )
        } returns Response.success(resourceDto)

        val result = repository.getPreviewUrl("/test.jpg", PreviewSize.L)

        assertTrue(result.isSuccess)
        assertEquals("https://preview.disk.yandex.ru/L/123", result.getOrNull())
    }

    // ==================== Helper Methods ====================

    private fun createFolderResourceDto(): ResourceDto {
        return ResourceDto(
            name = "Root",
            path = "/",
            type = "dir",
            embedded = EmbeddedResourcesDto(
                items = emptyList(),
                total = 0,
                limit = 20,
                offset = 0
            )
        )
    }

    private fun createPublicResourceDto(): PublicResourceDto {
        return PublicResourceDto(
            name = "Shared Folder",
            path = "/",
            type = "dir",
            publicKey = "https://disk.yandex.ru/d/test",
            embedded = EmbeddedResourcesDto(
                items = emptyList(),
                total = 0,
                limit = 20,
                offset = 0
            )
        )
    }
}
