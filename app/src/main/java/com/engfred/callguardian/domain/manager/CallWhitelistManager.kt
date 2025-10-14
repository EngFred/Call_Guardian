package com.engfred.callguardian.domain.manager

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.CallWhitelistRepository
import com.engfred.callguardian.domain.utils.PhoneNumberNormalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallWhitelistManager @Inject constructor(
    private val repository: CallWhitelistRepository,
    private val phoneNormalizer: PhoneNumberNormalizer,
    @ApplicationContext private val context: Context
) {
    private val _whitelistedNumbers = MutableStateFlow<List<String>>(emptyList())
    val whitelistedNumbers: StateFlow<List<String>> = _whitelistedNumbers.asStateFlow()

    // Synchronous cached copy for immediate access
    private var cachedWhitelist: List<WhitelistedContact> = emptyList()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val countryIso: String by lazy {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // Prioritize SIM country (stable), then network, then default
        tm.simCountryIso.takeIf { it.isNotBlank() }?.uppercase() ?:
        tm.networkCountryIso.takeIf { it.isNotBlank() }?.uppercase() ?:
        "UG"
    }
    // One-time log for debugging
    init {
        Log.d("WhitelistManager", "Using country ISO: $countryIso")
    }

    init {
        // Initial synchronous load
        scope.launch {
            cachedWhitelist = repository.getWhitelistedContacts().first()
            _whitelistedNumbers.value = cachedWhitelist.map { it.normalizedPhoneNumber }
        }

        // Continuous updates
        scope.launch {
            repository.getWhitelistedContacts().collectLatest { contacts ->
                cachedWhitelist = contacts
                _whitelistedNumbers.value = contacts.map { it.normalizedPhoneNumber }
            }
        }
    }

    // Synchronous check for call screening: normalize incoming and match normalized stored
    fun isNumberWhitelisted(incomingNumber: String): Boolean {
        val normalizedIncoming = phoneNormalizer.normalize(incomingNumber, countryIso)
            ?: run {
                Log.w("WhitelistManager", "Failed to normalize incoming: $incomingNumber")
                return false  // Invalid incoming, reject
            }
        Log.d("WhitelistManager", "Normalized incoming: $normalizedIncoming")
        val match = cachedWhitelist.any { it.normalizedPhoneNumber == normalizedIncoming && !it.isBlocked }
        Log.d("WhitelistManager", "Match found: $match for $normalizedIncoming")
        return match
    }
}