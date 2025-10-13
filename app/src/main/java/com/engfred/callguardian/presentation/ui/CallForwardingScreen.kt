package com.engfred.callguardian.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.engfred.callguardian.presentation.viewmodel.CallForwardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallForwardingScreen(viewModel: CallForwardingViewModel = hiltViewModel()) {
    val forwardingNumber by viewModel.forwardingNumber.collectAsState()
    val forwardWhenBusy by viewModel.forwardWhenBusy.collectAsState()
    val forwardWhenUnanswered by viewModel.forwardWhenUnanswered.collectAsState()
    val forwardWhenUnreachable by viewModel.forwardWhenUnreachable.collectAsState()

    val context = LocalContext.current

    // Launcher for CALL_PHONE permission
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, now attempt to set forwarding
            setCallForwarding(
                context,
                forwardingNumber,
                forwardWhenBusy,
                forwardWhenUnanswered,
                forwardWhenUnreachable
            )
        } else {
            Toast.makeText(context, "Permission to make calls is required.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Forwarding", fontWeight = FontWeight.Bold) }
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
                text = "Set Forwarding Rules",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = forwardingNumber,
                onValueChange = { viewModel.updateForwardingNumber(it) },
                label = { Text("Forward calls to...") },
                placeholder = { Text("e.g., 555-123-4567") },
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = "Phone icon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Forward when...",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ForwardingOption(
                label = "Busy",
                icon = Icons.Default.Close,
                checked = forwardWhenBusy,
                onClick = { viewModel.toggleForwardWhenBusy() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ForwardingOption(
                label = "Unanswered",
                icon = Icons.Default.Call,
                checked = forwardWhenUnanswered,
                onClick = { viewModel.toggleForwardWhenUnanswered() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ForwardingOption(
                label = "Unreachable",
                icon = Icons.Default.WifiOff,
                checked = forwardWhenUnreachable,
                onClick = { viewModel.toggleForwardWhenUnreachable() }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (forwardingNumber.isBlank() || !(forwardWhenBusy || forwardWhenUnanswered || forwardWhenUnreachable)) {
                        Toast.makeText(context, "Please enter a number and select at least one condition.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Check for permission before attempting to make the call
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            setCallForwarding(
                                context,
                                forwardingNumber,
                                forwardWhenBusy,
                                forwardWhenUnanswered,
                                forwardWhenUnreachable
                            )
                        } else {
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Set Forwarding Rules")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    // This MMI code is a universal way to deactivate all conditional forwarding
                    // The code is now properly encoded to handle the '#' character in the URI
                    val encodedCode = Uri.encode("##002#")
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse("tel:$encodedCode")
                    try {
                        context.startActivity(intent)
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "Call permission is required to clear rules.", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.resetState()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Clear All Rules")
            }
        }
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                color = if (checked) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = null, // Handled by the parent row's clickable modifier
        )
    }
}

/**
 * Helper function to construct and launch the call forwarding intent.
 * This function will prompt the dialer app with the correct MMI code.
 */
private fun setCallForwarding(
    context: Context,
    number: String,
    busy: Boolean,
    unanswered: Boolean,
    unreachable: Boolean
) {
    val codes = mutableListOf<String>()
    if (busy) codes.add("*21*$number#")
    if (unanswered) codes.add("*61*$number#")
    if (unreachable) codes.add("*62*$number#")

    if (codes.isEmpty()) {
        Toast.makeText(context, "Please select at least one condition.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        codes.forEach { code ->
            val intent = Intent(Intent.ACTION_CALL)
            // Use `Uri.encode` to properly handle the `#` character
            val encodedCode = Uri.encode(code)
            intent.data = Uri.parse("tel:$encodedCode")
            context.startActivity(intent)
        }
        Toast.makeText(context, "Forwarding rules set. Please confirm in dialer.", Toast.LENGTH_LONG).show()
    } catch (e: SecurityException) {
        // This catch block is crucial for handling cases where permission is not granted
        Toast.makeText(context, "Call permission is required to set rules.", Toast.LENGTH_SHORT).show()
    }
}