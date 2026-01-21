package com.dnovichkov.yadiskgallery.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("AuthState")
class AuthStateTest {
    @Nested
    @DisplayName("NotAuthenticated")
    inner class NotAuthenticatedTests {
        @Test
        @DisplayName("should create NotAuthenticated state")
        fun `should create NotAuthenticated state`() {
            val state = AuthState.NotAuthenticated

            assertFalse(state.isAuthenticated)
        }

        @Test
        @DisplayName("should be a singleton object")
        fun `should be a singleton object`() {
            val state1 = AuthState.NotAuthenticated
            val state2 = AuthState.NotAuthenticated

            assertTrue(state1 === state2)
        }
    }

    @Nested
    @DisplayName("Authenticating")
    inner class AuthenticatingTests {
        @Test
        @DisplayName("should create Authenticating state")
        fun `should create Authenticating state`() {
            val state = AuthState.Authenticating

            assertFalse(state.isAuthenticated)
        }
    }

    @Nested
    @DisplayName("Authenticated")
    inner class AuthenticatedTests {
        @Test
        @DisplayName("should create Authenticated state with user info")
        fun `should create Authenticated state with user info`() {
            val userInfo =
                UserInfo(
                    uid = "12345",
                    login = "user@yandex.ru",
                    displayName = "Test User",
                    avatarUrl = "https://avatars.yandex.net/avatar",
                )

            val state = AuthState.Authenticated(userInfo)

            assertTrue(state.isAuthenticated)
            assertEquals(userInfo, state.userInfo)
            assertEquals("12345", state.userInfo.uid)
            assertEquals("user@yandex.ru", state.userInfo.login)
            assertEquals("Test User", state.userInfo.displayName)
            assertEquals("https://avatars.yandex.net/avatar", state.userInfo.avatarUrl)
        }

        @Test
        @DisplayName("should create Authenticated state with minimal user info")
        fun `should create Authenticated state with minimal user info`() {
            val userInfo =
                UserInfo(
                    uid = "12345",
                    login = "user",
                    displayName = null,
                    avatarUrl = null,
                )

            val state = AuthState.Authenticated(userInfo)

            assertTrue(state.isAuthenticated)
            assertNull(state.userInfo.displayName)
            assertNull(state.userInfo.avatarUrl)
        }
    }

    @Nested
    @DisplayName("PublicAccess")
    inner class PublicAccessTests {
        @Test
        @DisplayName("should create PublicAccess state with URL")
        fun `should create PublicAccess state with URL`() {
            val state = AuthState.PublicAccess(publicUrl = "https://disk.yandex.ru/d/abc123")

            assertFalse(state.isAuthenticated)
            assertEquals("https://disk.yandex.ru/d/abc123", state.publicUrl)
        }
    }

    @Nested
    @DisplayName("AuthError")
    inner class AuthErrorTests {
        @Test
        @DisplayName("should create AuthError state with message")
        fun `should create AuthError state with message`() {
            val state = AuthState.AuthError(message = "Token expired")

            assertFalse(state.isAuthenticated)
            assertEquals("Token expired", state.message)
        }
    }

    @Nested
    @DisplayName("Polymorphism")
    inner class PolymorphismTests {
        @Test
        @DisplayName("should handle all states with when expression")
        fun `should handle all states with when expression`() {
            val states =
                listOf(
                    AuthState.NotAuthenticated,
                    AuthState.Authenticating,
                    AuthState.Authenticated(UserInfo("1", "user", null, null)),
                    AuthState.PublicAccess("https://disk.yandex.ru/d/abc"),
                    AuthState.AuthError("Error"),
                )

            val results =
                states.map { state ->
                    when (state) {
                        is AuthState.NotAuthenticated -> "not_authenticated"
                        is AuthState.Authenticating -> "authenticating"
                        is AuthState.Authenticated -> "authenticated:${state.userInfo.login}"
                        is AuthState.PublicAccess -> "public:${state.publicUrl}"
                        is AuthState.AuthError -> "error:${state.message}"
                    }
                }

            assertEquals("not_authenticated", results[0])
            assertEquals("authenticating", results[1])
            assertEquals("authenticated:user", results[2])
            assertEquals("public:https://disk.yandex.ru/d/abc", results[3])
            assertEquals("error:Error", results[4])
        }

        @Test
        @DisplayName("should check isAuthenticated for all states")
        fun `should check isAuthenticated for all states`() {
            assertFalse(AuthState.NotAuthenticated.isAuthenticated)
            assertFalse(AuthState.Authenticating.isAuthenticated)
            assertTrue(AuthState.Authenticated(UserInfo("1", "u", null, null)).isAuthenticated)
            assertFalse(AuthState.PublicAccess("url").isAuthenticated)
            assertFalse(AuthState.AuthError("err").isAuthenticated)
        }
    }
}
