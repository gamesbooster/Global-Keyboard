package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreditsSecurityManager
import com.example.data.LingoKeyPreferences
import com.example.model.KeyboardTheme

data class StoreThemeItem(
    val id: String,
    val title: String,
    val creditPrice: Int, // 0 = Free
    val isPro: Boolean = false,
    val badgeLabel: String,
    val themeRef: KeyboardTheme,
    val previewGradient: List<Color>,
    val graphicType: String // "spain_crest", "spain_trophy", "spain_player", "argentina_stars", "argentina_cup", "cyberpunk", "gold"
)

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

    val creditsSecurityManager = remember { CreditsSecurityManager.getInstance(context) }
    val aiCredits by creditsSecurityManager.aiCredits.collectAsState()

    var showUnlockDialog by remember { mutableStateOf<StoreThemeItem?>(null) }
    var showInsufficientCreditsDialog by remember { mutableStateOf<StoreThemeItem?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val bgColor = if (isDarkMode) Color(0xFF0F0F14) else Color(0xFFF8FAFC)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val borderColor = if (isDarkMode) Color(0xFF282A3A) else Color(0xFFE2E8F0)

    val spainFeatured = StoreThemeItem(
        id = "spain_2",
        title = "Spain Gold Crest",
        creditPrice = 0,
        badgeLabel = "Free",
        themeRef = KeyboardTheme.SPAIN_2,
        previewGradient = listOf(Color(0xFF800E1A), Color(0xFFB91C1C), Color(0xFFEAB308), Color(0xFF450A0A)),
        graphicType = "spain_crest"
    )

    val spainGridThemes = listOf(
        StoreThemeItem(
            id = "spain_4",
            title = "Spain Trophy Winners",
            creditPrice = 0,
            badgeLabel = "Free",
            themeRef = KeyboardTheme.SPAIN_4,
            previewGradient = listOf(Color(0xFF450A0A), Color(0xFF991B1B), Color(0xFFF59E0B)),
            graphicType = "spain_trophy"
        ),
        StoreThemeItem(
            id = "spain_3",
            title = "Spain Stadium 19",
            creditPrice = 0,
            badgeLabel = "Free",
            themeRef = KeyboardTheme.SPAIN_3,
            previewGradient = listOf(Color(0xFF2E090D), Color(0xFF7C2D12), Color(0xFFEA580C)),
            graphicType = "spain_player"
        )
    )

    val argentinaThemes = listOf(
        StoreThemeItem(
            id = "argentina_10",
            title = "Argentina 3 Stars",
            creditPrice = 50,
            badgeLabel = "50 Credits",
            themeRef = KeyboardTheme.ARGENTINA_10,
            previewGradient = listOf(Color(0xFF081420), Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFFACC15)),
            graphicType = "argentina_stars"
        ),
        StoreThemeItem(
            id = "argentina_gold",
            title = "Argentina Golden Edition",
            creditPrice = 100,
            isPro = true,
            badgeLabel = "100 Credits",
            themeRef = KeyboardTheme.ARGENTINA_GOLD,
            previewGradient = listOf(Color(0xFF0B1724), Color(0xFF1E3A5F), Color(0xFFEAB308), Color(0xFFCA8A04)),
            graphicType = "argentina_cup"
        )
    )

    val futuristicThemes = listOf(
        StoreThemeItem(
            id = "cyberpunk",
            title = "Cyberpunk Neon 2077",
            creditPrice = 50,
            badgeLabel = "50 Credits",
            themeRef = KeyboardTheme.CYBERPUNK,
            previewGradient = listOf(Color(0xFF0F001A), Color(0xFF00FFFF), Color(0xFFFF007F), Color(0xFFFFEE00)),
            graphicType = "cyberpunk"
        ),
        StoreThemeItem(
            id = "carbon_gold",
            title = "Carbon Luxury Gold",
            creditPrice = 100,
            isPro = true,
            badgeLabel = "100 Credits",
            themeRef = KeyboardTheme.CARBON_GOLD,
            previewGradient = listOf(Color(0xFF121212), Color(0xFF282828), Color(0xFFEAB308)),
            graphicType = "gold"
        )
    )

    fun applyOrPurchaseTheme(item: StoreThemeItem) {
        val isUnlocked = item.creditPrice == 0 || isPremiumUser || unlockedThemeIds.contains(item.id)
        if (isUnlocked) {
            preferences.setTheme(item.themeRef)
            Toast.makeText(context, "🎉 ${item.title} theme applied!", Toast.LENGTH_SHORT).show()
        } else {
            if (aiCredits >= item.creditPrice) {
                showUnlockDialog = item
            } else {
                showInsufficientCreditsDialog = item
            }
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Surface(
                color = if (isDarkMode) Color(0xFF161722) else Color(0xFFFFFFFF),
                shadowElevation = 4.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Store",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = textColor
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
                        // User Avatar & Credits Pill (Image 4 "P" avatar)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDarkMode) Color(0xFF222436) else Color(0xFFEEF2F6),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { onNavigateToSpinAndWin() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text(
                                    if (isPremiumUser) "👑 VIP" else "🪙 $aiCredits",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. In-Store Test Ad Banner (matching Image 4 top section)
            item {
                StoreAdBanner(isDarkMode = isDarkMode)
            }

            // 2. Category: Spain World Cup
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            "Spain Champions Collection",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Text(
                            "Official World Cup & European Champions keyboard themes",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }

                    // Featured Big Banner (Spain Gold Crest)
                    FeaturedThemeHeroCard(
                        item = spainFeatured,
                        isApplied = currentTheme.id == spainFeatured.themeRef.id,
                        onApply = { applyOrPurchaseTheme(spainFeatured) }
                    )

                    // 2-Column Grid (Spain Trophy & Spain Stadium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        spainGridThemes.forEach { item ->
                            val isUnlocked = item.creditPrice == 0 || isPremiumUser || unlockedThemeIds.contains(item.id)
                            val isApplied = currentTheme.id == item.themeRef.id
                            StoreThemeGridCard(
                                item = item,
                                isUnlocked = isUnlocked,
                                isApplied = isApplied,
                                isDarkMode = isDarkMode,
                                onAction = { applyOrPurchaseTheme(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Category: Argentina World Cup
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Argentina 3-Stars World Cup",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                            Text(
                                "World Cup winning sky blue & gold themes",
                                fontSize = 12.sp,
                                color = textMuted
                            )
                        }
                        Text(
                            "See more",
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToThemes?.invoke() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        argentinaThemes.forEach { item ->
                            val isUnlocked = item.creditPrice == 0 || isPremiumUser || unlockedThemeIds.contains(item.id)
                            val isApplied = currentTheme.id == item.themeRef.id
                            StoreThemeGridCard(
                                item = item,
                                isUnlocked = isUnlocked,
                                isApplied = isApplied,
                                isDarkMode = isDarkMode,
                                onAction = { applyOrPurchaseTheme(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Category: Futuristic & Luxury Neon
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            "Futuristic Neon & Luxury 3D",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Text(
                            "Cyberpunk RGB glow, carbon fiber & luxury gold",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        futuristicThemes.forEach { item ->
                            val isUnlocked = item.creditPrice == 0 || isPremiumUser || unlockedThemeIds.contains(item.id)
                            val isApplied = currentTheme.id == item.themeRef.id
                            StoreThemeGridCard(
                                item = item,
                                isUnlocked = isUnlocked,
                                isApplied = isApplied,
                                isDarkMode = isDarkMode,
                                onAction = { applyOrPurchaseTheme(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Unlock Confirmation Dialog
    showUnlockDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showUnlockDialog = null },
            title = {
                Text("Unlock ${item.title}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This theme requires ${item.creditPrice} AI credits.")
                    Text(
                        "Your Current Balance: $aiCredits Credits 🪙",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text("After unlocking, it will remain permanently available.", fontSize = 12.sp, color = textMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = preferences.unlockThemeWithCredits(item.id, item.creditPrice)
                        if (success) {
                            preferences.setTheme(item.themeRef)
                            Toast.makeText(context, "🎉 ${item.title} unlocked and applied!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Could not complete unlock. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                        showUnlockDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Unlock & Apply", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialog = null }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    // Insufficient Credits Dialog
    showInsufficientCreditsDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showInsufficientCreditsDialog = null },
            icon = {
                Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Need More AI Credits", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("The ${item.title} theme costs ${item.creditPrice} credits. You currently have $aiCredits credits.")
                    Text("Spin the Lucky Wheel or watch a quick video ad to earn free credits!", fontSize = 12.5.sp, color = textMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInsufficientCreditsDialog = null
                        onNavigateToSpinAndWin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308))
                ) {
                    Text("Spin & Win Credits", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsufficientCreditsDialog = null }) {
                    Text("Not Now", color = textMuted)
                }
            }
        )
    }
}

/**
 * Top Ad banner component (matching Image 4 and Image 3)
 */
@Composable
fun StoreAdBanner(isDarkMode: Boolean) {
    var isAdMuted by remember { mutableStateOf(true) }
    var showAdToast by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkMode) Color(0xFF0A0B10) else Color(0xFFE5E7EB))
    ) {
        // "Ad" text pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Ad",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }

        // Ad Creative Card (Dark letterbox with white Google Cloud creative)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.Black)
        ) {
            // Central Ad Content
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 280.dp)
                    .align(Alignment.Center)
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF4285F4)
                        )
                        Text(
                            "Cloud",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF5F6368)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF3C4043)
                    ) {
                        Text(
                            "TEST AD",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Top-Left Mute Button
            IconButton(
                onClick = { isAdMuted = !isAdMuted },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isAdMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom-Left Info icon
            IconButton(
                onClick = {
                    Toast.makeText(context, "Google AdMob Test Partner Creative", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ad Info",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Bottom-Right "Open" Pill Button (matching Image 3 & 4)
            Button(
                onClick = {
                    Toast.makeText(context, "Opening Google Cloud partner preview...", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3139)),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Text(
                    "Open",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

/**
 * Big Featured Theme Hero Card (Spain Gold Crest)
 */
@Composable
fun FeaturedThemeHeroCard(
    item: StoreThemeItem,
    isApplied: Boolean,
    onApply: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140306)),
        border = BorderStroke(1.dp, Color(0xFF8B1220)),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onApply() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Rich Football Crest Artistic Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Radial dark glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8B1220), Color(0xFF38060B), Color(0xFF0F0103)),
                        center = Offset(w * 0.7f, h * 0.5f),
                        radius = w * 0.65f
                    )
                )

                // Gold explosion lines
                for (i in 0..20) {
                    val angle = (i * 18.0) * (Math.PI / 180.0)
                    val r = h * 0.45f
                    val cx = w * 0.72f
                    val cy = h * 0.5f
                    val ex = (cx + Math.cos(angle) * r).toFloat()
                    val ey = (cy + Math.sin(angle) * r).toFloat()
                    drawLine(
                        color = Color(0xFFEAB308).copy(alpha = 0.25f),
                        start = Offset(cx, cy),
                        end = Offset(ex, ey),
                        strokeWidth = 2f
                    )
                }

                // Spanish Gold Shield Crest Representation
                val shieldPath = Path().apply {
                    moveTo(w * 0.72f - 30f, h * 0.35f)
                    lineTo(w * 0.72f + 30f, h * 0.35f)
                    lineTo(w * 0.72f + 26f, h * 0.6f)
                    cubicTo(
                        w * 0.72f + 20f, h * 0.75f,
                        w * 0.72f, h * 0.78f,
                        w * 0.72f, h * 0.8f
                    )
                    cubicTo(
                        w * 0.72f, h * 0.78f,
                        w * 0.72f - 20f, h * 0.75f,
                        w * 0.72f - 26f, h * 0.6f
                    )
                    close()
                }
                drawPath(shieldPath, Color(0xFFEAB308).copy(alpha = 0.7f), style = Stroke(width = 3.5f))
                drawCircle(Color(0xFFDC2626), radius = 12f, center = Offset(w * 0.72f, h * 0.52f))
            }

            // Text Overlays
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        item.title,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        item.badgeLabel,
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isApplied) Color(0xFF10B981) else Color.White.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isApplied) Color(0xFF10B981) else Color.White.copy(alpha = 0.3f))
                ) {
                    Text(
                        if (isApplied) "Applied ✓" else "Download now",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * 2-Column Grid Card for Store Themes
 */
@Composable
fun StoreThemeGridCard(
    item: StoreThemeItem,
    isUnlocked: Boolean,
    isApplied: Boolean,
    isDarkMode: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF161722) else Color(0xFFFFFFFF)
        ),
        border = BorderStroke(
            1.dp,
            if (isApplied) Color(0xFF10B981) else if (isDarkMode) Color(0xFF282A3A) else Color(0xFFE2E8F0)
        ),
        modifier = modifier.clickable { onAction() }
    ) {
        Column {
            // Visual Graphic Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Brush.linearGradient(item.previewGradient))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    when (item.graphicType) {
                        "spain_trophy" -> {
                            // Trophy and celebration confetti
                            drawCircle(Color(0xFFEAB308), radius = 18f, center = Offset(w * 0.5f, h * 0.45f))
                            drawLine(Color(0xFFEAB308), Offset(w * 0.5f, h * 0.45f), Offset(w * 0.5f, h * 0.75f), strokeWidth = 5f)
                            drawRect(Color(0xFFFBBF24), Offset(w * 0.4f, h * 0.75f), androidx.compose.ui.geometry.Size(w * 0.2f, 8f))
                            // Stars
                            drawCircle(Color.White, 2.5f, Offset(w * 0.3f, h * 0.3f))
                            drawCircle(Color.White, 2.5f, Offset(w * 0.7f, h * 0.35f))
                        }
                        "spain_player" -> {
                            // Stadium Sunset Silhouette & #19 jersey
                            drawCircle(Color(0xFFF97316).copy(alpha = 0.6f), radius = 35f, center = Offset(w * 0.5f, h * 0.55f))
                            drawRect(Color(0xFF7C2D12), Offset(w * 0.35f, h * 0.65f), androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.35f))
                        }
                        "argentina_stars" -> {
                            // 3 Golden Stars over Albiceleste stripes
                            drawLine(Color(0xFF38BDF8), Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth = 14f)
                            drawLine(Color(0xFF38BDF8), Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), strokeWidth = 14f)
                            drawCircle(Color(0xFFFACC15), 5f, Offset(w * 0.38f, h * 0.45f))
                            drawCircle(Color(0xFFFACC15), 6f, Offset(w * 0.5f, h * 0.35f))
                            drawCircle(Color(0xFFFACC15), 5f, Offset(w * 0.62f, h * 0.45f))
                        }
                        "argentina_cup" -> {
                            // Golden cup & glow
                            drawCircle(Color(0xFFEAB308).copy(alpha = 0.4f), radius = 28f, center = Offset(w * 0.5f, h * 0.5f))
                            drawCircle(Color(0xFFFACC15), radius = 14f, center = Offset(w * 0.5f, h * 0.45f))
                        }
                        "cyberpunk" -> {
                            // Neon grids
                            drawLine(Color(0xFF00FFFF), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 2f)
                            drawLine(Color(0xFFFF007F), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 2f)
                        }
                        "gold" -> {
                            // Luxury carbon diagonals
                            for (i in 0..10) {
                                drawLine(Color(0xFFEAB308).copy(alpha = 0.35f), Offset(i * 30f, 0f), Offset(i * 30f + 50f, h), strokeWidth = 1.5f)
                            }
                        }
                    }
                }

                // Top Badge (Free vs Price)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (item.creditPrice == 0) Color(0xFF10B981) else Color(0xFFEAB308),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        if (isApplied) "Applied" else item.badgeLabel,
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    if (isApplied) "Active ✓"
                    else if (isUnlocked) "Free (Unlocked)"
                    else "🪙 ${item.creditPrice} Credits",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isApplied) Color(0xFF10B981)
                    else if (isUnlocked) Color(0xFF10B981)
                    else Color(0xFFEAB308),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
