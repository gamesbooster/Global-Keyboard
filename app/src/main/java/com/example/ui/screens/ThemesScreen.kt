package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreditsSecurityManager
import com.example.data.LingoKeyPreferences
import com.example.model.KeyboardTheme
import com.example.model.ThemeStyle

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
    onNavigateToPro: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToSpinAndWin: () -> Unit = {}
) {
    val context = LocalContext.current
    val creditsSecurityManager = remember { CreditsSecurityManager.getInstance(context) }

    val currentTheme by preferences.currentTheme.collectAsState()
    val activeLanguage by preferences.activeLanguage.collectAsState()
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val unlockedThemeIds by preferences.unlockedThemeIds.collectAsState()
    val aiCredits by creditsSecurityManager.aiCredits.collectAsState()

    // Preview state: the theme currently displayed in the locked top preview
    var previewingTheme by remember(currentTheme) { mutableStateOf(currentTheme) }

    // Dialog states
    var unlockWithCreditsTarget by remember { mutableStateOf<KeyboardTheme?>(null) }
    var insufficientCreditsTarget by remember { mutableStateOf<KeyboardTheme?>(null) }
    var showCustomThemeDialog by remember { mutableStateOf(false) }

    // Custom Theme Builder State
    var selectedGradientIndex by remember { mutableStateOf(0) }
    var selectedAccentColor by remember { mutableStateOf(ACCENT_COLOR_PRESETS[0]) }
    var customKeyAlpha by remember { mutableFloatStateOf(0.85f) }

    val screenBg = if (isDarkMode) Color(0xFF0E0E13) else Color(0xFFF4F6F9)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Helper: Determine theme cost (0 for free, 50 or 100 for VIP/pro/store themes)
    fun getThemeCost(theme: KeyboardTheme): Int {
        return when {
            theme.id in listOf("spain_2", "spain_3", "spain_4", "minimal_light", "emerald", "midnight", "classic_dark") -> 0
            theme.isPro -> 100
            theme.category == "Store" || theme.id in listOf("argentina_10", "argentina_gold", "cyberpunk", "carbon_gold") -> 50
            theme.themeStyle in listOf(ThemeStyle.TACTILE_3D, ThemeStyle.NEUMORPHISM_LIGHT, ThemeStyle.NEUMORPHISM_DARK, ThemeStyle.GLASSMORPHISM, ThemeStyle.CLAYMORPHISM) -> 50
            else -> 0
        }
    }

    fun isThemeAvailable(theme: KeyboardTheme): Boolean {
        if (isPremiumUser) return true
        if (getThemeCost(theme) == 0) return true
        return unlockedThemeIds.contains(theme.id)
    }

    fun handleApplyOrUnlock(theme: KeyboardTheme) {
        val cost = getThemeCost(theme)
        val available = isThemeAvailable(theme)
        if (available) {
            preferences.setTheme(theme)
            previewingTheme = theme
            Toast.makeText(context, "🎉 ${theme.name} theme applied!", Toast.LENGTH_SHORT).show()
        } else {
            if (aiCredits >= cost) {
                unlockWithCreditsTarget = theme
            } else {
                insufficientCreditsTarget = theme
            }
        }
    }

    Scaffold(
        containerColor = screenBg,
        topBar = {
            Surface(
                color = if (isDarkMode) Color(0xFF151620) else Color(0xFFFFFFFF),
                shadowElevation = 3.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Themes & Styles",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                    },
                    actions = {
                        // Custom Studio Shortcut Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFEC4899).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { showCustomThemeDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Custom Studio",
                                    tint = Color(0xFFEC4899),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    "Custom",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEC4899)
                                )
                            }
                        }

                        // Credits Balance Pill (Tapping opens Spin & Win)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFEEF2F6),
                            border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { onNavigateToSpinAndWin() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🪙", fontSize = 12.sp)
                                Text(
                                    if (isPremiumUser) "👑 VIP" else "$aiCredits",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color(0xFFFDE047) else Color(0xFFCA8A04)
                                )
                            }
                        }

                        // Theme Store Shortcut Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF6366F1).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { onNavigateToStore() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = "Theme Store",
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Store",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // =========================================================================
            // 1. LOCKED / PINNED TOP SECTION: LIVE THEME KEYBOARD PREVIEW
            // This stays fixed at the top while users scroll the themes list below!
            // =========================================================================
            Surface(
                color = if (isDarkMode) Color(0xFF13141C) else Color(0xFFFFFFFF),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF222332) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Row of Locked Preview: Theme Name & Active Badge on Left, Apply/Unlock on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = previewingTheme.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (currentTheme.id == previewingTheme.id) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            "Active",
                                            color = Color(0xFF10B981),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Apply / Download Action Button
                        val cost = getThemeCost(previewingTheme)
                        val isAvailable = isThemeAvailable(previewingTheme)
                        val isCurrent = currentTheme.id == previewingTheme.id

                        if (isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("Active", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (isAvailable) {
                            Button(
                                onClick = { handleApplyOrUnlock(previewingTheme) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Apply", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { handleApplyOrUnlock(previewingTheme) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("🪙 $cost • Unlock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    // FULL INTERACTIVE 4-ROW KEYBOARD PREVIEW
                    val previewBgMod = if (previewingTheme.backgroundGradient != null) {
                        Modifier.background(Brush.verticalGradient(previewingTheme.backgroundGradient!!))
                    } else {
                        Modifier.background(previewingTheme.backgroundColor)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .then(previewBgMod)
                            .border(1.dp, previewingTheme.keyBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 4.dp, vertical = 5.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            // Row 1 (10 Keys - QWERTY)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                            ) {
                                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { char ->
                                    PreviewKey(
                                        theme = previewingTheme,
                                        text = char,
                                        modifier = Modifier.weight(1f),
                                        height = 28.dp,
                                        onClick = {}
                                    )
                                }
                            }

                            // Row 2 (A to L with half-spacers)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                            ) {
                                Spacer(modifier = Modifier.weight(0.5f))
                                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").forEach { char ->
                                    PreviewKey(
                                        theme = previewingTheme,
                                        text = char,
                                        modifier = Modifier.weight(1f),
                                        height = 28.dp,
                                        onClick = {}
                                    )
                                }
                                Spacer(modifier = Modifier.weight(0.5f))
                            }

                            // Row 3 (Shift, Z to M, Backspace)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PreviewKey(
                                    theme = previewingTheme,
                                    icon = Icons.Default.KeyboardArrowUp,
                                    modifier = Modifier.weight(1.4f),
                                    isSpecial = true,
                                    height = 28.dp,
                                    onClick = {}
                                )

                                listOf("Z", "X", "C", "V", "B", "N", "M").forEach { char ->
                                    PreviewKey(
                                        theme = previewingTheme,
                                        text = char,
                                        modifier = Modifier.weight(1f),
                                        height = 28.dp,
                                        onClick = {}
                                    )
                                }

                                PreviewKey(
                                    theme = previewingTheme,
                                    icon = Icons.AutoMirrored.Filled.Backspace,
                                    modifier = Modifier.weight(1.4f),
                                    isSpecial = true,
                                    height = 28.dp,
                                    onClick = {}
                                )
                            }

                            // Row 4 (?123, Language, Space, Send)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PreviewKey(
                                    theme = previewingTheme,
                                    text = "?123",
                                    modifier = Modifier.weight(1.3f),
                                    isSpecial = true,
                                    height = 28.dp,
                                    onClick = {}
                                )

                                PreviewKey(
                                    theme = previewingTheme,
                                    icon = Icons.Default.Language,
                                    modifier = Modifier.weight(1f),
                                    isSpecial = true,
                                    height = 28.dp,
                                    onClick = { preferences.switchNextLanguage() }
                                )

                                // Space Bar
                                PreviewKey(
                                    theme = previewingTheme,
                                    text = "Space",
                                    modifier = Modifier.weight(4.7f),
                                    height = 28.dp,
                                    onClick = {}
                                )

                                // Send Action Key
                                PreviewKey(
                                    theme = previewingTheme,
                                    icon = Icons.AutoMirrored.Filled.Send,
                                    modifier = Modifier.weight(1.4f),
                                    isSpecial = true,
                                    isSelected = true,
                                    height = 28.dp,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. SCROLLABLE THEMES GALLERY WITH 3D NEUMORPHISM CARDS
            // =========================================================================
            var selectedCategory by remember { mutableStateOf("All") }
            val categories = listOf(
                "All",
                "🔘 3D & Neumorphic",
                "🏆 Champions Edition",
                "🫧 Glass & Translucent",
                "🌌 Dark & Gradients",
                "🌸 Light & Pastels"
            )

            val filteredThemes = remember(selectedCategory) {
                when (selectedCategory) {
                    "🔘 3D & Neumorphic" -> KeyboardTheme.ALL_THEMES.filter {
                        it.themeStyle in listOf(
                            ThemeStyle.TACTILE_3D,
                            ThemeStyle.NEUMORPHISM_LIGHT,
                            ThemeStyle.NEUMORPHISM_DARK,
                            ThemeStyle.CLAYMORPHISM,
                            ThemeStyle.SKEUOMORPHISM
                        )
                    }
                    "🏆 Champions Edition" -> KeyboardTheme.ALL_THEMES.filter {
                        it.id in listOf("spain_2", "spain_3", "spain_4", "argentina_10", "argentina_gold")
                    }
                    "🫧 Glass & Translucent" -> KeyboardTheme.ALL_THEMES.filter {
                        it.themeStyle in listOf(ThemeStyle.GLASSMORPHISM, ThemeStyle.SEMI_TRANSPARENT) || it.keyAlpha < 1.0f
                    }
                    "🌌 Dark & Gradients" -> KeyboardTheme.ALL_THEMES.filter { it.isDark }
                    "🌸 Light & Pastels" -> KeyboardTheme.ALL_THEMES.filter { !it.isDark }
                    else -> KeyboardTheme.ALL_THEMES
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isCatActive = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCatActive) Color(0xFF6366F1) else if (isDarkMode) Color(0xFF1A1B26) else Color(0xFFE2E8F0),
                            border = BorderStroke(
                                1.dp,
                                if (isCatActive) Color(0xFF818CF8) else if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Transparent
                            ),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isCatActive) Color.White else if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = if (isCatActive) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Grid Count
                Text(
                    text = "${filteredThemes.size} Themes Available",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textMuted,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // 3D NEUMORPHISM THEME CARDS GRID (Optimized with keys & hardware acceleration)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredThemes,
                        key = { it.id },
                        contentType = { "theme_card" }
                    ) { theme ->
                        val isSelected = currentTheme.id == theme.id
                        val isPreviewed = previewingTheme.id == theme.id
                        val cost = remember(theme.id) { getThemeCost(theme) }
                        val isUnlocked = isThemeAvailable(theme)

                        NeumorphicThemeCard(
                            theme = theme,
                            isSelected = isSelected,
                            isPreviewed = isPreviewed,
                            isUnlocked = isUnlocked,
                            cost = cost,
                            isDarkMode = isDarkMode,
                            onClick = {
                                previewingTheme = theme
                            },
                            onApply = {
                                handleApplyOrUnlock(theme)
                            }
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    // 1. Unlock Theme with Credits Dialog
    unlockWithCreditsTarget?.let { theme ->
        val cost = getThemeCost(theme)
        AlertDialog(
            onDismissRequest = { unlockWithCreditsTarget = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪙", fontSize = 24.sp)
                }
            },
            title = {
                Text("Unlock ${theme.name}?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This premium 3D theme costs $cost credits to download and keep permanently.",
                        fontSize = 13.sp,
                        color = textColor
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Balance:", fontSize = 12.sp, color = textMuted)
                            Text("$aiCredits Credits 🪙", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = preferences.unlockThemeWithCredits(theme.id, cost)
                        if (success) {
                            preferences.setTheme(theme)
                            previewingTheme = theme
                            Toast.makeText(context, "🎉 ${theme.name} unlocked and applied!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Could not unlock. Please check credit balance.", Toast.LENGTH_SHORT).show()
                        }
                        unlockWithCreditsTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Unlock & Apply", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { unlockWithCreditsTarget = null }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    // 2. Insufficient Credits Dialog -> One-tap Redirect to Store / Spin & Win
    insufficientCreditsTarget?.let { theme ->
        val cost = getThemeCost(theme)
        AlertDialog(
            onDismissRequest = { insufficientCreditsTarget = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text("Need More Credits", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${theme.name} requires $cost credits. You have $aiCredits credits.",
                        fontSize = 13.sp,
                        color = textColor
                    )
                    Text(
                        "Visit the Theme Store to browse all wallpapers and themes, or Spin the Lucky Wheel to win free credits instantly!",
                        fontSize = 12.sp,
                        color = textMuted
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            insufficientCreditsTarget = null
                            onNavigateToSpinAndWin()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308))
                    ) {
                        Text("Spin & Win", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            insufficientCreditsTarget = null
                            onNavigateToStore()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text("Theme Store", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { insufficientCreditsTarget = null }) {
                    Text("Not Now", color = textMuted)
                }
            }
        )
    }

    // 3. Custom Theme Studio Dialog
    if (showCustomThemeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomThemeDialog = false },
            containerColor = if (isDarkMode) Color(0xFF181824) else Color(0xFFFFFFFF),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Custom Theme Studio", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1. Background Backdrop", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CUSTOM_GRADIENT_PRESETS.take(4).forEachIndexed { idx, preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.verticalGradient(preset.colors))
                                    .border(
                                        width = if (selectedGradientIndex == idx) 2.dp else 1.dp,
                                        color = if (selectedGradientIndex == idx) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
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
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.verticalGradient(preset.colors))
                                    .border(
                                        width = if (selectedGradientIndex == actualIdx) 2.dp else 1.dp,
                                        color = if (selectedGradientIndex == actualIdx) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
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

                    Text("2. Key Accent Glow", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ACCENT_COLOR_PRESETS.take(4).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        width = if (selectedAccentColor == color) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedAccentColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedAccentColor == color) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (color == Color.White) Color.Black else Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ACCENT_COLOR_PRESETS.drop(4).take(4).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        width = if (selectedAccentColor == color) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedAccentColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedAccentColor == color) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (color == Color.White) Color.Black else Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Text("3. Transparency (${(customKeyAlpha * 100).toInt()}%)", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                    Text("Apply Theme", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomThemeDialog = false }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }
}

/**
 * 3D Neumorphism Theme Card for the gallery.
 * Features extruded physical surface, dual shadows, beveled 3D rim,
 * embedded miniature keyboard canvas, theme name, and price/status pill.
 */
@Composable
fun MiniKeyboardCanvas(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier
) {
    val cornerRadiusDp = 10.dp
    val bgGradient = theme.backgroundGradient
    val bgColor = theme.backgroundColor
    val keyColor = theme.keyColor.copy(alpha = theme.keyAlpha.coerceAtLeast(0.4f))
    val keyBorderColor = theme.keyBorderColor.copy(alpha = 0.35f)
    val spaceColor = theme.keySpecialColor.copy(alpha = 0.9f)
    val accentColor = theme.accentColor.copy(alpha = 0.5f)

    Canvas(
        modifier = modifier.clip(RoundedCornerShape(cornerRadiusDp))
    ) {
        val w = size.width
        val h = size.height
        val crPx = cornerRadiusDp.toPx()

        // 1. Draw Theme Background
        if (bgGradient != null && bgGradient.size >= 2) {
            drawRoundRect(
                brush = Brush.verticalGradient(bgGradient),
                cornerRadius = CornerRadius(crPx, crPx)
            )
        } else {
            drawRoundRect(
                color = bgColor,
                cornerRadius = CornerRadius(crPx, crPx)
            )
        }

        // Background Outer Rim Border
        drawRoundRect(
            color = keyBorderColor.copy(alpha = 0.25f),
            cornerRadius = CornerRadius(crPx, crPx),
            style = Stroke(width = 1.dp.toPx())
        )

        val paddingH = 6.dp.toPx()
        val paddingV = 5.5.dp.toPx()
        val availW = w - paddingH * 2
        val availH = h - paddingV * 2

        val rowSpacing = 3.dp.toPx()
        val colSpacing = 2.5.dp.toPx()
        val numRows = 3
        val keyH = (availH - rowSpacing * (numRows - 1)) / numRows
        val keyCorner = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())

        // Row 1: 5 Keys
        val r1Count = 5
        val r1KeyW = (availW - colSpacing * (r1Count - 1)) / r1Count
        var y = paddingV
        for (i in 0 until r1Count) {
            val x = paddingH + i * (r1KeyW + colSpacing)
            drawRoundRect(
                color = keyColor,
                topLeft = Offset(x, y),
                size = Size(r1KeyW, keyH),
                cornerRadius = keyCorner
            )
            drawRoundRect(
                color = keyBorderColor,
                topLeft = Offset(x, y),
                size = Size(r1KeyW, keyH),
                cornerRadius = keyCorner,
                style = Stroke(width = 0.75.dp.toPx())
            )
        }

        // Row 2: 4 Keys (Centered)
        val r2Count = 4
        val r2KeyW = (availW - colSpacing * (r2Count - 1)) / r2Count
        y += keyH + rowSpacing
        for (i in 0 until r2Count) {
            val x = paddingH + i * (r2KeyW + colSpacing)
            drawRoundRect(
                color = keyColor,
                topLeft = Offset(x, y),
                size = Size(r2KeyW, keyH),
                cornerRadius = keyCorner
            )
            drawRoundRect(
                color = keyBorderColor,
                topLeft = Offset(x, y),
                size = Size(r2KeyW, keyH),
                cornerRadius = keyCorner,
                style = Stroke(width = 0.75.dp.toPx())
            )
        }

        // Row 3: Spacebar (Centered, width ~ 65% of available width)
        y += keyH + rowSpacing
        val spaceW = availW * 0.65f
        val spaceX = paddingH + (availW - spaceW) / 2f
        drawRoundRect(
            color = spaceColor,
            topLeft = Offset(spaceX, y),
            size = Size(spaceW, keyH),
            cornerRadius = keyCorner
        )
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(spaceX, y),
            size = Size(spaceW, keyH),
            cornerRadius = keyCorner,
            style = Stroke(width = 0.75.dp.toPx())
        )
    }
}

/**
 * High-performance 3D Neumorphism Theme Card for the gallery.
 * Optimized for silky-smooth 60/120 FPS scrolling with hardware Canvas rendering and remembered objects.
 */
@Composable
fun NeumorphicThemeCard(
    theme: KeyboardTheme,
    isSelected: Boolean,
    isPreviewed: Boolean,
    isUnlocked: Boolean,
    cost: Int,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = remember { RoundedCornerShape(16.dp) }

    val surfaceBrush = remember(isDarkMode) {
        if (isDarkMode) {
            Brush.linearGradient(listOf(Color(0xFF22242E), Color(0xFF16171D)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFAF8F5), Color(0xFFEBE7DF)))
        }
    }

    val rimBorder = remember(isSelected, isPreviewed, isDarkMode) {
        BorderStroke(
            width = if (isSelected) 2.dp else if (isPreviewed) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF10B981)
            else if (isPreviewed) Color(0xFF6366F1)
            else if (isDarkMode) Color.White.copy(alpha = 0.10f)
            else Color.Black.copy(alpha = 0.08f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = if (isSelected) 4.dp else 1.5.dp, shape = cardShape)
            .clip(cardShape)
            .background(surfaceBrush)
            .border(rimBorder, cardShape)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Hardware-accelerated mini keyboard canvas (0 layout overhead, silky smooth 60/120 FPS)
            MiniKeyboardCanvas(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )

            // Theme Name (Clean, without tags or emojis)
            Text(
                text = theme.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF1E2024),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Status / Action Bar
            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Active",
                            color = Color(0xFF10B981),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isUnlocked) {
                        Text(
                            "Free",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "🪙 $cost",
                            color = Color(0xFFEAB308),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnlocked) Color(0xFF6366F1) else Color(0xFFEAB308),
                        modifier = Modifier.clickable { onApply() }
                    ) {
                        Text(
                            text = if (isUnlocked) "Apply" else "Unlock",
                            color = if (isUnlocked) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewKey(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    isSpecial: Boolean = false,
    isSelected: Boolean = false,
    height: Dp = 28.dp,
    onClick: () -> Unit
) {
    val cornerRadius = (theme.keyCornerRadius * 0.8f).coerceAtLeast(3f).dp
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
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(13.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 11.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.NEUMORPHISM_LIGHT -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .border(0.8.dp, Color.White.copy(alpha = 0.8f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(13.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 11.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.NEUMORPHISM_DARK -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .border(0.8.dp, Color.White.copy(alpha = 0.15f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(13.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 11.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.TACTILE_3D -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .border(1.dp, theme.accentColor.copy(alpha = 0.6f), shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(13.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 11.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        ThemeStyle.CLAYMORPHISM -> {
            val keyBg = if (isSelected) theme.primaryColor else if (isSpecial) theme.keySpecialColor else theme.keyColor
            Box(
                modifier = baseModifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(keyBg)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(13.dp))
                } else if (text != null) {
                    Text(text, color = theme.textColor, fontSize = 11.sp, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        else -> {
            val keyBg = if (isSelected) theme.primaryColor
            else if (isSpecial) theme.keySpecialColor
            else theme.keyColor.copy(alpha = theme.keyAlpha)

            Box(
                modifier = baseModifier
                    .clip(shape)
                    .background(keyBg)
                    .border(0.5.dp, theme.keyBorderColor, shape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(13.dp))
                } else if (text != null) {
                    Text(
                        text = text,
                        color = theme.textColor,
                        fontSize = if (text.length > 5) 9.sp else 11.sp,
                        fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
