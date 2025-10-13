package com.engfred.callguardian.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.engfred.callguardian.data.database.CallGuardianDatabase
import com.engfred.callguardian.data.database.WhitelistedContactDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CallGuardianDatabase {
        return Room.databaseBuilder(
            context,
            CallGuardianDatabase::class.java,
            "call_guardian_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWhitelistedContactDao(database: CallGuardianDatabase): WhitelistedContactDao {
        return database.whitelistedContactDao()
    }

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}