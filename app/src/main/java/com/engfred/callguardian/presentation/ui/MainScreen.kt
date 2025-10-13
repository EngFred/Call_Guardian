package com.engfred.callguardian.presentation.ui

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.presentation.components.ContactListItem
import com.engfred.callguardian.presentation.viewmodel.MainViewModel
import com.engfred.callguardian.presentation.viewmodel.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavController
) {
    val whitelistedContacts by viewModel.whitelistedContacts.collectAsState()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<WhitelistedContact?>(null) }

    // Permission launcher for reading contacts (triggers sync and observer on grant)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, trigger sync, register observer
            viewModel.triggerSyncContacts()
            viewModel.registerObserver(context)
            Toast.makeText(context, "Access granted. Whitelist synced with contacts.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Cannot sync contacts.", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher to request Call Screening role
    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Call screening role granted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Call screening role not granted. App may not work properly.", Toast.LENGTH_LONG).show()
        }
    }

    // Check permissions and roles on launch
    LaunchedEffect(Unit) {
        // Request READ_CONTACTS if not granted, and sync/register on success
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            // Already granted, trigger sync and register observer
            viewModel.triggerSyncContacts()
            viewModel.registerObserver(context)
        }

        // Check and request call screening role
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleRequestLauncher.launch(intent)
            }
        }
    }

    if (showConfirmDialog && selectedContact != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Block Contact?") },
            text = { Text("Calls from ${selectedContact!!.contactName ?: selectedContact!!.phoneNumber} will be blocked. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeContact(selectedContact!!)
                        showConfirmDialog = false
                        selectedContact = null
                    }
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
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
                title = { Text("Call Guardian", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manage Call Forwarding") },
                            onClick = {
                                navController.navigate("call_forwarding_screen")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Blocked Contacts") },
                            onClick = {
                                navController.navigate("blocked_contacts_screen")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                navController.navigate("settings_screen")
                                expanded = false
                            }
                        )
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
                text = "Contacts not listed below will be blocked by default, This list syncs automatically with your contacts by default",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 0.dp).basicMarquee()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allowed Contacts (${whitelistedContacts.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { sortExpanded = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }

                    // Move the DropdownMenu here so it is anchored to the IconButton above.
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false },
                    ) {
                        SortType.entries.forEach { sortType ->
                            DropdownMenuItem(
                                text = { Text(sortType.name.replace("_", " -> ")) },
                                onClick = {
                                    viewModel.setSortType(sortType)
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (whitelistedContacts.isEmpty()) {
                Text(
                    "No allowed contacts yet. Grant contacts permission to sync.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(whitelistedContacts, key = { it.phoneNumber }) { contact ->
                        ContactListItem(
                            contact = contact,
                            isBlocked = false,
                            onActionClick = {
                                selectedContact = contact
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}
