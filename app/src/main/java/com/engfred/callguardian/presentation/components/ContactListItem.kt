package com.engfred.callguardian.presentation.components

import android.graphics.BitmapFactory
import android.provider.ContactsContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.engfred.callguardian.domain.models.WhitelistedContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ContactListItem(
    contact: WhitelistedContact,
    isBlocked: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var contactBitmap by remember { mutableStateOf<ImageVector?>(null) }  // Use ImageVector for fallback, Bitmap for photo

    // Async load photo if contactId present
    LaunchedEffect(contact.contactId) {
        if (contact.contactId != null) {
            withContext(Dispatchers.IO) {
                try {
                    val contactUri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
                        .appendPath(contact.contactId)
                        .build()
                    val inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                        context.contentResolver,
                        contactUri
                    )
                    inputStream?.use {
                        val bitmap = BitmapFactory.decodeStream(it)
                        // Note: In a full app, convert and store as ImageBitmap state
                        // For simplicity, we'll set a placeholder; extend with actual bitmap handling
                        // contactBitmap = bitmap?.asImageBitmap()  // Uncomment and adjust type if using ImageBitmap
                    }
                } catch (e: Exception) {
                    // Log error in prod
                }
            }
        }
    }

    val cardColor = if (isBlocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isBlocked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    val icon = if (isBlocked) Icons.Default.Check else Icons.Default.Delete
    val contentDescription = if (isBlocked) "Unblock Contact" else "Block Contact"
    val iconTint = if (isBlocked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar: Photo or Initial
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (contactBitmap != null) {
                    // If photo loaded (extend for Image(painter = rememberBitmapPainter(contactBitmap)))
                    Icon(
                        imageVector = contactBitmap!!,
                        contentDescription = "Contact photo",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified
                    )
                } else {
                    val firstInitial = contact.contactName?.firstOrNull()?.uppercase()?.take(1) ?: "?"
                    Text(
                        text = firstInitial,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Fallback to Person icon if no name/initial
            if (contact.contactName == null) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Unknown contact",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.contactName ?: "No Name",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = contact.originalPhoneNumber,  // Use original for display
                    color = textColor.copy(alpha = if (isBlocked) 0.7f else 1f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconTint
                )
            }
        }
    }
}