package com.dnovichkov.yadiskgallery.util

/**
 * Utility object containing mock API responses for E2E tests.
 */
object MockApiResponses {
    /**
     * Mock response for public folder with images and videos.
     */
    fun publicFolderResponse(
        folderName: String = "Test Public Folder",
        itemCount: Int = 5,
    ): String =
        """
        {
            "name": "$folderName",
            "path": "/",
            "type": "dir",
            "public_key": "test_public_key",
            "public_url": "https://disk.yandex.ru/d/test_public_key",
            "_embedded": {
                "items": [
                    ${generateItems(itemCount)}
                ],
                "offset": 0,
                "limit": 20,
                "total": $itemCount
            }
        }
        """.trimIndent()

    /**
     * Mock response for private folder.
     */
    fun privateFolderResponse(
        folderName: String = "Photos",
        path: String = "disk:/Photos",
        itemCount: Int = 5,
    ): String =
        """
        {
            "name": "$folderName",
            "path": "$path",
            "type": "dir",
            "resource_id": "test_resource_id",
            "_embedded": {
                "items": [
                    ${generateItems(itemCount)}
                ],
                "offset": 0,
                "limit": 20,
                "total": $itemCount
            }
        }
        """.trimIndent()

    /**
     * Mock response for subfolder.
     */
    fun subfolderResponse(
        folderName: String = "Vacation",
        path: String = "disk:/Photos/Vacation",
    ): String =
        """
        {
            "name": "$folderName",
            "path": "$path",
            "type": "dir",
            "resource_id": "subfolder_resource_id",
            "_embedded": {
                "items": [
                    {
                        "name": "beach.jpg",
                        "path": "$path/beach.jpg",
                        "type": "file",
                        "mime_type": "image/jpeg",
                        "size": 2048576,
                        "created": "2024-01-15T10:30:00+00:00",
                        "modified": "2024-01-15T10:30:00+00:00",
                        "preview": "https://preview.yandex.ru/beach_preview.jpg"
                    },
                    {
                        "name": "sunset.jpg",
                        "path": "$path/sunset.jpg",
                        "type": "file",
                        "mime_type": "image/jpeg",
                        "size": 3145728,
                        "created": "2024-01-15T18:00:00+00:00",
                        "modified": "2024-01-15T18:00:00+00:00",
                        "preview": "https://preview.yandex.ru/sunset_preview.jpg"
                    }
                ],
                "offset": 0,
                "limit": 20,
                "total": 2
            }
        }
        """.trimIndent()

    /**
     * Mock response for download link.
     */
    fun downloadLinkResponse(url: String = "https://downloader.disk.yandex.ru/test_file"): String =
        """
        {
            "href": "$url",
            "method": "GET",
            "templated": false
        }
        """.trimIndent()

    /**
     * Mock error response.
     */
    fun errorResponse(
        error: String = "DiskNotFoundError",
        message: String = "Resource not found",
        description: String = "The requested resource was not found.",
    ): String =
        """
        {
            "error": "$error",
            "message": "$message",
            "description": "$description"
        }
        """.trimIndent()

    /**
     * Mock disk info response.
     */
    fun diskInfoResponse(
        totalSpace: Long = 10737418240L,
        usedSpace: Long = 5368709120L,
    ): String =
        """
        {
            "total_space": $totalSpace,
            "used_space": $usedSpace,
            "trash_size": 0
        }
        """.trimIndent()

    /**
     * Generate items for folder response.
     */
    private fun generateItems(count: Int): String {
        val items = mutableListOf<String>()

        // Add a subfolder
        if (count > 0) {
            items.add(
                """
                {
                    "name": "Subfolder",
                    "path": "disk:/Photos/Subfolder",
                    "type": "dir",
                    "resource_id": "folder_001"
                }
                """.trimIndent(),
            )
        }

        // Add images
        for (i in 1 until count - 1) {
            items.add(
                """
                {
                    "name": "photo_$i.jpg",
                    "path": "disk:/Photos/photo_$i.jpg",
                    "type": "file",
                    "mime_type": "image/jpeg",
                    "size": ${1024 * 1024 * (i + 1)},
                    "created": "2024-01-${10 + i}T12:00:00+00:00",
                    "modified": "2024-01-${10 + i}T12:00:00+00:00",
                    "preview": "https://preview.yandex.ru/photo_${i}_preview.jpg"
                }
                """.trimIndent(),
            )
        }

        // Add a video
        if (count > 1) {
            items.add(
                """
                {
                    "name": "video.mp4",
                    "path": "disk:/Photos/video.mp4",
                    "type": "file",
                    "mime_type": "video/mp4",
                    "size": 52428800,
                    "created": "2024-01-20T15:30:00+00:00",
                    "modified": "2024-01-20T15:30:00+00:00",
                    "preview": "https://preview.yandex.ru/video_preview.jpg"
                }
                """.trimIndent(),
            )
        }

        return items.joinToString(",\n")
    }

    /**
     * Empty folder response.
     */
    fun emptyFolderResponse(folderName: String = "Empty Folder"): String =
        """
        {
            "name": "$folderName",
            "path": "/",
            "type": "dir",
            "public_key": "test_public_key",
            "_embedded": {
                "items": [],
                "offset": 0,
                "limit": 20,
                "total": 0
            }
        }
        """.trimIndent()
}
