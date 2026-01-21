package com.dnovichkov.yadiskgallery.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing response from /v1/disk/resources/files endpoint.
 * Returns a flat list of all files on the disk.
 */
@Serializable
data class FilesResponseDto(
    @SerialName("items")
    val items: List<ResourceDto>,

    @SerialName("offset")
    val offset: Int,

    @SerialName("limit")
    val limit: Int
)

/**
 * DTO representing response from /v1/disk/public/resources endpoint.
 * Returns information about a public resource.
 */
@Serializable
data class PublicResourceDto(
    @SerialName("name")
    val name: String,

    @SerialName("path")
    val path: String,

    @SerialName("type")
    val type: String,

    @SerialName("public_key")
    val publicKey: String? = null,

    @SerialName("public_url")
    val publicUrl: String? = null,

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
)
