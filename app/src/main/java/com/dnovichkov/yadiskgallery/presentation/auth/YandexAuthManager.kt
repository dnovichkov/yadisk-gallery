package com.dnovichkov.yadiskgallery.presentation.auth

import android.content.Context
import android.content.Intent
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper class for Yandex Auth SDK.
 * Provides a simplified interface for authentication operations.
 */
@Singleton
class YandexAuthManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val sdk: YandexAuthSdk by lazy {
            YandexAuthSdk.create(YandexAuthOptions(context))
        }

        /**
         * Creates an intent for starting the Yandex login flow.
         * Use this with an ActivityResultLauncher.
         */
        fun createLoginIntent(): Intent {
            val loginOptions = YandexAuthLoginOptions()
            return sdk.contract.createIntent(context, loginOptions)
        }

        /**
         * Parses the authentication result from the activity result intent.
         *
         * @param data The intent data from onActivityResult
         * @return YandexAuthResult containing token on success or error on failure
         */
        fun parseResult(data: Intent): YandexAuthResult {
            return sdk.contract.parseResult(0, data)
        }

        /**
         * Returns a contract for use with registerForActivityResult.
         * This is the recommended way to handle the auth flow in modern Android.
         */
        fun getContract() = sdk.contract

        /**
         * Extension function to register for Yandex auth result.
         * Simplifies the registration process.
         */
        companion object {
            /**
             * Extracts the OAuth token from a successful auth result.
             *
             * @param result The YandexAuthResult from the SDK
             * @return The OAuth token string, or null if authentication failed
             */
            fun extractToken(result: YandexAuthResult): String? {
                return when (result) {
                    is YandexAuthResult.Success -> result.token.value
                    is YandexAuthResult.Failure -> null
                    is YandexAuthResult.Cancelled -> null
                }
            }

            /**
             * Extracts the error message from a failed auth result.
             *
             * @param result The YandexAuthResult from the SDK
             * @return The error message, or null if authentication succeeded or was cancelled
             */
            fun extractError(result: YandexAuthResult): String? {
                return when (result) {
                    is YandexAuthResult.Success -> null
                    is YandexAuthResult.Failure -> result.exception.message
                    is YandexAuthResult.Cancelled -> "Authentication was cancelled"
                }
            }
        }
    }

/**
 * Sealed class representing the authentication state.
 */
sealed class AuthResult {
    /**
     * Authentication was successful.
     * @param token The OAuth access token
     */
    data class Success(val token: String) : AuthResult()

    /**
     * Authentication failed.
     * @param message The error message
     */
    data class Error(val message: String) : AuthResult()

    /**
     * Authentication was cancelled by the user.
     */
    data object Cancelled : AuthResult()

    companion object {
        /**
         * Converts a YandexAuthResult to our AuthResult type.
         */
        fun fromYandexResult(result: YandexAuthResult): AuthResult {
            return when (result) {
                is YandexAuthResult.Success -> Success(result.token.value)
                is YandexAuthResult.Failure ->
                    Error(
                        result.exception.message ?: "Unknown authentication error",
                    )
                is YandexAuthResult.Cancelled -> Cancelled
            }
        }
    }
}
