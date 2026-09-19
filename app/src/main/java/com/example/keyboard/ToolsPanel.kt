package com.example.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardTheme

data class ToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isFeatured: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun ToolsPanel(
    theme: KeyboardTheme,
    onOpenSmartReply: () -> Unit,
    onOpenTranslate: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenClipboard: () -> Unit,
    onOpenAI: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenLanguages: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCustomizeToolbar: () -> Unit = {},
    onOpenNumericPad: () -> Unit = {},
    onClose: () -> Unit,
    panelHeight: Dp = 268.dp
) {
    val toolItems = listOf(
        ToolItem(
            id = "numpad",
            title = "🔢 Number Pad",
            subtitle = "Dedicated 3x4 PIN & calculator keypad",
            icon = Icons.Default.Dialpad,
            accentColor = Color(0xFF0D9488),
            onClick = onOpenNumericPad
        ),
        ToolItem(
            id = "smart_reply",
            title = "✨ Smart Reply",
            subtitle = "Paste a received message and get reply suggestions.",
            icon = Icons.Default.AutoAwesome,
            accentColor = Color(0xFF6366F1),
            isFeatured = true,
            onClick = onOpenSmartReply
        ),
        ToolItem(
            id = "customize_toolbar",
            title = "✏️ Reorder Toolbar",
            subtitle = "Reorder & customize top toolbar tools",
            icon = Icons.Default.Tune,
            accentColor = Color(0xFFEC4899),
            onClick = onOpenCustomizeToolbar
        ),
        ToolItem(
            id = "translate",
            title = "🌐 Translate",
            subtitle = "Real-time multilingual translation",
            icon = Icons.Default.Translate,
            accentColor = Color(0xFF3B82F6),
            onClick = onOpenTranslate
        ),
        ToolItem(
            id = "voice",
            title = "🎙 Voice",
            subtitle = "Continuous speech recognition",
            icon = Icons.Default.Mic,
            accentColor = Color(0xFF10B981),
            onClick = onOpenVoice
        ),
        ToolItem(
            id = "clipboard",
            title = "📋 Clipboard",
            subtitle = "Saved clips & multi-paste manager",
            icon = Icons.Default.ContentPaste,
            accentColor = Color(0xFFF59E0B),
            onClick = onOpenClipboard
        ),
        ToolItem(
            id = "writing_assistant",
            title = "✍ Writing Assistant",
            subtitle = "Rewrite, tones & grammar correction",
            icon = Icons.Default.EditNote,
            accentColor = Color(0xFFEC4899),
            onClick = onOpenAI
        ),
        ToolItem(
            id = "themes",
            title = "🎨 Themes & Styles",
            subtitle = "3D, Glass, Neumorphic, Translucent & 10 Master Themes",
            icon = Icons.Default.Palette,
            accentColor = Color(0xFF8B5CF6),
            onClick = onOpenThemes
        ),
        ToolItem(
            id = "languages",
            title = "🌍 Languages",
            subtitle = "Indic, English & global languages",
            icon = Icons.Default.Language,
            accentColor = Color(0xFF06B6D4),
            onClick = onOpenLanguages
        ),
        ToolItem(
            id = "settings",
            title = "⚙️ Settings",
            subtitle = "Preferences, height & vibration",
            icon = Icons.Default.Settings,
            accentColor = Color(0xFF64748B),
            onClick = onOpenSettings
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Header
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
                    Icons.Default.Widgets,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "KEYBOARD TOOLS",
                    color = theme.textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close Tools",
                    tint = theme.textSecondaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Featured: Smart Reply Hero Card
        val smartReplyItem = toolItems.first()
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = theme.keyColor,
            border = BorderStroke(1.dp, smartReplyItem.accentColor.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { smartReplyItem.onClick() }
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                smartReplyItem.accentColor.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(smartReplyItem.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        smartReplyItem.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = smartReplyItem.title,
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = smartReplyItem.accentColor.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "NEW",
                                color = smartReplyItem.accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = smartReplyItem.subtitle,
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = smartReplyItem.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid of other tools
        val otherTools = toolItems.drop(1)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(otherTools) { item ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = theme.keyColor,
                    border = BorderStroke(0.5.dp, theme.keyBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(item.accentColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = item.accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
