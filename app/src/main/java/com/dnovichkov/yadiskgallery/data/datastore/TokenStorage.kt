package com.dnovichkov.yadiskgallery.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage for OAuth tokens using EncryptedSharedPreferences.
 */
@Singleton
class TokenStorage
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val masterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        private val securePreferences: SharedPreferences by lazy {
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

        private val _accessTokenFlow = MutableStateFlow<String?>(null)
        val accessTokenFlow: Flow<String?> = _accessTokenFlow.asStateFlow()

        init {
            // Initialize the flow with stored token
            _accessTokenFlow.value = securePreferences.getString(KEY_ACCESS_TOKEN, null)
        }

        /**
         * Save access token securely.
         */
        suspend fun saveAccessToken(token: String) =
            withContext(Dispatchers.IO) {
                securePreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, token)
                    .apply()
                _accessTokenFlow.value = token
            }

        /**
         * Get current access token.
         */
        suspend fun getAccessToken(): String? =
            withContext(Dispatchers.IO) {
                securePreferences.getString(KEY_ACCESS_TOKEN, null)
            }

        /**
         * Save refresh token securely.
         */
        suspend fun saveRefreshToken(token: String) =
            withContext(Dispatchers.IO) {
                securePreferences.edit()
                    .putString(KEY_REFRESH_TOKEN, token)
                    .apply()
            }

        /**
         * Get current refresh token.
         */
        suspend fun getRefreshToken(): String? =
            withContext(Dispatchers.IO) {
                securePreferences.getString(KEY_REFRESH_TOKEN, null)
            }

        /**
         * Save token expiration timestamp.
         */
        suspend fun saveTokenExpiration(expirationTime: Long) =
            withContext(Dispatchers.IO) {
                securePreferences.edit()
                    .putLong(KEY_TOKEN_EXPIRATION, expirationTime)
                    .apply()
            }

        /**
         * Get token expiration timestamp.
         */
        suspend fun getTokenExpiration(): Long =
            withContext(Dispatchers.IO) {
                securePreferences.getLong(KEY_TOKEN_EXPIRATION, 0L)
            }

        /**
         * Check if current token is expired.
         */
        suspend fun isTokenExpired(): Boolean =
            withContext(Dispatchers.IO) {
                val expiration = getTokenExpiration()
                if (expiration == 0L) return@withContext true
                System.currentTimeMillis() >= expiration - EXPIRATION_BUFFER_MS
            }

        /**
         * Save all token data at once.
         */
        suspend fun saveTokenData(
            accessToken: String,
            refreshToken: String?,
            expiresInSeconds: Long,
        ) = withContext(Dispatchers.IO) {
            val expirationTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
            securePreferences.edit().apply {
                putString(KEY_ACCESS_TOKEN, accessToken)
                if (refreshToken != null) {
                    putString(KEY_REFRESH_TOKEN, refreshToken)
                }
                putLong(KEY_TOKEN_EXPIRATION, expirationTime)
            }.apply()
            _accessTokenFlow.value = accessToken
        }

        /**
         * Clear all stored tokens.
         */
        suspend fun clearTokens() =
            withContext(Dispatchers.IO) {
                securePreferences.edit()
                    .remove(KEY_ACCESS_TOKEN)
                    .remove(KEY_REFRESH_TOKEN)
                    .remove(KEY_TOKEN_EXPIRATION)
                    .apply()
                _accessTokenFlow.value = null
            }

        /**
         * Check if user has a stored token (regardless of expiration).
         */
        suspend fun hasStoredToken(): Boolean =
            withContext(Dispatchers.IO) {
                securePreferences.contains(KEY_ACCESS_TOKEN)
            }

        companion object {
            private const val PREFS_FILE_NAME = "secure_token_storage"
            private const val KEY_ACCESS_TOKEN = "access_token"
            private const val KEY_REFRESH_TOKEN = "refresh_token"
            private const val KEY_TOKEN_EXPIRATION = "token_expiration"
            private const val EXPIRATION_BUFFER_MS = 60_000L // 1 minute buffer before expiration
        }
    }
