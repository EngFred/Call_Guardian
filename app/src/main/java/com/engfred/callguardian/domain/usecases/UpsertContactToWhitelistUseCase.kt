package com.engfred.callguardian.domain.usecases

import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.CallWhitelistRepository
import javax.inject.Inject

class UpsertContactToWhitelistUseCase @Inject constructor(
    private val repository: CallWhitelistRepository
) {
    suspend operator fun invoke(contact: WhitelistedContact) {
        repository.upsertContact(contact)
    }
}