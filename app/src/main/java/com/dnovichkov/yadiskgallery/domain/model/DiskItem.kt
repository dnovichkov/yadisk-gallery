package com.dnovichkov.yadiskgallery.domain.model

/**
 * Sealed interface representing an item on Yandex.Disk.
 * Can be either a file (media) or a directory (folder).
 *
 * Provides common properties for unified handling in UI lists.
 */
sealed interface DiskItem {
    val id: String
    val name: String
    val path: String

    /**
     * Represents a media file item on Yandex.Disk.
     *
     * @property mediaFile The underlying media file data
     */
    data class File(val mediaFile: MediaFile) : DiskItem {
        override val id: String get() = mediaFile.id
        override val name: String get() = mediaFile.name
        override val path: String get() = mediaFile.path
    }

    /**
     * Represents a directory/folder item on Yandex.Disk.
     *
     * @property folder The underlying folder data
     */
    data class Directory(val folder: Folder) : DiskItem {
        override val id: String get() = folder.id
        override val name: String get() = folder.name
        override val path: String get() = folder.path
    }
}
