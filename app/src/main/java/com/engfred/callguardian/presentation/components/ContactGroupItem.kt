package com.engfred.callguardian.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engfred.callguardian.domain.models.ContactGroup
import com.engfred.callguardian.domain.models.WhitelistedContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactGroupItem(
    group: ContactGroup,
    onGroupToggle: (ContactGroup) -> Unit,
    onBlockClick: (WhitelistedContact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp)
    ) {
        // Primary Item (clickable for expansion)
        ContactListItem(
            contact = group.primaryContact,
            isBlocked = false,
            moreCount = if (!group.isExpanded && group.otherContacts.isNotEmpty()) group.otherContacts.size else null,
            blockedCount = group.blockedCount,
            onActionClick = { onBlockClick(group.primaryContact) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = group.otherContacts.isNotEmpty()) {
                    onGroupToggle(group)
                }
        )

        // Expanded Sub-List (simple numbers only, no full items)
        AnimatedVisibility(
            visible = group.isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(12.dp)
            ) {
                group.otherContacts.forEach { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = contact.contactName ?: "",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = contact.originalPhoneNumber,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onBlockClick(contact) }) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = "Block this number",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}