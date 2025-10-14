package com.engfred.callguardian.domain.models

data class WhitelistedContact(
    val originalPhoneNumber: String,
    val normalizedPhoneNumber: String,
    val contactName: String?,
    val contactId: String? = null,
    val isBlocked: Boolean = false
)