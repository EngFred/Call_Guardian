package com.engfred.callguardian.domain.usecases

import android.net.Uri
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * Use case for retrieving a contact's details from a URI.
 * This class is part of the Domain Layer and is independent of
 * any Android framework dependencies.
 */
class GetContactFromUriUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    operator fun invoke(contactUri: Uri): WhitelistedContact? {
        return contactRepository.getContactFromUri(contactUri)
    }
}