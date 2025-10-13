package com.engfred.callguardian.data.repository

import com.engfred.callguardian.data.database.WhitelistedContactDao
import com.engfred.callguardian.data.mappers.WhitelistedContactMapper
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.CallWhitelistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CallWhitelistRepositoryImpl @Inject constructor(
    private val contactDao: WhitelistedContactDao
) : CallWhitelistRepository {

    override fun getWhitelistedContacts(): Flow<List<WhitelistedContact>> {
        return contactDao.getAllWhitelistedContacts().map { entities ->
            entities.map { WhitelistedContactMapper.mapFromEntity(it) }
        }
    }

    override suspend fun upsertContact(contact: WhitelistedContact) {
        contactDao.upsertContact(WhitelistedContactMapper.mapToEntity(contact))
    }

    override suspend fun removeContact(contact: WhitelistedContact) {
        contactDao.deleteContact(WhitelistedContactMapper.mapToEntity(contact))
    }

    override suspend fun updateBlockedStatus(phoneNumber: String, isBlocked: Boolean) {
        contactDao.updateBlockedStatus(phoneNumber, isBlocked)
    }
}