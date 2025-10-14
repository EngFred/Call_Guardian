package com.engfred.callguardian.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engfred.callguardian.domain.manager.ContactSyncManager
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.usecases.GetWhitelistedContactsUseCase
import com.engfred.callguardian.domain.usecases.UpdateBlockedStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.comparisons.compareBy
import kotlin.comparisons.compareByDescending

enum class SortType {
    A_Z,
    Z_A,
    RECENT
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateBlockedStatusUseCase: UpdateBlockedStatusUseCase,
    getWhitelistedContactsUseCase: GetWhitelistedContactsUseCase,
    private val contactSyncManager: ContactSyncManager
) : ViewModel() {

    private val _currentSortType = MutableStateFlow(SortType.A_Z)
    val currentSortType: StateFlow<SortType> = _currentSortType.asStateFlow()

    private val allContactsFlow = getWhitelistedContactsUseCase()

    // Exposes the list of non-blocked whitelisted contacts, sorted by current type
    val whitelistedContacts: StateFlow<List<WhitelistedContact>> = combine(
        allContactsFlow,
        _currentSortType
    ) { contacts, sortType ->
        contacts.filter { !it.isBlocked }
            .sortedWith(getComparator(sortType))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Exposes blocked contacts (sorted A-Z, non-reactive for simplicity)
    val blockedContacts: StateFlow<List<WhitelistedContact>> =
        allContactsFlow
            .map { contacts ->
                contacts.filter { it.isBlocked }
                    .sortedBy { it.contactName?.lowercase() ?: "" }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Helper for comparator
    private fun getComparator(sortType: SortType) = when (sortType) {
        SortType.A_Z -> compareBy<WhitelistedContact> { it.contactName?.lowercase() ?: "" }
        SortType.Z_A -> compareByDescending<WhitelistedContact> { it.contactName?.lowercase() ?: "" }
        SortType.RECENT -> compareByDescending<WhitelistedContact> { it.originalPhoneNumber }  // Use original for sort
    }

    // Function to trigger syncing contacts with whitelist (enqueues work)
    fun triggerSyncContacts() {
        contactSyncManager.triggerImmediateSync()
    }

    // Register observer for real-time changes (post-permission)
    fun registerObserver(context: Context) {
        contactSyncManager.registerObserverIfPermitted(context)
    }

    // Function to block a contact (sets isBlocked = true)
    fun removeContact(contact: WhitelistedContact) {
        viewModelScope.launch {
            updateBlockedStatusUseCase(contact.normalizedPhoneNumber, true)
        }
    }

    // Function to unblock a contact (sets isBlocked = false)
    fun unblockContact(contact: WhitelistedContact) {
        viewModelScope.launch {
            updateBlockedStatusUseCase(contact.normalizedPhoneNumber, false)
        }
    }

    // Update sort type (for menu toggle)
    fun setSortType(sortType: SortType) {
        _currentSortType.value = sortType
    }
}