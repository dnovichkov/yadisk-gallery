package com.dnovichkov.yadiskgallery.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds OAuth token to requests.
 */
@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    @Volatile
    private var accessToken: String? = null

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

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // If no token, proceed without authorization header
        val token = accessToken ?: return chain.proceed(originalRequest)

        // Add OAuth bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$token")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "OAuth "
    }
}
