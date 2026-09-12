package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.LingoKeyPreferences
import com.example.engine.AudioGuidanceHelper
import com.example.engine.ImeUtils
import com.example.engine.VoiceTTSEngine
import com.example.model.Language
import kotlinx.coroutines.delay

enum class OnboardingStep {
    WELCOME,
    PERMISSIONS_PRIVACY,
    LANGUAGE_SETUP,
    ENABLE_KEYBOARD,
    SELECT_KEYBOARD,
    KEYBOARD_READY,
    TUTORIAL
}

@Composable
fun OnboardingScreen(
    preferences: LingoKeyPreferences,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val voiceTTSEngine = remember { VoiceTTSEngine.getInstance(context) }

    val activeLanguage by preferences.activeLanguage.collectAsState()
    val enabledLanguages by preferences.enabledLanguages.collectAsState()

    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    // Live IME state
    var isEnabled by remember { mutableStateOf(ImeUtils.isImeEnabled(context)) }
    var isSelected by remember { mutableStateOf(ImeUtils.isImeSelected(context)) }

    // Update IME state when user returns to app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val enabled = ImeUtils.isImeEnabled(context)
                val selected = ImeUtils.isImeSelected(context)
                isEnabled = enabled
                isSelected = selected

                // Auto advance if returning after successful enable/select
                if (currentStep == OnboardingStep.ENABLE_KEYBOARD && enabled) {
                    currentStep = OnboardingStep.SELECT_KEYBOARD
                } else if (currentStep == OnboardingStep.SELECT_KEYBOARD && enabled && selected) {
                    currentStep = OnboardingStep.KEYBOARD_READY
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceTTSEngine.stop()
        }
    }

    // Function to play or stop voice guidance for the current step
    fun toggleVoiceGuidance(text: String) {
        if (isAudioPlaying) {
            voiceTTSEngine.stop()
            isAudioPlaying = false
        } else {
            isAudioPlaying = true
            voiceTTSEngine.speak(text, activeLanguage.ttsLocaleTag)
        }
    }

    // Auto-stop speech when step changes
    LaunchedEffect(currentStep) {
        voiceTTSEngine.stop()
        isAudioPlaying = false
    }

    Scaffold(
        containerColor = Color(0xFF0F1016),
        topBar = {
            // Header with App Brand and Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF059669)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("म", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        "Global Keyboard Dynamic",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (currentStep != OnboardingStep.KEYBOARD_READY && currentStep != OnboardingStep.TUTORIAL) {
                    TextButton(onClick = {
                        preferences.setOnboardingCompleted(true)
                        onComplete()
                    }) {
                        Text("Skip", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> {
                        WelcomeStep(
                            language = activeLanguage,
                            isAudioPlaying = isAudioPlaying,
                            onToggleAudio = {
                                toggleVoiceGuidance(AudioGuidanceHelper.getWelcomeAudioText(activeLanguage))
                            },
                            onNext = { currentStep = OnboardingStep.PERMISSIONS_PRIVACY }
                        )
                    }

                    OnboardingStep.PERMISSIONS_PRIVACY -> {
                        PermissionsStep(
                            language = activeLanguage,
                            isAudioPlaying = isAudioPlaying,
                            onToggleAudio = {
                                toggleVoiceGuidance(AudioGuidanceHelper.getPermissionsAudioText(activeLanguage))
                            },
                            onNext = { currentStep = OnboardingStep.LANGUAGE_SETUP },
                            onBack = { currentStep = OnboardingStep.WELCOME }
                        )
                    }

                    OnboardingStep.LANGUAGE_SETUP -> {
                        LanguageSetupStep(
                            activeLanguage = activeLanguage,
                            enabledLanguages = enabledLanguages,
                            onSelectPrimary = { lang ->
                                preferences.setActiveLanguage(lang)
                                val current = enabledLanguages.toMutableList()
                                if (current.none { it.id == lang.id }) {
                                    current.add(0, lang)
                                    preferences.setEnabledLanguages(current)
                                }
                            },
                            onNext = {
                                if (ImeUtils.isImeEnabled(context)) {
                                    currentStep = if (ImeUtils.isImeSelected(context)) {
                                        OnboardingStep.KEYBOARD_READY
                                    } else {
                                        OnboardingStep.SELECT_KEYBOARD
                                    }
                                } else {
                                    currentStep = OnboardingStep.ENABLE_KEYBOARD
                                }
                            },
                            onBack = { currentStep = OnboardingStep.PERMISSIONS_PRIVACY }
                        )
                    }

                    OnboardingStep.ENABLE_KEYBOARD -> {
                        EnableKeyboardStep(
                            context = context,
                            language = activeLanguage,
                            isEnabled = isEnabled,
                            isAudioPlaying = isAudioPlaying,
                            onToggleAudio = {
                                toggleVoiceGuidance(AudioGuidanceHelper.getEnableStepAudioText(activeLanguage))
                            },
                            onNext = {
                                isEnabled = ImeUtils.isImeEnabled(context)
                                if (isEnabled) {
                                    currentStep = OnboardingStep.SELECT_KEYBOARD
                                } else {
                                    ImeUtils.openKeyboardSettings(context)
                                }
                            }
                        )
                    }

                    OnboardingStep.SELECT_KEYBOARD -> {
                        SelectKeyboardStep(
                            context = context,
                            language = activeLanguage,
                            isSelected = isSelected,
                            isAudioPlaying = isAudioPlaying,
                            onToggleAudio = {
                                toggleVoiceGuidance(AudioGuidanceHelper.getSelectStepAudioText(activeLanguage))
                            },
                            onNext = {
                                isSelected = ImeUtils.isImeSelected(context)
                                if (isSelected) {
                                    currentStep = OnboardingStep.KEYBOARD_READY
                                } else {
                                    ImeUtils.showInputMethodPicker(context)
                                }
                            }
                        )
                    }

                    OnboardingStep.KEYBOARD_READY -> {
                        KeyboardReadyStep(
                            language = activeLanguage,
                            voiceTTSEngine = voiceTTSEngine,
                            onNext = { currentStep = OnboardingStep.TUTORIAL }
                        )
                    }

                    OnboardingStep.TUTORIAL -> {
                        InteractiveTutorialStep(
                            language = activeLanguage,
                            preferences = preferences,
                            isAudioPlaying = isAudioPlaying,
                            onToggleAudio = {
                                toggleVoiceGuidance(AudioGuidanceHelper.getTutorialAudioText(activeLanguage))
                            },
                            onFinish = {
                                preferences.setOnboardingCompleted(true)
                                onComplete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioVoiceButton(
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Voice Guidance"
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(24.dp),
        color = if (isPlaying) Color(0xFF059669) else Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlaying) Color(0xFF34D399) else Color(0xFF334155)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                contentDescription = label,
                tint = if (isPlaying) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (isPlaying) "Playing Audio..." else label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) Color.White else Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
fun WelcomeStep(
    language: Language,
    isAudioPlaying: Boolean,
    onToggleAudio: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Logo Banner
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF059669), Color(0xFF10B981))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Welcome to Global Keyboard",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                "Smart multilingual typing with Hinglish transliteration, dynamic themes, and instant voice dictation.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AudioVoiceButton(
                isPlaying = isAudioPlaying,
                onToggle = onToggleAudio,
                label = "Listen in ${language.displayName}"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Feature Highlights
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureRow(
                    icon = Icons.Default.Translate,
                    title = "abc → मराठी / हिंदी Transliteration",
                    desc = "Type 'amhi' to get 'आम्ही' or 'namaste' to get 'नमस्ते'"
                )
                FeatureRow(
                    icon = Icons.Default.Mic,
                    title = "Real-Time Voice Typing",
                    desc = "Speak naturally in your native language with instant text output"
                )
                FeatureRow(
                    icon = Icons.Default.Palette,
                    title = "Dynamic Themes & Key Borders",
                    desc = "Vibrant colors, dark mode, photo backgrounds, and custom styling"
                )
                FeatureRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI Writing & Grammar Tools",
                    desc = "Instant tone transformation and offline smart replies"
                )
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun PermissionsStep(
    language: Language,
    isAudioPlaying: Boolean,
    onToggleAudio: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Privacy & System Alert",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Why Android displays a keyboard permission warning",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            AudioVoiceButton(
                isPlaying = isAudioPlaying,
                onToggle = onToggleAudio,
                label = "Listen Explanation"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3346)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Standard Android Security Alert", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("When enabling any custom keyboard, Android OS displays a default warning dialog. This is completely standard across all Android devices.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("100% On-Device & Private", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Global Keyboard Dynamic processes your typing locally. Your personal keystrokes, passwords, and private data are never logged or sold.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Audio Recording Permission", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Microphone access is used strictly when you tap the mic key to speak and convert voice into text in real-time.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back", color = Color.White)
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                Text("I Understand & Agree", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun LanguageSetupStep(
    activeLanguage: Language,
    enabledLanguages: List<Language>,
    onSelectPrimary: (Language) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Choose Your Language",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Select your primary typing and transliteration language:",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Language.ALL_LANGUAGES) { lang ->
                    val isSelected = lang.id == activeLanguage.id
                    Surface(
                        onClick = { onSelectPrimary(lang) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF064E3B) else Color(0xFF1E1F2A),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF10B981) else Color(0xFF2E3346)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(lang.flagEmoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        lang.displayName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        lang.nativeName,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF34D399) else Color(0xFF94A3B8)
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back", color = Color.White)
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                Text("Continue to Setup", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun EnableKeyboardStep(
    context: Context,
    language: Language,
    isEnabled: Boolean,
    isAudioPlaying: Boolean,
    onToggleAudio: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF059669)),
                contentAlignment = Alignment.Center
            ) {
                Text("म", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "Global Keyboard Dynamic",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step 1 pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B)
            ) {
                Text(
                    "Step 1 of 2",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AudioVoiceButton(
                isPlaying = isAudioPlaying,
                onToggle = onToggleAudio,
                label = "Audio: How to Enable"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3346)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "1. Tap 'Enable Keyboard' below\n2. Switch on 'Global Keyboard Dynamic'\n3. Press OK on the standard confirmation dialog",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 22.sp
                    )

                    if (isEnabled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF064E3B)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                Text("Keyboard is Enabled in Android!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) Color(0xFF10B981) else Color(0xFF059669)
            )
        ) {
            Text(
                if (isEnabled) "Next: Select Keyboard" else "Enable Keyboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SelectKeyboardStep(
    context: Context,
    language: Language,
    isSelected: Boolean,
    isAudioPlaying: Boolean,
    onToggleAudio: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF059669)),
                contentAlignment = Alignment.Center
            ) {
                Text("म", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "Global Keyboard Dynamic",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B)
            ) {
                Text(
                    "Step 2 of 2",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AudioVoiceButton(
                isPlaying = isAudioPlaying,
                onToggle = onToggleAudio,
                label = "Audio: How to Select"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3346)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "1. Tap 'Select Keyboard' below\n2. In the 'Change Keyboard' dialog, choose 'Global Keyboard Dynamic'\n3. That's it! Your keyboard is ready to use",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 22.sp
                    )

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF064E3B)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                Text("Selected as Active Keyboard!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) Color(0xFF10B981) else Color(0xFF059669)
            )
        ) {
            Text(
                if (isSelected) "Proceed to Ready Screen" else "Select Global Keyboard Dynamic",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KeyboardReadyStep(
    language: Language,
    voiceTTSEngine: VoiceTTSEngine,
    onNext: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
        val readyVoice = AudioGuidanceHelper.getReadyAudioText(language)
        voiceTTSEngine.speak(readyVoice, language.ttsLocaleTag)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Green Circle with Checkmark (exact match to video at 00:14)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim.value)
                    .clip(CircleShape)
                    .background(Color(0xFF059669)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Ready",
                    tint = Color.White,
                    modifier = Modifier.size(68.dp)
                )
            }

            Text(
                text = "Keyboard is Ready!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "कीबोर्ड तयार आहे • आपका कीबोर्ड तैयार है",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF34D399)
            )

            Text(
                text = "Global Keyboard Dynamic is successfully configured as your active input method. You can now use it in WhatsApp, Chrome, Messages, and all apps.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
        ) {
            Text("See Quick Tutorial", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InteractiveTutorialStep(
    language: Language,
    preferences: LingoKeyPreferences,
    isAudioPlaying: Boolean,
    onToggleAudio: () -> Unit,
    onFinish: () -> Unit
) {
    var testInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Keyboard Quick Guide",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Try these powerful features right now:",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            AudioVoiceButton(
                isPlaying = isAudioPlaying,
                onToggle = onToggleAudio,
                label = "Audio: How to Type"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Test Typing Box
            OutlinedTextField(
                value = testInput,
                onValueChange = { testInput = it },
                placeholder = { Text("Tap here to test typing...", color = Color(0xFF64748B)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Feature Guide Cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TutorialTipRow(
                    badge = "abc → मराठी",
                    title = "Phonetic Transliteration",
                    desc = "Type 'amhi' to produce 'आम्ही', or 'bharat' to get 'भारत'."
                )
                TutorialTipRow(
                    badge = "Spacebar Drag",
                    title = "Smooth Cursor Movement",
                    desc = "Slide your finger horizontally across the spacebar to position the cursor."
                )
                TutorialTipRow(
                    badge = "Mic Key 🎙️",
                    title = "Real-Time Voice Typing",
                    desc = "Tap the microphone on the toolbar to speak in Marathi, Hindi, or English."
                )
                TutorialTipRow(
                    badge = "Toolbar ⚡",
                    title = "Emoji, Stickers & AI",
                    desc = "Access festive cultural stickers, emoji prediction, and instant AI rewrites."
                )
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
        ) {
            Text("Go to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181924)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3346)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF059669).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text(desc, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
fun TutorialTipRow(badge: String, title: String, desc: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161722)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26293A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF064E3B)
            ) {
                Text(
                    text = badge,
                    color = Color(0xFF6EE7B7),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Text(desc, fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}
