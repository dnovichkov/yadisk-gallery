package com.dnovichkov.yadiskgallery.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing a resource (file or folder) from Yandex.Disk API.
 *
 * API Reference: https://yandex.ru/dev/disk-api/doc/ru/reference/all-files
 */
@Serializable
data class ResourceDto(
    @SerialName("name")
    val name: String,

    @SerialName("path")
    val path: String,

    @SerialName("type")
    val type: String,

    @SerialName("resource_id")
    val resourceId: String? = null,

    @SerialName("mime_type")
    val mimeType: String? = null,

    @SerialName("size")
    val size: Long? = null,

    @SerialName("created")
    val created: String? = null,

    @SerialName("modified")
    val modified: String? = null,

    @SerialName("md5")
    val md5: String? = null,

    @SerialName("preview")
    val preview: String? = null,

    @SerialName("_embedded")
    val embedded: EmbeddedResourcesDto? = null
) {
    companion object {
        const val TYPE_FILE = "file"
        const val TYPE_DIR = "dir"
    }

    val isFile: Boolean get() = type == TYPE_FILE
    val isDirectory: Boolean get() = type == TYPE_DIR
}

/**
 * DTO representing embedded resources (folder contents) from Yandex.Disk API.
 */
@Serializable
data class EmbeddedResourcesDto(
    @SerialName("items")
    val items: List<ResourceDto>,

    @SerialName("offset")
    val offset: Int,

    @SerialName("limit")
    val limit: Int,

    @SerialName("total")
    val total: Int
)
