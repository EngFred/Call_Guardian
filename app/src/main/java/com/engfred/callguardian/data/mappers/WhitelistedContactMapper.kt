package com.engfred.callguardian.data.mappers

import com.engfred.callguardian.data.models.WhitelistedContactEntity
import com.engfred.callguardian.domain.models.WhitelistedContact

object WhitelistedContactMapper {
    fun mapFromEntity(entity: WhitelistedContactEntity): WhitelistedContact {
        return WhitelistedContact(
            phoneNumber = entity.phoneNumber,
            contactName = entity.contactName,
            isBlocked = entity.isBlocked
        )
    }

    fun mapToEntity(domainModel: WhitelistedContact): WhitelistedContactEntity {
        return WhitelistedContactEntity(
            phoneNumber = domainModel.phoneNumber,
            contactName = domainModel.contactName,
            isBlocked = domainModel.isBlocked
        )
    }
}