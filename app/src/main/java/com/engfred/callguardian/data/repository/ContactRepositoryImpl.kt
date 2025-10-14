package com.engfred.callguardian.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.ContactRepository
import com.engfred.callguardian.domain.utils.PhoneNumberNormalizer
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Concrete implementation of the ContactRepository.
 * This class is responsible for interacting with the Android ContentResolver
 * to retrieve contact data.
 */
class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val phoneNormalizer: PhoneNumberNormalizer
) : ContactRepository {

    private val contentResolver: ContentResolver = context.contentResolver
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val telephonyManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val countryIso: String by lazy {
        // Prioritize SIM country (stable), then network, then default
        telephonyManager.simCountryIso.takeIf { it.isNotBlank() }?.uppercase() ?:
        telephonyManager.networkCountryIso.takeIf { it.isNotBlank() }?.uppercase() ?:
        "UG"
    }
    // One-time log for debugging
    init {
        Log.d("ContactRepo", "Using country ISO: $countryIso")
    }

    override fun getContactFromUri(contactUri: Uri): WhitelistedContact? {
        var name: String? = null
        var firstValidNumber: WhitelistedContact? = null
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
                            "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC, ${ContactsContract.CommonDataKinds.Phone._ID} ASC"
                        )
                        phones?.use { p ->
                            while (p.moveToNext()) {  // Loop for all, but take first valid for single return
                                val phoneNumberIndex = p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (phoneNumberIndex != -1) {
                                    val rawNumber = p.getString(phoneNumberIndex)
                                    if (!rawNumber.isNullOrBlank()) {
                                        val original = rawNumber
                                        val normalized = phoneNormalizer.normalize(original, countryIso)
                                        if (!normalized.isNullOrBlank() && normalized.length >= 10) {
                                            firstValidNumber = WhitelistedContact(
                                                originalPhoneNumber = original,
                                                normalizedPhoneNumber = normalized,
                                                contactName = name,
                                                contactId = contactId
                                            )
                                            break  // First valid for single contact
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return firstValidNumber
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
                    null  // No sorting, get all
                )
                phonesCursor?.use { p ->
                    val numberIndex = p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numberIndex != -1) {
                        while (p.moveToNext()) {  // All phones
                            val rawNumber = p.getString(numberIndex)
                            if (!rawNumber.isNullOrBlank()) {
                                val original = rawNumber
                                val normalized = phoneNormalizer.normalize(original, countryIso)
                                if (!normalized.isNullOrBlank() && normalized.length >= 10) {
                                    contacts.add(
                                        WhitelistedContact(
                                            originalPhoneNumber = original,
                                            normalizedPhoneNumber = normalized,
                                            contactName = name,
                                            contactId = contactId
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return contacts
    }
}