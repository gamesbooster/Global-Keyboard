package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ads.AdMobRewardedAdManager
import com.example.data.LingoKeyPreferences
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.NeumorphicColors
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// 8 Slices matching the video lottery wheel
val WHEEL_SLICES = listOf(0, 100, 200, 0, 150, 50, 0, 500)

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinAndWinScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit,
    onNavigateToPro: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val aiCredits by preferences.aiCredits.collectAsState()
    val spinsRemaining by preferences.spinsRemainingToday.collectAsState()

    // Preload AdMob Rewarded Ad immediately upon entering screen
    LaunchedEffect(Unit) {
        AdMobRewardedAdManager.preload(context)
    }

    // Wheel Animation State
    val rotationAngle = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }

    // Dialog States
    var showHowItWorksDialog by remember { mutableStateOf(false) }
    var showLearnAIDialog by remember { mutableStateOf(false) }
    var wonCreditsDialog by remember { mutableStateOf<Int?>(null) }
    var showNoSpinsDialog by remember { mutableStateOf(false) }

    // Neumorphic Color Tokens
    val screenBg = if (isDarkMode) NeumorphicColors.DarkScreenBg else NeumorphicColors.LightScreenBg
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted
    val primaryGold = Color(0xFFF59E0B)

    val wellBrush = if (isDarkMode) {
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

    fun triggerVibration(durationMs: Long = 40L) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    fun startSpin() {
        if (isSpinning) return

        if (spinsRemaining <= 0) {
            showNoSpinsDialog = true
            return
        }

        // Deduct 1 spin from the 5 daily spins immediately
        val consumed = preferences.consumeSpin()
        if (!consumed) {
            showNoSpinsDialog = true
            return
        }

        isSpinning = true
        coroutineScope.launch {
            // Pick a winning index
            val winningIndex = Random.nextInt(WHEEL_SLICES.size)
            val wonAmount = WHEEL_SLICES[winningIndex]

            // Wheel calculation: 8 slices (45 deg each), top pointer at 270 degrees
            val sliceDegrees = 360f / WHEEL_SLICES.size
            val targetSliceCenter = winningIndex * sliceDegrees + (sliceDegrees / 2f)
            val currentRotation = rotationAngle.value % 360f

            val extraRevolutions = 360f * (5 + Random.nextInt(3))
            val targetOffset = (270f - targetSliceCenter + 360f) % 360f
            val finalTarget = rotationAngle.value + extraRevolutions + (targetOffset - (currentRotation % 360f) + 360f) % 360f

            rotationAngle.animateTo(
                targetValue = finalTarget,
                animationSpec = tween(
                    durationMillis = 3800,
                    easing = CubicBezierEasing(0.2f, 0.9f, 0.1f, 1.0f)
                )
            )

            // Spin animation completed
            triggerVibration(90L)
            isSpinning = false

            // Before granting reward, present real AdMob Rewarded Ad (strictly max 5 per day)
            val activity = context.findActivity()
            if (activity != null) {
                AdMobRewardedAdManager.showRewardedAd(
                    activity = activity,
                    onRewardGranted = {
                        if (wonAmount > 0) {
                            preferences.addCredits(wonAmount)
                        }
                    },
                    onComplete = {
                        wonCreditsDialog = wonAmount
                    }
                )
            } else {
                // Fallback in non-Activity environments
                if (wonAmount > 0) {
                    preferences.addCredits(wonAmount)
                }
                wonCreditsDialog = wonAmount
            }
        }
    }

    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Spin & Win",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
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
                    // Top Corner Remaining Spins Indicator (strictly 5 spins per day)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFE2E8F0),
                        border = BorderStroke(1.dp, if (spinsRemaining > 0) primaryGold.copy(alpha = 0.5f) else textMuted.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.RotateRight,
                                contentDescription = null,
                                tint = if (spinsRemaining > 0) primaryGold else textMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                "$spinsRemaining/5 spins",
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. MODERN 3D NEUMORPHISM BALANCE CARD
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                cornerRadius = 20.dp,
                elevation = 7.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Recessed 3D Well containing coin icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(wellBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🪙", fontSize = 22.sp)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Your AI Credits Balance",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textMuted
                            )
                            Text(
                                if (isPremiumUser) "Unlimited (VIP Active)" else "$aiCredits Credits",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPremiumUser) primaryGold else textColor
                            )
                        }
                    }

                    // Clean status pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (spinsRemaining > 0) NeumorphicColors.EmeraldAccent.copy(alpha = 0.15f) else textMuted.copy(alpha = 0.15f)
                    ) {
                        Text(
                            if (spinsRemaining > 0) "Active" else "Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (spinsRemaining > 0) NeumorphicColors.EmeraldAccent else textMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // 2. 3D NEUMORPHIC LUCKY WHEEL PEDESTAL STAGE
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkMode) Color(0xFF141622) else Color(0xFFE5E9F2)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // The Wheel Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle.value)
                ) {
                    val diameter = size.minDimension
                    val radius = diameter / 2f
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val sliceAngle = 360f / WHEEL_SLICES.size

                    // Outer dark ring
                    drawCircle(
                        color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                        radius = radius,
                        center = centerOffset
                    )

                    // Draw 8 colorful slices
                    for (i in WHEEL_SLICES.indices) {
                        val startAngle = i * sliceAngle
                        val isEven = i % 2 == 0
                        val sliceColor = if (isEven) Color(0xFF0D9488) else Color(0xFF2DD4BF)

                        drawArc(
                            color = sliceColor,
                            startAngle = startAngle,
                            sweepAngle = sliceAngle,
                            useCenter = true,
                            topLeft = Offset(centerOffset.x - radius + 8f, centerOffset.y - radius + 8f),
                            size = Size((radius - 8f) * 2, (radius - 8f) * 2)
                        )
                    }

                    // Inner golden border ring
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = radius - 8f,
                        center = centerOffset,
                        style = Stroke(width = 3f)
                    )

                    // Casino perimeter light pegs
                    val numDots = 24
                    for (d in 0 until numDots) {
                        val dotAngle = (d * 360f / numDots) * (PI / 180f)
                        val dotX = centerOffset.x + (radius - 4f) * cos(dotAngle).toFloat()
                        val dotY = centerOffset.y + (radius - 4f) * sin(dotAngle).toFloat()
                        drawCircle(
                            color = Color(0xFFFEF08A),
                            radius = 3f,
                            center = Offset(dotX, dotY)
                        )
                    }

                    // Center decorative golden hub
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 32f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 18f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = centerOffset
                    )
                }

                // Slice values text overlay
                WHEEL_SLICES.forEachIndexed { index, value ->
                    val sliceAngle = 360f / WHEEL_SLICES.size
                    val angleDeg = rotationAngle.value + index * sliceAngle + (sliceAngle / 2f)
                    val angleRad = (angleDeg * PI / 180f).toFloat()
                    val distance = 95.dp

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (distance.value * cos(angleRad)).dp,
                                y = (distance.value * sin(angleRad)).dp
                            )
                            .rotate(angleDeg + 90f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (value == 0) "0" else "$value",
                            color = if (index % 2 == 0) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }

                // TOP POINTER PIN (Gold Arrow)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-4).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp, 32.dp)) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height)
                            lineTo(0f, 0f)
                            lineTo(size.width, 0f)
                            close()
                        }
                        drawPath(path, color = Color(0xFFF59E0B))
                        drawCircle(
                            color = Color(0xFFFEF08A),
                            radius = 5f,
                            center = Offset(size.width / 2f, 8f)
                        )
                    }
                }
            }

            // 3. 3D TACTILE GOLDEN "SPIN" BUTTON
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable(enabled = !isSpinning && spinsRemaining > 0) {
                        startSpin()
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (spinsRemaining > 0) primaryGold else (if (isDarkMode) Color(0xFF1E202E) else Color(0xFFD1D5DB)),
                shadowElevation = if (spinsRemaining > 0) 5.dp else 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSpinning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF0F172A),
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                "LUCKY WHEEL SPINNING...",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                        }
                    } else if (spinsRemaining > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.RotateRight,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "SPIN THE WHEEL ($spinsRemaining LEFT)",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else {
                        Text(
                            "0 SPINS LEFT TODAY (RESETS TOMORROW)",
                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 4. "HOW DO YOU USE AI?" 3D NEUMORPHIC CARD
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                cornerRadius = 16.dp,
                elevation = 5.dp,
                onClick = { showLearnAIDialog = true }
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(wellBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = primaryGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "How do you use AI?",
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // 5. "HOW IT WORKS" TRANSPARENCY CARD (3D NEUMORPHIC)
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                cornerRadius = 16.dp,
                elevation = 5.dp,
                onClick = { showHowItWorksDialog = true }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "How it works",
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        "AI credits cost us real money as we buy them from global AI providers. Users get 5 daily spins per day to win free AI credits. Each spin displays a verified sponsored ad to fund the AI generation costs.",
                        color = textMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Read Transparency Policy",
                            color = primaryGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = primaryGold,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }

    // WON CREDITS CELEBRATION DIALOG (shown AFTER watching the rewarded ad)
    wonCreditsDialog?.let { won ->
        AlertDialog(
            onDismissRequest = { wonCreditsDialog = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDarkMode) Color(0xFF161824) else Color(0xFFF8FAFC),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (won > 0) "🎉 You Won!" else "Better Luck Next Time!",
                        color = if (won > 0) primaryGold else textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (won > 0) {
                        Text(
                            "+$won AI Credits",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            "Congratulations! Your credits have been verified and added to your vault. Use them for Smart Replies, rewrites, and instant translations!",
                            fontSize = 13.sp,
                            color = textMuted,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text("🪙 0 Credits", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textMuted)
                        Text(
                            "Don't worry! You still have daily spins left to try again.",
                            fontSize = 13.sp,
                            color = textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { wonCreditsDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Collect & Continue", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // NO SPINS LEFT TODAY DIALOG (Strictly 5 spins per day limit)
    if (showNoSpinsDialog) {
        AlertDialog(
            onDismissRequest = { showNoSpinsDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDarkMode) Color(0xFF161824) else Color(0xFFF8FAFC),
            title = {
                Text("Daily Spins Completed", color = textColor, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "You have used all 5 spins for today! Your 5 daily free spins will automatically reset tomorrow at midnight.",
                    color = textMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNoSpinsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // "HOW DO YOU USE AI" TUTORIAL MODAL
    if (showLearnAIDialog) {
        LearnAIModal(onDismiss = { showLearnAIDialog = false }, isDarkMode = isDarkMode)
    }

    // "PLEASE READ" TRANSPARENCY MODAL
    if (showHowItWorksDialog) {
        TransparencyModal(onDismiss = { showHowItWorksDialog = false }, isDarkMode = isDarkMode)
    }
}

/**
 * 3-Step Interactive "Learn to use AI" guide.
 */
@Composable
fun LearnAIModal(onDismiss: () -> Unit, isDarkMode: Boolean = true) {
    var step by remember { mutableIntStateOf(1) }
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val bgColor = if (isDarkMode) Color(0xFF0F111A) else Color(0xFFF1F5F9)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
            color = bgColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                    Text(
                        "Learn to use AI",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Visual Representation of Step
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Mock Keyboard Surface
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkMode) Color(0xFF181B28) else Color.White,
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E3248) else Color(0xFFCBD5E1))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Input line
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDarkMode) Color(0xFF222638) else Color(0xFFE2E8F0),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    when (step) {
                                        1 -> "Are you free for lunch? |"
                                        2 -> "Drafting smart reply with AI..."
                                        else -> "Yes! Let's meet at 1 PM at the cafe! ✓"
                                    },
                                    color = textColor,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            // Mock Keyboard Keys
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Toolbar Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("🌐", fontSize = 14.sp)
                                        Text("📋", fontSize = 14.sp)
                                        Text("🎨", fontSize = 14.sp)
                                    }

                                    // Highlighted AI Button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (step == 1) Color(0xFFF59E0B) else Color(0xFF334155),
                                        border = if (step == 1) BorderStroke(2.dp, Color.White) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = if (step == 1) Color.Black else Color(0xFFFDE047),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "AI",
                                                color = if (step == 1) Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // QWERTY visual mock row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").forEach { char ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isDarkMode) Color(0xFF282C40) else Color(0xFFE2E8F0),
                                            modifier = Modifier.size(26.dp, 32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(char, color = textColor, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Step Text Description
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            when (step) {
                                1 -> "1. Turn on AI"
                                2 -> "2. Choose an AI action"
                                else -> "3. Insert the result"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFF59E0B)
                        )

                        Text(
                            when (step) {
                                1 -> "Tap the ✨ AI button on the top row of the keyboard"
                                2 -> "Pick any AI action, such as smart reply, rewrite, or translate"
                                else -> "Tap ✓ to insert the result directly into any app"
                            },
                            fontSize = 13.sp,
                            color = textMuted,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Step indicator dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..3).forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (step == index) 16.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (step == index) Color(0xFFF59E0B) else Color(0xFF64748B).copy(alpha = 0.4f))
                            )
                        }
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        if (step < 3) step++ else onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        if (step < 3) "Next Step" else "Start Using AI",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * "Please Read" transparency modal.
 */
@Composable
fun TransparencyModal(onDismiss: () -> Unit, isDarkMode: Boolean = true) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0xFF161824) else Color(0xFFF8FAFC)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E3248) else Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Please Read",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                // 1. Chances of winning
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎲", fontSize = 20.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "5 Daily Spins",
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            "Every user receives 5 daily spins per calendar day. Free AI credits are distributed through this random lottery wheel.",
                            color = textMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                // 2. AI credits cost money
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🤖", fontSize = 20.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Why Sponsored Ads?",
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            "The AI credits awarded through the spin wheel cost real money to maintain server inference. To offer them completely free of charge, each spin is sponsored by an advertisement.",
                            color = textMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I understand", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
