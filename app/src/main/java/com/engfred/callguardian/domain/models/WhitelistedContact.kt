package com.engfred.callguardian.domain.models

data class WhitelistedContact(
    val phoneNumber: String,
    val contactName: String?,
    val isBlocked: Boolean = false
)