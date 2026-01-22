package com.dnovichkov.yadiskgallery.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.dnovichkov.yadiskgallery.util.MockApiResponses
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

/**
 * E2E tests for public folder browsing flow.
 */
@HiltAndroidTest
class PublicFolderE2ETest : BaseE2ETest() {
    @Test
    fun testEnterPublicUrl_andNavigateToGallery() {
        // Given: Mock API response for public folder
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse())

        // When: User enters a public URL
        composeTestRule.onNodeWithText("Public folder")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Public link")
            .performClick()

        composeTestRule.onNodeWithText("Enter public URL or key")
            .performTextInput("https://disk.yandex.ru/d/test_key")

        // And: Clicks save
        composeTestRule.onNodeWithText("Save", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Gallery is displayed with content
        composeTestRule.onNodeWithText("YaDisk Gallery")
            .assertIsDisplayed()
    }

    @Test
    fun testPublicFolderWithContent_displaysItems() {
        // Given: Mock API response for folder with items
        enqueueSuccessResponse(
            MockApiResponses.publicFolderResponse(
                folderName = "My Photos",
                itemCount = 5,
            ),
        )

        // When: Navigate to gallery (assuming public URL is set)
        composeTestRule.waitForIdle()

        // Then: Items are displayed
        // Note: The actual items depend on MockApiResponses.generateItems
        composeTestRule.onNodeWithText("Subfolder", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testEmptyPublicFolder_displaysEmptyState() {
        // Given: Mock API response for empty folder
        enqueueSuccessResponse(MockApiResponses.emptyFolderResponse())

        // When: Navigate to gallery
        composeTestRule.waitForIdle()

        // Then: Empty state is displayed
        composeTestRule.onNodeWithText("No items found", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testPublicFolderError_displaysErrorMessage() {
        // Given: Mock API error response
        enqueueErrorResponse(
            code = 404,
            json =
                MockApiResponses.errorResponse(
                    error = "DiskNotFoundError",
                    message = "Resource not found",
                ),
        )

        // When: Navigate to gallery
        composeTestRule.waitForIdle()

        // Then: Error state is displayed with retry option
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testClearPublicUrl() {
        // Given: Settings screen is displayed
        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()

        // When: User clears the public URL
        composeTestRule.onNodeWithContentDescription("Clear", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: The text field should be empty
        composeTestRule.onNodeWithText("Enter public URL or key")
            .assertExists()
    }
}
