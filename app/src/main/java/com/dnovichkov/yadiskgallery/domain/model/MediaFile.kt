package com.dnovichkov.yadiskgallery.domain.model

import java.time.Instant

/**
 * Represents a media file (image or video) from Yandex.Disk.
 *
 * @property id Unique identifier of the file
 * @property name File name with extension
 * @property path Full path to the file on Yandex.Disk
 * @property type Type of media (IMAGE or VIDEO)
 * @property mimeType MIME type of the file (e.g., "image/jpeg", "video/mp4")
 * @property size File size in bytes
 * @property createdAt File creation timestamp
 * @property modifiedAt Last modification timestamp
 * @property previewUrl URL for the preview/thumbnail image
 * @property md5 MD5 hash of the file for integrity verification
 */
data class MediaFile(
    val id: String,
    val name: String,
    val path: String,
    val type: MediaType,
    val mimeType: String,
    val size: Long,
    val createdAt: Instant?,
    val modifiedAt: Instant?,
    val previewUrl: String?,
    val md5: String?
)

/**
 * Type of media content.
 */
enum class MediaType {
    IMAGE,
    VIDEO
}
