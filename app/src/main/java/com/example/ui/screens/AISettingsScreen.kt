package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit,
    onNavigateToPro: () -> Unit = {},
    onNavigateToSmartReply: () -> Unit = {}
) {
    val autoCorrection by preferences.autoCorrection.collectAsState()
    val showSuggestions by preferences.showSuggestions.collectAsState()
    val transliterationEnabled by preferences.transliterationEnabled.collectAsState()
    val emojiSuggestionsEnabled by preferences.emojiSuggestionsEnabled.collectAsState()
    val keyHeightDp by preferences.keyHeightDp.collectAsState()
    val keyPreviewEnabled by preferences.keyPreviewEnabled.collectAsState()
    val keyVibration by preferences.keyVibration.collectAsState()
    val showNumberRow by preferences.showNumberRow.collectAsState()

    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val aiUsageCount by preferences.aiUsageCount.collectAsState()

    val bgColor = if (isDarkMode) Color(0xFF0F1016) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Keyboard & AI Settings", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPro) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = "VIP PRO",
                            tint = if (isPremiumUser) Color(0xFFF59E0B) else Color(0xFF10B981)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // VIP AI Usage Status Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremiumUser) {
                        if (isDarkMode) Color(0xFF14241B) else Color(0xFFF0FDF4)
                    } else {
                        if (isDarkMode) Color(0xFF1C2230) else Color(0xFFEFF6FF)
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isPremiumUser) Color(0xFF10B981) else Color(0xFF3B82F6).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPro() }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = if (isPremiumUser) Color(0xFF10B981) else Color(0xFF3B82F6))
                            Text(
                                if (isPremiumUser) "Gemini AI Unlimited Access" else "Gemini AI Free Tier",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFF59E0B)
                        ) {
                            Text(
                                if (isPremiumUser) "VIP UNLIMITED" else "UPGRADE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!isPremiumUser) {
                        Text(
                            "Free tier includes 10 smart rewrites. Upgrade to VIP for unlimited generations.",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Usage: $aiUsageCount / 10 used", fontSize = 11.sp, color = textMuted)
                            Text("${(10 - aiUsageCount).coerceAtLeast(0)} left", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                        }
                        LinearProgressIndicator(
                            progress = { (aiUsageCount.toFloat() / 10f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF3B82F6),
                            trackColor = if (isDarkMode) Color(0xFF2C2C38) else Color(0xFFE2E8F0)
                        )
                    } else {
                        Text(
                            "You have unlimited Gemini AI rewrite credits across all tones and languages.",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFFA7F3D0) else Color(0xFF065F46)
                        )
                    }
                }
            }

            // Smart Reply Feature Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.6f) else Color(0xFFEEF2FF)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSmartReply() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✨ Smart Reply Settings", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = textColor)
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF6366F1).copy(alpha = 0.2f)) {
                                Text("NEW", color = Color(0xFF818CF8), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text(
                            "Configure default reply tones, output languages, and test multi-lingual responses.",
                            color = textMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                }
            }

            Text("Indian Typing & Transliteration", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Phonetic Transliteration (Hinglish)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                            Text("Auto-converts English letters to Hindi/Indic script (e.g. namaste → नमस्ते)", fontSize = 12.sp, color = textMuted)
                        }
                        Switch(
                            checked = transliterationEnabled,
                            onCheckedChange = { preferences.setTransliterationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            )
                        )
                    }

                    HorizontalDivider(color = borderColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Contextual Emoji Predictions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                            Text("Shows relevant emojis as you type (e.g. chai ☕, fire 🔥, party 🎉)", fontSize = 12.sp, color = textMuted)
                        }
                        Switch(
                            checked = emojiSuggestionsEnabled,
                            onCheckedChange = { preferences.setEmojiSuggestionsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            )
                        )
                    }
                }
            }

            Text("Smart Prediction & Correction", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Correction", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                            Text("Automatically fixes common typos on spacebar", fontSize = 12.sp, color = textMuted)
                        }
                        Switch(
                            checked = autoCorrection,
                            onCheckedChange = { preferences.setAutoCorrection(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            )
                        )
                    }

                    HorizontalDivider(color = borderColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Word Suggestions Strip", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                            Text("Shows top 3 word completions above keyboard", fontSize = 12.sp, color = textMuted)
                        }
                        Switch(
                            checked = showSuggestions,
                            onCheckedChange = { preferences.setShowSuggestions(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            )
                        )
                    }
                }
            }

            Text("Hardware & Feedback", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Key Height
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Keyboard Height Customization", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                            Text("${keyHeightDp} dp", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                42 to "Compact\n(42dp)",
                                46 to "Sleek\n(46dp)",
                                50 to "Regular\n(50dp)",
                                56 to "Tall\n(56dp)"
                            ).forEach { (h, label) ->
                                val isSelected = keyHeightDp == h
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { preferences.setKeyHeightDp(h) },
                                    label = { Text(label, fontSize = 10.sp, lineHeight = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = borderColor)

                    // Key Haptic Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Feedback on Tap", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                            Text("Tactile confirmation for zero-miss typing", fontSize = 12.sp, color = textMuted)
                        }
                        Switch(
                            checked = keyVibration,
                            onCheckedChange = { preferences.setKeyVibration(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            )
                        )
                    }

                    HorizontalDivider(color = borderColor)

                    // Dedicated Number Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dedicated Number Row", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                            Text("Shows 1234567890 row above letters", fontSize = 12.sp, color = textMuted)
                        }
                        Switch(
                            checked = showNumberRow,
                            onCheckedChange = { preferences.setShowNumberRow(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            )
                        )
                    }
                }
            }

            Text("AI Persona & Tone Rewriting", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)

            val personas = listOf(
                Triple("💼 Professional", "Polished corporate email & business phrasing", false),
                Triple("🤝 Friendly", "Warm, approachable, and engaging text", false),
                Triple("😎 Casual", "Relaxed phrasing for friends & group chats", false),
                Triple("🙏 Polite", "Courteous requests and grateful responses", false),
                Triple("✨ Poetic Shayari", "Lyrical Urdu/Hindi phrasing for special moments", true),
                Triple("🎬 Bollywood Dramatic", "Iconic Hindi cinema expressive punchlines", true),
                Triple("💎 Executive VIP", "High-stakes executive negotiation & clarity", true)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((title, desc, isPro) in personas) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textColor)
                                Text(desc, fontSize = 11.sp, color = textMuted)
                            }
                            if (isPro) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF59E0B)
                                ) {
                                    Text(
                                        "👑 VIP",
                                        color = Color.Black,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "FREE",
                                        color = Color(0xFF10B981),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
