package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.VoiceTTSEngine
import com.example.model.RealtimeTTSMode
import com.example.model.VoiceGender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val voiceTTSEngine = remember { VoiceTTSEngine.getInstance(context) }

    val isDarkMode by preferences.isDarkMode.collectAsState()
    val voiceGender by preferences.voiceGender.collectAsState()
    val realtimeTTSMode by preferences.realtimeTTSMode.collectAsState()
    val voicePitch by preferences.voicePitch.collectAsState()
    val voiceSpeed by preferences.voiceSpeed.collectAsState()
    val voiceGuidanceEnabled by preferences.voiceGuidanceEnabled.collectAsState()
    val activeLanguage by preferences.activeLanguage.collectAsState()
    val realtimeAutoTranslate by preferences.realtimeAutoTranslate.collectAsState()
    val targetTranslateLang by preferences.targetTranslationLanguage.collectAsState()

    var testSpeechText by remember { mutableStateOf("नमस्ते! ग्लोबल कीबोर्ड डायनामिक में आपका स्वागत है।") }

    val bgColor = if (isDarkMode) Color(0xFF0F1016) else Color(0xFFF8FAFC)
    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val accentColor = Color(0xFF10B981)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Voice & Speech TTS Studio", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
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
            // 1. VOICE GENDER PERSONA (Namaste, Soft Gentle, Female, Male)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Voice Tone Persona",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Selected: ${voiceGender.displayName}",
                    fontSize = 12.sp,
                    color = textMuted
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VoicePersonaCard(
                        title = "Namaste Voice",
                        nativeTag = "नमस्ते",
                        emoji = "🙏",
                        subtitle = "Warm, gentle & polite natural Indian tone",
                        isRecommended = true,
                        isSelected = voiceGender == VoiceGender.NAMASTE,
                        isDarkMode = isDarkMode,
                        onClick = {
                            preferences.setVoiceGender(VoiceGender.NAMASTE)
                            voiceTTSEngine.setVoiceGender(VoiceGender.NAMASTE)
                            val sample = if (activeLanguage.code == "mr") "नमस्कार! ग्लोबल कीबोर्ड तयार आहे." else "नमस्ते! ग्लोबल कीबोर्ड डायनामिक तैयार है।"
                            voiceTTSEngine.speak(sample, activeLanguage.ttsLocaleTag)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    VoicePersonaCard(
                        title = "Soft Gentle",
                        nativeTag = "सौम्य",
                        emoji = "🍃",
                        subtitle = "Quiet, soothing & very easy on the ears",
                        isRecommended = false,
                        isSelected = voiceGender == VoiceGender.SOFT,
                        isDarkMode = isDarkMode,
                        onClick = {
                            preferences.setVoiceGender(VoiceGender.SOFT)
                            voiceTTSEngine.setVoiceGender(VoiceGender.SOFT)
                            voiceTTSEngine.speak("Soft gentle voice output selected.", activeLanguage.ttsLocaleTag)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VoicePersonaCard(
                        title = "Female Voice",
                        nativeTag = "महिला",
                        emoji = "👩",
                        subtitle = "Clear, crisp, melodic tone",
                        isRecommended = false,
                        isSelected = voiceGender == VoiceGender.FEMALE,
                        isDarkMode = isDarkMode,
                        onClick = {
                            preferences.setVoiceGender(VoiceGender.FEMALE)
                            voiceTTSEngine.setVoiceGender(VoiceGender.FEMALE)
                            voiceTTSEngine.speak("Female voice output selected.", activeLanguage.ttsLocaleTag)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    VoicePersonaCard(
                        title = "Male Voice",
                        nativeTag = "पुरुष",
                        emoji = "👨",
                        subtitle = "Deep, warm, resonant tone",
                        isRecommended = false,
                        isSelected = voiceGender == VoiceGender.MALE,
                        isDarkMode = isDarkMode,
                        onClick = {
                            preferences.setVoiceGender(VoiceGender.MALE)
                            voiceTTSEngine.setVoiceGender(VoiceGender.MALE)
                            voiceTTSEngine.speak("Male voice output selected.", activeLanguage.ttsLocaleTag)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. REAL-TIME TTS SPEECH MODE (Off, Word, Sentence, Manual)
            Text(
                "When should the keyboard speak?",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = accentColor,
                letterSpacing = 0.5.sp
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RealtimeTTSMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { preferences.setRealtimeTTSMode(mode) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mode.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textColor)
                                Text(mode.description, fontSize = 11.sp, color = textMuted)
                            }
                            RadioButton(
                                selected = realtimeTTSMode == mode,
                                onClick = { preferences.setRealtimeTTSMode(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                        }
                    }
                }
            }

            // 3. VOICE GUIDANCE & TUTORIAL AUDIO TOGGLE
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Spoken Guidance & Tutorials", fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                        Text("Speaks setup steps, permissions info, and tips aloud", fontSize = 11.sp, color = textMuted)
                    }
                    Switch(
                        checked = voiceGuidanceEnabled,
                        onCheckedChange = { preferences.setVoiceGuidanceEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF059669)
                        )
                    )
                }
            }

            // 4. REALTIME MULTILINGUAL TRANSLATION AUDIO
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Translate on Space", fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                        Text("Translates and pronounces words into ${targetTranslateLang.displayName}", fontSize = 11.sp, color = textMuted)
                    }
                    Switch(
                        checked = realtimeAutoTranslate,
                        onCheckedChange = { preferences.setRealtimeAutoTranslate(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF059669)
                        )
                    )
                }
            }

            // 5. PITCH & SPEED SLIDERS (MODULATION)
            Text(
                "Voice Pitch & Cadence",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = accentColor,
                letterSpacing = 0.5.sp
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Voice Pitch", fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
                            Text("${String.format("%.2f", voicePitch)}x", fontSize = 12.sp, color = accentColor)
                        }
                        Slider(
                            value = voicePitch,
                            onValueChange = {
                                preferences.setVoicePitch(it)
                                voiceTTSEngine.setPitch(it)
                            },
                            valueRange = 0.6f..1.4f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Speech Speed (Lower = Softer/Relaxed)", fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
                            Text("${String.format("%.2f", voiceSpeed)}x", fontSize = 12.sp, color = accentColor)
                        }
                        Slider(
                            value = voiceSpeed,
                            onValueChange = {
                                preferences.setVoiceSpeed(it)
                                voiceTTSEngine.setSpeed(it)
                            },
                            valueRange = 0.6f..1.3f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                    }

                    // Interactive Test Phrases
                    Text("Try Sample Speech:", fontSize = 11.sp, color = textMuted, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "नमस्ते! आपका स्वागत है।",
                            "नमस्कार! आपले स्वागत आहे.",
                            "Namaste! Global Keyboard is ready.",
                            "Thank you for using Global Keyboard."
                        ).forEach { sample ->
                            Surface(
                                onClick = {
                                    testSpeechText = sample
                                    voiceTTSEngine.speak(sample, activeLanguage.ttsLocaleTag)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDarkMode) Color(0xFF26293A) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Text(
                                    sample,
                                    fontSize = 11.sp,
                                    color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Test and Stop action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                voiceTTSEngine.speak(testSpeechText, activeLanguage.ttsLocaleTag)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Voice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { voiceTTSEngine.stop() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMuted),
                            border = BorderStroke(1.dp, borderColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoicePersonaCard(
    title: String,
    nativeTag: String,
    emoji: String,
    subtitle: String,
    isRecommended: Boolean,
    isSelected: Boolean,
    isDarkMode: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unselectedBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val selectedBg = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFECFDF5)
    val unselectedBorder = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val selectedBorder = Color(0xFF10B981)
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isSelected) {
        if (isDarkMode) Color(0xFF6EE7B7) else Color(0xFF047857)
    } else {
        if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedBg else unselectedBg
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) selectedBorder else unselectedBorder
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 24.sp)
                if (isRecommended) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF059669)
                    ) {
                        Text(
                            "Recommended",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                "$title ($nativeTag)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = titleColor
            )

            Text(
                subtitle,
                fontSize = 10.5.sp,
                color = subtitleColor,
                lineHeight = 13.sp
            )
        }
    }
}
