package com.dnovichkov.yadiskgallery.domain.model

/**
 * Represents a paginated result from the API.
 *
 * @param T The type of items in the result
 * @property items List of items in the current page
 * @property offset Current offset (number of items skipped)
 * @property limit Maximum number of items per page
 * @property total Total number of items available (null if unknown)
 * @property hasMore Whether there are more items to load
 */
data class PagedResult<T>(
    val items: List<T>,
    val offset: Int,
    val limit: Int,
    val total: Int?,
    val hasMore: Boolean,
) {
    /**
     * The offset for the next page of results.
     */
    val nextOffset: Int
        get() = offset + items.size

    /**
     * Whether the result is empty (no items).
     */
    val isEmpty: Boolean
        get() = items.isEmpty()

    /**
     * Whether the result has items.
     */
    val isNotEmpty: Boolean
        get() = items.isNotEmpty()
}
