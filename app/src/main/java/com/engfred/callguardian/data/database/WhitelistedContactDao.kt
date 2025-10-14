package com.engfred.callguardian.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.engfred.callguardian.data.models.WhitelistedContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistedContactDao {

    @Query("SELECT * FROM whitelisted_contacts")
    fun getAllWhitelistedContacts(): Flow<List<WhitelistedContactEntity>>

    @Upsert
    suspend fun upsertContact(contact: WhitelistedContactEntity)

    @Delete
    suspend fun deleteContact(contact: WhitelistedContactEntity)

    @Query("UPDATE whitelisted_contacts SET isBlocked = :isBlocked WHERE normalizedPhoneNumber = :normalizedPhoneNumber")
    suspend fun updateBlockedStatus(normalizedPhoneNumber: String, isBlocked: Boolean)
}