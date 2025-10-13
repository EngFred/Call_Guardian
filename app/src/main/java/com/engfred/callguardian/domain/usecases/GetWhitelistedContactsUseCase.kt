package com.engfred.callguardian.domain.usecases

import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.CallWhitelistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWhitelistedContactsUseCase @Inject constructor(
    private val repository: CallWhitelistRepository
) {
    operator fun invoke(): Flow<List<WhitelistedContact>> {
        return repository.getWhitelistedContacts()
    }
}