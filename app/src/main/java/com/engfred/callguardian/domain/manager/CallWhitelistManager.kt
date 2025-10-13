package com.engfred.callguardian.domain.manager

import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.repository.CallWhitelistRepository
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
    private val repository: CallWhitelistRepository
) {
    private val _whitelistedNumbers = MutableStateFlow<List<String>>(emptyList())
    val whitelistedNumbers: StateFlow<List<String>> = _whitelistedNumbers.asStateFlow()

    // Synchronous cached copy for immediate access
    private var cachedWhitelist: List<WhitelistedContact> = emptyList()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Initial synchronous load
        scope.launch {
            cachedWhitelist = repository.getWhitelistedContacts().first()
            _whitelistedNumbers.value = cachedWhitelist.map { it.phoneNumber }
        }

        // Continuous updates
        scope.launch {
            repository.getWhitelistedContacts().collectLatest { contacts ->
                cachedWhitelist = contacts
                _whitelistedNumbers.value = contacts.map { it.phoneNumber }
            }
        }
    }

    // Synchronous check for call screening: in DB and not blocked
    fun isNumberWhitelisted(number: String): Boolean {
        return cachedWhitelist.any { it.phoneNumber == number && !it.isBlocked }
    }
}