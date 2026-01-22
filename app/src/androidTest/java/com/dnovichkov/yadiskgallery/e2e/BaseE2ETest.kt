package com.dnovichkov.yadiskgallery.e2e

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.dnovichkov.yadiskgallery.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

/**
 * Base class for E2E tests providing common setup and utilities.
 */
@HiltAndroidTest
abstract class BaseE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Before
    open fun setUp() {
        hiltRule.inject()
    }

    @After
    open fun tearDown() {
        // MockWebServer will be shut down by Hilt when the test component is destroyed
    }

    /**
     * Enqueue a successful JSON response.
     */
    protected fun enqueueSuccessResponse(json: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .setHeader("Content-Type", "application/json"),
        )
    }

    /**
     * Enqueue an error response.
     */
    protected fun enqueueErrorResponse(
        code: Int,
        json: String,
    ) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody(json)
                .setHeader("Content-Type", "application/json"),
        )
    }

    /**
     * Enqueue a network error (connection failure).
     */
    protected fun enqueueNetworkError() {
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST),
        )
    }

    /**
     * Wait for idle state.
     */
    protected fun waitForIdle() {
        composeTestRule.waitForIdle()
    }
}
