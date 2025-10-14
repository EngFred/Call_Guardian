package com.engfred.callguardian.service

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.engfred.callguardian.domain.manager.CallWhitelistManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "CallScreeningService"

@AndroidEntryPoint
class CallScreeningServiceImpl : CallScreeningService() {

    @Inject
    lateinit var callWhitelistManager: CallWhitelistManager

    override fun onScreenCall(details: Call.Details) {
        val rawIncomingNumber = details.handle?.schemeSpecificPart
        if (rawIncomingNumber == null) {
            Log.i(TAG, "No incoming number found. Rejecting by default.")
            respondToCall(details, createRejectResponse())
            return
        }

        if (rawIncomingNumber.length < 7) {  // Too short/invalid (e.g., private caller)
            Log.i(TAG, "Invalid short number: $rawIncomingNumber. Rejecting.")
            respondToCall(details, createRejectResponse())
            return
        }

        try {
            // Pass raw (may have +/spaces); let manager normalize robustly
            val isWhitelisted = callWhitelistManager.isNumberWhitelisted(rawIncomingNumber)

            val response = if (isWhitelisted) {
                Log.i(TAG, "Number $rawIncomingNumber is whitelisted. Allowing call.")
                createAllowResponse()
            } else {
                Log.i(TAG, "Number $rawIncomingNumber is NOT whitelisted. Rejecting call.")
                createRejectResponse()
            }

            // Send the single, final response to the OS
            respondToCall(details, response)
        } catch (e: Exception) {
            Log.e(TAG, "Error screening call for $rawIncomingNumber", e)
            // Default to reject on error for safety
            respondToCall(details, createRejectResponse())
        }
    }

    private fun createRejectResponse(): CallResponse {
        return CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .build()
    }

    private fun createAllowResponse(): CallResponse {
        // An empty builder allows the call to proceed as normal
        return CallResponse.Builder().build()
    }
}