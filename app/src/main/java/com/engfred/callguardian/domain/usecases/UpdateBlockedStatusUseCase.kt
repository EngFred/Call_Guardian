package com.engfred.callguardian.domain.usecases

import com.engfred.callguardian.domain.repository.CallWhitelistRepository
import javax.inject.Inject

class UpdateBlockedStatusUseCase @Inject constructor(
    private val repository: CallWhitelistRepository
) {
    suspend operator fun invoke(normalizedPhoneNumber: String, isBlocked: Boolean) {
        repository.updateBlockedStatus(normalizedPhoneNumber, isBlocked)
    }
}