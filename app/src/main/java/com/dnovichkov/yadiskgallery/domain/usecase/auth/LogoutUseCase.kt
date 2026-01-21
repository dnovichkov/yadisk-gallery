package com.dnovichkov.yadiskgallery.domain.usecase.auth

import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import javax.inject.Inject

/**
 * Use case for logging out the current user.
 */
class LogoutUseCase
    @Inject
    constructor(
        private val authRepository: IAuthRepository,
    ) {
        /**
         * Logs out the current user, clearing stored tokens.
         *
         * @return Result indicating success or error
         */
        suspend operator fun invoke(): Result<Unit> {
            return authRepository.logout()
        }
    }
