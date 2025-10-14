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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engfred.callguardian.domain.models.WhitelistedContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ContactListItem(
    contact: WhitelistedContact,
    isBlocked: Boolean,
    onActionClick: () -> Unit,
    moreCount: Int? = null,
    blockedCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var contactBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

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
                        contactBitmap = bitmap?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    // Log error in prod
                }
            }
        }
    }

    val backgroundColor = if (isBlocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isBlocked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    val icon = if (isBlocked) Icons.Default.Remove else Icons.Default.Block
    val contentDescription = if (isBlocked) "Remove from Blocked List" else "Block Contact"
    val iconTint = if (isBlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar: Photo or Initial
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (contactBitmap != null) {
                Image(
                    bitmap = contactBitmap!!,
                    contentDescription = "Contact photo",
                    modifier = Modifier.size(55.dp)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = contact.originalPhoneNumber,  // Use original for display
                    color = textColor.copy(alpha = if (isBlocked) 0.7f else 1f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                moreCount?.takeIf { it > 0 }?.let {
                    Text(
                        text = "+${it} more",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                blockedCount?.takeIf { it > 0 }?.let {
                    Text(
                        text = "(${it} blocked)",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
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