package com.dnovichkov.yadiskgallery.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing a download link response from Yandex.Disk API.
 * Used for both private (/v1/disk/resources/download) and
 * public (/v1/disk/public/resources/download) resources.
 */
@Serializable
data class DownloadLinkDto(
    @SerialName("href")
    val href: String,

    @SerialName("method")
    val method: String,

    @SerialName("templated")
    val templated: Boolean = false
)
