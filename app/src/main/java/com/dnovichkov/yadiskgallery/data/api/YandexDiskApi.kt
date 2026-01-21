package com.dnovichkov.yadiskgallery.data.api

import com.dnovichkov.yadiskgallery.data.api.dto.DownloadLinkDto
import com.dnovichkov.yadiskgallery.data.api.dto.FilesResponseDto
import com.dnovichkov.yadiskgallery.data.api.dto.PublicResourceDto
import com.dnovichkov.yadiskgallery.data.api.dto.ResourceDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Yandex.Disk REST API.
 *
 * Base URL: https://cloud-api.yandex.net/
 * API Reference: https://yandex.ru/dev/disk-api/doc/ru/
 */
interface YandexDiskApi {
    companion object {
        const val BASE_URL = "https://cloud-api.yandex.net/"
    }

    // ==================== Private Resources ====================

    /**
     * Get resource metadata (file or folder).
     *
     * @param path Path to the resource
     * @param fields Fields to include in response (comma-separated)
     * @param limit Number of items per page for folders
     * @param offset Offset for pagination
     * @param previewSize Preview image size (S, M, L, XL, XXL)
     * @param previewCrop Crop preview to square
     * @param sort Sort field: name, path, created, modified, size
     */
    @GET("v1/disk/resources")
    suspend fun getResource(
        @Query("path") path: String,
        @Query("fields") fields: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("preview_size") previewSize: String? = null,
        @Query("preview_crop") previewCrop: Boolean? = null,
        @Query("sort") sort: String? = null,
    ): Response<ResourceDto>

    /**
     * Get flat list of all files on the disk.
     *
     * @param limit Number of items per page (max 1000)
     * @param offset Offset for pagination
     * @param mediaType Filter by media type: audio, backup, book, compressed, data, development,
     *                  diskimage, document, encoded, executable, flash, font, image, settings,
     *                  spreadsheet, text, unknown, video, web
     * @param fields Fields to include in response
     * @param previewSize Preview image size
     * @param previewCrop Crop preview to square
     * @param sort Sort field
     */
    @GET("v1/disk/resources/files")
    suspend fun getAllFiles(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("media_type") mediaType: String? = null,
        @Query("fields") fields: String? = null,
        @Query("preview_size") previewSize: String? = null,
        @Query("preview_crop") previewCrop: Boolean? = null,
        @Query("sort") sort: String? = null,
    ): Response<FilesResponseDto>

    /**
     * Get download link for a file.
     *
     * @param path Path to the file
     */
    @GET("v1/disk/resources/download")
    suspend fun getDownloadLink(
        @Query("path") path: String,
    ): Response<DownloadLinkDto>

    // ==================== Public Resources ====================

    /**
     * Get public resource metadata.
     *
     * @param publicKey Public key or URL
     * @param path Path within the public folder (for nested items)
     * @param limit Number of items per page
     * @param offset Offset for pagination
     * @param previewSize Preview image size
     * @param previewCrop Crop preview to square
     * @param sort Sort field
     */
    @GET("v1/disk/public/resources")
    suspend fun getPublicResource(
        @Query("public_key") publicKey: String,
        @Query("path") path: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("preview_size") previewSize: String? = null,
        @Query("preview_crop") previewCrop: Boolean? = null,
        @Query("sort") sort: String? = null,
    ): Response<PublicResourceDto>

    /**
     * Get download link for a public file.
     *
     * @param publicKey Public key or URL
     * @param path Path within the public folder (for nested items)
     */
    @GET("v1/disk/public/resources/download")
    suspend fun getPublicDownloadLink(
        @Query("public_key") publicKey: String,
        @Query("path") path: String? = null,
    ): Response<DownloadLinkDto>

    // ==================== Disk Info ====================

    /**
     * Get disk information (quota, used space, etc.).
     */
    @GET("v1/disk")
    suspend fun getDiskInfo(): Response<DiskInfoDto>
}

/**
 * DTO for disk information.
 */
@kotlinx.serialization.Serializable
data class DiskInfoDto(
    @kotlinx.serialization.SerialName("total_space")
    val totalSpace: Long,
    @kotlinx.serialization.SerialName("used_space")
    val usedSpace: Long,
    @kotlinx.serialization.SerialName("trash_size")
    val trashSize: Long? = null,
)
