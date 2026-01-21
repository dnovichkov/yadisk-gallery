package com.dnovichkov.yadiskgallery.domain.usecase.auth

import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import javax.inject.Inject

/**
 * Use case for initiating and handling OAuth authentication.
 */
class AuthorizeUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    /**
     * Initiates OAuth authentication with Yandex.
     *
     * @return Result indicating success or error
     */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.authenticate()
    }

    /**
     * Handles the OAuth callback with authorization code.
     *
     * @param authCode Authorization code from OAuth callback
     * @return Result containing user info or error
     */
    suspend fun handleCallback(authCode: String): Result<UserInfo> {
        return authRepository.handleAuthCallback(authCode)
    }

    /**
     * Sets public access mode for a public folder URL.
     *
     * @param url The public folder URL
     * @return Result indicating success or error
     */
    suspend fun setPublicAccess(url: String): Result<Unit> {
        return authRepository.setPublicAccess(url)
    }
}
