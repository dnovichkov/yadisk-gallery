package com.dnovichkov.yadiskgallery.data.mapper

import com.dnovichkov.yadiskgallery.data.api.dto.ResourceDto
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.Folder
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Mapper for converting API DTOs to domain models.
 */
class ResourceMapper
    @Inject
    constructor() {
        /**
         * Converts a ResourceDto to MediaFile domain model.
         */
        fun toMediaFile(dto: ResourceDto): MediaFile {
            return MediaFile(
                id = dto.resourceId ?: dto.path,
                name = dto.name,
                path = dto.path,
                type = detectMediaType(dto.mimeType),
                mimeType = dto.mimeType ?: "application/octet-stream",
                size = dto.size ?: 0L,
                createdAt = parseDateTime(dto.created),
                modifiedAt = parseDateTime(dto.modified),
                previewUrl = dto.preview,
                md5 = dto.md5,
            )
        }

        /**
         * Converts a ResourceDto to Folder domain model.
         */
        fun toFolder(dto: ResourceDto): Folder {
            return Folder(
                id = dto.resourceId ?: dto.path,
                name = dto.name,
                path = dto.path,
                itemsCount = dto.embedded?.total,
                createdAt = parseDateTime(dto.created),
                modifiedAt = parseDateTime(dto.modified),
            )
        }

        /**
         * Converts a ResourceDto to appropriate DiskItem (File or Directory).
         */
        fun toDiskItem(dto: ResourceDto): DiskItem {
            return when {
                dto.isDirectory -> DiskItem.Directory(toFolder(dto))
                else -> DiskItem.File(toMediaFile(dto))
            }
        }

        /**
         * Converts a list of ResourceDtos to DiskItems.
         *
         * @param dtos List of resource DTOs
         * @param mediaOnly If true, filters out non-media files
         */
        fun toDiskItems(
            dtos: List<ResourceDto>,
            mediaOnly: Boolean = false,
        ): List<DiskItem> {
            return dtos
                .filter { dto ->
                    if (mediaOnly && dto.isFile) {
                        isMediaFile(dto.mimeType)
                    } else {
                        true
                    }
                }
                .map { toDiskItem(it) }
        }

        /**
         * Detects MediaType from MIME type.
         */
        private fun detectMediaType(mimeType: String?): MediaType {
            return when {
                mimeType == null -> MediaType.IMAGE
                mimeType.startsWith("video/") -> MediaType.VIDEO
                mimeType.startsWith("image/") -> MediaType.IMAGE
                else -> MediaType.IMAGE
            }
        }

        /**
         * Checks if the MIME type represents a media file (image or video).
         */
        private fun isMediaFile(mimeType: String?): Boolean {
            return mimeType != null && (
                mimeType.startsWith("image/") || mimeType.startsWith("video/")
            )
        }

        /**
         * Parses ISO 8601 datetime string to Instant.
         */
        private fun parseDateTime(dateTime: String?): Instant? {
            if (dateTime == null) return null

            return try {
                Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateTime))
            } catch (e: DateTimeParseException) {
                try {
                    Instant.parse(dateTime)
                } catch (e: DateTimeParseException) {
                    null
                }
            }
        }
    }
