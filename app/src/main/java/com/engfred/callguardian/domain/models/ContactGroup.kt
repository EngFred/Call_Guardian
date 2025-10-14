package com.engfred.callguardian.domain.models

data class ContactGroup(
    val contactName: String?,
    val contactId: String?,
    val primaryContact: WhitelistedContact,  // First number as primary
    val otherContacts: List<WhitelistedContact>,  // Rest
    val isExpanded: Boolean = false,
    val blockedCount: Int = 0  // For badge if some blocked
)