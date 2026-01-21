package com.dnovichkov.yadiskgallery.domain.usecase.settings

import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving user settings.
 */
class GetSettingsUseCase
    @Inject
    constructor(
        private val settingsRepository: ISettingsRepository,
    ) {
        /**
         * Gets the current user settings.
         *
         * @return Result containing user settings or error
         */
        suspend operator fun invoke(): Result<UserSettings> {
            return settingsRepository.getSettings()
        }

        /**
         * Observes user settings changes.
         *
         * @return Flow of user settings
         */
        fun observeSettings(): Flow<UserSettings> {
            return settingsRepository.observeSettings()
        }
    }
