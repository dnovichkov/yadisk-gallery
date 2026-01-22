package com.dnovichkov.yadiskgallery.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds OAuth token to requests and handles token expiration.
 */
@Singleton
class AuthInterceptor
    @Inject
    constructor() : Interceptor {
        @Volatile
        private var accessToken: String? = null

        @Volatile
        private var onTokenExpired: (() -> Unit)? = null

        /**
         * Updates the access token used for authentication.
         */
        fun setAccessToken(token: String?) {
            accessToken = token
        }

        /**
         * Gets the current access token.
         */
        fun getAccessToken(): String? = accessToken

        /**
         * Clears the access token (used on logout).
         */
        fun clearAccessToken() {
            accessToken = null
        }

        /**
         * Sets a callback to be invoked when the token is expired (401 response).
         * This can be used to trigger re-authentication.
         */
        fun setOnTokenExpiredCallback(callback: (() -> Unit)?) {
            onTokenExpired = callback
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // If no token, proceed without authorization header
            val token = accessToken ?: return chain.proceed(originalRequest)

            // Add OAuth bearer token
            val authenticatedRequest =
                originalRequest.newBuilder()
                    .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$token")
                    .build()

            val response = chain.proceed(authenticatedRequest)

            // Handle 401 Unauthorized - token may be expired
            if (response.code == HTTP_UNAUTHORIZED) {
                // Notify that token has expired
                onTokenExpired?.invoke()
            }

            return response
        }

        companion object {
            private const val HEADER_AUTHORIZATION = "Authorization"
            private const val BEARER_PREFIX = "OAuth "
            private const val HTTP_UNAUTHORIZED = 401
        }
    }
