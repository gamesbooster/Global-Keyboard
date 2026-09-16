package com.example.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardTheme

data class ToolDefinition(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val description: String,
    val badgeColor: Color = Color(0xFF6366F1)
)

object ToolbarToolRegistry {
    val DEFAULT_TOOLBAR = listOf(
        "translate",
        "smart_reply",
        "ai",
        "tools",
        "voice",
        "stickers",
        "settings"
    )

    val ALL_TOOLS = listOf(
        ToolDefinition("translate", "Translate", Icons.Default.Translate, "Real-time multilingual translation", Color(0xFF3B82F6)),
        ToolDefinition("smart_reply", "Smart Reply", Icons.Default.AutoAwesome, "Instant AI reply suggestions", Color(0xFF6366F1)),
        ToolDefinition("ai", "Gemini AI", Icons.Default.AutoAwesome, "AI writing assistant & tone changer", Color(0xFFEC4899)),
        ToolDefinition("tools", "Tools", Icons.Default.Widgets, "All utilities & keyboard drawers", Color(0xFFF59E0B)),
        ToolDefinition("voice", "Voice Mic", Icons.Default.Mic, "Speech-to-text typing", Color(0xFF10B981)),
        ToolDefinition("stickers", "Templates", Icons.Default.Celebration, "Viral memes, festival & card templates", Color(0xFF8B5CF6)),
        ToolDefinition("settings", "Settings", Icons.Default.Settings, "Vibration, themes & preferences", Color(0xFF64748B)),
        ToolDefinition("lang_selector", "Language Pill", Icons.Default.Language, "Auto / EN ⇄ HI quick selector", Color(0xFF0EA5E9)),
        ToolDefinition("tts_speech", "Live Voice TTS", Icons.AutoMirrored.Filled.VolumeUp, "Real-time spoken feedback toggle", Color(0xFF10B981)),
        ToolDefinition("clipboard", "Clipboard", Icons.Default.ContentPaste, "Multi-paste & copied snippets", Color(0xFF06B6D4)),
        ToolDefinition("emoji", "Emoji", Icons.Default.SentimentSatisfied, "Emoji keyboard picker", Color(0xFFEAB308)),
        ToolDefinition("themes", "Themes", Icons.Default.Palette, "Keyboards styles & customizer", Color(0xFF8B5CF6)),
        ToolDefinition("languages", "Languages", Icons.Default.Language, "Quick switch input languages", Color(0xFF14B8A6))
    )
}

@Composable
fun ToolbarCustomizePanel(
    theme: KeyboardTheme,
    currentOrder: List<String>,
    onSaveOrder: (List<String>) -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 268.dp
) {
    var activeTools by remember(currentOrder) {
        val initial = if (currentOrder.isEmpty()) ToolbarToolRegistry.DEFAULT_TOOLBAR else currentOrder
        mutableStateOf(initial.filter { id -> ToolbarToolRegistry.ALL_TOOLS.any { it.id == id } })
    }

    val availableTools = remember(activeTools) {
        ToolbarToolRegistry.ALL_TOOLS.filter { tool -> !activeTools.contains(tool.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. HEADER
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
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Customize Toolbar",
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.keyColor,
                    border = BorderStroke(0.75.dp, theme.keyBorderColor),
                    modifier = Modifier.clickable {
                        activeTools = ToolbarToolRegistry.DEFAULT_TOOLBAR
                        onSaveOrder(ToolbarToolRegistry.DEFAULT_TOOLBAR)
                    }
                ) {
                    Text(
                        "Reset",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.primaryColor,
                    modifier = Modifier.clickable {
                        onSaveOrder(activeTools)
                        onClose()
                    }
                ) {
                    Text(
                        "✓ Done",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. ACTIVE TOOLBAR SECTION (What appears on keyboard)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ACTIVE TOOLBAR (${activeTools.size} items)",
                    color = theme.accentColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tap ‹ › to reorder, ✕ to remove",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp
                )
            }

            // Horizontal visual dock
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.surfaceColor,
                border = BorderStroke(0.75.dp, theme.accentColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activeTools.forEachIndexed { index, toolId ->
                        val def = ToolbarToolRegistry.ALL_TOOLS.firstOrNull { it.id == toolId }
                        if (def != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.keySpecialColor,
                                border = BorderStroke(0.5.dp, theme.keyBorderColor),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Move Left button
                                    if (index > 0) {
                                        IconButton(
                                            onClick = {
                                                val list = activeTools.toMutableList()
                                                val tmp = list[index]
                                                list[index] = list[index - 1]
                                                list[index - 1] = tmp
                                                activeTools = list
                                                onSaveOrder(list)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Move Left",
                                                tint = theme.accentColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    Icon(
                                        def.icon,
                                        contentDescription = def.label,
                                        tint = def.badgeColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = def.label,
                                        color = theme.textColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    // Move Right button
                                    if (index < activeTools.size - 1) {
                                        IconButton(
                                            onClick = {
                                                val list = activeTools.toMutableList()
                                                val tmp = list[index]
                                                list[index] = list[index + 1]
                                                list[index + 1] = tmp
                                                activeTools = list
                                                onSaveOrder(list)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Move Right",
                                                tint = theme.accentColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    // Remove button (✕)
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red.copy(alpha = 0.15f))
                                            .clickable {
                                                val list = activeTools.filter { it != toolId }
                                                activeTools = list
                                                onSaveOrder(list)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.Red,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. MORE TOOLS SECTION (Tap + to add)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "MORE AVAILABLE TOOLS (Tap to add)",
                color = theme.textSecondaryColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            if (availableTools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "All available tools are active in your toolbar!",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(availableTools) { tool ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.surfaceColor,
                            border = BorderStroke(0.5.dp, theme.keyBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val list = activeTools + tool.id
                                    activeTools = list
                                    onSaveOrder(list)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        tool.icon,
                                        contentDescription = null,
                                        tint = tool.badgeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            tool.label,
                                            color = theme.textColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            tool.description,
                                            color = theme.textSecondaryColor,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = theme.primaryColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = theme.primaryColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
