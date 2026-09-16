package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.ClipboardRepository
import com.example.data.LingoKeyPreferences
import com.example.engine.*
import com.example.ui.components.*
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
    onNavigateToSmartReply: () -> Unit = {},
    onNavigateToSpinAndWin: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToTranslate: () -> Unit = {}
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

    // Mode, Entitlements and Credits
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val aiCredits by preferences.aiCredits.collectAsState()
    val spinsRemainingToday by preferences.spinsRemainingToday.collectAsState()

    // Authentic 3D Neumorphism Color Palette (Matching Sample Images in Light Mode & Dark Mode)
    val bgColor = if (isDarkMode) NeumorphicColors.DarkScreenBg else NeumorphicColors.LightScreenBg
    val cardBg = if (isDarkMode) NeumorphicColors.DarkCardTop else NeumorphicColors.LightCardTop
    val cardSubtleBg = if (isDarkMode) NeumorphicColors.DarkWellTop else NeumorphicColors.LightWellTop
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted
    val borderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x28000000)
    val navBarBg = if (isDarkMode) NeumorphicColors.DarkScreenBg else NeumorphicColors.LightScreenBg

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
            NeumorphicTopHeader(
                activeLanguageName = activeLanguage.displayName,
                isPremiumUser = isPremiumUser,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { preferences.toggleDarkMode() },
                onOpenVip = { selectedTab = 1 },
                onOpenHelp = { showHelpDialog = true }
            )
        },
        bottomBar = {
            NeumorphicBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                isDarkMode = isDarkMode
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
        ) {
            when (selectedTab) {
                0 -> HomeTab(
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
                    onNavigateToThemes = onNavigateToThemes,
                    onNavigateToVoiceSettings = onNavigateToVoiceSettings,
                    onNavigateToAISettings = onNavigateToAISettings,
                    onNavigateToSmartReply = onNavigateToSmartReply,
                    onNavigateToPro = { selectedTab = 1 },
                    onRestartOnboarding = onRestartOnboarding,
                    onNavigateToSpinAndWin = onNavigateToSpinAndWin,
                    onNavigateToStore = onNavigateToStore,
                    onNavigateToTranslate = onNavigateToTranslate
                )
                1 -> ProVipTab(
                    context = context,
                    preferences = preferences,
                    isDarkMode = isDarkMode,
                    isPremiumUser = isPremiumUser
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
                    onNavigateToPro = { selectedTab = 1 },
                    onRestartOnboarding = onRestartOnboarding,
                    onNavigateToSpinAndWin = onNavigateToSpinAndWin
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
fun HomeTab(
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
    onNavigateToThemes: () -> Unit,
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToAISettings: () -> Unit,
    onNavigateToSmartReply: () -> Unit,
    onNavigateToPro: () -> Unit,
    onRestartOnboarding: () -> Unit,
    onNavigateToSpinAndWin: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToTranslate: () -> Unit = {}
) {
    val aiCredits by preferences.aiCredits.collectAsState()
    val spinsRemainingToday by preferences.spinsRemainingToday.collectAsState()

    var showCustomizeDialog by remember { mutableStateOf(false) }
    var showClipboardDialog by remember { mutableStateOf(false) }

    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Compact IME Status Banner (Reduced Height)
        if (!isImeEnabled || !isImeSelected) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF3B1A03) else Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                        Column {
                            Text("Keyboard Not Active", fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF92400E), fontSize = 12.5.sp)
                            Text(
                                if (!isImeEnabled) "Enable in Android settings" else "Select Global Keyboard as active",
                                color = if (isDarkMode) Color(0xFFFDE68A) else Color(0xFFB45309),
                                fontSize = 10.5.sp
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(if (!isImeEnabled) "Enable" else "Select", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            }
        } else {
            // Active Verification Pill (3D Neumorphic Compact Card)
            NeumorphicCard(
                isDarkMode = isDarkMode,
                cornerRadius = 14.dp,
                elevation = 5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(NeumorphicColors.EmeraldAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeumorphicColors.EmeraldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        "Global Keyboard Dynamic is active & ready in all apps",
                        color = textColor,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 2. Primary 3D Feature Cards (6 grid cards + horizontal Smart Reply card)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Languages & Customize Theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardFeatureCard(
                    icon = Icons.Default.Translate,
                    title = "Languages",
                    desc = "${activeLanguage.displayName} (15+ Available)",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToLanguages,
                    modifier = Modifier.weight(1f)
                )
                DashboardFeatureCard(
                    icon = Icons.Default.Palette,
                    title = "Customize Theme",
                    desc = "Colors, Gradients & Keys",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToThemes,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Instant Translate & AI Writing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardFeatureCard(
                    icon = Icons.Default.GTranslate,
                    title = "Translate",
                    desc = "Copy, Paste & Translate",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToTranslate,
                    modifier = Modifier.weight(1f)
                )
                DashboardFeatureCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI Writing",
                    desc = "Tone, Grammar & Rewrites",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToAISettings,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: VIP Store & Voice and Tone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardFeatureCard(
                    icon = Icons.Default.WorkspacePremium,
                    title = "VIP Store",
                    desc = "Unlock All Features & Themes",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToStore,
                    modifier = Modifier.weight(1f)
                )
                DashboardFeatureCard(
                    icon = Icons.Default.Mic,
                    title = "Voice & Tone",
                    desc = "TTS, Audio & Dictation",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToVoiceSettings,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: ✨ Smart Reply (Full-width horizontal card)
            DashboardFeatureCard(
                icon = Icons.Default.AutoAwesome,
                title = "✨ Smart Reply",
                desc = "Contextual suggestions for incoming messages",
                isDarkMode = isDarkMode,
                onClick = onNavigateToSmartReply,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Real Google AdMob Live Banner (Official Google Test Ad Unit - Real-time loading)
        RealAdMobBanner(isDarkMode = isDarkMode)

        // 4. Spin & Win Free AI Credits Card (3D Neumorphic Physical Wheel Card)
        val spinTextColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
        val spinTextMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted
        val wheelWellBrush = if (isDarkMode) {
            Brush.linearGradient(
                colors = listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        }

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkMode = isDarkMode,
            cornerRadius = 18.dp,
            elevation = 7.dp,
            onClick = onNavigateToSpinAndWin
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 3D Recessed Wheel Graphic
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(wheelWellBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(40.dp)) {
                                val radius = size.minDimension / 2f
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val slices = 8
                                val sliceSweep = 360f / slices

                                for (i in 0 until slices) {
                                    drawArc(
                                        color = if (i % 2 == 0) NeumorphicColors.EmeraldAccent else NeumorphicColors.EmeraldAccent.copy(alpha = 0.45f),
                                        startAngle = i * sliceSweep,
                                        sweepAngle = sliceSweep,
                                        useCenter = true,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2)
                                    )
                                }
                                drawCircle(
                                    color = Color(0xFFF59E0B),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 2f)
                                )
                                drawCircle(
                                    color = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFE2E8F0),
                                    radius = 7f,
                                    center = center
                                )
                                drawCircle(
                                    color = Color(0xFFF59E0B),
                                    radius = 3f,
                                    center = center
                                )
                            }
                            // Mini top indicator pointer
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-2).dp)
                            ) {
                                Text("▼", color = Color(0xFFF59E0B), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "Spin & Win",
                                fontWeight = FontWeight.Bold,
                                color = spinTextColor,
                                fontSize = 14.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    if (isPremiumUser) "🪙 Unlimited (VIP)" else "🪙 $aiCredits Credits",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "• $spinsRemainingToday/5 spins left",
                                    color = spinTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 3D Neumorphic Style Action Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B),
                        shadowElevation = 3.dp,
                        modifier = Modifier.clickable { onNavigateToSpinAndWin() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                Icons.Default.RotateRight,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "SPIN",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. VIP In-App Purchase Banner (Upgrade to PRO)
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
    }

    if (showCustomizeDialog) {
        CustomizeKeyboardDialog(
            preferences = preferences,
            isDarkMode = isDarkMode,
            onDismiss = { showCustomizeDialog = false }
        )
    }

    if (showClipboardDialog) {
        ClipboardManagerDialog(
            context = context,
            isDarkMode = isDarkMode,
            onDismiss = { showClipboardDialog = false }
        )
    }
}

/**
 * 3D Real Neumorphic Dashboard Feature Card.
 * Maintains EXACT size and dimensions as requested, with full 3D Neumorphic cuts, dual shadows, and color styling.
 */
@Composable
fun DashboardFeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicFeatureCard(
        icon = icon,
        title = title,
        desc = desc,
        isDarkMode = isDarkMode,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun CustomizeKeyboardDialog(
    preferences: LingoKeyPreferences,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val keyVibration by preferences.keyVibration.collectAsState()
    val keySound by preferences.keySound.collectAsState()
    val showKeyBorders by preferences.showKeyBorders.collectAsState()
    val keyHeightDp by preferences.keyHeightDp.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Customize Keyboard", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Vibration Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Key Vibration", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Switch(
                        checked = keyVibration,
                        onCheckedChange = { preferences.setKeyVibration(it) }
                    )
                }

                // Sound Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Key Sound", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Switch(
                        checked = keySound,
                        onCheckedChange = { preferences.setKeySound(it) }
                    )
                }

                // Key Borders Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Key Borders", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Switch(
                        checked = showKeyBorders,
                        onCheckedChange = { preferences.setShowKeyBorders(it) }
                    )
                }

                // Key Height Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Keyboard Height", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("${keyHeightDp}dp", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                    }
                    Slider(
                        value = keyHeightDp.toFloat(),
                        onValueChange = { preferences.setKeyHeightDp(it.toInt()) },
                        valueRange = 40f..64f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ClipboardManagerDialog(
    context: Context,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val sampleSnippets = listOf(
        "Hello! How are you doing today?",
        "Thank you so much for your help!",
        "I'm on my way, see you soon.",
        "Can we talk a bit later?",
        "धन्यवाद / शुक्रिया!"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Clipboard & Quick Snippets", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tap any quick snippet to copy to your clipboard:", fontSize = 12.5.sp)
                sampleSnippets.forEach { snippet ->
                    Surface(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Snippet", snippet)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied: $snippet", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMode) Color(0xFF222433) else Color(0xFFF1F5F9),
                        border = BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF33364D) else Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(snippet, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = Color(0xFF10B981))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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
    onNavigateToThemes: () -> Unit,
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToAISettings: () -> Unit,
    onNavigateToSmartReply: () -> Unit,
    onNavigateToPro: () -> Unit,
    onRestartOnboarding: () -> Unit,
    onNavigateToSpinAndWin: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToTranslate: () -> Unit = {}
) = HomeTab(
    context = context,
    preferences = preferences,
    activeLanguage = activeLanguage,
    isImeEnabled = isImeEnabled,
    isImeSelected = isImeSelected,
    currentTypingStyle = currentTypingStyle,
    isDarkMode = isDarkMode,
    isPremiumUser = isPremiumUser,
    onSelectTypingStyle = onSelectTypingStyle,
    onNavigateToLanguages = onNavigateToLanguages,
    onNavigateToThemes = onNavigateToThemes,
    onNavigateToVoiceSettings = onNavigateToVoiceSettings,
    onNavigateToAISettings = onNavigateToAISettings,
    onNavigateToSmartReply = onNavigateToSmartReply,
    onNavigateToPro = onNavigateToPro,
    onRestartOnboarding = onRestartOnboarding,
    onNavigateToSpinAndWin = onNavigateToSpinAndWin,
    onNavigateToStore = onNavigateToStore,
    onNavigateToTranslate = onNavigateToTranslate
)

/**
 * 3D Neumorphic VIP PRO Tab.
 * Prominently presents all Pro/VIP features, subscription options, and instant activation.
 */
@Composable
fun ProVipTab(
    context: Context,
    preferences: LingoKeyPreferences,
    isDarkMode: Boolean,
    isPremiumUser: Boolean
) {
    var selectedPlanId by remember { mutableStateOf("annual") }
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 3D Neumorphic VIP Hero Banner
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkMode = isDarkMode,
            cornerRadius = 20.dp,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3D Gold Crown Emblem
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
                            )
                        )
                        .border(1.5.dp, Color(0xFFFDE68A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = if (isPremiumUser) "You are a VIP PRO Member" else "Unlock VIP PRO Membership",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isPremiumUser) "Lifetime VIP privileges active: all 15+ luxury themes, unlimited AI writing tools & Namaste HD voices unlocked."
                    else "Experience zero ads, 15+ VIP themes, infinite AI tone rewrites, HD Namaste neural voice & unlimited custom templates.",
                    fontSize = 12.sp,
                    color = textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                if (isPremiumUser) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Text("Lifetime VIP Pass Active", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Pricing Plans (3D Neumorphic Selection)
        Text(
            text = "Select Subscription Plan",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) NeumorphicColors.EmeraldAccent else Color(0xFF059669)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PlanCard(
                title = "Annual VIP Pass",
                price = "₹499 / year",
                periodNote = "Only ₹41/month • 7-day free trial",
                badge = "BEST VALUE • SAVE 60%",
                isSelected = selectedPlanId == "annual",
                isDarkMode = isDarkMode,
                onClick = { selectedPlanId = "annual" }
            )

            PlanCard(
                title = "Lifetime Ultra",
                price = "₹999 one-time",
                periodNote = "Pay once, enjoy VIP features forever",
                badge = "NO RECURRING FEES",
                isSelected = selectedPlanId == "lifetime",
                isDarkMode = isDarkMode,
                onClick = { selectedPlanId = "lifetime" }
            )

            PlanCard(
                title = "Monthly Pass",
                price = "₹99 / month",
                periodNote = "Flexible subscription • Cancel anytime",
                badge = null,
                isSelected = selectedPlanId == "monthly",
                isDarkMode = isDarkMode,
                onClick = { selectedPlanId = "monthly" }
            )
        }

        // 3. 3D Action Button
        Button(
            onClick = {
                preferences.activatePro()
                Toast.makeText(context, "🎉 Welcome to VIP PRO! All features unlocked successfully.", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF059669)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFDE68A), modifier = Modifier.size(20.dp))
                Text(
                    text = if (isPremiumUser) "VIP PRO ACTIVATED" else "UPGRADE TO VIP PRO NOW",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            }
        }

        // 4. VIP Features List (3D Neumorphic Grid / Cards)
        Text(
            text = "VIP PRO Exclusive Privileges",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) NeumorphicColors.EmeraldAccent else Color(0xFF059669)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VipBenefitRow(Icons.Default.Palette, "15+ Luxury Themes", "Exclusive Neon, Cyberpunk, AMOLED & Gold VIP styles", isDarkMode)
            VipBenefitRow(Icons.Default.AutoAwesome, "Unlimited AI Writing", "Infinite AI rewrites, tone changing & smart grammar checks", isDarkMode)
            VipBenefitRow(Icons.Default.Mic, "HD Neural Voice & Namaste Audio", "Premium neural TTS output & continuous dictation engine", isDarkMode)
            VipBenefitRow(Icons.Default.Block, "100% Zero Ads Guarantee", "Enjoy completely ad-free setup & keyboard experience", isDarkMode)
            VipBenefitRow(Icons.Default.DashboardCustomize, "Unlimited Custom Card Templates", "Create, customize & save infinite cards on the keyboard", isDarkMode)
            VipBenefitRow(Icons.Default.Bolt, "Ultra-Fast Cloud Transliteration", "Priority phonetic engine with near-zero latency", isDarkMode)
        }

        // 5. Restore Purchase & Security Note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    preferences.restorePurchases()
                    Toast.makeText(context, "Purchases restored successfully!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = textMuted)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore Purchases", color = textMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    periodNote: String,
    badge: String?,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted

    val wellBrush = if (isDarkMode) {
        Brush.linearGradient(listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom))
    } else {
        Brush.linearGradient(listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom))
    }

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        cornerRadius = 16.dp,
        elevation = if (isSelected) 3.dp else 6.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) Modifier
                        .background(wellBrush)
                        .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                    else Modifier
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (isSelected) Color(0xFFF59E0B) else textMuted.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                            if (badge != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        badge,
                                        color = Color(0xFFF59E0B),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(periodNote, fontSize = 11.sp, color = textMuted)
                    }
                }

                Text(
                    price,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (isSelected) Color(0xFFF59E0B) else textColor
                )
            }
        }
    }
}

@Composable
private fun VipBenefitRow(
    icon: ImageVector,
    title: String,
    desc: String,
    isDarkMode: Boolean
) {
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isDarkMode) Color(0xFF064E3B).copy(alpha = 0.4f)
                    else Color(0xFFD1FAE5)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NeumorphicColors.EmeraldAccent, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = textColor)
            Text(desc, fontSize = 11.sp, color = textMuted, maxLines = 1)
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
    onRestartOnboarding: () -> Unit,
    onNavigateToSpinAndWin: () -> Unit = {}
) {
    val context = LocalContext.current
    var showHeightDialog by remember { mutableStateOf(false) }
    var showSoundVibrationDialog by remember { mutableStateOf(false) }
    var showEmojiNumbersDialog by remember { mutableStateOf(false) }
    var showTypingDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted
    val dividerColor = if (isDarkMode) Color(0x18FFFFFF) else Color(0x12000000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top 3D Neumorphic Header Card: Status & Theme Mode Switcher
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkMode = isDarkMode,
            cornerRadius = 18.dp,
            elevation = 7.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Recessed 3D Well with App Glyph
                    val wellBrush = if (isDarkMode) {
                        Brush.linearGradient(listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom))
                    } else {
                        Brush.linearGradient(listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom))
                    }
                    val wellBorder = if (isDarkMode) {
                        BorderStroke(1.dp, Brush.linearGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.White.copy(alpha = 0.10f))))
                    } else {
                        BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0x2A000000), Color.White.copy(alpha = 0.85f))))
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(wellBrush)
                            .border(wellBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = NeumorphicColors.EmeraldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Global Keyboard", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                            if (isPremiumUser) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF59E0B)
                                ) {
                                    Text("VIP", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                        Text(
                            "Language: ${activeLanguage.displayName} • v2.5.0",
                            fontSize = 11.5.sp,
                            color = textMuted
                        )
                    }
                }

                // Tactile 3D Day / Night Mode Toggle Button
                val modeButtonBrush = if (isDarkMode) {
                    Brush.linearGradient(listOf(Color(0xFF2E313D), Color(0xFF1E2028)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFFAF7F2), Color(0xFFE5E0D7)))
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(modeButtonBrush)
                        .clickable { preferences.toggleDarkMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark/Light Mode",
                        tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section Title: Settings (Clean & Minimalist like reference image)
        Text(
            text = "Settings",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NeumorphicColors.EmeraldAccent,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )

        // Main Unified 3D Neumorphic Settings Card (No individual border cards!)
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkMode = isDarkMode,
            cornerRadius = 20.dp,
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Themes
                NeumorphicSettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Themes",
                    subtitle = "Custom backgrounds, key borders & colors",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToThemes
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 2. Keyboard height
                NeumorphicSettingsItem(
                    icon = Icons.Default.SwapVert,
                    title = "Keyboard height",
                    subtitle = "$keyHeightDp dp",
                    isDarkMode = isDarkMode,
                    trailingBadge = "${keyHeightDp}dp",
                    onClick = { showHeightDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 3. Sound & vibration
                NeumorphicSettingsItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Sound & vibration",
                    subtitle = when {
                        keyVibration && keySound -> "Sound & vibration enabled"
                        keyVibration -> "Vibration enabled"
                        keySound -> "Sound enabled"
                        else -> "Muted"
                    },
                    isDarkMode = isDarkMode,
                    onClick = { showSoundVibrationDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 4. Emojis, numbers & symbols
                NeumorphicSettingsItem(
                    icon = Icons.Default.EmojiEmotions,
                    title = "Emojis, numbers & symbols",
                    subtitle = if (showNumberRow) "Number row on • Emoji suggestions" else "Emoji suggestions",
                    isDarkMode = isDarkMode,
                    onClick = { showEmojiNumbersDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 5. Typing & Language
                NeumorphicSettingsItem(
                    icon = Icons.Default.Translate,
                    title = "Typing",
                    subtitle = "Language: ${activeLanguage.displayName} • Transliteration",
                    isDarkMode = isDarkMode,
                    onClick = { showTypingDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 6. AI & Smart Reply
                NeumorphicSettingsItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI & Smart Reply",
                    subtitle = "Contextual replies, tones & rewrite tools",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToSmartReply
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 7. Voice Typing
                NeumorphicSettingsItem(
                    icon = Icons.Default.Mic,
                    title = "Voice typing & audio",
                    subtitle = "Real-time speech to text & Namaste audio",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToVoiceSettings
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 8. Spin & Win (Credit Wheel)
                NeumorphicSettingsItem(
                    icon = Icons.Default.RotateRight,
                    title = "Spin & Win",
                    subtitle = "Daily lucky spin for free AI credits",
                    isDarkMode = isDarkMode,
                    trailingBadge = "5 Spins",
                    badgeColor = Color(0xFFF59E0B),
                    onClick = onNavigateToSpinAndWin
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 9. Premium
                NeumorphicSettingsItem(
                    icon = Icons.Default.WorkspacePremium,
                    title = "Premium",
                    subtitle = if (isPremiumUser) "VIP Lifetime Active (15+ Luxury Themes)" else "Unlock VIP themes & unlimited AI",
                    isDarkMode = isDarkMode,
                    trailingBadge = if (isPremiumUser) "VIP Active" else "Upgrade",
                    badgeColor = Color(0xFFF59E0B),
                    onClick = onNavigateToPro
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 10. Re-run Setup
                NeumorphicSettingsItem(
                    icon = Icons.Default.RestartAlt,
                    title = "Re-run setup",
                    subtitle = "Restart onboarding guide & setup steps",
                    isDarkMode = isDarkMode,
                    onClick = onRestartOnboarding
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 11. Privacy & Security
                NeumorphicSettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy & security",
                    subtitle = "100% on-device private processing",
                    isDarkMode = isDarkMode,
                    onClick = onNavigateToPrivacy
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                    thickness = 0.8.dp,
                    color = dividerColor
                )

                // 12. About
                NeumorphicSettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Global Keyboard Dynamic • v2.5.0",
                    isDarkMode = isDarkMode,
                    onClick = { showAboutDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Global Keyboard Dynamic • Version 2.5.0\nCrafted with 3D Neumorphism UI/UX",
            fontSize = 11.sp,
            color = textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
    }

    // --- Interactive 3D Dialogs ---

    // 1. Keyboard Height Dialog with live slider and visual preview
    if (showHeightDialog) {
        var tempHeight by remember { mutableFloatStateOf(keyHeightDp.toFloat()) }
        AlertDialog(
            onDismissRequest = { showHeightDialog = false },
            title = {
                Text("Keyboard Height", fontWeight = FontWeight.Bold, color = textColor)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Adjust vertical height of letter keys. Current: ${tempHeight.toInt()} dp",
                        fontSize = 12.5.sp,
                        color = textMuted
                    )

                    // Live Key Height Visual Simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tempHeight.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkMode) Color(0xFF262832) else Color(0xFFE2DDD4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Preview: Key Height (${tempHeight.toInt()} dp)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }

                    // Slider (40dp to 64dp)
                    Slider(
                        value = tempHeight,
                        onValueChange = { tempHeight = it },
                        valueRange = 40f..64f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = NeumorphicColors.EmeraldAccent,
                            activeTrackColor = NeumorphicColors.EmeraldAccent
                        )
                    )

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(42 to "Short", 48 to "Normal", 54 to "Tall", 60 to "Extra").forEach { (h, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (tempHeight.toInt() == h) NeumorphicColors.EmeraldAccent else (if (isDarkMode) Color(0xFF262830) else Color(0xFFE5E0D6)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tempHeight = h.toFloat() }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (tempHeight.toInt() == h) Color.White else textColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferences.setKeyHeightDp(tempHeight.toInt())
                        showHeightDialog = false
                    }
                ) {
                    Text("Apply (${tempHeight.toInt()} dp)", color = NeumorphicColors.EmeraldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHeightDialog = false }) {
                    Text("Cancel", color = textMuted)
                }
            },
            containerColor = if (isDarkMode) NeumorphicColors.DarkCardTop else NeumorphicColors.LightCardTop
        )
    }

    // 2. Sound & Vibration Dialog with test haptic button
    if (showSoundVibrationDialog) {
        AlertDialog(
            onDismissRequest = { showSoundVibrationDialog = false },
            title = {
                Text("Sound & Vibration", fontWeight = FontWeight.Bold, color = textColor)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Tactile haptic feedback and acoustic keypress audio:", fontSize = 12.sp, color = textMuted)

                    // Vibrate toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vibrate on Keypress", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 13.sp)
                            Text("Tactile haptic response on tap", fontSize = 11.sp, color = textMuted)
                        }
                        Switch(
                            checked = keyVibration,
                            onCheckedChange = { preferences.setKeyVibration(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeumorphicColors.EmeraldAccent
                            )
                        )
                    }

                    // Sound toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sound on Keypress", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 13.sp)
                            Text("Subtle mechanical click audio", fontSize = 11.sp, color = textMuted)
                        }
                        Switch(
                            checked = keySound,
                            onCheckedChange = { preferences.setKeySound(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeumorphicColors.EmeraldAccent
                            )
                        )
                    }

                    // Test Vibration Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkMode) Color(0xFF262832) else Color(0xFFE2DDD5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator?.vibrate(35)
                                    }
                                    Toast.makeText(context, "Haptic pulse triggered", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {}
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = NeumorphicColors.EmeraldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Vibration Feedback", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoundVibrationDialog = false }) {
                    Text("Done", color = NeumorphicColors.EmeraldAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDarkMode) NeumorphicColors.DarkCardTop else NeumorphicColors.LightCardTop
        )
    }

    // 3. Emojis, Numbers & Symbols Dialog
    if (showEmojiNumbersDialog) {
        AlertDialog(
            onDismissRequest = { showEmojiNumbersDialog = false },
            title = {
                Text("Emojis, Numbers & Symbols", fontWeight = FontWeight.Bold, color = textColor)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Number Row Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dedicated Number Row", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 13.sp)
                            Text("Shows 1-0 row persistently above letter keys", fontSize = 11.sp, color = textMuted)
                        }
                        Switch(
                            checked = showNumberRow,
                            onCheckedChange = { preferences.setShowNumberRow(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeumorphicColors.EmeraldAccent
                            )
                        )
                    }

                    // Emoji Suggestions Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Contextual Emoji Suggestions", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 13.sp)
                            Text("Displays relevant emojis above suggestion strip", fontSize = 11.sp, color = textMuted)
                        }
                        Switch(
                            checked = emojiSuggestionsEnabled,
                            onCheckedChange = { preferences.setEmojiSuggestionsEnabled(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeumorphicColors.EmeraldAccent
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmojiNumbersDialog = false }) {
                    Text("Done", color = NeumorphicColors.EmeraldAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDarkMode) NeumorphicColors.DarkCardTop else NeumorphicColors.LightCardTop
        )
    }

    // 4. Typing Preferences Dialog
    if (showTypingDialog) {
        AlertDialog(
            onDismissRequest = { showTypingDialog = false },
            title = {
                Text("Typing & Language", fontWeight = FontWeight.Bold, color = textColor)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Phonetic Transliteration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Phonetic Transliteration", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 13.sp)
                            Text("Type in English letters to get Indic script (e.g. namaste -> नमस्ते)", fontSize = 11.sp, color = textMuted)
                        }
                        Switch(
                            checked = transliterationEnabled,
                            onCheckedChange = { preferences.setTransliterationEnabled(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeumorphicColors.EmeraldAccent
                            )
                        )
                    }

                    // Auto-Correction
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Correction", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 13.sp)
                            Text("Automatically correct typos on spacebar tap", fontSize = 11.sp, color = textMuted)
                        }
                        Switch(
                            checked = autoCorrection,
                            onCheckedChange = { preferences.setAutoCorrection(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeumorphicColors.EmeraldAccent
                            )
                        )
                    }

                    // Auto-Capitalization
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Capitalization", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 13.sp)
                            Text("Capitalize first letter of every new sentence", fontSize = 11.sp, color = textMuted)
                        }
                        Switch(
                            checked = autoCapitalization,
                            onCheckedChange = { preferences.setAutoCapitalization(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeumorphicColors.EmeraldAccent
                            )
                        )
                    }

                    // Button to open full language picker
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkMode) Color(0xFF262832) else Color(0xFFE2DDD5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showTypingDialog = false
                                onNavigateToLanguages()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = NeumorphicColors.EmeraldAccent, modifier = Modifier.size(18.dp))
                                Text("Manage Languages (${activeLanguage.displayName})", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = textMuted, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypingDialog = false }) {
                    Text("Done", color = NeumorphicColors.EmeraldAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDarkMode) NeumorphicColors.DarkCardTop else NeumorphicColors.LightCardTop
        )
    }

    // 5. About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("About Global Keyboard", fontWeight = FontWeight.Bold, color = textColor)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Global Keyboard Dynamic • Version 2.5.0", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                    Text("• 100% On-Device Privacy: Your keystrokes and voice dictation never leave your phone.", fontSize = 12.sp, color = textMuted)
                    Text("• 3D Neumorphism Design: Soft extruded surfaces, tactile lighting and dual depth.", fontSize = 12.sp, color = textMuted)
                    Text("• Multi-Language: Phonetic transliteration for Hindi, Marathi, Gujarati, Tamil and more.", fontSize = 12.sp, color = textMuted)
                    Text("• AI Smart Reply & Namaste TTS: Intelligent contextual suggestions with custom tones.", fontSize = 12.sp, color = textMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = NeumorphicColors.EmeraldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAboutDialog = false
                        onNavigateToPrivacy()
                    }
                ) {
                    Text("Privacy Policy", color = textMuted)
                }
            },
            containerColor = if (isDarkMode) NeumorphicColors.DarkCardTop else NeumorphicColors.LightCardTop
        )
    }
}

/**
 * 3D Neumorphic Settings Item Row.
 * Sits cleanly within the unified Neumorphic card without ugly individual box borders,
 * using a tactile 3D recessed icon well and subtle typography.
 */
@Composable
fun NeumorphicSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDarkMode: Boolean,
    trailingBadge: String? = null,
    badgeColor: Color = NeumorphicColors.EmeraldAccent,
    onClick: () -> Unit
) {
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted

    val wellBrush = if (isDarkMode) {
        Brush.linearGradient(listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom))
    } else {
        Brush.linearGradient(listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom))
    }

    val wellBorder = if (isDarkMode) {
        BorderStroke(1.dp, Brush.linearGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.White.copy(alpha = 0.10f))))
    } else {
        BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0x2A000000), Color.White.copy(alpha = 0.85f))))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 3D Recessed Neumorphic Icon Well
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(wellBrush)
                    .border(wellBorder, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeumorphicColors.EmeraldAccent,
                    modifier = Modifier.size(19.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    fontSize = 13.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = textMuted,
                    maxLines = 1
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (trailingBadge != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.18f),
                    border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = trailingBadge,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = textMuted.copy(alpha = 0.55f),
                modifier = Modifier.size(11.dp)
            )
        }
    }
}
