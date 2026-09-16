package com.example.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardRepository
import com.example.model.KeyboardTheme

@Composable
fun ClipboardPanel(
    theme: KeyboardTheme,
    clipboardRepository: ClipboardRepository,
    onPaste: (String) -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 268.dp
) {
    val items by clipboardRepository.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Pinned

    val filteredItems = remember(items, searchQuery, selectedFilter) {
        items.filter { item ->
            val matchesSearch = searchQuery.isBlank() || item.text.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                1 -> item.isPinned
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // 1. Top Header Row: Title, Filters & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    "Clipboard",
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            // Tabs / Filters: All / Pinned & Clear
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == 0) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceColor,
                    border = BorderStroke(0.75.dp, if (selectedFilter == 0) theme.primaryColor else theme.keyBorderColor),
                    modifier = Modifier.clickable { selectedFilter = 0 }
                ) {
                    Text(
                        "All (${items.size})",
                        color = if (selectedFilter == 0) theme.primaryColor else theme.textSecondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                val pinnedCount = items.count { it.isPinned }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == 1) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceColor,
                    border = BorderStroke(0.75.dp, if (selectedFilter == 1) theme.primaryColor else theme.keyBorderColor),
                    modifier = Modifier.clickable { selectedFilter = 1 }
                ) {
                    Text(
                        "📌 Pinned ($pinnedCount)",
                        color = if (selectedFilter == 1) theme.primaryColor else theme.textSecondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (items.any { !it.isPinned }) {
                    TextButton(
                        onClick = { clipboardRepository.clearAll(keepPinned = true) },
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Clear", color = Color(0xFFEF4444), fontSize = 11.sp)
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.textSecondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Search Field
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = theme.surfaceColor,
            border = BorderStroke(0.5.dp, theme.keyBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = theme.textSecondaryColor,
                    modifier = Modifier.size(14.dp)
                )
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = theme.textColor, fontSize = 12.sp),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Search clipboard...",
                                color = theme.textSecondaryColor.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(18.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = theme.textSecondaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Vertical List View (Vertical Cards)
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        if (searchQuery.isNotEmpty()) "No matching clips found" else "No copied clips yet",
                        color = theme.textSecondaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Text copied anywhere on your phone will appear here",
                        color = theme.textSecondaryColor.copy(alpha = 0.6f),
                        fontSize = 10.5.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            0.75.dp,
                            if (item.isPinned) theme.primaryColor.copy(alpha = 0.5f) else theme.keyBorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPaste(item.text) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.text,
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Tap to paste",
                                        color = theme.primaryColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (item.isPinned) {
                                        Text(
                                            "• 📌 Pinned",
                                            color = theme.accentColor,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { clipboardRepository.togglePin(item.id) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Pin",
                                        tint = if (item.isPinned) theme.primaryColor else theme.textSecondaryColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { clipboardRepository.deleteClip(item.id) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = theme.textSecondaryColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Privacy Guarantee Footer (Google Play & AdMob Policy Compliance)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🔒 Stored 100% on-device • Google Play & AdMob Policy Compliant",
                color = theme.textSecondaryColor.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}
