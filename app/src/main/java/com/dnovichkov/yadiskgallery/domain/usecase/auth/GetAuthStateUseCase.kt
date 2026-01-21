package com.dnovichkov.yadiskgallery.domain.usecase.auth

import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the current authentication state.
 */
class GetAuthStateUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    /**
     * Gets the current authentication state.
     *
     * @return Current authentication state
     */
    suspend operator fun invoke(): AuthState {
        return authRepository.getAuthState()
    }

    /**
     * Observes authentication state changes.
     *
     * @return Flow of authentication states
     */
    fun observeAuthState(): Flow<AuthState> {
        return authRepository.observeAuthState()
    }

    /**
     * Checks if the user is currently authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    suspend fun isAuthenticated(): Boolean {
        return authRepository.getAuthState().isAuthenticated
    }
}
