package com.engfred.callguardian

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import com.engfred.callguardian.di.AppEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

@HiltAndroidApp
class CallGuardianApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Enqueue initial periodic sync (worker uses injected factory)
        EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)
            .contactSyncManager()
            .enqueuePeriodicSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}