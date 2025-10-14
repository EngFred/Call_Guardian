package com.engfred.callguardian.domain.usecases

import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.CallWhitelistRepository
import com.engfred.callguardian.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val callWhitelistRepository: CallWhitelistRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val allPhoneContacts = contactRepository.getAllContacts()
        val normalizedSet = allPhoneContacts.map { it.normalizedPhoneNumber }.toSet()
        val currentDbContacts = callWhitelistRepository.getWhitelistedContacts().first()

        // Upsert new/updated phone contacts (preserve isBlocked for existing)
        allPhoneContacts.forEach { phoneContact ->
            val existingDbContact = currentDbContacts.find { it.normalizedPhoneNumber == phoneContact.normalizedPhoneNumber }
            val contactToUpsert = existingDbContact?.copy(
                contactName = phoneContact.contactName,
                originalPhoneNumber = phoneContact.originalPhoneNumber,
                contactId = phoneContact.contactId
            ) ?: phoneContact.copy(isBlocked = false)
            callWhitelistRepository.upsertContact(contactToUpsert)
        }

        // Remove DB entries for deleted phone contacts
        currentDbContacts
            .filter { contact -> !normalizedSet.contains(contact.normalizedPhoneNumber) }
            .forEach { contact ->
                callWhitelistRepository.removeContact(contact)
            }
    }
}