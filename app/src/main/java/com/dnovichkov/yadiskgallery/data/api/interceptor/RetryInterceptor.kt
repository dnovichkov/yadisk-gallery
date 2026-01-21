package com.dnovichkov.yadiskgallery.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * OkHttp interceptor that implements retry logic with exponential backoff.
 */
@Singleton
class RetryInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var lastException: IOException? = null

            repeat(MAX_RETRIES) { attempt ->
                try {
                    val response = chain.proceed(request)

                    // Don't retry on client errors (4xx) except rate limiting
                    if (response.isSuccessful || (response.code in 400..499 && response.code != 429)) {
                        return response
                    }

                    // Retry on server errors (5xx) and rate limiting (429)
                    if (response.code in 500..599 || response.code == 429) {
                        response.close()

                        if (attempt < MAX_RETRIES - 1) {
                            val delayMs = calculateDelay(attempt, response.code == 429)
                            Thread.sleep(delayMs)
                        } else {
                            // Return last response on final attempt
                            return chain.proceed(request)
                        }
                    } else {
                        return response
                    }
                } catch (e: IOException) {
                    lastException = e

                    if (attempt < MAX_RETRIES - 1) {
                        val delayMs = calculateDelay(attempt, false)
                        Thread.sleep(delayMs)
                    }
                }
            }

            throw lastException ?: IOException("Request failed after $MAX_RETRIES retries")
        }

        /**
         * Calculates delay using exponential backoff with jitter.
         *
         * @param attempt Current attempt number (0-indexed)
         * @param isRateLimited Whether this is a rate limiting retry
         * @return Delay in milliseconds
         */
        private fun calculateDelay(
            attempt: Int,
            isRateLimited: Boolean,
        ): Long {
            val baseDelay = if (isRateLimited) RATE_LIMIT_BASE_DELAY_MS else BASE_DELAY_MS

            // Exponential backoff: baseDelay * 2^attempt
            val exponentialDelay = baseDelay * 2.0.pow(attempt.toDouble()).toLong()

            // Cap at max delay
            val cappedDelay = min(exponentialDelay, MAX_DELAY_MS)

            // Add jitter (±20%)
            val jitter = (cappedDelay * 0.2 * (Math.random() * 2 - 1)).toLong()

            return cappedDelay + jitter
        }

        companion object {
            private const val MAX_RETRIES = 3
            private const val BASE_DELAY_MS = 1000L
            private const val RATE_LIMIT_BASE_DELAY_MS = 5000L
            private const val MAX_DELAY_MS = 30000L
        }
    }
