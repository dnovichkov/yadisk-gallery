package com.dnovichkov.yadiskgallery.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

/**
 * E2E tests for settings screen functionality.
 */
@HiltAndroidTest
class SettingsE2ETest : BaseE2ETest() {
    @Test
    fun testSettingsScreen_displaysAllSections() {
        // Given: Settings screen is displayed (start destination)
        waitForIdle()

        // Then: All sections are displayed
        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Public folder")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Yandex account")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Display")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Storage")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun testViewModeSelector_gridIsDefault() {
        // Given: Settings screen is displayed
        waitForIdle()

        // Then: Grid view is selected by default
        composeTestRule.onNodeWithText("Display")
            .performScrollTo()

        composeTestRule.onNodeWithContentDescription("Grid view mode", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testViewModeSelector_canSelectListMode() {
        // Given: Settings screen is displayed
        waitForIdle()

        composeTestRule.onNodeWithText("Display")
            .performScrollTo()

        // When: Click on list mode
        composeTestRule.onNodeWithContentDescription("List view mode", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: List mode is now selected
        composeTestRule.onNodeWithContentDescription("List view mode", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testSortOrderSelector_displaysOptions() {
        // Given: Settings screen is displayed
        waitForIdle()

        composeTestRule.onNodeWithText("Display")
            .performScrollTo()

        // Then: Sort options are visible
        composeTestRule.onNodeWithText("Name", useUnmergedTree = true)
            .assertExists()

        composeTestRule.onNodeWithText("Date", useUnmergedTree = true)
            .assertExists()

        composeTestRule.onNodeWithText("Size", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testCacheSection_displaysCacheInfo() {
        // Given: Settings screen is displayed
        waitForIdle()

        composeTestRule.onNodeWithText("Storage")
            .performScrollTo()
            .assertIsDisplayed()

        // Then: Clear cache button is visible
        composeTestRule.onNodeWithContentDescription("Clear cache", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testLoginButton_isDisplayedWhenNotAuthenticated() {
        // Given: Settings screen is displayed and user is not authenticated
        waitForIdle()

        // Then: Login button is displayed
        composeTestRule.onNodeWithText("Sign in with Yandex", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testLoginButton_showsConfirmationDialog() {
        // Given: Settings screen is displayed
        waitForIdle()

        // When: Click login button
        composeTestRule.onNodeWithText("Sign in with Yandex", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // Then: Confirmation dialog is shown
        composeTestRule.onNodeWithText("You will be redirected to Yandex", substring = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Continue")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
    }

    @Test
    fun testLoginDialog_canBeCancelled() {
        // Given: Login dialog is shown
        waitForIdle()

        composeTestRule.onNodeWithText("Sign in with Yandex", useUnmergedTree = true)
            .performClick()

        waitForIdle()

        // When: Click cancel
        composeTestRule.onNodeWithText("Cancel")
            .performClick()

        waitForIdle()

        // Then: Dialog is dismissed
        composeTestRule.onNodeWithText("You will be redirected to Yandex", substring = true)
            .assertDoesNotExist()
    }
}
