package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CreditsSecurityManager
import com.example.data.LingoKeyPreferences
import com.example.model.KeyboardTheme
import com.example.ui.components.GoogleSignInBottomSheet
import com.example.ui.components.SignInTriggerReason
import com.example.model.ThemeStyle

// ==========================================
// MODELS & DATA DEFINITIONS
// ==========================================

enum class StoreTab(val title: String, val icon: String) {
    THEMES("THEMES", "🎨"),
    WALLPAPERS("WALLPAPERS", "🖼️"),
    KEY_STYLES("KEY STYLES", "🔘")
}

data class StoreThemeItem(
    val id: String,
    val title: String,
    val category: String, // "3D", "Neumorphism", "Glass", "Neon", "Minimal", "Nature", "AMOLED", "Popular"
    val creditPrice: Int, // 0 = Free
    val isPro: Boolean = false,
    val themeRef: KeyboardTheme,
    val description: String
)

data class StoreWallpaperItem(
    val id: String,
    val title: String,
    val category: String, // "Abstract", "Aurora", "Nature", "Space", "Gradient", "Dark", "Minimal"
    val creditPrice: Int, // 0 = Free
    val isPro: Boolean = false,
    val colors: List<Color>,
    val accentColor: Color,
    val emoji: String,
    val subtitle: String
)

data class StoreKeyStyleItem(
    val id: String,
    val title: String,
    val description: String,
    val creditPrice: Int, // 0 = Free
    val isPro: Boolean = false,
    val cornerRadius: Float,
    val hasBorder: Boolean,
    val isTactile3D: Boolean = false,
    val isGlass: Boolean = false,
    val isNeumorphic: Boolean = false,
    val isPill: Boolean = false,
    val isEdgeGlow: Boolean = false,
    val isMetallic: Boolean = false
)

// ==========================================
// MAIN STORE SCREEN COMPOSABLE
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit,
    onNavigateToSpinAndWin: () -> Unit,
    onNavigateToPro: () -> Unit,
    onNavigateToThemes: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val currentTheme by preferences.currentTheme.collectAsState()
    val unlockedThemeIds by preferences.unlockedThemeIds.collectAsState()
    val showKeyBorders by preferences.showKeyBorders.collectAsState()
    val keyHeightDp by preferences.keyHeightDp.collectAsState()

    val creditsSecurityManager = remember { CreditsSecurityManager.getInstance(context) }
    val aiCredits by creditsSecurityManager.aiCredits.collectAsState()

    var selectedTab by remember { mutableStateOf(StoreTab.THEMES) }
    var selectedThemeCategory by remember { mutableStateOf("All") }
    var selectedWallpaperCategory by remember { mutableStateOf("All") }

    // Dialog States
    var showCustomThemeBuilder by remember { mutableStateOf(false) }
    var previewingTheme by remember { mutableStateOf<KeyboardTheme?>(null) }
    var showUnlockThemeDialog by remember { mutableStateOf<StoreThemeItem?>(null) }
    var showUnlockWallpaperDialog by remember { mutableStateOf<StoreWallpaperItem?>(null) }
    var showUnlockKeyStyleDialog by remember { mutableStateOf<StoreKeyStyleItem?>(null) }
    var showInsufficientCreditsDialog by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showVipUpgradeDialog by remember { mutableStateOf<String?>(null) }

    // Google Account & Cloud Sync State
    val isSignedIn by preferences.isSignedIn.collectAsState()
    val userEmail by preferences.userEmail.collectAsState()
    val userDisplayName by preferences.userDisplayName.collectAsState()
    var showSignInSheet by remember { mutableStateOf(false) }
    var signInReason by remember { mutableStateOf(SignInTriggerReason.GENERAL) }

    // Global MKeyboard Identity Colors (Dark mist-green & Slate)
    val bgColor = if (isDarkMode) Color(0xFF0D0F14) else Color(0xFFF6F8FA)
    val cardBg = if (isDarkMode) Color(0xFF161922) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldAccent = Color(0xFF10B981)
    val goldColor = Color(0xFFF59E0B)
    val borderCol = if (isDarkMode) Color(0xFF222736) else Color(0xFFE2E8F0)

    // Catalog of Themes matching user specifications
    val themesCatalog = remember {
        listOf(
            StoreThemeItem(
                id = "semi_transparent_classic",
                title = "Signature Dynamic",
                category = "Glass",
                creditPrice = 0,
                themeRef = KeyboardTheme.SEMI_TRANSPARENT_CLASSIC,
                description = "Frosted glass translucent keys with fluid mist glow"
            ),
            StoreThemeItem(
                id = "tactile_3d_dark",
                title = "3D Gloss",
                category = "3D",
                creditPrice = 35,
                themeRef = KeyboardTheme.TACTILE_3D_DARK,
                description = "Sculpted tactile buttons with physical micro-depth & top gloss"
            ),
            StoreThemeItem(
                id = "neumorphism_dark",
                title = "Soft Neumorphism",
                category = "Neumorphism",
                creditPrice = 0,
                themeRef = KeyboardTheme.NEUMORPHISM_DARK,
                description = "Dark Soft UI extruded depth with ambient dual illumination"
            ),
            StoreThemeItem(
                id = "crystal",
                title = "Crystal Glass",
                category = "Glass",
                creditPrice = 0,
                themeRef = KeyboardTheme.CRYSTAL,
                description = "Refractive frosted crystal sheen with sharp light borders"
            ),
            StoreThemeItem(
                id = "neon_cyber",
                title = "Neon Dynamic",
                category = "Neon",
                creditPrice = 50,
                themeRef = KeyboardTheme.NEON_CYBER,
                description = "High-energy cyberpunk RGB illuminated edge glow"
            ),
            StoreThemeItem(
                id = "sunset_aurora",
                title = "Aurora Flow",
                category = "Nature",
                creditPrice = 0,
                themeRef = KeyboardTheme.SUNSET_AURORA,
                description = "Northern lights flowing dusky magenta and turquoise"
            ),
            StoreThemeItem(
                id = "carbon_gold",
                title = "Liquid Metal",
                category = "3D",
                creditPrice = 0,
                isPro = true,
                themeRef = KeyboardTheme.CARBON_GOLD,
                description = "Polished titanium and molten gold luxury metallic finish"
            ),
            StoreThemeItem(
                id = "forest_green",
                title = "Nature Flow",
                category = "Nature",
                creditPrice = 0,
                themeRef = KeyboardTheme.FOREST_GREEN,
                description = "Botanical dark mist-green organic forest theme"
            ),
            StoreThemeItem(
                id = "light_minimal",
                title = "Ultra Minimal",
                category = "Minimal",
                creditPrice = 0,
                themeRef = KeyboardTheme.LIGHT_MINIMAL,
                description = "Clean zero-distraction layout with micro-stroke typography"
            ),
            StoreThemeItem(
                id = "amoled_black",
                title = "AMOLED Black",
                category = "AMOLED",
                creditPrice = 0,
                themeRef = KeyboardTheme.AMOLED_BLACK,
                description = "True pitch-black 0% battery saver OLED display"
            ),
            StoreThemeItem(
                id = "spain_2",
                title = "Spain Gold Crest",
                category = "Popular",
                creditPrice = 0,
                themeRef = KeyboardTheme.SPAIN_2,
                description = "World champion championship gold & royal crimson"
            ),
            StoreThemeItem(
                id = "argentina_10",
                title = "Argentina 3-Star",
                category = "Popular",
                creditPrice = 40,
                themeRef = KeyboardTheme.ARGENTINA_10,
                description = "Sky blue & golden world cup stars tribute"
            ),
            StoreThemeItem(
                id = "cosmic",
                title = "Cosmic Nebula",
                category = "Neon",
                creditPrice = 0,
                isPro = true,
                themeRef = KeyboardTheme.COSMIC,
                description = "Deep interstellar violet nebula with floating stars"
            ),
            StoreThemeItem(
                id = "sunset",
                title = "Sunset Horizon",
                category = "Nature",
                creditPrice = 0,
                themeRef = KeyboardTheme.SUNSET,
                description = "Dusk horizon with warm twilight pink and amber keys"
            ),
            StoreThemeItem(
                id = "neobrutalism_pop",
                title = "Neobrutalism Pop",
                category = "Minimal",
                creditPrice = 0,
                themeRef = KeyboardTheme.NEOBRUTALISM_POP,
                description = "High-contrast bold pop typography & hard offset borders"
            )
        )
    }

    // Catalog of Wallpapers
    val wallpapersCatalog = remember {
        listOf(
            StoreWallpaperItem(
                id = "wp_aurora",
                title = "Aurora Borealis",
                category = "Aurora",
                creditPrice = 0,
                colors = listOf(Color(0xFF042F2E), Color(0xFF0D9488), Color(0xFF1E1B4B), Color(0xFF0F172A)),
                accentColor = Color(0xFF2DD4BF),
                emoji = "🌌",
                subtitle = "Emerald green and indigo polar lights"
            ),
            StoreWallpaperItem(
                id = "wp_cyberwave",
                title = "Abstract Cyberwave",
                category = "Abstract",
                creditPrice = 30,
                colors = listOf(Color(0xFF1E0836), Color(0xFF581C87), Color(0xFF9333EA), Color(0xFF0F0C20)),
                accentColor = Color(0xFFC084FC),
                emoji = "⚡",
                subtitle = "Electric neon purple synth waves"
            ),
            StoreWallpaperItem(
                id = "wp_mystic_forest",
                title = "Mystic Forest",
                category = "Nature",
                creditPrice = 0,
                colors = listOf(Color(0xFF052E16), Color(0xFF14532D), Color(0xFF064E3B), Color(0xFF022C22)),
                accentColor = Color(0xFF10B981),
                emoji = "🌿",
                subtitle = "Deep botanical mist-green forest"
            ),
            StoreWallpaperItem(
                id = "wp_nebula_space",
                title = "Deep Space Nebula",
                category = "Space",
                creditPrice = 0,
                isPro = true,
                colors = listOf(Color(0xFF090A1A), Color(0xFF1E1B4B), Color(0xFF4C1D95), Color(0xFF000000)),
                accentColor = Color(0xFF818CF8),
                emoji = "🪐",
                subtitle = "Cosmic dust and ultraviolet starlight"
            ),
            StoreWallpaperItem(
                id = "wp_sunset_dunes",
                title = "Sunset Dunes",
                category = "Gradient",
                creditPrice = 0,
                colors = listOf(Color(0xFF431407), Color(0xFF9A3412), Color(0xFFEA580C), Color(0xFF1C1917)),
                accentColor = Color(0xFFF97316),
                emoji = "🌅",
                subtitle = "Warm terracotta dusk gradient"
            ),
            StoreWallpaperItem(
                id = "wp_liquid_chrome",
                title = "Liquid Chrome",
                category = "Abstract",
                creditPrice = 40,
                colors = listOf(Color(0xFF1E293B), Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF0F172A)),
                accentColor = Color(0xFFE2E8F0),
                emoji = "🪙",
                subtitle = "Silver platinum and titanium sheen"
            ),
            StoreWallpaperItem(
                id = "wp_minimal_slate",
                title = "Minimal Slate",
                category = "Minimal",
                creditPrice = 0,
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
                accentColor = Color(0xFF38BDF8),
                emoji = "⚪",
                subtitle = "Clean architectural dark graphite"
            ),
            StoreWallpaperItem(
                id = "wp_golden_luxury",
                title = "Golden Obsidian",
                category = "Premium",
                creditPrice = 0,
                isPro = true,
                colors = listOf(Color(0xFF0A0A0A), Color(0xFF262010), Color(0xFF785918), Color(0xFF050505)),
                accentColor = Color(0xFFF59E0B),
                emoji = "👑",
                subtitle = "Black marble with molten gold veins"
            ),
            StoreWallpaperItem(
                id = "wp_abyssal_ocean",
                title = "Abyssal Ocean",
                category = "Nature",
                creditPrice = 0,
                colors = listOf(Color(0xFF021B2B), Color(0xFF033959), Color(0xFF086788), Color(0xFF011420)),
                accentColor = Color(0xFF38BDF8),
                emoji = "🌊",
                subtitle = "Deep marine trench with bio-blue aura"
            ),
            StoreWallpaperItem(
                id = "wp_synthwave_grid",
                title = "Neon Horizon",
                category = "Dark",
                creditPrice = 35,
                colors = listOf(Color(0xFF1A0B2E), Color(0xFF4A154B), Color(0xFF2E0854), Color(0xFF0D0221)),
                accentColor = Color(0xFFEC4899),
                emoji = "🌆",
                subtitle = "80s retrowave synth horizon"
            )
        )
    }

    // Catalog of Key Styles
    val keyStylesCatalog = remember {
        listOf(
            StoreKeyStyleItem(
                id = "key_flat",
                title = "Flat",
                description = "Clean minimalist flat surface with crisp zero-shadow clarity",
                creditPrice = 0,
                cornerRadius = 6f,
                hasBorder = false
            ),
            StoreKeyStyleItem(
                id = "key_3d",
                title = "3D",
                description = "Tactile sculpted keys with mechanical depth extrusion & bottom bevel",
                creditPrice = 30,
                cornerRadius = 8f,
                hasBorder = true,
                isTactile3D = true
            ),
            StoreKeyStyleItem(
                id = "key_glossy",
                title = "Glossy",
                description = "High-sheen reflective glass surface with vibrant top-edge highlight",
                creditPrice = 25,
                cornerRadius = 8f,
                hasBorder = true,
                isTactile3D = true
            ),
            StoreKeyStyleItem(
                id = "key_glass",
                title = "Glass",
                description = "Frosted translucent glassmorphism with delicate refraction border",
                creditPrice = 0,
                cornerRadius = 8f,
                hasBorder = true,
                isGlass = true
            ),
            StoreKeyStyleItem(
                id = "key_crystal",
                title = "Crystal",
                description = "Sharp diamond-cut translucent keycaps with icy clear borders",
                creditPrice = 0,
                cornerRadius = 4f,
                hasBorder = true,
                isGlass = true
            ),
            StoreKeyStyleItem(
                id = "key_soft_neumorphic",
                title = "Soft Neumorphic",
                description = "Soft UI extruded embossed keys with subtle ambient dual lighting",
                creditPrice = 0,
                cornerRadius = 10f,
                hasBorder = false,
                isNeumorphic = true
            ),
            StoreKeyStyleItem(
                id = "key_rounded",
                title = "Rounded",
                description = "Ergonomic curved keys with generous smooth 12dp rounded corners",
                creditPrice = 0,
                cornerRadius = 12f,
                hasBorder = true
            ),
            StoreKeyStyleItem(
                id = "key_pill",
                title = "Pill",
                description = "Modern capsule pill-shaped keys designed for tactile precision",
                creditPrice = 0,
                cornerRadius = 22f,
                hasBorder = true,
                isPill = true
            ),
            StoreKeyStyleItem(
                id = "key_metallic",
                title = "Metallic",
                description = "Brushed chrome and titanium sheen with high-gloss perimeter line",
                creditPrice = 0,
                isPro = true,
                cornerRadius = 6f,
                hasBorder = true,
                isMetallic = true
            ),
            StoreKeyStyleItem(
                id = "key_edge_glow",
                title = "Edge Glow",
                description = "Illuminated neon perimeter contours that glow on dark backgrounds",
                creditPrice = 40,
                cornerRadius = 8f,
                hasBorder = true,
                isEdgeGlow = true
            ),
            StoreKeyStyleItem(
                id = "key_minimal",
                title = "Minimal",
                description = "Borderless floating typography with zero visual friction",
                creditPrice = 0,
                cornerRadius = 4f,
                hasBorder = false
            )
        )
    }

    // Helper: Checks if an item is unlocked
    fun checkIsUnlocked(id: String, creditPrice: Int, isPro: Boolean): Boolean {
        if (creditPrice == 0 && !isPro) return true
        if (isPremiumUser) return true
        return unlockedThemeIds.contains(id)
    }

    // Helper: Apply or purchase theme
    fun handleThemeAction(item: StoreThemeItem) {
        val isUnlocked = checkIsUnlocked(item.id, item.creditPrice, item.isPro)
        if (isUnlocked) {
            preferences.setTheme(item.themeRef)
            Toast.makeText(context, "Applied ${item.title} to keyboard!", Toast.LENGTH_SHORT).show()
        } else if (item.isPro) {
            showVipUpgradeDialog = item.title
        } else {
            if (aiCredits >= item.creditPrice) {
                showUnlockThemeDialog = item
            } else {
                showInsufficientCreditsDialog = Pair(item.title, item.creditPrice)
            }
        }
    }

    // Helper: Apply or purchase wallpaper
    fun handleWallpaperAction(wp: StoreWallpaperItem) {
        val isUnlocked = checkIsUnlocked(wp.id, wp.creditPrice, wp.isPro)
        if (isUnlocked) {
            preferences.setCustomTheme(
                name = wp.title,
                bgGradient = wp.colors,
                keyColor = Color.White.copy(alpha = 0.22f),
                textColor = Color.White,
                accentColor = wp.accentColor,
                keyAlpha = 0.85f
            )
            Toast.makeText(context, "Applied ${wp.title} wallpaper to keyboard!", Toast.LENGTH_SHORT).show()
        } else if (wp.isPro) {
            showVipUpgradeDialog = wp.title
        } else {
            if (aiCredits >= wp.creditPrice) {
                showUnlockWallpaperDialog = wp
            } else {
                showInsufficientCreditsDialog = Pair(wp.title, wp.creditPrice)
            }
        }
    }

    // Helper: Apply or purchase key style
    fun handleKeyStyleAction(style: StoreKeyStyleItem) {
        val isUnlocked = checkIsUnlocked(style.id, style.creditPrice, style.isPro)
        if (isUnlocked) {
            preferences.setShowKeyBorders(style.hasBorder)
            preferences.keyRoundnessDp.value = style.cornerRadius.toInt()
            // Modify custom theme with key style characteristics
            val base = currentTheme
            preferences.setCustomTheme(
                name = "${style.title} Style",
                bgGradient = base.backgroundGradient ?: listOf(base.backgroundColor, base.surfaceColor),
                keyColor = if (style.isGlass) Color.White.copy(alpha = 0.22f) else base.keyColor,
                textColor = base.textColor,
                accentColor = base.accentColor,
                keyAlpha = if (style.isGlass) 0.85f else 1.0f
            )
            Toast.makeText(context, "Applied ${style.title} key style!", Toast.LENGTH_SHORT).show()
        } else if (style.isPro) {
            showVipUpgradeDialog = style.title
        } else {
            if (aiCredits >= style.creditPrice) {
                showUnlockKeyStyleDialog = style
            } else {
                showInsufficientCreditsDialog = Pair(style.title, style.creditPrice)
            }
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Theme Store",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Design & Customize Your Keyboard",
                            color = textMuted,
                            fontSize = 11.5.sp
                        )
                    }
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
                    // Credits Chip (Clickable to open Spin & Win)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF1E222D) else Color(0xFFE2E8F0),
                        border = BorderStroke(1.dp, emeraldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { onNavigateToSpinAndWin() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🪙", fontSize = 12.sp)
                            Text(
                                "$aiCredits",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = emeraldAccent
                            )
                        }
                    }

                    // VIP Status Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPremiumUser) emeraldAccent.copy(alpha = 0.15f) else goldColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isPremiumUser) emeraldAccent else goldColor),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                Toast.makeText(context, "👑 7-Day Free Trial available on VIP plans!", Toast.LENGTH_SHORT).show()
                                onNavigateToPro()
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👑", fontSize = 11.sp)
                            Text(
                                if (isPremiumUser) "VIP ACTIVE" else "VIP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPremiumUser) emeraldAccent else goldColor
                            )
                        }
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
        ) {
            // Google Account Profile & Cloud Credits Banner
            if (isSignedIn) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkMode) Color(0xFF131D18) else Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, emeraldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(emeraldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    userDisplayName.firstOrNull()?.uppercase() ?: "G",
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        userDisplayName.ifEmpty { "Google Account" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = emeraldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    userEmail.ifEmpty { "Cloud Sync Active" },
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = emeraldAccent.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = emeraldAccent, modifier = Modifier.size(14.dp))
                                Text("Synced", color = emeraldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkMode) Color(0xFF191D2A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2A3147) else Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clickable {
                            signInReason = SignInTriggerReason.GENERAL
                            showSignInSheet = true
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkMode) Color(0xFF2A3147) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = textMuted, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(
                                    "Sign in with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = textColor
                                )
                                Text(
                                    "Save your credits & Pro status across all your devices",
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkMode) Color(0xFF3B82F6) else Color(0xFF2563EB)
                        ) {
                            Text(
                                "SIGN IN",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // FIXED TOP SEGMENTED TABS (THEMES | WALLPAPERS | KEY STYLES)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                color = if (isDarkMode) Color(0xFF161922) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StoreTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val tabBg by animateColorAsState(
                            targetValue = if (isSelected) emeraldAccent else Color.Transparent,
                            animationSpec = tween(220)
                        )
                        val tabTextCol by animateColorAsState(
                            targetValue = if (isSelected) Color.White else textMuted,
                            animationSpec = tween(220)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tabBg,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable { selectedTab = tab }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(tab.icon, fontSize = 12.sp)
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = tabTextCol
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // PROMINENT "+ CREATE YOUR STYLE" BANNER
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clickable { showCustomThemeBuilder = true },
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) Color(0xFF13201D) else Color(0xFFECFDF5),
                border = BorderStroke(1.2.dp, emeraldAccent.copy(alpha = 0.7f)),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(emeraldAccent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = emeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "CUSTOM THEME BUILDER",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                color = textColor
                            )
                            Text(
                                "Mix background, key style, shape & colors",
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = emeraldAccent
                    ) {
                        Text(
                            "+ CREATE",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // ==========================================
            // SCROLLABLE CONTENT FOR ACTIVE TAB
            // ==========================================
            when (selectedTab) {
                // TAB 1: THEMES MARKETPLACE
                StoreTab.THEMES -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Categories Row
                        val themeCategories = listOf("All", "3D", "Neumorphism", "Glass", "Neon", "Minimal", "Nature", "AMOLED", "Premium")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            themeCategories.forEach { cat ->
                                CategoryChip(
                                    title = cat,
                                    isSelected = selectedThemeCategory == cat,
                                    isDarkMode = isDarkMode,
                                    onClick = { selectedThemeCategory = cat }
                                )
                            }
                        }

                        // Filtered List
                        val filteredThemes = themesCatalog.filter { item ->
                            when (selectedThemeCategory) {
                                "All" -> true
                                "Premium" -> item.isPro
                                else -> item.category.equals(selectedThemeCategory, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredThemes, key = { it.id }) { item ->
                                val isUnlocked = checkIsUnlocked(item.id, item.creditPrice, item.isPro)
                                val isApplied = currentTheme.id == item.themeRef.id

                                ThemeCard(
                                    item = item,
                                    isUnlocked = isUnlocked,
                                    isApplied = isApplied,
                                    isDarkMode = isDarkMode,
                                    onPreview = { previewingTheme = item.themeRef },
                                    onApply = { handleThemeAction(item) }
                                )
                            }

                            // Premium UX Section
                            item {
                                PremiumStoreUxSection(
                                    isDarkMode = isDarkMode,
                                    isPremiumUser = isPremiumUser,
                                    onExplorePremium = {
                                        Toast.makeText(context, "👑 7-Day Free Trial available on annual subscription!", Toast.LENGTH_SHORT).show()
                                        onNavigateToPro()
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                // TAB 2: WALLPAPERS MARKETPLACE
                StoreTab.WALLPAPERS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Categories Row
                        val wallpaperCategories = listOf("All", "Abstract", "Aurora", "Nature", "Space", "Gradient", "Dark", "Minimal", "Premium")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            wallpaperCategories.forEach { cat ->
                                CategoryChip(
                                    title = cat,
                                    isSelected = selectedWallpaperCategory == cat,
                                    isDarkMode = isDarkMode,
                                    onClick = { selectedWallpaperCategory = cat }
                                )
                            }
                        }

                        // Filtered List
                        val filteredWallpapers = wallpapersCatalog.filter { wp ->
                            when (selectedWallpaperCategory) {
                                "All" -> true
                                "Premium" -> wp.isPro
                                else -> wp.category.equals(selectedWallpaperCategory, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredWallpapers, key = { it.id }) { wp ->
                                val isUnlocked = checkIsUnlocked(wp.id, wp.creditPrice, wp.isPro)
                                val isApplied = currentTheme.name == wp.title

                                WallpaperCard(
                                    wallpaper = wp,
                                    isUnlocked = isUnlocked,
                                    isApplied = isApplied,
                                    isDarkMode = isDarkMode,
                                    onPreview = {
                                        previewingTheme = KeyboardTheme(
                                            id = wp.id,
                                            name = wp.title,
                                            isDark = true,
                                            primaryColor = wp.accentColor,
                                            backgroundColor = wp.colors.firstOrNull() ?: Color(0xFF0F172A),
                                            surfaceColor = wp.colors.getOrNull(1) ?: Color(0xFF1E293B),
                                            keyColor = Color.White.copy(alpha = 0.22f),
                                            keySpecialColor = Color.White.copy(alpha = 0.32f),
                                            keyPressedColor = wp.accentColor,
                                            textColor = Color.White,
                                            textSecondaryColor = Color.White.copy(alpha = 0.75f),
                                            accentColor = wp.accentColor,
                                            keyBorderColor = Color.White.copy(alpha = 0.3f),
                                            backgroundGradient = wp.colors,
                                            keyAlpha = 0.85f,
                                            backdropType = "custom"
                                        )
                                    },
                                    onApply = { handleWallpaperAction(wp) }
                                )
                            }

                            // Premium UX Section
                            item {
                                PremiumStoreUxSection(
                                    isDarkMode = isDarkMode,
                                    isPremiumUser = isPremiumUser,
                                    onExplorePremium = {
                                        Toast.makeText(context, "👑 7-Day Free Trial available on annual subscription!", Toast.LENGTH_SHORT).show()
                                        onNavigateToPro()
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                // TAB 3: KEY STYLES MARKETPLACE
                StoreTab.KEY_STYLES -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(keyStylesCatalog, key = { it.id }) { keyStyle ->
                            val isUnlocked = checkIsUnlocked(keyStyle.id, keyStyle.creditPrice, keyStyle.isPro)
                            val isApplied = preferences.showKeyBorders.value == keyStyle.hasBorder &&
                                    preferences.keyRoundnessDp.value == keyStyle.cornerRadius.toInt()

                            KeyStyleCard(
                                keyStyle = keyStyle,
                                isUnlocked = isUnlocked,
                                isApplied = isApplied,
                                isDarkMode = isDarkMode,
                                currentTheme = currentTheme,
                                onPreview = {
                                    previewingTheme = currentTheme.copy(
                                        keyCornerRadius = keyStyle.cornerRadius,
                                        keyBorderColor = if (keyStyle.hasBorder) currentTheme.accentColor.copy(alpha = 0.4f) else Color.Transparent
                                    )
                                },
                                onApply = { handleKeyStyleAction(keyStyle) }
                            )
                        }

                        // Premium UX Section
                        item {
                            PremiumStoreUxSection(
                                isDarkMode = isDarkMode,
                                isPremiumUser = isPremiumUser,
                                onExplorePremium = {
                                    Toast.makeText(context, "👑 7-Day Free Trial available on annual subscription!", Toast.LENGTH_SHORT).show()
                                    onNavigateToPro()
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MODAL DIALOGS
    // ==========================================

    // 1. Custom Theme Builder Modal
    if (showCustomThemeBuilder) {
        CustomThemeBuilderDialog(
            isDarkMode = isDarkMode,
            preferences = preferences,
            currentTheme = currentTheme,
            onDismiss = { showCustomThemeBuilder = false },
            onSave = { name, gradient, keyColor, textColor, accentColor, keyRoundness, keyHeight, showBorders ->
                preferences.setCustomTheme(
                    name = name,
                    bgGradient = gradient,
                    keyColor = keyColor,
                    textColor = textColor,
                    accentColor = accentColor,
                    keyAlpha = 0.9f
                )
                preferences.keyRoundnessDp.value = keyRoundness
                preferences.setKeyHeightDp(keyHeight)
                preferences.setShowKeyBorders(showBorders)
                showCustomThemeBuilder = false
                Toast.makeText(context, "🎨 Saved & applied '$name' to keyboard!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // 2. Full Realistic Keyboard Preview Dialog
    previewingTheme?.let { themeToPreview ->
        KeyboardFullPreviewDialog(
            theme = themeToPreview,
            isDarkMode = isDarkMode,
            onDismiss = { previewingTheme = null },
            onApply = {
                preferences.setTheme(themeToPreview)
                previewingTheme = null
                Toast.makeText(context, "Applied ${themeToPreview.name}!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. Unlock with Credits Dialog (Theme)
    showUnlockThemeDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showUnlockThemeDialog = null },
            title = { Text("Unlock ${item.title}?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Unlock this keyboard theme permanently for ${item.creditPrice} credits.")
                    Text("Your current balance: $aiCredits 🪙", fontWeight = FontWeight.Bold, color = emeraldAccent)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = preferences.unlockThemeWithCredits(item.id, item.creditPrice)
                        if (success) {
                            preferences.setTheme(item.themeRef)
                            showUnlockThemeDialog = null
                            Toast.makeText(context, "Unlocked and applied ${item.title}!", Toast.LENGTH_SHORT).show()
                        } else {
                            showUnlockThemeDialog = null
                            showInsufficientCreditsDialog = Pair(item.title, item.creditPrice)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent)
                ) {
                    Text("Unlock for ${item.creditPrice} 🪙", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockThemeDialog = null }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    // 4. Unlock with Credits Dialog (Wallpaper)
    showUnlockWallpaperDialog?.let { wp ->
        AlertDialog(
            onDismissRequest = { showUnlockWallpaperDialog = null },
            title = { Text("Unlock ${wp.title} Wallpaper?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Unlock this custom keyboard wallpaper for ${wp.creditPrice} credits.")
                    Text("Your current balance: $aiCredits 🪙", fontWeight = FontWeight.Bold, color = emeraldAccent)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = preferences.unlockThemeWithCredits(wp.id, wp.creditPrice)
                        if (success) {
                            preferences.setCustomTheme(
                                name = wp.title,
                                bgGradient = wp.colors,
                                keyColor = Color.White.copy(alpha = 0.22f),
                                textColor = Color.White,
                                accentColor = wp.accentColor,
                                keyAlpha = 0.85f
                            )
                            showUnlockWallpaperDialog = null
                            Toast.makeText(context, "Unlocked and applied ${wp.title} wallpaper!", Toast.LENGTH_SHORT).show()
                        } else {
                            showUnlockWallpaperDialog = null
                            showInsufficientCreditsDialog = Pair(wp.title, wp.creditPrice)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent)
                ) {
                    Text("Unlock for ${wp.creditPrice} 🪙", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockWallpaperDialog = null }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    // 5. Unlock with Credits Dialog (Key Style)
    showUnlockKeyStyleDialog?.let { ks ->
        AlertDialog(
            onDismissRequest = { showUnlockKeyStyleDialog = null },
            title = { Text("Unlock ${ks.title} Key Style?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Unlock this 3D keycap design for ${ks.creditPrice} credits.")
                    Text("Your current balance: $aiCredits 🪙", fontWeight = FontWeight.Bold, color = emeraldAccent)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = preferences.unlockThemeWithCredits(ks.id, ks.creditPrice)
                        if (success) {
                            preferences.setShowKeyBorders(ks.hasBorder)
                            preferences.keyRoundnessDp.value = ks.cornerRadius.toInt()
                            showUnlockKeyStyleDialog = null
                            Toast.makeText(context, "Unlocked and applied ${ks.title} style!", Toast.LENGTH_SHORT).show()
                        } else {
                            showUnlockKeyStyleDialog = null
                            showInsufficientCreditsDialog = Pair(ks.title, ks.creditPrice)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent)
                ) {
                    Text("Unlock for ${ks.creditPrice} 🪙", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockKeyStyleDialog = null }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    // 6. Insufficient Credits Dialog
    showInsufficientCreditsDialog?.let { (itemTitle, cost) ->
        AlertDialog(
            onDismissRequest = { showInsufficientCreditsDialog = null },
            title = { Text("More Credits Needed", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("'$itemTitle' requires $cost credits. You currently have $aiCredits credits.")
                    Text("Top up instantly with a credits pack or spin the wheel for free daily credits!", color = textMuted, fontSize = 12.5.sp)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showInsufficientCreditsDialog = null
                            onNavigateToSpinAndWin()
                        }
                    ) {
                        Text("Spin 🎡", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            showInsufficientCreditsDialog = null
                            onNavigateToPro()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent)
                    ) {
                        Text("Buy Credits 🪙", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsufficientCreditsDialog = null }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    // 7. VIP Upgrade Prompt Dialog
    showVipUpgradeDialog?.let { itemName ->
        AlertDialog(
            onDismissRequest = { showVipUpgradeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("👑", fontSize = 18.sp)
                    Text("VIP Exclusive Content", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("'$itemName' is an exclusive VIP item.")
                    Text("Get instant access to all 15+ VIP themes, premium wallpapers, 3D key styles, and unlimited Gemini AI Writing Assistant.", fontSize = 13.sp, color = textMuted)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = goldColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, goldColor)
                    ) {
                        Text(
                            "⚡ 7-Day Free Trial available on annual plans",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val proceedToPro = {
                            showVipUpgradeDialog = null
                            Toast.makeText(context, "👑 7-Day Free Trial available on annual subscription!", Toast.LENGTH_SHORT).show()
                            onNavigateToPro()
                        }
                        if (!isSignedIn) {
                            showVipUpgradeDialog = null
                            signInReason = SignInTriggerReason.UPGRADE_PRO
                            showSignInSheet = true
                        } else {
                            proceedToPro()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = goldColor)
                ) {
                    Text("Start 7-Day Trial 👑", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVipUpgradeDialog = null }) {
                    Text("Close", color = textMuted)
                }
            }
        )
    }

    // Just-In-Time Google Sign-In Sheet
    if (showSignInSheet) {
        GoogleSignInBottomSheet(
            preferences = preferences,
            reason = signInReason,
            onDismiss = { showSignInSheet = false },
            onSignInSuccess = {
                showSignInSheet = false
                if (signInReason == SignInTriggerReason.UPGRADE_PRO) {
                    onNavigateToPro()
                }
            }
        )
    }
}

// ==========================================
// REUSABLE COMPONENTS
// ==========================================

/**
 * Filter Category Chip
 */
@Composable
fun CategoryChip(
    title: String,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val emeraldAccent = Color(0xFF10B981)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) emeraldAccent else if (isDarkMode) Color(0xFF1C1F2B) else Color(0xFFE2E8F0),
        border = BorderStroke(
            1.dp,
            if (isSelected) emeraldAccent else if (isDarkMode) Color(0xFF2E3346) else Color(0xFFCBD5E1)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Realistic Keyboard Preview Component
 */
@Composable
fun ThemePreview(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    keyCornerRadius: Float = theme.keyCornerRadius,
    showBorders: Boolean = true
) {
    val bgBrush = if (theme.backgroundGradient != null && theme.backgroundGradient.isNotEmpty()) {
        Brush.verticalGradient(theme.backgroundGradient)
    } else {
        Brush.verticalGradient(listOf(theme.backgroundColor, theme.surfaceColor))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Row 1: Q W E R T Y U I O P
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { letter ->
                    MiniKey(
                        letter = letter,
                        theme = theme,
                        modifier = Modifier.weight(1f),
                        cornerRadius = keyCornerRadius,
                        showBorder = showBorders
                    )
                }
            }

            // Row 2: A S D F G H J K L
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").forEach { letter ->
                    MiniKey(
                        letter = letter,
                        theme = theme,
                        modifier = Modifier.weight(1f),
                        cornerRadius = keyCornerRadius,
                        showBorder = showBorders
                    )
                }
            }

            // Row 3: Spacebar & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 123 key
                Surface(
                    shape = RoundedCornerShape(keyCornerRadius.dp),
                    color = theme.keySpecialColor,
                    border = if (showBorders) BorderStroke(0.8.dp, theme.keyBorderColor) else null,
                    modifier = Modifier
                        .weight(1.8f)
                        .height(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("?123", color = theme.textSecondaryColor, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Spacebar
                Surface(
                    shape = RoundedCornerShape(keyCornerRadius.dp),
                    color = theme.keyColor,
                    border = if (showBorders) BorderStroke(0.8.dp, theme.keyBorderColor) else null,
                    modifier = Modifier
                        .weight(5f)
                        .height(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Global MKeyboard", color = theme.textSecondaryColor, fontSize = 8.5.sp, maxLines = 1)
                    }
                }

                // Enter Key
                Surface(
                    shape = RoundedCornerShape(keyCornerRadius.dp),
                    color = theme.primaryColor,
                    modifier = Modifier
                        .weight(2f)
                        .height(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("↵", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniKey(
    letter: String,
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    cornerRadius: Float = 5f,
    showBorder: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(cornerRadius.dp),
        color = theme.keyColor,
        border = if (showBorder) BorderStroke(0.8.dp, theme.keyBorderColor) else null,
        modifier = modifier.height(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter,
                color = theme.textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Reusable Theme Card
 */
@Composable
fun ThemeCard(
    item: StoreThemeItem,
    isUnlocked: Boolean,
    isApplied: Boolean,
    isDarkMode: Boolean,
    onPreview: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) Color(0xFF161922) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderCol = if (isApplied) Color(0xFF10B981) else if (isDarkMode) Color(0xFF222736) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(if (isApplied) 1.5.dp else 1.dp, borderCol),
        shadowElevation = if (isApplied) 4.dp else 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Realistic Keyboard Preview
            ThemePreview(
                theme = item.themeRef,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPreview)
            )

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isApplied) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    "ACTIVE",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = item.description,
                        color = textMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price Badge
                CreditPrice(
                    creditPrice = item.creditPrice,
                    isPro = item.isPro,
                    isUnlocked = isUnlocked
                )
            }

            // Action Buttons Row (Preview & Apply)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PreviewButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f)
                )

                ApplyButton(
                    isApplied = isApplied,
                    isUnlocked = isUnlocked,
                    creditPrice = item.creditPrice,
                    isPro = item.isPro,
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Reusable Wallpaper Card
 */
@Composable
fun WallpaperCard(
    wallpaper: StoreWallpaperItem,
    isUnlocked: Boolean,
    isApplied: Boolean,
    isDarkMode: Boolean,
    onPreview: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) Color(0xFF161922) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderCol = if (isApplied) Color(0xFF10B981) else if (isDarkMode) Color(0xFF222736) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(if (isApplied) 1.5.dp else 1.dp, borderCol),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Large Wallpaper Visual Preview with Sample Keyboard Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(wallpaper.colors))
                    .clickable(onClick = onPreview)
                    .padding(10.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Mini Overlay Showing Readable Keys Over Wallpaper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Q", "W", "E", "R", "T", "Y").forEach { l ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(l, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(2.5f)
                            .height(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Space", color = Color.White, fontSize = 8.sp)
                        }
                    }
                }

                // Top Floating Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                ) {
                    Text(
                        "${wallpaper.emoji} ${wallpaper.category}",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallpaper.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Text(
                        text = wallpaper.subtitle,
                        color = textMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                CreditPrice(
                    creditPrice = wallpaper.creditPrice,
                    isPro = wallpaper.isPro,
                    isUnlocked = isUnlocked
                )
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PreviewButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f)
                )

                ApplyButton(
                    isApplied = isApplied,
                    isUnlocked = isUnlocked,
                    creditPrice = wallpaper.creditPrice,
                    isPro = wallpaper.isPro,
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Reusable Key Style Card
 */
@Composable
fun KeyStyleCard(
    keyStyle: StoreKeyStyleItem,
    isUnlocked: Boolean,
    isApplied: Boolean,
    isDarkMode: Boolean,
    currentTheme: KeyboardTheme,
    onPreview: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) Color(0xFF161922) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderCol = if (isApplied) Color(0xFF10B981) else if (isDarkMode) Color(0xFF222736) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(if (isApplied) 1.5.dp else 1.dp, borderCol),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Info & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = keyStyle.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = textColor
                        )

                        if (isApplied) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    "ACTIVE",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = keyStyle.description,
                        color = textMuted,
                        fontSize = 11.5.sp,
                        maxLines = 2,
                        lineHeight = 15.sp
                    )
                }

                CreditPrice(
                    creditPrice = keyStyle.creditPrice,
                    isPro = keyStyle.isPro,
                    isUnlocked = isUnlocked
                )
            }

            // Interactive Sample Keycap Previews (Showing [Q] [W] [E] in this style)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF0F1118) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Q", "W", "E").forEach { letter ->
                        Surface(
                            shape = RoundedCornerShape(keyStyle.cornerRadius.dp),
                            color = if (keyStyle.isGlass) Color.White.copy(alpha = 0.22f)
                            else if (keyStyle.isMetallic) Color(0xFF334155)
                            else currentTheme.keyColor,
                            border = if (keyStyle.hasBorder) BorderStroke(
                                1.dp,
                                if (keyStyle.isEdgeGlow) Color(0xFF10B981) else currentTheme.keyBorderColor
                            ) else null,
                            shadowElevation = if (keyStyle.isTactile3D) 4.dp else 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    letter,
                                    color = if (keyStyle.isMetallic) Color(0xFFF1F5F9) else currentTheme.textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Sample Space Key
                    Surface(
                        shape = RoundedCornerShape(keyStyle.cornerRadius.dp),
                        color = if (keyStyle.isGlass) Color.White.copy(alpha = 0.22f)
                        else currentTheme.keyColor,
                        border = if (keyStyle.hasBorder) BorderStroke(
                            1.dp,
                            if (keyStyle.isEdgeGlow) Color(0xFF10B981) else currentTheme.keyBorderColor
                        ) else null,
                        shadowElevation = if (keyStyle.isTactile3D) 4.dp else 1.dp,
                        modifier = Modifier
                            .weight(2.5f)
                            .height(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Space",
                                color = currentTheme.textSecondaryColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PreviewButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f)
                )

                ApplyButton(
                    isApplied = isApplied,
                    isUnlocked = isUnlocked,
                    creditPrice = keyStyle.creditPrice,
                    isPro = keyStyle.isPro,
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Reusable Credit / Free / VIP Price Tag
 */
@Composable
fun CreditPrice(
    creditPrice: Int,
    isPro: Boolean,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    if (isUnlocked) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF10B981).copy(alpha = 0.14f),
            modifier = modifier
        ) {
            Text(
                "UNLOCKED",
                color = Color(0xFF10B981),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    } else if (isPro) {
        PremiumBadge(modifier = modifier)
    } else if (creditPrice > 0) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF59E0B).copy(alpha = 0.16f),
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("🪙", fontSize = 10.sp)
                Text(
                    "$creditPrice",
                    color = Color(0xFFF59E0B),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF3B82F6).copy(alpha = 0.14f),
            modifier = modifier
        ) {
            Text(
                "FREE",
                color = Color(0xFF3B82F6),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Reusable Premium Badge
 */
@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    val goldColor = Color(0xFFF59E0B)
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = goldColor.copy(alpha = 0.18f),
        border = BorderStroke(0.8.dp, goldColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text("👑", fontSize = 9.sp)
            Text(
                "VIP",
                color = goldColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Reusable Preview Button
 */
@Composable
fun PreviewButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF64748B).copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
            Text("Preview", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Reusable Apply Button
 */
@Composable
fun ApplyButton(
    isApplied: Boolean,
    isUnlocked: Boolean,
    creditPrice: Int,
    isPro: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emeraldAccent = Color(0xFF10B981)
    val goldColor = Color(0xFFF59E0B)

    if (isApplied) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = emeraldAccent.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, emeraldAccent),
            modifier = modifier.height(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "Applied ✓",
                    color = emeraldAccent,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else if (isUnlocked || creditPrice == 0 && !isPro) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = modifier.height(36.dp)
        ) {
            Text("Apply", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }
    } else if (isPro) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = goldColor),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = modifier.height(36.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("👑", fontSize = 11.sp)
                Text("Unlock VIP", color = Color.Black, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = goldColor),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = modifier.height(36.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🪙", fontSize = 10.sp)
                Text("Unlock ($creditPrice)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Subtle Premium Store Section
 */
@Composable
fun PremiumStoreUxSection(
    isDarkMode: Boolean,
    isPremiumUser: Boolean,
    onExplorePremium: () -> Unit
) {
    val goldColor = Color(0xFFF59E0B)
    val cardBg = if (isDarkMode) Color(0xFF171510) else Color(0xFFFEFDF7)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.2.dp, goldColor.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👑", fontSize = 20.sp)
                    Column {
                        Text(
                            "Unlock the Full MKeyboard Experience",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                        )
                        Text(
                            "Everything unlimited • No daily coin caps",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = goldColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        if (isPremiumUser) "ACTIVE" else "VIP STORE",
                        color = goldColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Benefits mini pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("15+ VIP Themes", "HD Wallpapers", "3D Key Styles", "Gemini AI").forEach { perk ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMode) Color(0xFF262114) else Color(0xFFFFFBEB),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = perk,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = goldColor,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Button
            Button(
                onClick = onExplorePremium,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Explore Premium (7-Day Trial)", color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

// ==========================================
// CUSTOM THEME BUILDER MODAL
// ==========================================

@Composable
fun CustomThemeBuilderDialog(
    isDarkMode: Boolean,
    preferences: LingoKeyPreferences,
    currentTheme: KeyboardTheme,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        gradient: List<Color>,
        keyColor: Color,
        textColor: Color,
        accentColor: Color,
        keyRoundness: Int,
        keyHeight: Int,
        showBorders: Boolean
    ) -> Unit
) {
    var themeName by remember { mutableStateOf("My Custom Style") }

    // Wallpapers / Gradients presets
    val gradientPresets = remember {
        listOf(
            "Mist Green" to listOf(Color(0xFF042F2E), Color(0xFF0D9488), Color(0xFF0F172A)),
            "Deep Obsidian" to listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0A0F1D)),
            "Cosmic Violet" to listOf(Color(0xFF1E0836), Color(0xFF581C87), Color(0xFF110726)),
            "Sunset Horizon" to listOf(Color(0xFF431407), Color(0xFF9A3412), Color(0xFF1C1917)),
            "Midnight Ocean" to listOf(Color(0xFF021B2B), Color(0xFF033959), Color(0xFF011420)),
            "Pure AMOLED" to listOf(Color(0xFF000000), Color(0xFF000000))
        )
    }
    var selectedGradientIndex by remember { mutableStateOf(0) }

    // Accent Colors
    val accentColors = remember {
        listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF6366F1), // Indigo
            Color(0xFFA855F7), // Purple
            Color(0xFF06B6D4), // Cyan
            Color(0xFFF59E0B), // Gold
            Color(0xFFF43F5E), // Rose
            Color(0xFF84CC16)  // Lime
        )
    }
    var selectedAccentColor by remember { mutableStateOf(accentColors[0]) }

    // Key Style
    val keyStyleOptions = remember {
        listOf(
            Triple("Translucent Glass", Color.White.copy(alpha = 0.22f), Color.White),
            Triple("Solid Dark", Color(0xFF1E293B), Color.White),
            Triple("Solid Light", Color(0xFFF1F5F9), Color(0xFF0F172A)),
            Triple("Soft Tint", Color(0xFF10B981).copy(alpha = 0.25f), Color.White)
        )
    }
    var selectedKeyStyleIndex by remember { mutableStateOf(0) }

    // Key Roundness
    var keyRoundness by remember { mutableStateOf(8) }
    // Key Height
    var keyHeight by remember { mutableStateOf(48) }
    // Key Borders
    var showBorders by remember { mutableStateOf(true) }

    val activeGradient = gradientPresets[selectedGradientIndex].second
    val activeKeyColor = keyStyleOptions[selectedKeyStyleIndex].second
    val activeTextColor = keyStyleOptions[selectedKeyStyleIndex].third

    // Live Preview Theme
    val livePreviewTheme = remember(selectedGradientIndex, selectedKeyStyleIndex, selectedAccentColor, keyRoundness, showBorders) {
        KeyboardTheme(
            id = "preview_custom",
            name = themeName,
            isDark = true,
            primaryColor = selectedAccentColor,
            backgroundColor = activeGradient.first(),
            surfaceColor = activeGradient.getOrElse(1) { activeGradient.first() },
            keyColor = activeKeyColor,
            keySpecialColor = activeKeyColor.copy(alpha = (activeKeyColor.alpha * 1.3f).coerceAtMost(1f)),
            keyPressedColor = selectedAccentColor,
            textColor = activeTextColor,
            textSecondaryColor = activeTextColor.copy(alpha = 0.7f),
            accentColor = selectedAccentColor,
            keyBorderColor = if (showBorders) selectedAccentColor.copy(alpha = 0.4f) else Color.Transparent,
            backgroundGradient = activeGradient,
            keyAlpha = 0.9f,
            keyCornerRadius = keyRoundness.toFloat(),
            backdropType = "custom"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (isDarkMode) Color(0xFF13161F) else Color(0xFFFFFFFF),
            border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Custom Theme Builder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Text(
                            "Live Real-Time Keyboard Preview",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // LIVE KEYBOARD PREVIEW
                ThemePreview(
                    theme = livePreviewTheme,
                    keyCornerRadius = keyRoundness.toFloat(),
                    showBorders = showBorders,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Theme Name Input
                    OutlinedTextField(
                        value = themeName,
                        onValueChange = { themeName = it },
                        label = { Text("Theme Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 2. Background & Wallpaper Gradients
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("1. Background Wallpaper:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            gradientPresets.forEachIndexed { index, (label, gradient) ->
                                val isSel = selectedGradientIndex == index
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) Color(0xFF10B981) else Color(0x33888888)),
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(44.dp)
                                        .clickable { selectedGradientIndex = index }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.linearGradient(gradient)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Key Style
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("2. Key Style & Translucency:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            keyStyleOptions.forEachIndexed { idx, (label, _, _) ->
                                val isSel = selectedKeyStyleIndex == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) Color(0xFF10B981) else if (isDarkMode) Color(0xFF1E222E) else Color(0xFFE2E8F0),
                                    border = BorderStroke(1.dp, if (isSel) Color(0xFF10B981) else Color(0x22FFFFFF)),
                                    modifier = Modifier.clickable { selectedKeyStyleIndex = idx }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Accent Color Swatches
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("3. Accent Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            accentColors.forEach { col ->
                                val isSel = selectedAccentColor == col
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            if (isSel) 2.5.dp else 0.dp,
                                            if (isSel) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { selectedAccentColor = col },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSel) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 5. Key Shape / Roundness Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("4. Key Shape (Radius):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${keyRoundness}dp", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = keyRoundness.toFloat(),
                            onValueChange = { keyRoundness = it.toInt() },
                            valueRange = 2f..22f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981)
                            )
                        )
                    }

                    // 6. Key Height & Key Borders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("5. Key Borders:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Outlines individual keys", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = showBorders,
                            onCheckedChange = { showBorders = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save Theme Button
                Button(
                    onClick = {
                        onSave(
                            themeName.ifBlank { "My Custom Theme" },
                            activeGradient,
                            activeKeyColor,
                            activeTextColor,
                            selectedAccentColor,
                            keyRoundness,
                            keyHeight,
                            showBorders
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Save Theme & Apply", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// FULL KEYBOARD PREVIEW DIALOG
// ==========================================

@Composable
fun KeyboardFullPreviewDialog(
    theme: KeyboardTheme,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    var typedText by remember { mutableStateOf("Global MKeyboard") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) Color(0xFF161922) else Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            theme.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Text(
                            "Theme Full View",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Interactive Typed Text Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkMode) Color(0xFF0F1118) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0x33888888)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = typedText.ifBlank { "Tap keys to test..." },
                        color = if (isDarkMode) Color.White else Color.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Keyboard Preview
                ThemePreview(
                    theme = theme,
                    modifier = Modifier.fillMaxWidth()
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp)
                    ) {
                        Text("Apply This Theme", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
