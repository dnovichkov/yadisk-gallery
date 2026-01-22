package com.dnovichkov.yadiskgallery.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dnovichkov.yadiskgallery.util.MockApiResponses
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

/**
 * E2E tests for error handling scenarios.
 */
@HiltAndroidTest
class ErrorHandlingE2ETest : BaseE2ETest() {
    @Test
    fun testNetworkError_displaysErrorState() {
        // Given: Network error occurs
        enqueueNetworkError()

        // When: Gallery tries to load
        waitForIdle()

        // Then: Error state is displayed
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testNotFoundError_displaysErrorMessage() {
        // Given: 404 error response
        enqueueErrorResponse(
            code = 404,
            json =
                MockApiResponses.errorResponse(
                    error = "DiskNotFoundError",
                    message = "Resource not found",
                    description = "The requested resource was not found.",
                ),
        )

        // When: Gallery tries to load
        waitForIdle()

        // Then: Error with retry is displayed
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testUnauthorizedError_displaysErrorMessage() {
        // Given: 401 error response
        enqueueErrorResponse(
            code = 401,
            json =
                MockApiResponses.errorResponse(
                    error = "UnauthorizedError",
                    message = "Unauthorized",
                    description = "Authorization required.",
                ),
        )

        // When: Gallery tries to load
        waitForIdle()

        // Then: Error with retry is displayed
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testRetryAfterError_loadsSuccessfully() {
        // Given: First request fails, second succeeds
        enqueueErrorResponse(
            code = 500,
            json =
                MockApiResponses.errorResponse(
                    error = "InternalServerError",
                    message = "Server error",
                ),
        )
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse())

        // When: Gallery loads with error
        waitForIdle()

        // Then: Error state is displayed
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .assertExists()

        // When: User clicks retry
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Content is displayed
        composeTestRule.onNodeWithText("Subfolder", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testRateLimitError_displaysErrorMessage() {
        // Given: 429 rate limit error
        enqueueErrorResponse(
            code = 429,
            json =
                MockApiResponses.errorResponse(
                    error = "TooManyRequestsError",
                    message = "Too many requests",
                    description = "Please try again later.",
                ),
        )

        // When: Gallery tries to load
        waitForIdle()

        // Then: Error with retry is displayed
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testInvalidPublicKey_displaysError() {
        // Given: Invalid public key error
        enqueueErrorResponse(
            code = 400,
            json =
                MockApiResponses.errorResponse(
                    error = "InvalidPublicKeyError",
                    message = "Invalid public key",
                    description = "The public key format is invalid.",
                ),
        )

        // When: Gallery tries to load
        waitForIdle()

        // Then: Error with retry is displayed
        composeTestRule.onNodeWithText("Retry", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testSettingsScreen_displaysOnStartup() {
        // Given: App starts
        waitForIdle()

        // Then: Settings screen is displayed (start destination)
        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()
    }
}
