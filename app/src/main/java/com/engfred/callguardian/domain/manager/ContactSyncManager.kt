package com.engfred.callguardian.domain.manager

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.engfred.callguardian.data.worker.SyncContactsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactSyncManager @Inject constructor(
    private val workManager: WorkManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var observerRegistered = false
    private var contentResolver: ContentResolver? = null
    private var observer: ContentObserver? = null

    private fun createObserver(): ContentObserver {
        return object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                // Ignore self-changes (though unlikely, as we don't modify contacts)
                if (selfChange) return
                // Enqueue immediate sync work
                val immediateSyncRequest = OneTimeWorkRequestBuilder<SyncContactsWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)  // Run soon, but respect quotas
                    .build()
                workManager.enqueueUniqueWork(
                    "immediate_contact_sync",
                    ExistingWorkPolicy.REPLACE,
                    immediateSyncRequest
                )
            }
        }
    }

    fun registerObserverIfPermitted(context: Context) {
        if (observerRegistered) return

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        contentResolver = context.contentResolver
        observer = createObserver()

        contentResolver?.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,  // Include sub-URIs
            observer!!
        )
        contentResolver?.registerContentObserver(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            true,
            observer!!
        )

        observerRegistered = true
    }

    fun enqueuePeriodicSync() {
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncContactsWorker>(
            15, TimeUnit.MINUTES  // Minimum interval for efficiency
        ).setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiresBatteryNotLow(false)  // Run even on low battery (local op)
                .build()
        ).build()
        workManager.enqueueUniquePeriodicWork(
            "periodic_contact_sync",
            ExistingPeriodicWorkPolicy.KEEP,  // Don't cancel if already running
            periodicSyncRequest
        )
    }

    fun triggerImmediateSync() {
        scope.launch {
            val request = OneTimeWorkRequestBuilder<SyncContactsWorker>().build()
            workManager.enqueueUniqueWork(
                "manual_contact_sync",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}