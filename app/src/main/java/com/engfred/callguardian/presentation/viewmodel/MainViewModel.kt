package com.engfred.callguardian.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engfred.callguardian.domain.manager.ContactSyncManager
import com.engfred.callguardian.domain.models.ContactGroup
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.domain.usecases.GetWhitelistedContactsUseCase
import com.engfred.callguardian.domain.usecases.UpsertContactToWhitelistUseCase
import com.engfred.callguardian.domain.usecases.UpdateBlockedStatusUseCase
import com.engfred.callguardian.domain.utils.PhoneNumberNormalizer
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
    private val upsertContactToWhitelistUseCase: UpsertContactToWhitelistUseCase,
    getWhitelistedContactsUseCase: GetWhitelistedContactsUseCase,
    private val phoneNormalizer: PhoneNumberNormalizer,
    private val contactSyncManager: ContactSyncManager
) : ViewModel() {

    private val _currentSortType = MutableStateFlow(SortType.A_Z)
    val currentSortType: StateFlow<SortType> = _currentSortType.asStateFlow()

    private val _expandedKeys = MutableStateFlow<Set<String>>(emptySet())

    private val allContactsFlow = getWhitelistedContactsUseCase()

    // Group non-blocked contacts by contactId (or name for manual)
    private val groupedContactsFlow = allContactsFlow.map { contacts ->
        val nonBlocked = contacts.filter { !it.isBlocked }
        nonBlocked.groupBy { it.contactId ?: it.contactName ?: "" }  // Group by ID or name fallback
            .map { (key, groupList) ->
                val sortedGroup = groupList.sortedBy { it.originalPhoneNumber }
                val primary = sortedGroup.first()
                val others = sortedGroup.drop(1)
                val blockedInGroup = contacts.count { it.contactId == key && it.isBlocked }
                ContactGroup(
                    contactName = primary.contactName,
                    contactId = primary.contactId,
                    primaryContact = primary,
                    otherContacts = others,
                    blockedCount = blockedInGroup
                )
            }
    }

    // Exposes grouped non-blocked whitelisted contacts, sorted by current type
    val whitelistedContacts: StateFlow<List<ContactGroup>> = combine(
        groupedContactsFlow,
        _expandedKeys,
        _currentSortType
    ) { rawGroups, expanded, sortType ->
        rawGroups.map { group ->
            val key = group.contactId ?: group.contactName ?: ""
            group.copy(isExpanded = expanded.contains(key))
        }.sortedWith(getGroupComparator(sortType))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Flat blocked contacts for BlockedScreen (no grouping)
    val flatBlockedContacts: StateFlow<List<WhitelistedContact>> = allContactsFlow
        .map { contacts ->
            contacts.filter { it.isBlocked }
                .sortedBy { it.contactName?.lowercase() ?: it.originalPhoneNumber }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Helper for group comparator (by name or primary number)
    private fun getGroupComparator(sortType: SortType) = when (sortType) {
        SortType.A_Z -> compareBy<ContactGroup> { it.contactName?.lowercase() ?: "" }
        SortType.Z_A -> compareByDescending<ContactGroup> { it.contactName?.lowercase() ?: "" }
        SortType.RECENT -> compareByDescending<ContactGroup> { it.primaryContact.originalPhoneNumber }
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

    // Add manual contact to whitelist
    fun addManualContact(name: String?, number: String, countryIso: String) {
        viewModelScope.launch {
            val normalized = phoneNormalizer.normalize(number, countryIso)
            if (!normalized.isNullOrBlank() && normalized.length >= 10) {
                val newContact = WhitelistedContact(
                    originalPhoneNumber = number,
                    normalizedPhoneNumber = normalized,
                    contactName = name?.takeIf { it.isNotBlank() },
                    contactId = null  // Manual, no ID
                )
                upsertContactToWhitelistUseCase(newContact)
            }
        }
    }

    // Toggle group expansion (for UI state; not persisted)
    fun toggleGroupExpansion(group: ContactGroup) {
        val key = group.contactId ?: group.contactName ?: ""
        val currentExpanded = _expandedKeys.value.contains(key)
        _expandedKeys.value = _expandedKeys.value.toMutableSet().apply {
            if (currentExpanded) {
                remove(key)
            } else {
                add(key)
            }
        }
    }

    // Update sort type (for menu toggle)
    fun setSortType(sortType: SortType) {
        _currentSortType.value = sortType
    }
}