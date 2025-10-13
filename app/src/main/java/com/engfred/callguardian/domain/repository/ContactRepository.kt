package com.engfred.callguardian.domain.repository

import android.net.Uri
import com.engfred.callguardian.domain.models.WhitelistedContact

/**
 * Defines the contract for retrieving contact information.
 * This is part of the Data Layer, but defined as an interface
 * in a way that the Domain Layer can depend on it.
 */
interface ContactRepository {

    /**
     * Retrieves a contact's details from a given URI.
     *
     * @param contactUri The Uri of the selected contact.
     * @return A WhitelistedContact object if a valid contact with a phone number is found,
     * otherwise returns null.
     */
    fun getContactFromUri(contactUri: Uri): WhitelistedContact?

    /**
     * Retrieves all contacts that have at least one phone number.
     *
     * @return A list of WhitelistedContact objects, each with a normalized phone number and display name.
     */
    fun getAllContacts(): List<WhitelistedContact>
}