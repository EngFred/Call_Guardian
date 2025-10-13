package com.engfred.callguardian.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * Concrete implementation of the ContactRepository.
 * This class is responsible for interacting with the Android ContentResolver
 * to retrieve contact data.
 */
class ContactRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : ContactRepository {

    override fun getContactFromUri(contactUri: Uri): WhitelistedContact? {
        var name: String? = null
        var number: String? = null
        val cursor = contentResolver.query(contactUri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val contactIdIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                if (contactIdIndex != -1 && hasPhoneIndex != -1 && nameIndex != -1) {
                    name = c.getString(nameIndex)
                    val contactId = c.getString(contactIdIndex)
                    val hasPhone = c.getInt(hasPhoneIndex)

                    if (hasPhone > 0) {
                        val phones = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )
                        phones?.use { p ->
                            if (p.moveToFirst()) {
                                val phoneNumberIndex = p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                                if (phoneNumberIndex != -1) {
                                    number = p.getString(phoneNumberIndex)?.replace("[^0-9]".toRegex(), "")
                                }
                            }
                        }
                    }
                }
            }
        }
        return if (!number.isNullOrBlank()) {
            WhitelistedContact(phoneNumber = number!!, contactName = name)
        } else {
            null
        }
    }

    override fun getAllContacts(): List<WhitelistedContact> {
        val contacts = mutableListOf<WhitelistedContact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1",
            null,
            null
        )
        cursor?.use { c ->
            val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            if (idIndex == -1 || nameIndex == -1) return emptyList()

            while (c.moveToNext()) {
                val contactId = c.getString(idIndex)
                val name = c.getString(nameIndex)

                val phonesCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC, ${ContactsContract.CommonDataKinds.Phone._ID} ASC"
                )
                phonesCursor?.use { p ->
                    if (p.moveToFirst()) {
                        val numberIndex = p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numberIndex != -1) {
                            val rawNumber = p.getString(numberIndex)
                            val normalizedNumber = rawNumber?.replace("[^0-9]".toRegex(), "")
                            if (!normalizedNumber.isNullOrBlank() && normalizedNumber.length >= 7) {
                                contacts.add(
                                    WhitelistedContact(
                                        phoneNumber = normalizedNumber,
                                        contactName = name
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return contacts
    }
}