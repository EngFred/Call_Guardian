package com.engfred.callguardian.presentation.ui

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.engfred.callguardian.domain.models.WhitelistedContact
import com.engfred.callguardian.presentation.components.ContactGroupItem
import com.engfred.callguardian.presentation.viewmodel.MainViewModel
import com.engfred.callguardian.presentation.viewmodel.SortType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavController
) {
    val groupedContacts by viewModel.whitelistedContacts.collectAsState()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var sortExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<WhitelistedContact?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(TextFieldValue("")) }
    var numberInput by remember { mutableStateOf(TextFieldValue("")) }

    val countryIso = remember {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.simCountryIso.takeIf { it.isNotBlank() }?.uppercase() ?: "UG"
    }

    // Permission launcher...
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.triggerSyncContacts()
            viewModel.registerObserver(context)
            Toast.makeText(context, "Access granted. Whitelist synced with contacts.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Cannot sync contacts.", Toast.LENGTH_LONG).show()
        }
    }

    // Role launcher...
    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Call screening role granted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Call screening role not granted. App may not work properly.", Toast.LENGTH_LONG).show()
        }
    }

    // Check permissions...
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            viewModel.triggerSyncContacts()
            viewModel.registerObserver(context)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleRequestLauncher.launch(intent)
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add to Whitelist") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name (optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = numberInput,
                        onValueChange = { numberInput = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addManualContact(nameInput.text, numberInput.text, countryIso)
                        showAddDialog = false
                        nameInput = TextFieldValue("")
                        numberInput = TextFieldValue("")
                        Toast.makeText(context, "Added to whitelist.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Confirm Block Dialog
    if (showConfirmDialog && selectedContact != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Block Number?") },
            text = { Text("Calls from ${selectedContact!!.contactName ?: selectedContact!!.originalPhoneNumber} will be blocked.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeContact(selectedContact!!)
                    showConfirmDialog = false
                    selectedContact = null
                }) { Text("Block") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false; selectedContact = null }) { Text("Cancel") } }
        )
    }

    // Navigation Drawer Sheet
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Menu",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Divider(color = MaterialTheme.colorScheme.outline)
                    DropdownMenuItem(
                        text = { Text("Manage Call Forwarding") },
                        onClick = { scope.launch { drawerState.close(); navController.navigate("call_forwarding_screen") } }
                    )
                    DropdownMenuItem(
                        text = { Text("View Blocked Contacts") },
                        onClick = { scope.launch { drawerState.close(); navController.navigate("blocked_contacts_screen") } }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { scope.launch { drawerState.close(); navController.navigate("settings_screen") } }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Call Guardian", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { sortExpanded = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
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
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Allowed Contacts (${groupedContacts.sumOf { if (it.isExpanded) it.otherContacts.size + 1 else 1 }})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (groupedContacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No allowed contacts yet. Grant contacts permission to sync or add manually.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)  // Space for FAB
                    ) {
                        items(groupedContacts, key = { it.contactId ?: it.contactName ?: "" }) { group ->
                            ContactGroupItem(
                                group = group,
                                onGroupToggle = { viewModel.toggleGroupExpansion(it) },
                                onBlockClick = { contact ->
                                    selectedContact = contact
                                    showConfirmDialog = true
                                }
                            )
                            if (groupedContacts.last() != group) {
                                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
