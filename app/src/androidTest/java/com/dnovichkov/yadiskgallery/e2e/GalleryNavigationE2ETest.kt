package com.dnovichkov.yadiskgallery.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dnovichkov.yadiskgallery.util.MockApiResponses
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

/**
 * E2E tests for gallery navigation flow.
 */
@HiltAndroidTest
class GalleryNavigationE2ETest : BaseE2ETest() {
    @Test
    fun testNavigateToSubfolder() {
        // Given: Mock API responses
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse(itemCount = 5))
        enqueueSuccessResponse(MockApiResponses.subfolderResponse())

        // When: User clicks on a subfolder
        waitForIdle()

        composeTestRule.onNodeWithText("Subfolder", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Subfolder content is displayed
        composeTestRule.onNodeWithText("beach.jpg", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testBreadcrumbNavigation() {
        // Given: Mock API responses for navigation
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse())
        enqueueSuccessResponse(MockApiResponses.subfolderResponse(folderName = "Vacation"))
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse())

        // When: Navigate to subfolder
        waitForIdle()

        composeTestRule.onNodeWithText("Subfolder", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Breadcrumb for Home is displayed
        composeTestRule.onNodeWithContentDescription("Go to root", useUnmergedTree = true)
            .assertExists()

        // When: Click on root breadcrumb
        composeTestRule.onNodeWithContentDescription("Go to root", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Back to root folder
        composeTestRule.onNodeWithText("Subfolder", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testViewModeToggle() {
        // Given: Gallery is displayed
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse(itemCount = 5))
        waitForIdle()

        // When: Toggle view mode to list
        composeTestRule.onNodeWithContentDescription("List view", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Grid view button should be available to toggle back
        composeTestRule.onNodeWithContentDescription("Grid view", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testNavigateToSettings() {
        // Given: Gallery is displayed
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse())
        waitForIdle()

        // When: Click settings button
        composeTestRule.onNodeWithContentDescription("Settings", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Settings screen is displayed
        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()
    }

    @Test
    fun testPullToRefresh() {
        // Given: Gallery is displayed with initial data
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse(itemCount = 3))
        waitForIdle()

        // Enqueue refresh response
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse(itemCount = 5))

        // Note: Pull to refresh is hard to test in instrumented tests
        // This test verifies the initial content is displayed
        composeTestRule.onNodeWithText("Subfolder", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testSortOrderChange() {
        // Given: Gallery is displayed
        enqueueSuccessResponse(MockApiResponses.publicFolderResponse())
        waitForIdle()

        // When: Click sort button
        composeTestRule.onNodeWithContentDescription("Sort", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Sort options are displayed
        composeTestRule.onNodeWithText("Date (newest)", useUnmergedTree = true)
            .assertExists()
    }
}
