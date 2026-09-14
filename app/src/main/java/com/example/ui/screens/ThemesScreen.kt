package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.LanguageDetector
import com.example.engine.TranslationMatrix
import com.example.engine.VoiceTTSEngine
import com.example.model.KeyboardTheme
import com.example.model.ThemeStyle
import com.example.model.Language
import com.example.model.RealtimeTTSMode

data class GradientPreset(val name: String, val colors: List<Color>)

val CUSTOM_GRADIENT_PRESETS = listOf(
    GradientPreset("Galaxy Nebula", listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))),
    GradientPreset("Sunset Flame", listOf(Color(0xFF2D112C), Color(0xFF5A1846), Color(0xFFC70039))),
    GradientPreset("Cyberpunk Neon", listOf(Color(0xFF0B0014), Color(0xFF1D002C), Color(0xFF080D21))),
    GradientPreset("Northern Aurora", listOf(Color(0xFF021B1A), Color(0xFF064E3B), Color(0xFF0F766E))),
    GradientPreset("Cherry Blossom", listOf(Color(0xFF2B0916), Color(0xFF501229), Color(0xFF881337))),
    GradientPreset("Deep Ocean", listOf(Color(0xFF031024), Color(0xFF082F49), Color(0xFF0284C7))),
    GradientPreset("Emerald Forest", listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF047857))),
    GradientPreset("Obsidian Pure", listOf(Color(0xFF0A0A0A), Color(0xFF171717), Color(0xFF262626)))
)

val ACCENT_COLOR_PRESETS = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFA855F7), // Purple
    Color(0xFFEF4444), // Red
    Color(0xFFFFFFFF)  // White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit,
    onNavigateToPro: () -> Unit = {}
) {
    val context = LocalContext.current
    val voiceTTSEngine = remember { VoiceTTSEngine.getInstance(context) }

    val currentTheme by preferences.currentTheme.collectAsState()
    val showNumberRow by preferences.showNumberRow.collectAsState()
    val keyVibration by preferences.keyVibration.collectAsState()
    val activeLanguage by preferences.activeLanguage.collectAsState()
    val targetTranslateLang by preferences.targetTranslationLanguage.collectAsState()
    val realtimeTTSMode by preferences.realtimeTTSMode.collectAsState()
    val realtimeAutoTranslate by preferences.realtimeAutoTranslate.collectAsState()

    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    var lockedThemeSelected by remember { mutableStateOf<KeyboardTheme?>(null) }

    val screenBg = if (isDarkMode) Color(0xFF0F0F12) else Color(0xFFF8FAFC)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    var showCustomThemeDialog by remember { mutableStateOf(false) }
    var testInputText by remember { mutableStateOf("नमस्ते LingoKey") }
    var lastComposedWord by remember { mutableStateOf("") }

    // Custom Theme Builder State
    var selectedGradientIndex by remember { mutableStateOf(0) }
    var selectedAccentColor by remember { mutableStateOf(ACCENT_COLOR_PRESETS[0]) }
    var customKeyAlpha by remember { mutableFloatStateOf(0.85f) }

    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Themes & Appearance", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPro) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = "VIP / PRO Upgrades",
                            tint = if (isPremiumUser) Color(0xFFF59E0B) else Color(0xFF10B981)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(screenBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CREATE CUSTOM THEME CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { showCustomThemeDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFF06B6D4))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Create Custom Theme", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Pick your background gradient, key transparency & accents", color = Color(0xFFC7D2FE), fontSize = 12.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF818CF8))
                }
            }

            // LIVE INTERACTIVE THEME PLAYGROUND
            Text("Live Theme Interactive Playground", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF94A3B8), letterSpacing = 1.sp)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Preview Display Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F0F16))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Live Text Output:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text(
                                    text = testInputText.ifBlank { "Type on keyboard below..." },
                                    color = if (testInputText.isNotBlank()) Color.White else Color(0xFF64748B),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (testInputText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        voiceTTSEngine.speak(testInputText, activeLanguage.ttsLocaleTag)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play Voice", tint = Color(0xFF6366F1))
                                }
                            }
                        }
                    }

                    // Simulated Embedded Keyboard Component in Active Theme
                    val previewBgMod = if (currentTheme.backgroundGradient != null) {
                        Modifier.background(Brush.verticalGradient(currentTheme.backgroundGradient!!))
                    } else {
                        Modifier.background(currentTheme.backgroundColor)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .then(previewBgMod)
                            .border(1.dp, currentTheme.keyBorderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(6.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.5.dp)) {
                            // Row 1 (10 Keys - QWERTY)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { char ->
                                    PreviewKey(
                                        theme = currentTheme,
                                        text = char,
                                        modifier = Modifier.weight(1f),
                                        height = 38.dp,
                                        onClick = {
                                            testInputText += char.lowercase()
                                            lastComposedWord += char.lowercase()
                                        }
                                    )
                                }
                            }

                            // Row 2 (Gboard Staggered with 0.5f Spacers)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Spacer(modifier = Modifier.weight(0.5f))
                                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").forEach { char ->
                                    PreviewKey(
                                        theme = currentTheme,
                                        text = char,
                                        modifier = Modifier.weight(1f),
                                        height = 38.dp,
                                        onClick = {
                                            testInputText += char.lowercase()
                                            lastComposedWord += char.lowercase()
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.weight(0.5f))
                            }

                            // Row 3 (Shift, 7 Keys, Backspace)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PreviewKey(
                                    theme = currentTheme,
                                    icon = Icons.Default.KeyboardArrowUp,
                                    modifier = Modifier.weight(1.5f),
                                    isSpecial = true,
                                    height = 38.dp,
                                    onClick = {}
                                )

                                listOf("Z", "X", "C", "V", "B", "N", "M").forEach { char ->
                                    PreviewKey(
                                        theme = currentTheme,
                                        text = char,
                                        modifier = Modifier.weight(1f),
                                        height = 38.dp,
                                        onClick = {
                                            testInputText += char.lowercase()
                                            lastComposedWord += char.lowercase()
                                        }
                                    )
                                }

                                PreviewKey(
                                    theme = currentTheme,
                                    icon = Icons.AutoMirrored.Filled.Backspace,
                                    modifier = Modifier.weight(1.5f),
                                    isSpecial = true,
                                    height = 38.dp,
                                    onClick = {
                                        if (testInputText.isNotEmpty()) {
                                            testInputText = testInputText.dropLast(1)
                                        }
                                        if (lastComposedWord.isNotEmpty()) {
                                            lastComposedWord = lastComposedWord.dropLast(1)
                                        }
                                    }
                                )
                            }

                            // Row 4 (Gboard Bottom Row: ?123, Lang, Space, Enter)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PreviewKey(
                                    theme = currentTheme,
                                    text = "?123",
                                    modifier = Modifier.weight(1.3f),
                                    isSpecial = true,
                                    height = 38.dp,
                                    onClick = {}
                                )

                                PreviewKey(
                                    theme = currentTheme,
                                    icon = Icons.Default.Language,
                                    modifier = Modifier.weight(1f),
                                    isSpecial = true,
                                    height = 38.dp,
                                    onClick = { preferences.switchNextLanguage() }
                                )

                                // Space Bar with Theme & Audio TTS
                                PreviewKey(
                                    theme = currentTheme,
                                    text = "${currentTheme.name} • Space",
                                    modifier = Modifier.weight(4.7f),
                                    height = 38.dp,
                                    onClick = {
                                        val wordToSpeak = lastComposedWord.trim()
                                        if (wordToSpeak.isNotBlank()) {
                                            voiceTTSEngine.speakWord(wordToSpeak, activeLanguage.ttsLocaleTag)
                                        }
                                        testInputText += " "
                                        lastComposedWord = ""
                                    }
                                )

                                // Send / Action Key
                                PreviewKey(
                                    theme = currentTheme,
                                    icon = Icons.AutoMirrored.Filled.Send,
                                    modifier = Modifier.weight(1.4f),
                                    isSpecial = true,
                                    isSelected = true,
                                    height = 38.dp,
                                    onClick = {
                                        if (testInputText.isNotBlank()) {
                                            voiceTTSEngine.speak(testInputText, activeLanguage.ttsLocaleTag)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // PRESET THEMES HEADER & CATEGORIES
            var selectedCategory by remember { mutableStateOf("All") }
            val categories = listOf("All", "✨ UI Trends", "🌌 Dark & Gradients", "🌸 Light & Pastels")

            val filteredThemes = remember(selectedCategory) {
                when (selectedCategory) {
                    "✨ UI Trends" -> KeyboardTheme.ALL_THEMES.filter { it.category == "UI Trends" }
                    "🌌 Dark & Gradients" -> KeyboardTheme.ALL_THEMES.filter { it.isDark && it.category != "UI Trends" }
                    "🌸 Light & Pastels" -> KeyboardTheme.ALL_THEMES.filter { !it.isDark && it.category != "UI Trends" }
                    else -> KeyboardTheme.ALL_THEMES
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Theme Gallery", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                Text("${filteredThemes.size} of ${KeyboardTheme.ALL_THEMES.size}", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isCatActive = selectedCategory == cat
                    val count = when (cat) {
                        "✨ UI Trends" -> KeyboardTheme.ALL_THEMES.count { it.category == "UI Trends" }
                        "🌌 Dark & Gradients" -> KeyboardTheme.ALL_THEMES.count { it.isDark && it.category != "UI Trends" }
                        "🌸 Light & Pastels" -> KeyboardTheme.ALL_THEMES.count { !it.isDark && it.category != "UI Trends" }
                        else -> KeyboardTheme.ALL_THEMES.size
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCatActive) Color(0xFF6366F1) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isCatActive) Color(0xFF818CF8) else Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = "$cat ($count)",
                            color = if (isCatActive) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isCatActive) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // PRESET THEMES GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredThemes) { theme ->
                    val isSelected = currentTheme.id == theme.id

                    val bgMod = if (theme.backgroundGradient != null) {
                        Modifier.background(Brush.verticalGradient(theme.backgroundGradient))
                    } else {
                        Modifier.background(theme.backgroundColor)
                    }

                    val badgeLabel = when (theme.themeStyle) {
                        ThemeStyle.GLASSMORPHISM -> "Glass"
                        ThemeStyle.NEUMORPHISM_LIGHT -> "Neumorphic Light"
                        ThemeStyle.NEUMORPHISM_DARK -> "Neumorphic Dark"
                        ThemeStyle.FLAT_DESIGN -> "Flat UI"
                        ThemeStyle.MINIMALISM -> "Minimal"
                        ThemeStyle.SKEUOMORPHISM -> "3D Retro"
                        ThemeStyle.MATERIAL_YOU -> "Material You"
                        else -> if (theme.backgroundGradient != null) "Gradient" else "Classic"
                    }

                    val isLocked = theme.isPro && !isPremiumUser

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                if (isLocked) {
                                    lockedThemeSelected = theme
                                } else {
                                    preferences.setTheme(theme)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(bgMod)
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                theme.name,
                                                color = theme.textColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                maxLines = 1
                                            )
                                            if (theme.isPro) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFF59E0B)
                                                ) {
                                                    Text(
                                                        text = "VIP",
                                                        color = Color.Black,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = theme.accentColor.copy(alpha = 0.22f)
                                        ) {
                                            Text(
                                                text = badgeLabel,
                                                color = theme.accentColor,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Mock Keyboard Keys Preview Styled by Theme
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                                ) {
                                    listOf("Q", "W", "E", "R", "T").forEach { letter ->
                                        val keyShape = RoundedCornerShape(theme.keyCornerRadius.dp)
                                        val keyBg = when (theme.themeStyle) {
                                            ThemeStyle.GLASSMORPHISM -> theme.keyColor.copy(alpha = 0.3f)
                                            ThemeStyle.FLAT_DESIGN -> theme.keyColor
                                            ThemeStyle.NEUMORPHISM_LIGHT, ThemeStyle.NEUMORPHISM_DARK -> theme.keyColor
                                            else -> theme.keyColor.copy(alpha = theme.keyAlpha)
                                        }
                                        val borderStroke = when (theme.themeStyle) {
                                            ThemeStyle.GLASSMORPHISM -> BorderStroke(0.7.dp, Color.White.copy(alpha = 0.45f))
                                            ThemeStyle.NEUMORPHISM_LIGHT -> BorderStroke(0.8.dp, Color.White.copy(alpha = 0.8f))
                                            ThemeStyle.NEUMORPHISM_DARK -> BorderStroke(0.6.dp, Color(0xFF333B44))
                                            ThemeStyle.SKEUOMORPHISM -> BorderStroke(0.8.dp, Color(0xFF101215))
                                            else -> BorderStroke(0.5.dp, theme.keyBorderColor)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(26.dp)
                                                .clip(keyShape)
                                                .background(keyBg)
                                                .border(borderStroke, keyShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(letter, color = theme.textColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                // Space Bar Preview
                                val spaceShape = RoundedCornerShape(theme.keyCornerRadius.dp)
                                val spaceBg = when (theme.themeStyle) {
                                    ThemeStyle.GLASSMORPHISM -> theme.keyColor.copy(alpha = 0.3f)
                                    ThemeStyle.FLAT_DESIGN -> theme.keyColor
                                    else -> theme.keyColor.copy(alpha = theme.keyAlpha)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                        .clip(spaceShape)
                                        .background(spaceBg)
                                        .border(0.5.dp, theme.keyBorderColor, spaceShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Space", color = theme.textSecondaryColor, fontSize = 8.sp)
                                }
                            }

                            // VIP Lock Overlay
                            if (isLocked) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.52f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Black, modifier = Modifier.size(18.dp))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF59E0B)
                                        ) {
                                            Text(
                                                "VIP LOCKED",
                                                color = Color.Black,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TYPING EXPERIENCE SETTINGS
            Text("Typing Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF94A3B8), letterSpacing = 1.sp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1C1C24))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Always Show Number Row", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                            Text("Shows dedicated numbers (0-9) above keys", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = showNumberRow,
                            onCheckedChange = { preferences.setShowNumberRow(it) }
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Key Press Haptic Vibration", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                            Text("Tactile haptic feedback when typing", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = keyVibration,
                            onCheckedChange = { preferences.setKeyVibration(it) }
                        )
                    }
                }
            }
        }
    }

    // CUSTOM THEME STUDIO DIALOG
    if (showCustomThemeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomThemeDialog = false },
            containerColor = Color(0xFF181822),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Custom Theme Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("1. Select Background Backdrop", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    // Gradient Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CUSTOM_GRADIENT_PRESETS.take(4).forEachIndexed { idx, preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.verticalGradient(preset.colors))
                                    .border(
                                        width = if (selectedGradientIndex == idx) 2.dp else 1.dp,
                                        color = if (selectedGradientIndex == idx) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedGradientIndex = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedGradientIndex == idx) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CUSTOM_GRADIENT_PRESETS.drop(4).take(4).forEachIndexed { idx, preset ->
                            val actualIdx = idx + 4
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.verticalGradient(preset.colors))
                                    .border(
                                        width = if (selectedGradientIndex == actualIdx) 2.dp else 1.dp,
                                        color = if (selectedGradientIndex == actualIdx) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedGradientIndex = actualIdx },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedGradientIndex == actualIdx) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Text("2. Key Accent Glow", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    // Accent Colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ACCENT_COLOR_PRESETS.take(4).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color)
                                    .border(
                                        width = if (selectedAccentColor == color) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedAccentColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedAccentColor == color) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (color == Color.White) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ACCENT_COLOR_PRESETS.drop(4).take(4).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color)
                                    .border(
                                        width = if (selectedAccentColor == color) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedAccentColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedAccentColor == color) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (color == Color.White) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Text("3. Key Surface Transparency (${(customKeyAlpha * 100).toInt()}%)", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = customKeyAlpha,
                        onValueChange = { customKeyAlpha = it },
                        valueRange = 0.3f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = selectedAccentColor, activeTrackColor = selectedAccentColor)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val preset = CUSTOM_GRADIENT_PRESETS[selectedGradientIndex]
                        preferences.setCustomTheme(
                            name = preset.name,
                            bgGradient = preset.colors,
                            keyColor = Color.White.copy(alpha = customKeyAlpha * 0.18f),
                            textColor = Color.White,
                            accentColor = selectedAccentColor,
                            keyAlpha = customKeyAlpha
                        )
                        showCustomThemeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Apply Theme", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomThemeDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Locked VIP Theme Alert Dialog
    if (lockedThemeSelected != null) {
        AlertDialog(
            onDismissRequest = { lockedThemeSelected = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    "Unlock ${lockedThemeSelected?.name}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "This luxury theme is exclusive to Global Keyboard PRO members. Upgrade now to unlock all 15+ VIP themes, unlimited AI rewrites, and HD neural voices.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        lockedThemeSelected = null
                        onNavigateToPro()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Unlock with PRO", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { lockedThemeSelected = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PreviewKey(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSpecial: Boolean = false,
    isSelected: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 38.dp,
    onClick: () -> Unit
) {
    val cornerRadius = theme.keyCornerRadius.dp
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier.height(height)

    when (theme.themeStyle) {
        ThemeStyle.GLASSMORPHISM -> {
            val glassBg = if (isSelected) theme.primaryColor.copy(alpha = 0.65f)
            else if (isSpecial) theme.keySpecialColor.copy(alpha = 0.35f)
            else theme.keyColor.copy(alpha = 0.25f)

            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(glassBg)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))))
                    .border(0.85.dp, Color.White.copy(alpha = 0.45f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.NEUMORPHISM_LIGHT -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .border(1.dp, Color.White.copy(alpha = 0.85f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.NEUMORPHISM_DARK -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .border(0.75.dp, Color(0xFF333B44), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.FLAT_DESIGN -> {
            val keyBg = if (isSelected) theme.accentColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.MINIMALISM -> {
            val keyBg = if (isSelected) theme.accentColor.copy(alpha = 0.35f) else if (isSpecial) theme.keySpecialColor.copy(alpha = theme.keyAlpha) else theme.keyColor.copy(alpha = theme.keyAlpha)
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .border(0.5.dp, theme.keyBorderColor, shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.SKEUOMORPHISM -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)))
                    .border(1.dp, Color(0xFF101215), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.SEMI_TRANSPARENT -> {
            val semiBg = if (isSelected) theme.primaryColor.copy(alpha = 0.55f)
            else if (isSpecial) theme.keySpecialColor.copy(alpha = 0.35f)
            else theme.keyColor.copy(alpha = 0.35f)
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(semiBg)
                    .border(0.75.dp, theme.keyBorderColor.copy(alpha = 0.6f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.TACTILE_3D -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.18f))))
                    .border(1.2.dp, theme.keyBorderColor, shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.CLAYMORPHISM -> {
            val clayBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(clayBg)
                    .border(1.dp, Color.White.copy(alpha = 0.45f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.FLAT_2_0 -> {
            val flatBg = if (isSelected) theme.accentColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(flatBg)
                    .border(0.8.dp, theme.keyBorderColor.copy(alpha = 0.5f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.NEOBRUTALISM -> {
            val bruteBg = if (isSelected) theme.accentColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(bruteBg)
                    .border(1.6.dp, Color.Black, shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        ThemeStyle.MATERIAL_YOU, ThemeStyle.STANDARD -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor.copy(alpha = theme.keyAlpha) else theme.keyColor.copy(alpha = theme.keyAlpha)
            val finalShape = if (isSelected && isSpecial) RoundedCornerShape(16.dp) else shape
            Box(
                modifier = baseModifier
                    .clip(finalShape)
                    .background(keyBg)
                    .border(0.5.dp, theme.keyBorderColor, finalShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    val tint = if (isSelected) (if (!theme.isDark || theme.themeStyle == ThemeStyle.MATERIAL_YOU) Color(0xFF042A5C) else Color.White) else theme.textColor
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                } else if (text != null) {
                    val textColor = if (isSelected) (if (!theme.isDark || theme.themeStyle == ThemeStyle.MATERIAL_YOU) Color(0xFF042A5C) else Color.White) else theme.textColor
                    Text(text, color = textColor, fontSize = 13.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
