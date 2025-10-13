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
        val phoneNumbersSet = allPhoneContacts.map { it.phoneNumber }.toSet()
        val currentDbContacts = callWhitelistRepository.getWhitelistedContacts().first()

        // Upsert new phone contacts (isBlocked=false); update names for existing (keep isBlocked)
        allPhoneContacts.forEach { phoneContact ->
            val existingDbContact = currentDbContacts.find { it.phoneNumber == phoneContact.phoneNumber }
            val contactToUpsert = existingDbContact?.copy(contactName = phoneContact.contactName)
                ?: phoneContact.copy(isBlocked = false)
            callWhitelistRepository.upsertContact(contactToUpsert)
        }

        // Remove DB entries for deleted phone contacts
        currentDbContacts
            .filter { contact -> !phoneNumbersSet.contains(contact.phoneNumber) }
            .forEach { contact ->
                callWhitelistRepository.removeContact(contact)
            }
    }
}