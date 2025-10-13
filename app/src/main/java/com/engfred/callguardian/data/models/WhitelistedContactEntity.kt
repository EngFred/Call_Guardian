package com.engfred.callguardian.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelisted_contacts")
data class WhitelistedContactEntity(
    @PrimaryKey
    val phoneNumber: String,
    val contactName: String?,
    val isBlocked: Boolean = false
)