package com.engfred.callguardian.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.engfred.callguardian.data.models.WhitelistedContactEntity

@Database(entities = [WhitelistedContactEntity::class], version = 1, exportSchema = false)
abstract class CallGuardianDatabase : RoomDatabase() {
    abstract fun whitelistedContactDao(): WhitelistedContactDao
}