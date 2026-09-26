package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.AudioGuidanceHelper
import com.example.engine.ImeUtils
import com.example.engine.VoiceTTSEngine
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    preferences: LingoKeyPreferences,
    onNavigateNext: (destination: String) -> Unit
) {
    val context = LocalContext.current
    val voiceTTSEngine = remember { VoiceTTSEngine.getInstance(context) }
    val activeLanguage by preferences.activeLanguage.collectAsState()
    val onboardingCompleted by preferences.onboardingCompleted.collectAsState()
    val voiceGuidanceEnabled by preferences.voiceGuidanceEnabled.collectAsState()

    // Animations
    val scaleAnim = remember { Animatable(0.6f) }
    val alphaAnim = remember { Animatable(0f) }
    val textAlphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Trigger smooth scale & fade-in
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        textAlphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = LinearOutSlowInEasing)
        )

        // Voice output on splash
        if (voiceGuidanceEnabled) {
            try {
                val welcomeText = AudioGuidanceHelper.getSplashAudioText(activeLanguage)
                voiceTTSEngine.speak(welcomeText, activeLanguage.ttsLocaleTag)
            } catch (_: Exception) {
            }
        }

        // Fast clean transition delay
        delay(1000)

        try {
            val isReady = ImeUtils.isKeyboardReady(context)
            if (onboardingCompleted && isReady) {
                onNavigateNext("dashboard")
            } else {
                onNavigateNext("onboarding")
            }
        } catch (_: Exception) {
            onNavigateNext("dashboard")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D0E15),
                        Color(0xFF131625),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Animated App Logo with glowing aura
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF047857))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF064E3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "म",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // App Name & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.alpha(textAlphaAnim.value)
            ) {
                Text(
                    text = "Global Keyboard Dynamic",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Smart • Multilingual • Voice Typing",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF34D399),
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Bottom version / privacy badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(textAlphaAnim.value)
        ) {
            Text(
                text = "100% Private & On-Device Processing",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
