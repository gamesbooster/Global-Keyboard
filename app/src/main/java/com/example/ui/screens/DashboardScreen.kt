package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.ClipboardRepository
import com.example.data.LingoKeyPreferences
import com.example.engine.*
import com.example.model.KeyboardTheme
import com.example.model.Language
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    preferences: LingoKeyPreferences,
    onNavigateToLanguages: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToAISettings: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToPro: () -> Unit,
    onRestartOnboarding: () -> Unit,
    onNavigateToSmartReply: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activeLanguage by preferences.activeLanguage.collectAsState()
    val typingStyle by preferences.typingStyle.collectAsState()
    val currentTheme by preferences.currentTheme.collectAsState()
    val showKeyBorders by preferences.showKeyBorders.collectAsState()
    val showNumberRow by preferences.showNumberRow.collectAsState()
    val keyVibration by preferences.keyVibration.collectAsState()
    val keySound by preferences.keySound.collectAsState()
    val keyHeightDp by preferences.keyHeightDp.collectAsState()
    val transliterationEnabled by preferences.transliterationEnabled.collectAsState()
    val emojiSuggestionsEnabled by preferences.emojiSuggestionsEnabled.collectAsState()
    val autoCorrection by preferences.autoCorrection.collectAsState()
    val autoCapitalization by preferences.autoCapitalization.collectAsState()

    // Mode and Entitlements
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()

    // Dynamic Color Palette for Light (White) Mode & Dark Mode
    val bgColor = if (isDarkMode) Color(0xFF0F1016) else Color(0xFFF8FAFC)
    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val cardSubtleBg = if (isDarkMode) Color(0xFF222433) else Color(0xFFF1F5F9)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val navBarBg = if (isDarkMode) Color(0xFF161722) else Color(0xFFFFFFFF)

    // Dynamic IME check
    var isImeEnabled by remember { mutableStateOf(ImeUtils.isKeyboardEnabled(context)) }
    var isImeSelected by remember { mutableStateOf(ImeUtils.isKeyboardSelected(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isImeEnabled = ImeUtils.isKeyboardEnabled(context)
                isImeSelected = ImeUtils.isKeyboardSelected(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Logo & App Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF059669), Color(0xFF10B981))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "म",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column {
                                Text(
                                    "Global Keyboard Dynamic",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "${activeLanguage.displayName} • Active",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981)
                                    )
                                    if (isPremiumUser) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "VIP",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Right Corner Icons (Light/Dark Mode toggle, VIP Crown, Help)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // 1. Normal White / Dark Mode Toggle Icon
                            IconButton(onClick = { preferences.toggleDarkMode() }) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                    tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF059669),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // 2. VIP / In-App Purchase Icon
                            IconButton(onClick = onNavigateToPro) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "VIP / PRO Upgrades",
                                    tint = if (isPremiumUser) Color(0xFFF59E0B) else Color(0xFF10B981),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // 3. Help / Tutorial Icon
                            IconButton(onClick = { showHelpDialog = true }) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "Help & Tutorial",
                                    tint = textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = navBarBg,
                contentColor = textColor
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Keyboard, contentDescription = "Layouts") },
                    label = { Text("Layouts", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF10B981),
                        selectedTextColor = Color(0xFF10B981),
                        indicatorColor = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFD1FAE5),
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Themes") },
                    label = { Text("Themes", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF10B981),
                        selectedTextColor = Color(0xFF10B981),
                        indicatorColor = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFD1FAE5),
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF10B981),
                        selectedTextColor = Color(0xFF10B981),
                        indicatorColor = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFD1FAE5),
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
        ) {
            when (selectedTab) {
                0 -> LayoutsTab(
                    context = context,
                    preferences = preferences,
                    activeLanguage = activeLanguage,
                    isImeEnabled = isImeEnabled,
                    isImeSelected = isImeSelected,
                    currentTypingStyle = typingStyle,
                    isDarkMode = isDarkMode,
                    isPremiumUser = isPremiumUser,
                    onSelectTypingStyle = { preferences.setTypingStyle(it) },
                    onNavigateToLanguages = onNavigateToLanguages,
                    onNavigateToVoiceSettings = onNavigateToVoiceSettings,
                    onNavigateToAISettings = onNavigateToAISettings,
                    onNavigateToSmartReply = onNavigateToSmartReply,
                    onNavigateToPro = onNavigateToPro,
                    onRestartOnboarding = onRestartOnboarding
                )
                1 -> ThemesTab(
                    preferences = preferences,
                    currentTheme = currentTheme,
                    showKeyBorders = showKeyBorders,
                    isDarkMode = isDarkMode,
                    isPremiumUser = isPremiumUser,
                    onToggleKeyBorders = { preferences.setShowKeyBorders(it) },
                    onNavigateToFullThemes = onNavigateToThemes,
                    onNavigateToPro = onNavigateToPro
                )
                2 -> SettingsTab(
                    preferences = preferences,
                    showNumberRow = showNumberRow,
                    keyVibration = keyVibration,
                    keySound = keySound,
                    keyHeightDp = keyHeightDp,
                    transliterationEnabled = transliterationEnabled,
                    emojiSuggestionsEnabled = emojiSuggestionsEnabled,
                    autoCorrection = autoCorrection,
                    autoCapitalization = autoCapitalization,
                    activeLanguage = activeLanguage,
                    isDarkMode = isDarkMode,
                    isPremiumUser = isPremiumUser,
                    onNavigateToLanguages = onNavigateToLanguages,
                    onNavigateToThemes = onNavigateToThemes,
                    onNavigateToVoiceSettings = onNavigateToVoiceSettings,
                    onNavigateToAISettings = onNavigateToAISettings,
                    onNavigateToSmartReply = onNavigateToSmartReply,
                    onNavigateToPrivacy = onNavigateToPrivacy,
                    onNavigateToPro = onNavigateToPro,
                    onRestartOnboarding = onRestartOnboarding
                )
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("How to Use Global Keyboard", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("• Phonetic Transliteration: Type in English (e.g. 'amhi') to get Indic script ('आम्ही').", color = textMuted, fontSize = 13.sp)
                    Text("• Spacebar Cursor: Slide horizontally on the spacebar to move the cursor smoothly.", color = textMuted, fontSize = 13.sp)
                    Text("• Voice Typing: Tap the microphone key to dictate in real-time.", color = textMuted, fontSize = 13.sp)
                    Text("• In-App VIP: Unlock 15+ VIP luxury themes and unlimited AI transformations.", color = textMuted, fontSize = 13.sp)
                    Text("• Mode Toggle: Tap the Sun/Moon icon in the top right corner anytime.", color = textMuted, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got It", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showHelpDialog = false
                    onRestartOnboarding()
                }) {
                    Text("Re-run Setup", color = textMuted)
                }
            },
            containerColor = cardBg
        )
    }
}

@Composable
fun LayoutsTab(
    context: Context,
    preferences: LingoKeyPreferences,
    activeLanguage: Language,
    isImeEnabled: Boolean,
    isImeSelected: Boolean,
    currentTypingStyle: String,
    isDarkMode: Boolean,
    isPremiumUser: Boolean,
    onSelectTypingStyle: (String) -> Unit,
    onNavigateToLanguages: () -> Unit,
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToAISettings: () -> Unit,
    onNavigateToSmartReply: () -> Unit,
    onNavigateToPro: () -> Unit,
    onRestartOnboarding: () -> Unit
) {
    var testInput by remember { mutableStateOf("") }
    var transliteratedPreview by remember { mutableStateOf("") }

    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val cardSubtleBg = if (isDarkMode) Color(0xFF222433) else Color(0xFFF1F5F9)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)

    LaunchedEffect(testInput, currentTypingStyle) {
        if (currentTypingStyle == "transliteration" && testInput.isNotBlank()) {
            val words = testInput.split(" ")
            val converted = words.map { word ->
                if (word.isBlank()) "" else TransliteratorEngine.transliterate(word, activeLanguage.id)
            }
            transliteratedPreview = converted.joinToString(" ")
        } else {
            transliteratedPreview = testInput
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. IME Status Banner
        if (!isImeEnabled || !isImeSelected) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF451A03) else Color(0xFFFFFBEB)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B))
                        Column {
                            Text("Keyboard Not Active", fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF92400E), fontSize = 13.sp)
                            Text(
                                if (!isImeEnabled) "Enable in Android settings" else "Select Global Keyboard as active",
                                color = if (isDarkMode) Color(0xFFFDE68A) else Color(0xFFB45309),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (!isImeEnabled) {
                                ImeUtils.openKeyboardSettings(context)
                            } else {
                                ImeUtils.showInputMethodPicker(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (!isImeEnabled) "Enable" else "Select", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Active Verification Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF064E3B).copy(alpha = 0.6f) else Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFF059669))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Text(
                        "Global Keyboard Dynamic is active & ready in all apps",
                        color = if (isDarkMode) Color(0xFFECFDF5) else Color(0xFF065F46),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 2. VIP In-App Purchase Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPremiumUser) {
                    if (isDarkMode) Color(0xFF14241B) else Color(0xFFF0FDF4)
                } else {
                    if (isDarkMode) Color(0xFF1E1A2E) else Color(0xFFFAF5FF)
                }
            ),
            border = BorderStroke(
                1.dp,
                if (isPremiumUser) Color(0xFF10B981) else Color(0xFFA855F7)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPro() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPremiumUser) Color(0xFF10B981).copy(alpha = 0.15f)
                                else Color(0xFFA855F7).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFA855F7),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                if (isPremiumUser) "VIP PRO Member" else "Upgrade to PRO (In-App Purchase)",
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 13.5.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFF59E0B)
                            ) {
                                Text(
                                    if (isPremiumUser) "ACTIVE" else "VIP",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            if (isPremiumUser) "All 15+ VIP themes & unlimited AI rewrites unlocked"
                            else "Unlock all locked themes, unlimited AI tones & HD Namaste voices",
                            color = textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFA855F7),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // 3. Choose Your Typing Style Section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Choose your typing style",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TypingStyleCard(
                    title = "abc → ${activeLanguage.nativeName}",
                    subtitle = "amhi → आम्ही",
                    isSelected = currentTypingStyle == "transliteration",
                    isDarkMode = isDarkMode,
                    onClick = { onSelectTypingStyle("transliteration") },
                    modifier = Modifier.weight(1f)
                )
                TypingStyleCard(
                    title = "English",
                    subtitle = "qwerty",
                    isSelected = currentTypingStyle == "english",
                    isDarkMode = isDarkMode,
                    onClick = { onSelectTypingStyle("english") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TypingStyleCard(
                    title = "अक्षर Akshar",
                    subtitle = "अ आ इ ई / क ख",
                    isSelected = currentTypingStyle == "akshar",
                    isDarkMode = isDarkMode,
                    onClick = { onSelectTypingStyle("akshar") },
                    modifier = Modifier.weight(1f)
                )
                TypingStyleCard(
                    title = "Voice Typing",
                    subtitle = "Speak to type 🎙️",
                    isSelected = currentTypingStyle == "voice",
                    isDarkMode = isDarkMode,
                    onClick = { onSelectTypingStyle("voice") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Interactive Test Typing Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Try typing here...", fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                    if (testInput.isNotBlank()) {
                        TextButton(
                            onClick = { testInput = "" },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clear", color = textMuted, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = testInput,
                    onValueChange = { testInput = it },
                    placeholder = { Text("Tap to type with Global Keyboard...", color = textMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = cardSubtleBg,
                        unfocusedContainerColor = cardSubtleBg
                    )
                )

                if (currentTypingStyle == "transliteration" && transliteratedPreview.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMode) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Transliterated Output:", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(transliteratedPreview, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sample test chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("namaste", "amhi", "dhanyawad", "kasa ahes", "shubh ratri").forEach { sample ->
                        Surface(
                            onClick = { testInput = sample },
                            shape = RoundedCornerShape(14.dp),
                            color = cardSubtleBg,
                            border = BorderStroke(0.5.dp, borderColor)
                        ) {
                            Text(
                                sample,
                                color = textColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Spacebar gesture tip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Swipe, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                    Text(
                        "Spacebar gesture: Drag left/right to move cursor smoothly",
                        color = textMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 5. Quick Tools & Utilities
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Quick Tools", fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickToolCard(
                    icon = Icons.Default.Translate,
                    title = "Languages",
                    desc = "${activeLanguage.displayName} (15+ Available)",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToLanguages,
                    modifier = Modifier.weight(1f)
                )
                QuickToolCard(
                    icon = Icons.Default.Mic,
                    title = "Voice & Audio",
                    desc = "TTS & Dictation Engine",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToVoiceSettings,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickToolCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI Writing",
                    desc = "Tone, Grammar & Rewrites",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToAISettings,
                    modifier = Modifier.weight(1f)
                )
                QuickToolCard(
                    icon = Icons.Default.WorkspacePremium,
                    title = "VIP Store",
                    desc = if (isPremiumUser) "PRO Active" else "Unlock All Features",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToPro,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: Smart Reply Studio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToolCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "✨ Smart Reply",
                    desc = "Contextual suggestions for messages",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToSmartReply,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun TypingStyleCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        if (isDarkMode) Color(0xFF064E3B) else Color(0xFFECFDF5)
    } else {
        if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    }
    val borderColor = if (isSelected) Color(0xFF10B981) else if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val titleColor = if (isSelected) {
        if (isDarkMode) Color.White else Color(0xFF065F46)
    } else {
        if (isDarkMode) Color.White else Color(0xFF0F172A)
    }
    val subtitleColor = if (isSelected) {
        if (isDarkMode) Color(0xFF6EE7B7) else Color(0xFF059669)
    } else {
        if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = titleColor
                )
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 11.sp,
                color = subtitleColor
            )
        }
    }
}

@Composable
fun QuickToolCard(
    icon: ImageVector,
    title: String,
    desc: String,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = textColor, fontSize = 12.sp)
                Text(desc, fontSize = 10.sp, color = textMuted, maxLines = 1)
            }
        }
    }
}

@Composable
fun ThemesTab(
    preferences: LingoKeyPreferences,
    currentTheme: KeyboardTheme,
    showKeyBorders: Boolean,
    isDarkMode: Boolean,
    isPremiumUser: Boolean,
    onToggleKeyBorders: (Boolean) -> Unit,
    onNavigateToFullThemes: () -> Unit,
    onNavigateToPro: () -> Unit
) {
    val themePresets = listOf(
        KeyboardTheme.EMERALD,
        KeyboardTheme.MIDNIGHT,
        KeyboardTheme.OCEAN,
        KeyboardTheme.SUNSET,
        KeyboardTheme.CYBERPUNK,
        KeyboardTheme.COSMIC,
        KeyboardTheme.AURORA,
        KeyboardTheme.AMOLED
    )

    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Toggle Key Borders
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
                Column {
                    Text("Show key borders", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                    Text("Outlines individual keys with clear borders", fontSize = 11.sp, color = textMuted)
                }
                Switch(
                    checked = showKeyBorders,
                    onCheckedChange = onToggleKeyBorders,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF059669)
                    )
                )
            }
        }

        // Active Theme Preview Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.backgroundColor),
            border = BorderStroke(2.dp, currentTheme.primaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Current Theme: ${currentTheme.name}", fontWeight = FontWeight.Bold, color = currentTheme.textColor, fontSize = 14.sp)
                            if (currentTheme.isPro) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B)) {
                                    Text("VIP", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                        Text("Applied to all keyboard views", fontSize = 11.sp, color = currentTheme.textSecondaryColor)
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = currentTheme.primaryColor) {
                        Text("Active", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                // Mini Key Preview Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { letter ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = currentTheme.keyColor,
                            border = if (showKeyBorders) BorderStroke(1.dp, currentTheme.keyBorderColor) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(letter, color = currentTheme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Theme Gallery Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Popular Themes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
            TextButton(onClick = onNavigateToFullThemes) {
                Text("View All (15+) →", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Theme Presets Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            items(themePresets) { theme ->
                val isSelected = currentTheme.id == theme.id
                val isLocked = theme.isPro && !isPremiumUser

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.backgroundColor),
                    border = BorderStroke(
                        if (isSelected) 2.5.dp else 1.dp,
                        if (isSelected) Color(0xFF10B981) else borderColor
                    ),
                    modifier = Modifier
                        .height(84.dp)
                        .clickable {
                            if (isLocked) {
                                onNavigateToPro()
                            } else {
                                preferences.setTheme(theme)
                            }
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(theme.name, color = theme.textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                } else if (theme.isPro) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B)) {
                                        Text("VIP", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(theme.primaryColor))
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(theme.keyColor))
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(theme.surfaceColor))
                            }
                        }

                        if (isLocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTab(
    preferences: LingoKeyPreferences,
    showNumberRow: Boolean,
    keyVibration: Boolean,
    keySound: Boolean,
    keyHeightDp: Int,
    transliterationEnabled: Boolean,
    emojiSuggestionsEnabled: Boolean,
    autoCorrection: Boolean,
    autoCapitalization: Boolean,
    activeLanguage: Language,
    isDarkMode: Boolean,
    isPremiumUser: Boolean,
    onNavigateToLanguages: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToAISettings: () -> Unit,
    onNavigateToSmartReply: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToPro: () -> Unit,
    onRestartOnboarding: () -> Unit
) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: In-App Purchase & VIP Membership
        SettingsSectionHeader("In-App Purchase & Membership")
        SettingsRowItem(
            icon = Icons.Default.WorkspacePremium,
            title = if (isPremiumUser) "VIP Membership (Active)" else "Upgrade to PRO VIP",
            subtitle = if (isPremiumUser) "All 15+ VIP themes, unlimited AI & HD voices unlocked"
            else "Unlock all luxury themes, unlimited AI and studio voices",
            isDarkMode = isDarkMode,
            onClick = onNavigateToPro
        )

        // Section: Appearance & Layout
        SettingsSectionHeader("Appearance & Mode")
        // White Mode / Dark Mode Toggle
        SettingsToggleItem(
            icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            title = if (isDarkMode) "Dark Mode (Active)" else "Normal White / Light Mode (Active)",
            subtitle = "Toggle between clean white theme and dark theme",
            checked = isDarkMode,
            isDarkMode = isDarkMode,
            onCheckedChange = { preferences.setDarkMode(it) }
        )
        SettingsRowItem(
            icon = Icons.Default.Palette,
            title = "Themes & Colors",
            subtitle = "Custom backgrounds, key borders, and palettes",
            isDarkMode = isDarkMode,
            onClick = onNavigateToThemes
        )
        SettingsToggleItem(
            icon = Icons.Default.Pin,
            title = "Number Row",
            subtitle = "Show dedicated numbers 1-0 above letter keys",
            checked = showNumberRow,
            isDarkMode = isDarkMode,
            onCheckedChange = { preferences.setShowNumberRow(it) }
        )

        // Section: Typing & Input
        SettingsSectionHeader("Typing & Language")
        SettingsRowItem(
            icon = Icons.Default.Language,
            title = "Languages",
            subtitle = "Active: ${activeLanguage.displayName} (${activeLanguage.nativeName})",
            isDarkMode = isDarkMode,
            onClick = onNavigateToLanguages
        )
        SettingsToggleItem(
            icon = Icons.Default.Translate,
            title = "Phonetic Transliteration",
            subtitle = "Convert Hinglish/English typing to Indic script",
            checked = transliterationEnabled,
            isDarkMode = isDarkMode,
            onCheckedChange = { preferences.setTransliterationEnabled(it) }
        )
        SettingsToggleItem(
            icon = Icons.Default.EmojiEmotions,
            title = "Contextual Emoji Suggestions",
            subtitle = "Suggest relevant emojis as you type words",
            checked = emojiSuggestionsEnabled,
            isDarkMode = isDarkMode,
            onCheckedChange = { preferences.setEmojiSuggestionsEnabled(it) }
        )
        SettingsToggleItem(
            icon = Icons.Default.Spellcheck,
            title = "Auto-Correction",
            subtitle = "Correct typos automatically upon spacebar",
            checked = autoCorrection,
            isDarkMode = isDarkMode,
            onCheckedChange = { preferences.setAutoCorrection(it) }
        )

        // Section: AI & Smart Tools
        SettingsSectionHeader("AI & Smart Tools")
        SettingsRowItem(
            icon = Icons.Default.AutoAwesome,
            title = "✨ Smart Reply Settings",
            subtitle = "Contextual replies, default tones & interactive tester",
            isDarkMode = isDarkMode,
            onClick = onNavigateToSmartReply
        )
        SettingsRowItem(
            icon = Icons.Default.EditNote,
            title = "AI Writing Assistant",
            subtitle = "Grammar corrections, tones & auto-capitalization",
            isDarkMode = isDarkMode,
            onClick = onNavigateToAISettings
        )
        SettingsRowItem(
            icon = Icons.Default.Mic,
            title = "Voice Typing & Namaste Audio",
            subtitle = "Speech recognition and voice output preferences",
            isDarkMode = isDarkMode,
            onClick = onNavigateToVoiceSettings
        )

        // Section: Haptics & Feedback
        SettingsSectionHeader("Haptics & Feedback")
        SettingsToggleItem(
            icon = Icons.Default.Vibration,
            title = "Vibrate on Keypress",
            subtitle = "Subtle tactile haptic feedback",
            checked = keyVibration,
            isDarkMode = isDarkMode,
            onCheckedChange = { preferences.setKeyVibration(it) }
        )
        SettingsToggleItem(
            icon = Icons.Default.VolumeUp,
            title = "Sound on Keypress",
            subtitle = "Soft clicking sound on tap",
            checked = keySound,
            isDarkMode = isDarkMode,
            onCheckedChange = { preferences.setKeySound(it) }
        )

        // Section: Advanced & Privacy
        SettingsSectionHeader("System & Privacy")
        SettingsRowItem(
            icon = Icons.Default.Security,
            title = "Privacy & Security",
            subtitle = "100% on-device processing guarantee",
            isDarkMode = isDarkMode,
            onClick = onNavigateToPrivacy
        )
        SettingsRowItem(
            icon = Icons.Default.RestartAlt,
            title = "Re-run Setup & Tutorial",
            subtitle = "Open onboarding walkthrough and voice guide",
            isDarkMode = isDarkMode,
            onClick = onRestartOnboarding
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Global Keyboard Dynamic • Version 2.5.0\nCrafted with Material 3 & Jetpack Compose",
            fontSize = 11.sp,
            color = textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF10B981),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                    Text(subtitle, fontSize = 11.sp, color = textMuted)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = textMuted)
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isDarkMode: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                    Text(subtitle, fontSize = 11.sp, color = textMuted)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF059669)
                )
            )
        }
    }
}
