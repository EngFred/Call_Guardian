package com.engfred.callguardian.data.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engfred.callguardian.domain.usecases.SyncContactsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "SyncContactsWorker"

@HiltWorker
class SyncContactsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncContactsUseCase: SyncContactsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Check permission before syncing
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "READ_CONTACTS permission denied; skipping sync")
            return Result.failure()
        }

        return try {
            syncContactsUseCase()
            Log.i(TAG, "Contacts sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during contacts sync", e)
            // Log error in production (e.g., via Timber); retry if transient
            Result.retry()
        }
    }
}