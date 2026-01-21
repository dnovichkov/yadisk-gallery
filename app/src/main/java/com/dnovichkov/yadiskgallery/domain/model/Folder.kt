package com.dnovichkov.yadiskgallery.domain.model

import java.time.Instant

/**
 * Represents a folder on Yandex.Disk.
 *
 * @property id Unique identifier of the folder
 * @property name Folder name
 * @property path Full path to the folder on Yandex.Disk
 * @property itemsCount Number of items in the folder (null if not loaded)
 * @property createdAt Folder creation timestamp
 * @property modifiedAt Last modification timestamp
 */
data class Folder(
    val id: String,
    val name: String,
    val path: String,
    val itemsCount: Int?,
    val createdAt: Instant?,
    val modifiedAt: Instant?,
)
