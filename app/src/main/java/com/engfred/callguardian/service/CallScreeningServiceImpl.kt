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

        // Normalize to digits only (matches stored whitelist format)
        val incomingNumber = rawIncomingNumber.replace(Regex("[^0-9]"), "")
        if (incomingNumber.length < 7) {  // Too short/invalid (e.g., private caller)
            Log.i(TAG, "Invalid short number: $incomingNumber. Rejecting.")
            respondToCall(details, createRejectResponse())
            return
        }

        try {
            // Use the synchronous cached check to make the decision immediately
            val isWhitelisted = callWhitelistManager.isNumberWhitelisted(incomingNumber)

            val response = if (isWhitelisted) {
                Log.i(TAG, "Number $incomingNumber is whitelisted. Allowing call.")
                createAllowResponse()
            } else {
                Log.i(TAG, "Number $incomingNumber is NOT whitelisted. Rejecting call.")
                createRejectResponse()
            }

            // Send the single, final response to the OS
            respondToCall(details, response)
        } catch (e: Exception) {
            Log.e(TAG, "Error screening call for $incomingNumber", e)
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