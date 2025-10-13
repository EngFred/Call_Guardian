package com.engfred.callguardian.domain.repository

import com.engfred.callguardian.domain.models.WhitelistedContact
import kotlinx.coroutines.flow.Flow

interface CallWhitelistRepository {
    fun getWhitelistedContacts(): Flow<List<WhitelistedContact>>
    suspend fun upsertContact(contact: WhitelistedContact)
    suspend fun removeContact(contact: WhitelistedContact)
    suspend fun updateBlockedStatus(phoneNumber: String, isBlocked: Boolean)
}