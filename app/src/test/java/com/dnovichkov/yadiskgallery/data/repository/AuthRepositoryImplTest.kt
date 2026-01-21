package com.dnovichkov.yadiskgallery.data.repository

import app.cash.turbine.test
import com.dnovichkov.yadiskgallery.data.datastore.SettingsDataStore
import com.dnovichkov.yadiskgallery.data.datastore.TokenStorage
import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthRepositoryImplTest {
    private lateinit var tokenStorage: TokenStorage
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: AuthRepositoryImpl

    @BeforeEach
    fun setup() {
        tokenStorage = mockk(relaxed = true)
        settingsDataStore = mockk(relaxed = true)
        every { settingsDataStore.settings } returns flowOf(UserSettings.default())
        repository = AuthRepositoryImpl(tokenStorage, settingsDataStore)
    }

    @Test
    fun `initial auth state is NotAuthenticated`() =
        runTest {
            repository.observeAuthState().test {
                assertEquals(AuthState.NotAuthenticated, awaitItem())
            }
        }

    @Test
    fun `authenticate sets state to Authenticating`() =
        runTest {
            val result = repository.authenticate()

            assertTrue(result.isSuccess)
            repository.observeAuthState().test {
                assertEquals(AuthState.Authenticating, awaitItem())
            }
        }

    @Test
    fun `onAuthenticationSuccess updates state and stores tokens`() =
        runTest {
            val userInfo =
                UserInfo(
                    uid = "123",
                    login = "user@yandex.ru",
                    displayName = "Test User",
                    avatarUrl = null,
                )

            repository.onAuthenticationSuccess(
                accessToken = "test_token",
                refreshToken = "refresh_token",
                expiresInSeconds = 3600,
                userInfo = userInfo,
            )

            repository.observeAuthState().test {
                val state = awaitItem()
                assertTrue(state is AuthState.Authenticated)
                assertEquals(userInfo, (state as AuthState.Authenticated).userInfo)
            }

            coVerify { tokenStorage.saveTokenData("test_token", "refresh_token", 3600) }
            coVerify { settingsDataStore.setAuthenticated(true) }
        }

    @Test
    fun `logout clears tokens and sets state to NotAuthenticated`() =
        runTest {
            // First authenticate
            val userInfo = UserInfo("123", "user@yandex.ru", null, null)
            repository.onAuthenticationSuccess("token", null, 3600, userInfo)

            // Then logout
            val result = repository.logout()

            assertTrue(result.isSuccess)
            coVerify { tokenStorage.clearTokens() }
            coVerify { settingsDataStore.setAuthenticated(false) }

            repository.observeAuthState().test {
                assertEquals(AuthState.NotAuthenticated, awaitItem())
            }
        }

    @Test
    fun `getAccessToken delegates to token storage`() =
        runTest {
            coEvery { tokenStorage.getAccessToken() } returns "test_token"

            val token = repository.getAccessToken()

            assertEquals("test_token", token)
        }

    @Test
    fun `getAccessToken returns null when no token`() =
        runTest {
            coEvery { tokenStorage.getAccessToken() } returns null

            val token = repository.getAccessToken()

            assertNull(token)
        }

    @Test
    fun `isTokenValid returns true when token exists and not expired`() =
        runTest {
            coEvery { tokenStorage.getAccessToken() } returns "test_token"
            coEvery { tokenStorage.isTokenExpired() } returns false

            val isValid = repository.isTokenValid()

            assertTrue(isValid)
        }

    @Test
    fun `isTokenValid returns false when token expired`() =
        runTest {
            coEvery { tokenStorage.getAccessToken() } returns "test_token"
            coEvery { tokenStorage.isTokenExpired() } returns true

            val isValid = repository.isTokenValid()

            assertFalse(isValid)
        }

    @Test
    fun `isTokenValid returns false when no token`() =
        runTest {
            coEvery { tokenStorage.getAccessToken() } returns null

            val isValid = repository.isTokenValid()

            assertFalse(isValid)
        }

    @Test
    fun `setPublicAccess updates state and stores url`() =
        runTest {
            val url = "https://disk.yandex.ru/d/test"

            val result = repository.setPublicAccess(url)

            assertTrue(result.isSuccess)
            coVerify { settingsDataStore.setPublicFolderUrl(url) }

            repository.observeAuthState().test {
                val state = awaitItem()
                assertTrue(state is AuthState.PublicAccess)
                assertEquals(url, (state as AuthState.PublicAccess).publicUrl)
            }
        }

    @Test
    fun `onAuthenticationError sets state to AuthError`() =
        runTest {
            val errorMessage = "Authentication failed"

            repository.onAuthenticationError(errorMessage)

            repository.observeAuthState().test {
                val state = awaitItem()
                assertTrue(state is AuthState.AuthError)
                assertEquals(errorMessage, (state as AuthState.AuthError).message)
            }
        }

    @Test
    fun `getAuthState returns PublicAccess when public url is set`() =
        runTest {
            val publicUrl = "https://disk.yandex.ru/d/test"
            val settings = UserSettings.default().copy(publicFolderUrl = publicUrl)
            every { settingsDataStore.settings } returns flowOf(settings)
            coEvery { tokenStorage.getAccessToken() } returns null

            val state = repository.getAuthState()

            assertEquals(AuthState.PublicAccess(publicUrl), state)
        }
}
