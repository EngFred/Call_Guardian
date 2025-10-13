package com.engfred.callguardian.di

import com.engfred.callguardian.domain.repository.CallWhitelistRepository
import com.engfred.callguardian.data.repository.CallWhitelistRepositoryImpl
import com.engfred.callguardian.domain.repository.ContactRepository
import com.engfred.callguardian.data.repository.ContactRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCallWhitelistRepository(
        impl: CallWhitelistRepositoryImpl
    ): CallWhitelistRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        impl: ContactRepositoryImpl
    ): ContactRepository
}