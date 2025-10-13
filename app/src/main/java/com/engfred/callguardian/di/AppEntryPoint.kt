package com.engfred.callguardian.di

import com.engfred.callguardian.domain.manager.ContactSyncManager
import com.engfred.callguardian.domain.usecases.GetWhitelistedContactsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun getWhitelistedContactsUseCase(): GetWhitelistedContactsUseCase
    fun contactSyncManager(): ContactSyncManager
}