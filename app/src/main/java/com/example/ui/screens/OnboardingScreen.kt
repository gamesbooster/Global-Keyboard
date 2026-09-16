package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.example.model.LanguageCategory
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

    val voiceGuidanceEnabled by preferences.voiceGuidanceEnabled.collectAsState()

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        currentStep = OnboardingStep.LANGUAGE_SETUP
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

    // Auto-play voice guidance when entering step or on first launch (default ON)
    LaunchedEffect(currentStep, activeLanguage) {
        voiceTTSEngine.stop()
        if (voiceGuidanceEnabled) {
            delay(350)
            val guidanceText = when (currentStep) {
                OnboardingStep.WELCOME -> AudioGuidanceHelper.getWelcomeAudioText(activeLanguage)
                OnboardingStep.PERMISSIONS_PRIVACY -> AudioGuidanceHelper.getPermissionsAudioText(activeLanguage)
                OnboardingStep.LANGUAGE_SETUP -> AudioGuidanceHelper.getLanguageStepAudioText(activeLanguage)
                OnboardingStep.ENABLE_KEYBOARD -> AudioGuidanceHelper.getEnableStepAudioText(activeLanguage)
                OnboardingStep.SELECT_KEYBOARD -> AudioGuidanceHelper.getSelectStepAudioText(activeLanguage)
                OnboardingStep.KEYBOARD_READY -> AudioGuidanceHelper.getReadyAudioText(activeLanguage)
                OnboardingStep.TUTORIAL -> AudioGuidanceHelper.getTutorialAudioText(activeLanguage)
            }
            voiceTTSEngine.speak(guidanceText, activeLanguage.ttsLocaleTag)
            isAudioPlaying = true
        } else {
            isAudioPlaying = false
        }
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
                            onNext = {
                                val hasAudioPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasAudioPerm) {
                                    currentStep = OnboardingStep.LANGUAGE_SETUP
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
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
                imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
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
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(30.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            AudioVoiceButton(
                isPlaying = isAudioPlaying,
                onToggle = onToggleAudio,
                label = "Listen Explanation"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // HERO CARD MATCHING USER SCREENSHOT: "Your data is safe"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Safe Shield",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Your data is safe",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            "Global Keyboard Dynamic does not collect anything that you type. The warning you see during installation is displayed for ALL THIRD-PARTY keyboards from the Android System.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3346)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Column {
                            Text("100% On-Device & Private", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                            Text("Your personal keystrokes, passwords, and private messages are processed entirely locally and never logged or sent to any remote server.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Column {
                            Text("Microphone for Real-time Voice Typing", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                            Text("Microphone access is strictly user-initiated when you tap the mic button to speak, converting voice to text instantly.", fontSize = 11.sp, color = Color(0xFF94A3B8))
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LanguageCategory.ALL) }

    val filteredLanguages = remember(searchQuery, selectedCategory) {
        Language.ALL_LANGUAGES.filter { lang ->
            val matchesCategory = when (selectedCategory) {
                LanguageCategory.ALL -> true
                LanguageCategory.INDIAN -> !lang.isGlobal
                LanguageCategory.GLOBAL -> lang.isGlobal
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                lang.displayName.contains(searchQuery, ignoreCase = true) ||
                lang.nativeName.contains(searchQuery, ignoreCase = true) ||
                lang.code.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                "Choose Your Language",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Select your primary typing & transliteration language (${Language.ALL_LANGUAGES.size} available):",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 30+ languages...", fontSize = 12.5.sp, color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1C28),
                    unfocusedContainerColor = Color(0xFF141620),
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF2E3346),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageCategory.entries.forEach { category ->
                    val isCatSelected = selectedCategory == category
                    Surface(
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCatSelected) Color(0xFF059669) else Color(0xFF1E202E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCatSelected) Color(0xFF34D399) else Color(0xFF2E3346)
                        )
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCatSelected) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FULL-SCREEN LazyColumn with 20% reduced vertical height per card
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredLanguages) { lang ->
                    val isSelected = lang.id == activeLanguage.id
                    Surface(
                        onClick = { onSelectPrimary(lang) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF064E3B) else Color(0xFF1A1C28),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF10B981) else Color(0xFF2B2F42)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(lang.flagEmoji, fontSize = 20.sp)
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            lang.displayName,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 13.5.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (lang.isGlobal) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (lang.isGlobal) "Global" else "Indian",
                                                color = if (lang.isGlobal) Color(0xFF60A5FA) else Color(0xFFFBBF24),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "${lang.nativeName} • ${lang.layoutType.name}",
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFF34D399) else Color(0xFF94A3B8)
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
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
