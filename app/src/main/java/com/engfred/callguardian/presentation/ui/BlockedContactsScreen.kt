package com.engfred.callguardian.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.presentation.components.ContactListItem
import com.engfred.callguardian.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedContactsScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val blockedContacts by viewModel.blockedContacts.collectAsState()
    var showUnblockDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<WhitelistedContact?>(null) }

    if (showUnblockDialog && selectedContact != null) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            title = { Text("Unblock Contact?") },
            text = { Text("Calls from ${selectedContact!!.contactName ?: selectedContact!!.phoneNumber} will be allowed again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.unblockContact(selectedContact!!)
                        showUnblockDialog = false
                        selectedContact = null
                    }
                ) {
                    Text("Unblock")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnblockDialog = false
                        selectedContact = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked Contacts (${blockedContacts.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "These contacts are blocked from calling.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (blockedContacts.isEmpty()) {
                Text(
                    "No blocked contacts yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(blockedContacts, key = { it.phoneNumber }) { contact ->
                        ContactListItem(
                            contact = contact,
                            isBlocked = true,
                            onActionClick = {
                                selectedContact = contact
                                showUnblockDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}