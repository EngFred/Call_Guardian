package com.engfred.callguardian.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.engfred.callguardian.presentation.viewmodel.CallForwardingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallForwardingScreen(
    navController: NavController,
    viewModel: CallForwardingViewModel = hiltViewModel()
) {
    val forwardingNumber by viewModel.forwardingNumber.collectAsState()
    val forwardAll by viewModel.forwardAll.collectAsState()
    val forwardWhenBusy by viewModel.forwardWhenBusy.collectAsState()
    val forwardWhenUnanswered by viewModel.forwardWhenUnanswered.collectAsState()
    val forwardWhenUnreachable by viewModel.forwardWhenUnreachable.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Launcher for CALL_PHONE permission
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permission granted. Try the action again.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Call permission is required to send MMI codes.", Toast.LENGTH_LONG).show()
        }
    }

    // Contact picker launcher: returns a contact Uri or null
    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri == null) {
            Toast.makeText(context, "No contact selected.", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        try {
            val number = getPhoneNumberFromContactUri(context, contactUri)
            if (!number.isNullOrBlank()) {
                // cleaned number (keeps + if present)
                val cleaned = number.replace("[^+0-9]".toRegex(), "")
                viewModel.updateForwardingNumber(cleaned)
                Toast.makeText(context, "Selected contact number filled.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Selected contact has no phone number.", Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            Toast.makeText(context, "Failed to get contact: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher for READ_CONTACTS permission (for contact picker)
    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // permission granted: launch contact picker
            pickContactLauncher.launch(null)
        } else {
            Toast.makeText(context, "Contacts permission is required to pick a contact.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Forwarding", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                .padding(16.dp)
        ) {
            Text(
                text = "Forward my calls to:",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = forwardingNumber,
                onValueChange = { viewModel.updateForwardingNumber(it) },
                label = { Text("Destination number") },
                placeholder = { Text("e.g., 0777 123456 or +256777123456") },
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = "Phone icon") },
                trailingIcon = {
                    IconButton(onClick = {
                        // Check/read contacts permission, then launch picker
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                            pickContactLauncher.launch(null)
                        } else {
                            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Pick contact")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Forward when...",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ForwardingOption(
                label = "All calls (forward everything immediately)",
                icon = Icons.Default.PhoneForwarded,
                checked = forwardAll,
                onClick = { viewModel.toggleForwardAll() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ForwardingOption(
                label = "Line is busy",
                icon = Icons.Default.Close,
                checked = forwardWhenBusy,
                onClick = { viewModel.toggleForwardWhenBusy() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ForwardingOption(
                label = "No answer",
                icon = Icons.Default.Call,
                checked = forwardWhenUnanswered,
                onClick = { viewModel.toggleForwardWhenUnanswered() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ForwardingOption(
                label = "Phone is unreachable/off",
                icon = Icons.Default.WifiOff,
                checked = forwardWhenUnreachable,
                onClick = { viewModel.toggleForwardWhenUnreachable() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Helpful guidance
            Text(
                text = "Tip: Use full international format (e.g., +2567XXXXXXX) or a local number starting with 0 — the app will try to normalize local Ugandan numbers.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Buttons row (unchanged behaviour)
            val coroutineScope = rememberCoroutineScope()
            Column {
                Button(
                    onClick = {
                        // Validate inputs
                        if (forwardingNumber.isBlank() || !(forwardAll || forwardWhenBusy || forwardWhenUnanswered || forwardWhenUnreachable)) {
                            Toast.makeText(context, "Enter a destination number and select at least one forwarding condition.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Permission check
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                            return@Button
                        }

                        // Build and run MMI activation codes
                        val normalized = normalizeNumberForNetwork(forwardingNumber.trim())
                        val codesToActivate = buildActivationCodes(
                            normalized,
                            forwardAll,
                            forwardWhenBusy,
                            forwardWhenUnanswered,
                            forwardWhenUnreachable
                        )
                        coroutineScope.launch {
                            // Send activation codes sequentially with a small delay
                            sendMmiCodesSequentially(context, codesToActivate)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Enable selected forwarding")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        // Validate selection - number not required to remove (deactivation uses codes)
                        if (!(forwardAll || forwardWhenBusy || forwardWhenUnanswered || forwardWhenUnreachable)) {
                            Toast.makeText(context, "Select at least one forwarding condition to disable.", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        // Permission check
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                            return@OutlinedButton
                        }

                        val codesToDeactivate = buildDeactivationCodes(
                            forwardAll,
                            forwardWhenBusy,
                            forwardWhenUnanswered,
                            forwardWhenUnreachable
                        )
                        coroutineScope.launch {
                            sendMmiCodesSequentially(context, codesToDeactivate)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Disable selected forwarding")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        // Clear all forwarding (unregister all diversions)
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                            return@OutlinedButton
                        }

                        coroutineScope.launch {
                            // universal clear
                            sendMmiCodesSequentially(context, listOf("##002#"))
                        }
                        viewModel.resetState()
                        Toast.makeText(context, "Sent request to clear all forwarding. Please confirm in the phone UI.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Clear all forwarding")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Robust helper to resolve a phone number from a contact Uri returned by PickContact().
 *
 * Strategy:
 * 1) Try to query ContactsContract.CommonDataKinds.Phone.NUMBER directly from the provided Uri (works when
 *    the returned Uri points to a "data" or "phone" row).
 * 2) If that fails or returns empty, query the Contacts table to obtain the contact ID, then query
 *    the Phone table (ContactsContract.CommonDataKinds.Phone.CONTENT_URI) for the contact's primary number.
 *
 * Returns null if no phone number found.
 */
private fun getPhoneNumberFromContactUri(context: Context, contactUri: Uri): String? {
    val resolver = context.contentResolver

    // Attempt 1: try to query NUMBER directly on the provided URI
    try {
        val projectionPhone = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor = resolver.query(contactUri, projectionPhone, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val index = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (index != -1) {
                    val raw = c.getString(index)
                    if (!raw.isNullOrBlank()) return raw
                }
            }
        }
    } catch (iae: IllegalArgumentException) {
        // Column not present for this URI — fall through to fallback method
    } catch (t: Throwable) {
        // Any other error — fall through to fallback
    }

    // Attempt 2: query contact Id from the Uri, then query Phone table
    try {
        val projectionContact = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER)
        val contactCursor = resolver.query(contactUri, projectionContact, null, null, null)
        contactCursor?.use { cc ->
            if (cc.moveToFirst()) {
                val idIndex = cc.getColumnIndex(ContactsContract.Contacts._ID)
                val hasPhoneIndex = cc.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                if (idIndex != -1) {
                    val contactId = cc.getString(idIndex)
                    val hasPhone = if (hasPhoneIndex != -1) cc.getInt(hasPhoneIndex) else 1
                    if (hasPhone > 0 && !contactId.isNullOrBlank()) {
                        // Query the Phone table for this contact id (prefer primary)
                        val phonesCursor = resolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"
                        )
                        phonesCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val numIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIndex != -1) {
                                    val raw = pc.getString(numIndex)
                                    if (!raw.isNullOrBlank()) return raw
                                }
                            }
                        }
                    }
                }
            }
        }
    } catch (t: Throwable) {
        // Give up and return null if anything goes wrong here
    }

    // No number found
    return null
}

private fun normalizeNumberForNetwork(input: String): String {
    var n = input.replace("\\s+".toRegex(), "") // remove spaces
    if (n.startsWith("+")) return n
    if (n.startsWith("0") && n.length >= 9) {
        // assume Uganda local: 0XXXXXXXX -> +256XXXXXXXX (drop leading 0)
        return "+256" + n.substring(1)
    }
    return n
}

private fun buildActivationCodes(number: String, all: Boolean, busy: Boolean, unanswered: Boolean, unreachable: Boolean): List<String> {
    val codes = mutableListOf<String>()
    if (all) codes.add("**21*$number#")
    if (busy) codes.add("**67*$number#")
    if (unanswered) codes.add("**61*$number#")
    if (unreachable) codes.add("**62*$number#")
    return codes
}

private fun buildDeactivationCodes(all: Boolean, busy: Boolean, unanswered: Boolean, unreachable: Boolean): List<String> {
    val codes = mutableListOf<String>()
    if (all) codes.add("##21#")
    if (busy) codes.add("##67#")
    if (unanswered) codes.add("##61#")
    if (unreachable) codes.add("##62#")
    return codes
}

private suspend fun sendMmiCodesSequentially(context: Context, codes: List<String>) {
    if (codes.isEmpty()) {
        Toast.makeText(context, "No codes to send.", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        codes.forEachIndexed { index, code ->
            val encoded = Uri.encode(code)
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = "tel:$encoded".toUri()
            context.startActivity(intent)
            delay(1200L)
        }
        Toast.makeText(context, "Forwarding request(s) sent. Confirm any prompts in the phone UI.", Toast.LENGTH_LONG).show()
    } catch (se: SecurityException) {
        Toast.makeText(context, "Call permission is required to send forwarding codes.", Toast.LENGTH_SHORT).show()
    } catch (t: Throwable) {
        Toast.makeText(context, "Failed to send forwarding codes: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun ForwardingOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text takes remaining space so checkbox aligns uniformly across rows
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (checked) MaterialTheme.colorScheme.onSurface else Color.Gray,
            modifier = Modifier
                .weight(1f)
        )

        Checkbox(
            checked = checked,
            onCheckedChange = null // handled by parent row clickable
        )
    }
}
