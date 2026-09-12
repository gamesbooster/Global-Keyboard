package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
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
import com.example.model.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit
) {
    val enabledLanguages by preferences.enabledLanguages.collectAsState()
    val activeLanguage by preferences.activeLanguage.collectAsState()
    val targetTranslateLang by preferences.targetTranslationLanguage.collectAsState()
    val autoDetectLanguage by preferences.autoDetectLanguage.collectAsState()
    val isDarkMode by preferences.isDarkMode.collectAsState()

    val screenBg = if (isDarkMode) Color(0xFF0F0F12) else Color(0xFFF8FAFC)
    val cardBg = if (isDarkMode) Color(0xFF1C1C24) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF2D2D38) else Color(0xFFE2E8F0)

    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Multilingual & Translation Studio", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(screenBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. AUTO-DETECT LANGUAGE TOGGLE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF6366F1).copy(alpha = 0.3f) else Color(0xFF6366F1).copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Auto-Detect Language", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                                Text("Instantly recognizes Hindi, Marathi, Bengali, Spanish, etc. while typing", fontSize = 11.sp, color = textMuted)
                            }
                        }
                        Switch(
                            checked = autoDetectLanguage,
                            onCheckedChange = { preferences.setAutoDetectLanguage(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6366F1)
                            )
                        )
                    }
                }
            }

            // 2. OUTPUT TRANSLATION LANGUAGE PICKER
            item {
                Text(
                    "Default Output & Voice Translation Language",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Language.ALL_LANGUAGES) { lang ->
                        val isTarget = targetTranslateLang.id == lang.id
                        val chipBg = if (isTarget) Color(0xFF10B981) else cardBg
                        val chipBorder = if (isTarget) Color(0xFF10B981) else borderColor

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = chipBg,
                            border = BorderStroke(1.dp, chipBorder),
                            modifier = Modifier.clickable {
                                preferences.setTargetTranslationLanguage(lang)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lang.flagEmoji, fontSize = 16.sp)
                                Column {
                                    Text(
                                        lang.displayName,
                                        color = if (isTarget) Color.White else textColor,
                                        fontSize = 13.sp,
                                        fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        lang.nativeName,
                                        color = if (isTarget) Color.White.copy(alpha = 0.8f) else textMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                if (isTarget) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 3. AVAILABLE INPUT KEYBOARD LANGUAGES
            item {
                Text(
                    "Enabled Input Keyboard Languages",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textMuted,
                    letterSpacing = 0.8.sp
                )
            }

            items(Language.ALL_LANGUAGES) { lang ->
                val isEnabled = enabledLanguages.any { it.id == lang.id }
                val isPrimary = activeLanguage.id == lang.id

                val cardContainer = if (isPrimary) {
                    if (isDarkMode) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
                } else cardBg

                val cardBorder = if (isPrimary) {
                    Color(0xFF6366F1).copy(alpha = 0.6f)
                } else borderColor

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardContainer),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isEnabled) {
                                val list = enabledLanguages.toMutableList()
                                list.add(lang)
                                preferences.setEnabledLanguages(list)
                            }
                            preferences.setActiveLanguage(lang)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(lang.flagEmoji, fontSize = 24.sp)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(lang.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                                    if (isPrimary) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF6366F1)
                                        ) {
                                            Text("Active Primary", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text("${lang.nativeName} • Script: ${lang.layoutType.name}", fontSize = 12.sp, color = textMuted)
                            }
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                val list = enabledLanguages.toMutableList()
                                if (checked) {
                                    if (!list.any { it.id == lang.id }) list.add(lang)
                                } else {
                                    if (list.size > 1) list.removeAll { it.id == lang.id }
                                }
                                preferences.setEnabledLanguages(list)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            )
                        )
                    }
                }
            }
        }
    }
}
