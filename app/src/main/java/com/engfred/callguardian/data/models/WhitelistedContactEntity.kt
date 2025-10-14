package com.engfred.callguardian.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelisted_contacts")
data class WhitelistedContactEntity(
    @PrimaryKey
    val normalizedPhoneNumber: String,
    val originalPhoneNumber: String,
    val contactName: String?,
    val contactId: String? = null,
    val isBlocked: Boolean = false
)