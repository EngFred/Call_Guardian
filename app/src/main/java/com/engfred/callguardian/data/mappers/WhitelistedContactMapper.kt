package com.engfred.callguardian.data.mappers

import com.engfred.callguardian.data.models.WhitelistedContactEntity
import com.engfred.callguardian.domain.models.WhitelistedContact

object WhitelistedContactMapper {
    fun mapToEntity(contact: WhitelistedContact): WhitelistedContactEntity =
        WhitelistedContactEntity(
            normalizedPhoneNumber = contact.normalizedPhoneNumber,
            originalPhoneNumber = contact.originalPhoneNumber,
            contactName = contact.contactName,
            contactId = contact.contactId,
            isBlocked = contact.isBlocked
        )

    fun mapFromEntity(entity: WhitelistedContactEntity): WhitelistedContact =
        WhitelistedContact(
            originalPhoneNumber = entity.originalPhoneNumber,
            normalizedPhoneNumber = entity.normalizedPhoneNumber,
            contactName = entity.contactName,
            contactId = entity.contactId,
            isBlocked = entity.isBlocked
        )
}