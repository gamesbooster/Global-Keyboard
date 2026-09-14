package com.example.keyboard

import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import com.example.engine.smartreply.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.ClipboardRepository
import com.example.data.LingoKeyPreferences
import com.example.engine.*
import com.example.model.*
import kotlinx.coroutines.launch

@Composable
fun ComposeKeyboardView(
    preferences: LingoKeyPreferences,
    clipboardRepository: ClipboardRepository,
    aiProvider: AIProvider,
    voiceTTSEngine: VoiceTTSEngine,
    onCommitText: (String) -> Unit,
    onDeleteSurroundingText: (Int, Int) -> Unit,
    onPerformEditorAction: () -> Unit,
    onGetContextText: () -> String,
    onReplaceAllText: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenThemesStore: () -> Unit = onOpenSettings,
    onMoveCursor: (Int) -> Unit = {},
    onStartSpeechRecognition: (() -> Unit)? = null,
    onStopSpeechRecognition: (() -> Unit)? = null,
    registerVoiceListener: (((VoiceTypingStatus, Float, String) -> Unit) -> Unit)? = null,
    onGetEditorInfo: () -> EditorInfo? = { null },
    isInputConnectionActive: () -> Boolean = { true }
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val smartReplyRepository = remember {
        SmartReplyRepository(
            provider = GeminiSmartReplyProvider(),
            usageRepository = DefaultSmartReplyUsageRepository(preferences)
        )
    }
    val currentEditorInfo = onGetEditorInfo()
    val isSensitive = remember(currentEditorInfo) {
        smartReplyRepository.isSensitiveField(currentEditorInfo)
    }
    val isConnActive = isInputConnectionActive()

    val currentTheme by preferences.currentTheme.collectAsState()
    val activeLanguage by preferences.activeLanguage.collectAsState()
    val enabledLanguages by preferences.enabledLanguages.collectAsState()
    val showNumberRow by preferences.showNumberRow.collectAsState()
    val showSuggestions by preferences.showSuggestions.collectAsState()
    val transliterationEnabled by preferences.transliterationEnabled.collectAsState()
    val emojiSuggestionsEnabled by preferences.emojiSuggestionsEnabled.collectAsState()
    val autoCorrection by preferences.autoCorrection.collectAsState()
    val keyVibration by preferences.keyVibration.collectAsState()
    val voiceGender by preferences.voiceGender.collectAsState()
    val realtimeTTSMode by preferences.realtimeTTSMode.collectAsState()
    val realtimeAutoTranslate by preferences.realtimeAutoTranslate.collectAsState()
    val targetTranslateLang by preferences.targetTranslationLanguage.collectAsState()
    val autoDetectLanguage by preferences.autoDetectLanguage.collectAsState()
    val keyHeightDp by preferences.keyHeightDp.collectAsState()
    val keyPreviewEnabled by preferences.keyPreviewEnabled.collectAsState()

    var keyboardMode by remember { mutableStateOf(KeyboardMode.ALPHABET) }
    var isShifted by remember { mutableStateOf(false) }
    var isCapsLock by remember { mutableStateOf(false) }
    var currentComposingWord by remember { mutableStateOf("") }
    var lastCommittedWord by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf("I", "Hello", "How", "Thank you")) }

    // Quick Language Selector Sheet State
    var showLanguagePickerSheet by remember { mutableStateOf(false) }

    // Subpanel States
    var aiActionState by remember { mutableStateOf<AIState>(AIState.Idle) }
    var selectedTone by remember { mutableStateOf(ToneType.PROFESSIONAL) }
    var smartReplies by remember { mutableStateOf<List<SmartReplyOption>>(emptyList()) }
    var translateSourceText by remember { mutableStateOf("") }
    var translateResultText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var voiceRecognizedText by remember { mutableStateOf("") }
    var voiceStatus by remember { mutableStateOf(VoiceTypingStatus.IDLE) }
    var voiceRmsLevel by remember { mutableStateOf(0f) }
    var voiceErrorMessage by remember { mutableStateOf<String?>(null) }

    // Register real-time voice recognition callbacks
    DisposableEffect(registerVoiceListener) {
        registerVoiceListener?.invoke { status, rms, textOrError ->
            voiceStatus = status
            voiceRmsLevel = rms
            when (status) {
                VoiceTypingStatus.IDLE -> {
                    if (textOrError.isNotBlank()) {
                        voiceRecognizedText = textOrError
                    }
                }
                VoiceTypingStatus.CONNECTING -> {
                    voiceErrorMessage = null
                    if (textOrError.isNotBlank()) {
                        voiceRecognizedText = textOrError
                    }
                }
                VoiceTypingStatus.LISTENING -> {
                    voiceErrorMessage = null
                    if (textOrError.isNotBlank()) {
                        voiceRecognizedText = textOrError
                    }
                }
                VoiceTypingStatus.ERROR -> {
                    voiceErrorMessage = textOrError
                }
            }
        }
        onDispose {
            onStopSpeechRecognition?.invoke()
        }
    }

    // Refresh suggestions including phonetic transliteration & contextual emojis
    fun refreshSuggestions(composingWord: String) {
        val context = onGetContextText().ifEmpty { lastCommittedWord }
        val base = SuggestionEngine.getSuggestions(composingWord, context, activeLanguage.code)

        val translitCandidates = if (transliterationEnabled && composingWord.isNotBlank()) {
            TransliteratorEngine.getTransliterationCandidates(composingWord, activeLanguage.code)
        } else emptyList()

        val emojiCandidates = if (emojiSuggestionsEnabled && composingWord.isNotBlank()) {
            EmojiSuggestionEngine.getMatchingEmojis(composingWord)
        } else emptyList()

        val combined = mutableListOf<String>()
        if (translitCandidates.isNotEmpty()) {
            combined.addAll(translitCandidates)
        }
        for (s in base) {
            if (!combined.contains(s)) {
                combined.add(s)
            }
        }
        if (emojiCandidates.isNotEmpty()) {
            for (e in emojiCandidates.take(2)) {
                if (!combined.contains(e)) {
                    combined.add(e)
                }
            }
        }
        suggestions = combined
    }

    // Update suggestions when active language or preferences change
    LaunchedEffect(activeLanguage.code, transliterationEnabled, emojiSuggestionsEnabled) {
        refreshSuggestions(currentComposingWord)
    }

    fun vibrate() {
        if (keyVibration) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Trigger Real-time Speech Voice Output
    fun triggerWordTTS(word: String, isTranslated: Boolean = false) {
        if (realtimeTTSMode == RealtimeTTSMode.OFF) return
        val cleanWord = word.trim()
        if (cleanWord.isBlank()) return

        val langTag = if (isTranslated) targetTranslateLang.ttsLocaleTag else activeLanguage.ttsLocaleTag
        voiceTTSEngine.speakWord(cleanWord, langTag)
    }

    fun handleKeyPress(char: String) {
        vibrate()
        val textToCommit = char

        onCommitText(textToCommit)
        if (isShifted && !isCapsLock) {
            isShifted = false
        }

        // Check if punctuation completes a word
        if (char in "\n\t.,!?") {
            val finishedWord = currentComposingWord.trim()
            if (finishedWord.isNotEmpty()) {
                lastCommittedWord = finishedWord
                SuggestionEngine.learnWord(finishedWord)
                if (autoDetectLanguage && finishedWord.length >= 2) {
                    val detected = LanguageDetector.detectLanguage(finishedWord)
                    if (detected.id != activeLanguage.id) {
                        preferences.setActiveLanguage(detected)
                    }
                }

                if (realtimeTTSMode == RealtimeTTSMode.WORD_BY_WORD) {
                    val wordToSpeak = if (realtimeAutoTranslate) {
                        GoogleTranslationEngine.translateFast(finishedWord, activeLanguage.code, targetTranslateLang.code)
                    } else {
                        finishedWord
                    }
                    triggerWordTTS(wordToSpeak, isTranslated = realtimeAutoTranslate)
                }
            }
            currentComposingWord = ""
            refreshSuggestions("")
        } else if (char != " ") {
            currentComposingWord += textToCommit
            // Prefetch translation in background while user types so Space is instant!
            if (realtimeAutoTranslate && currentComposingWord.isNotBlank()) {
                GoogleTranslationEngine.prefetch(currentComposingWord, activeLanguage.code, targetTranslateLang.code)
            }
            // Auto-detect language dynamically while typing
            if (autoDetectLanguage && currentComposingWord.length >= 3) {
                val detected = LanguageDetector.detectLanguage(currentComposingWord)
                if (detected.id != activeLanguage.id && detected.layoutType == activeLanguage.layoutType) {
                    // Update active language seamlessly
                    preferences.setActiveLanguage(detected)
                }
            }
            refreshSuggestions(currentComposingWord)
        }
    }

    fun handleBackspace() {
        vibrate()
        onDeleteSurroundingText(1, 0)
        if (currentComposingWord.isNotEmpty()) {
            currentComposingWord = currentComposingWord.dropLast(1)
            refreshSuggestions(currentComposingWord)
        } else {
            refreshSuggestions("")
        }
    }

    fun handleSpace() {
        vibrate()
        var wordToProcess = currentComposingWord.trim()

        // 1. Auto-detection on space
        if (autoDetectLanguage && wordToProcess.length >= 2) {
            val detected = LanguageDetector.detectLanguage(wordToProcess)
            if (detected.id != activeLanguage.id) {
                preferences.setActiveLanguage(detected)
            }
        }

        // 2. Hindi / Bengali / Indic Transliteration on Space
        if (transliterationEnabled && wordToProcess.isNotEmpty()) {
            val translitCandidates = TransliteratorEngine.getTransliterationCandidates(wordToProcess, activeLanguage.code)
            if (translitCandidates.isNotEmpty() && translitCandidates.first() != wordToProcess) {
                val transliterated = translitCandidates.first()
                onDeleteSurroundingText(currentComposingWord.length, 0)
                onCommitText(transliterated)
                wordToProcess = transliterated
            }
        }

        // 3. Auto-correction
        if (autoCorrection && wordToProcess.isNotEmpty()) {
            val corrected = SuggestionEngine.getAutoCorrection(wordToProcess, activeLanguage.code)
            if (corrected != null && corrected != wordToProcess) {
                onDeleteSurroundingText(currentComposingWord.length, 0)
                onCommitText(corrected)
                wordToProcess = corrected
            }
        }

        // 4. Realtime auto translate check
        var wasTranslated = false
        if (realtimeAutoTranslate && wordToProcess.isNotBlank()) {
            val trans = GoogleTranslationEngine.translateFast(wordToProcess, activeLanguage.code, targetTranslateLang.code)
            if (trans != wordToProcess && trans.isNotBlank()) {
                onDeleteSurroundingText(wordToProcess.length, 0)
                onCommitText(trans)
                wordToProcess = trans
                wasTranslated = true
            } else {
                // If not cached yet, query Google Translate asynchronously and replace word
                val wordForAsync = wordToProcess
                val srcLang = activeLanguage.code
                val dstLang = targetTranslateLang.code
                coroutineScope.launch {
                    val online = GoogleTranslationEngine.translate(wordForAsync, srcLang, dstLang)
                    if (online.isNotBlank() && online != wordForAsync) {
                        onDeleteSurroundingText(wordForAsync.length + 1, 0)
                        onCommitText("$online ")
                        triggerWordTTS(online, isTranslated = true)
                    }
                }
            }
        }

        // 5. Realtime Voice Output (TTS) on Space!
        if (wordToProcess.isNotEmpty()) {
            triggerWordTTS(wordToProcess, isTranslated = wasTranslated)
            lastCommittedWord = wordToProcess
            SuggestionEngine.learnWord(wordToProcess)
        }

        // Commit space and reset composing word
        onCommitText(" ")
        currentComposingWord = ""
        refreshSuggestions("")
    }

    val backgroundModifier = if (currentTheme.backgroundGradient != null) {
        Modifier.background(Brush.verticalGradient(currentTheme.backgroundGradient!!))
    } else {
        Modifier.background(currentTheme.backgroundColor)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(backgroundModifier)
            .navigationBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        // TOP SMART TOOLBAR
        SmartToolbar(
            theme = currentTheme,
            activeLanguage = activeLanguage,
            voiceGender = voiceGender,
            realtimeTTSMode = realtimeTTSMode,
            realtimeAutoTranslate = realtimeAutoTranslate,
            targetTranslateLang = targetTranslateLang,
            autoDetectLanguage = autoDetectLanguage,
            transliterationEnabled = transliterationEnabled,
            onToggleTransliteration = {
                val nextState = !transliterationEnabled
                preferences.setTransliterationEnabled(nextState)
            },
            showNumberRow = showNumberRow,
            onToggleNumberRow = {
                preferences.setShowNumberRow(!showNumberRow)
            },
            keyHeightDp = keyHeightDp,
            onCycleHeight = {
                val next = when (keyHeightDp) {
                    42 -> 46
                    46 -> 50
                    50 -> 56
                    56 -> 60
                    else -> 42
                }
                preferences.setKeyHeightDp(next)
            },
            onOpenLanguagePicker = {
                keyboardMode = if (keyboardMode == KeyboardMode.LANGUAGE_PANEL) KeyboardMode.ALPHABET else KeyboardMode.LANGUAGE_PANEL
            },
            onOpenAI = { keyboardMode = KeyboardMode.AI_PANEL; aiActionState = AIState.Idle },
            onOpenTranslate = {
                keyboardMode = KeyboardMode.TRANSLATE_PANEL
                translateSourceText = onGetContextText()
                if (translateSourceText.isNotBlank()) {
                    translateResultText = GoogleTranslationEngine.translateFast(translateSourceText, activeLanguage.code, targetTranslateLang.code)
                    coroutineScope.launch {
                        isTranslating = true
                        val online = GoogleTranslationEngine.translate(translateSourceText, activeLanguage.code, targetTranslateLang.code)
                        translateResultText = online
                        isTranslating = false
                    }
                }
            },
            onOpenVoice = {
                keyboardMode = KeyboardMode.VOICE_PANEL
                voiceRecognizedText = ""
                voiceErrorMessage = null
                onStartSpeechRecognition?.invoke()
            },
            onToggleVoiceGender = {
                val allGenders = VoiceGender.values()
                val nextIndex = (allGenders.indexOf(voiceGender) + 1) % allGenders.size
                val nextGender = allGenders[nextIndex]
                preferences.setVoiceGender(nextGender)
                voiceTTSEngine.setVoiceGender(nextGender)
                voiceTTSEngine.speak("Switched to ${nextGender.displayName}", activeLanguage.ttsLocaleTag)
            },
            onToggleRealtimeTTS = {
                val nextMode = if (realtimeTTSMode == RealtimeTTSMode.OFF) RealtimeTTSMode.WORD_BY_WORD else RealtimeTTSMode.OFF
                preferences.setRealtimeTTSMode(nextMode)
                if (nextMode != RealtimeTTSMode.OFF) {
                    voiceTTSEngine.speak("Live voice output enabled", activeLanguage.ttsLocaleTag)
                }
            },
            onToggleRealtimeTranslate = {
                val nextState = !realtimeAutoTranslate
                preferences.setRealtimeAutoTranslate(nextState)
                if (nextState && realtimeTTSMode != RealtimeTTSMode.OFF) {
                    voiceTTSEngine.speak("Realtime auto translation enabled to ${targetTranslateLang.displayName}", targetTranslateLang.ttsLocaleTag)
                }
            },
            onOpenClipboard = { keyboardMode = KeyboardMode.CLIPBOARD },
            onOpenStickers = { keyboardMode = KeyboardMode.EMOJI },
            onOpenEmoji = { keyboardMode = KeyboardMode.EMOJI },
            onOpenSettings = onOpenSettings,
            onOpenTools = { keyboardMode = KeyboardMode.TOOLS_PANEL },
            onOpenSmartReply = { keyboardMode = KeyboardMode.SMART_REPLY_PANEL }
        )

        // DYNAMIC KEYBOARD PANEL HEIGHT (Strictly synchronized with keyHeightDp to prevent panel reduction)
        val standardPanelHeight = remember(keyHeightDp, showNumberRow, showSuggestions) {
            var h = (keyHeightDp * 4).dp + 16.dp
            if (showSuggestions) h += 40.dp
            if (showNumberRow) h += ((keyHeightDp * 0.78f).coerceAtLeast(36f)).dp + 4.dp
            h.coerceAtLeast(265.dp)
        }

        // CONTENT AREA (Panels OR Main Keyboard)
        AnimatedContent(
            targetState = keyboardMode,
            label = "keyboard_mode_anim"
        ) { mode ->
            when (mode) {
                KeyboardMode.TOOLS_PANEL -> {
                    ToolsPanel(
                        theme = currentTheme,
                        onOpenSmartReply = { keyboardMode = KeyboardMode.SMART_REPLY_PANEL },
                        onOpenTranslate = {
                            keyboardMode = KeyboardMode.TRANSLATE_PANEL
                            translateSourceText = onGetContextText()
                        },
                        onOpenVoice = {
                            keyboardMode = KeyboardMode.VOICE_PANEL
                            voiceRecognizedText = ""
                            voiceErrorMessage = null
                            onStartSpeechRecognition?.invoke()
                        },
                        onOpenClipboard = { keyboardMode = KeyboardMode.CLIPBOARD },
                        onOpenAI = { keyboardMode = KeyboardMode.AI_PANEL; aiActionState = AIState.Idle },
                        onOpenThemes = {
                            onOpenThemesStore()
                        },
                        onOpenLanguages = { keyboardMode = KeyboardMode.LANGUAGE_PANEL },
                        onOpenSettings = onOpenSettings,
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.SMART_REPLY_PANEL -> {
                    SmartReplyPanel(
                        theme = currentTheme,
                        repository = smartReplyRepository,
                        isSensitiveField = isSensitive,
                        isInputConnectionActive = isConnActive,
                        onInsertReply = { replyText ->
                            onCommitText(replyText)
                            keyboardMode = KeyboardMode.ALPHABET
                        },
                        onBack = { keyboardMode = KeyboardMode.TOOLS_PANEL },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        onOpenSettings = onOpenSettings,
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.THEMES_PANEL -> {
                    ThemesPanel(
                        theme = currentTheme,
                        onSelectTheme = { selectedTheme ->
                            preferences.setTheme(selectedTheme)
                        },
                        onOpenFullSettings = onOpenSettings,
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.AI_PANEL -> {
                    AIPanel(
                        theme = currentTheme,
                        aiState = aiActionState,
                        currentContextText = onGetContextText(),
                        selectedTone = selectedTone,
                        smartReplies = smartReplies,
                        onSelectTone = { tone, textToProcess ->
                            selectedTone = tone
                            coroutineScope.launch {
                                aiActionState = AIState.Loading
                                val text = textToProcess.ifBlank { onGetContextText() }
                                val res = aiProvider.rewrite(text, tone)
                                aiActionState = res.fold(
                                    onSuccess = { AIState.Success(originalText = text, resultText = it, actionTitle = "${tone.iconEmoji} ${tone.displayName} Rewrite") },
                                    onFailure = { AIState.Error(it.message ?: "AI rewrite failed") }
                                )
                            }
                        },
                        onFixGrammar = { textToProcess ->
                            coroutineScope.launch {
                                aiActionState = AIState.Loading
                                val text = textToProcess.ifBlank { onGetContextText() }
                                val res = aiProvider.fixGrammar(text)
                                aiActionState = res.fold(
                                    onSuccess = { AIState.Success(originalText = text, resultText = it, actionTitle = "Grammar & Spelling Corrected") },
                                    onFailure = { AIState.Error(it.message ?: "Grammar fix failed") }
                                )
                            }
                        },
                        onSmartReplies = { textToProcess ->
                            coroutineScope.launch {
                                aiActionState = AIState.Loading
                                val text = textToProcess.ifBlank { onGetContextText() }
                                val res = aiProvider.generateSmartReplies(text)
                                smartReplies = res.getOrElse { emptyList() }
                                aiActionState = AIState.Idle
                            }
                        },
                        onContinueText = { textToProcess ->
                            coroutineScope.launch {
                                aiActionState = AIState.Loading
                                val text = textToProcess.ifBlank { onGetContextText() }
                                val res = aiProvider.continueText(text)
                                aiActionState = res.fold(
                                    onSuccess = { AIState.Success(originalText = text, resultText = it, actionTitle = "AI Continued Text") },
                                    onFailure = { AIState.Error(it.message ?: "Failed") }
                                )
                            }
                        },
                        onCustomPrompt = { prompt ->
                            coroutineScope.launch {
                                aiActionState = AIState.Loading
                                val res = aiProvider.customPrompt(prompt)
                                aiActionState = res.fold(
                                    onSuccess = { AIState.Success(originalText = prompt, resultText = it, actionTitle = "✨ Gemini AI Response") },
                                    onFailure = { AIState.Error(it.message ?: "AI generation failed") }
                                )
                            }
                        },
                        onInsertResult = { text ->
                            onReplaceAllText(text)
                            keyboardMode = KeyboardMode.ALPHABET
                        },
                        onSpeakResult = { text ->
                            voiceTTSEngine.speak(text, activeLanguage.ttsLocaleTag)
                        },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.TRANSLATE_PANEL -> {
                    TranslatePanel(
                        theme = currentTheme,
                        sourceLang = activeLanguage,
                        targetLang = targetTranslateLang,
                        sourceText = translateSourceText,
                        resultText = translateResultText,
                        isTranslating = isTranslating,
                        allLanguages = Language.ALL_LANGUAGES,
                        onSourceTextChange = { txt ->
                            translateSourceText = txt
                            if (txt.isNotBlank()) {
                                translateResultText = GoogleTranslationEngine.translateFast(txt, activeLanguage.code, targetTranslateLang.code)
                                coroutineScope.launch {
                                    isTranslating = true
                                    val online = GoogleTranslationEngine.translate(txt, activeLanguage.code, targetTranslateLang.code)
                                    translateResultText = online
                                    isTranslating = false
                                }
                            } else {
                                translateResultText = ""
                            }
                        },
                        onTargetLangChange = { lang ->
                            preferences.setTargetTranslationLanguage(lang)
                            if (translateSourceText.isNotBlank()) {
                                translateResultText = GoogleTranslationEngine.translateFast(translateSourceText, activeLanguage.code, lang.code)
                                coroutineScope.launch {
                                    isTranslating = true
                                    val online = GoogleTranslationEngine.translate(translateSourceText, activeLanguage.code, lang.code)
                                    translateResultText = online
                                    isTranslating = false
                                }
                            }
                        },
                        onTranslate = {
                            if (translateSourceText.isNotBlank()) {
                                coroutineScope.launch {
                                    isTranslating = true
                                    val online = GoogleTranslationEngine.translate(translateSourceText, activeLanguage.code, targetTranslateLang.code)
                                    translateResultText = online
                                    isTranslating = false
                                }
                            }
                        },
                        onReverseLanguages = {
                            val oldSource = activeLanguage
                            val oldTarget = targetTranslateLang
                            preferences.setActiveLanguage(oldTarget)
                            preferences.setTargetTranslationLanguage(oldSource)
                            val oldRes = translateResultText
                            translateResultText = translateSourceText
                            translateSourceText = oldRes
                            if (translateSourceText.isNotBlank()) {
                                coroutineScope.launch {
                                    isTranslating = true
                                    val online = GoogleTranslationEngine.translate(translateSourceText, oldTarget.code, oldSource.code)
                                    translateResultText = online
                                    isTranslating = false
                                }
                            }
                        },
                        onInsert = {
                            if (translateResultText.isNotBlank()) {
                                onReplaceAllText(translateResultText)
                            }
                            keyboardMode = KeyboardMode.ALPHABET
                        },
                        onSpeak = { text, lang ->
                            voiceTTSEngine.speak(text, lang.ttsLocaleTag)
                        },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.VOICE_PANEL -> {
                    VoicePanel(
                        theme = currentTheme,
                        voiceStatus = voiceStatus,
                        rmsLevel = voiceRmsLevel,
                        errorMessage = voiceErrorMessage,
                        activeLanguage = activeLanguage,
                        allLanguages = enabledLanguages.ifEmpty { Language.ALL_LANGUAGES },
                        recognizedText = voiceRecognizedText,
                        targetLang = targetTranslateLang,
                        onSelectLanguage = { newLang ->
                            preferences.setActiveLanguage(newLang)
                            voiceErrorMessage = null
                            onStartSpeechRecognition?.invoke()
                        },
                        onStartListening = {
                            voiceErrorMessage = null
                            onStartSpeechRecognition?.invoke()
                        },
                        onStopListening = {
                            onStopSpeechRecognition?.invoke()
                        },
                        onInsert = {
                            if (voiceRecognizedText.isNotBlank()) {
                                onCommitText(voiceRecognizedText)
                            }
                            keyboardMode = KeyboardMode.ALPHABET
                        },
                        onTranslateAndInsert = {
                            if (voiceRecognizedText.isNotBlank()) {
                                val fast = GoogleTranslationEngine.translateFast(voiceRecognizedText, activeLanguage.code, targetTranslateLang.code)
                                onCommitText(fast)
                                coroutineScope.launch {
                                    val online = GoogleTranslationEngine.translate(voiceRecognizedText, activeLanguage.code, targetTranslateLang.code)
                                    if (online != fast && online.isNotBlank()) {
                                        onDeleteSurroundingText(fast.length, 0)
                                        onCommitText(online)
                                    }
                                }
                            }
                            keyboardMode = KeyboardMode.ALPHABET
                        },
                        onClose = {
                            onStopSpeechRecognition?.invoke()
                            keyboardMode = KeyboardMode.ALPHABET
                        },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.EMOJI -> {
                    EmojiPanel(
                        theme = currentTheme,
                        onEmojiClick = { emoji ->
                            vibrate()
                            onCommitText(emoji)
                        },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        onDelete = { handleBackspace() },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.CLIPBOARD -> {
                    ClipboardPanel(
                        theme = currentTheme,
                        clipboardRepository = clipboardRepository,
                        onPaste = { text ->
                            vibrate()
                            onCommitText(text)
                            keyboardMode = KeyboardMode.ALPHABET
                        },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.LANGUAGE_PANEL -> {
                    LanguagePanel(
                        theme = currentTheme,
                        activeLanguage = activeLanguage,
                        targetLanguage = targetTranslateLang,
                        onSelectSourceLanguage = { lang ->
                            preferences.setAutoDetectLanguage(false)
                            preferences.setActiveLanguage(lang)
                            voiceTTSEngine.speak("Language set to ${lang.displayName}", lang.ttsLocaleTag)
                        },
                        onSelectTargetLanguage = { lang ->
                            preferences.setTargetTranslationLanguage(lang)
                            voiceTTSEngine.speak("Translating to ${lang.displayName}", lang.ttsLocaleTag)
                        },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                else -> {
                    // MAIN TYPING KEYBOARD (ALPHABET / NUMBERS / SYMBOLS)
                    Column {
                        // SUGGESTION ROW
                        if (showSuggestions) {
                            SuggestionBar(
                                suggestions = suggestions,
                                currentComposingWord = currentComposingWord,
                                activeLanguage = activeLanguage,
                                targetLang = targetTranslateLang,
                                realtimeAutoTranslate = realtimeAutoTranslate,
                                theme = currentTheme,
                                onSuggestionClick = { word, isDirectTranslation ->
                                    vibrate()
                                    if (currentComposingWord.isNotEmpty()) {
                                        onDeleteSurroundingText(currentComposingWord.length, 0)
                                    }
                                    val isEmoji = word.length <= 2 && !word.first().isLetterOrDigit()
                                    val textToInsert = if (isDirectTranslation) {
                                        word
                                    } else if (realtimeAutoTranslate && !isEmoji) {
                                        GoogleTranslationEngine.translateFast(word, activeLanguage.code, targetTranslateLang.code)
                                    } else {
                                        word
                                    }
                                    val suffix = if (isEmoji) "" else " "
                                    onCommitText("$textToInsert$suffix")
                                    if (!isEmoji) {
                                        triggerWordTTS(textToInsert, isTranslated = isDirectTranslation || realtimeAutoTranslate)
                                        lastCommittedWord = textToInsert
                                        SuggestionEngine.learnWord(textToInsert)
                                    }
                                    currentComposingWord = ""
                                    refreshSuggestions("")
                                }
                            )
                        }

                        // Dynamic key height according to user ergonomics preference
                        val currentKeyHeight = keyHeightDp.dp

                        // QUICK MATRAS / VELANTI / VOWEL SIGNS BAR (Bengali, Hindi, Marathi, Gujarati, Tamil, Telugu, etc.)
                        val quickMatras = KeyboardLayouts.getQuickMatrasForLayout(activeLanguage.layoutType)
                        if (keyboardMode == KeyboardMode.ALPHABET && quickMatras != null) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(currentTheme.surfaceColor.copy(alpha = 0.5f))
                                    .padding(horizontal = 3.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(quickMatras) { matra ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = currentTheme.keyColor,
                                        border = BorderStroke(0.75.dp, currentTheme.keyBorderColor),
                                        modifier = Modifier
                                            .height((currentKeyHeight * 0.72f).coerceAtLeast(32.dp))
                                            .clickable { handleKeyPress(matra) }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = matra,
                                                color = currentTheme.textColor,
                                                fontSize = 16.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // NUMBER ROW (Optional - Clean Gboard Height)
                        if (showNumberRow && keyboardMode == KeyboardMode.ALPHABET) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                for (num in KeyboardLayouts.NUMBER_ROW) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 2.dp, vertical = 2.dp)
                                    ) {
                                        ThemedKeyBox(
                                            theme = currentTheme,
                                            modifier = Modifier.fillMaxWidth(),
                                            isSpecial = false,
                                            isSelected = false,
                                            height = (currentKeyHeight * 0.78f).coerceAtLeast(36.dp),
                                            onClick = { handleKeyPress(num) }
                                        ) {
                                            Text(
                                                text = num,
                                                color = currentTheme.textColor,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // KEY ROWS ACCORDING TO MODE (Exact Gboard Staggering & Muscle-Memory Geometry)
                        val rows = when (keyboardMode) {
                            KeyboardMode.NUMBERS -> KeyboardLayouts.NUMBERS_PAGE_ROWS
                            KeyboardMode.SYMBOLS -> KeyboardLayouts.SYMBOLS_PAGE_ROWS
                            else -> KeyboardLayouts.getRowsForLayout(activeLanguage.layoutType, isShifted = isShifted || isCapsLock)
                        }

                        for ((rowIndex, rowKeys) in rows.withIndex()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                val isLastRow = (rowIndex == rows.size - 1)

                                // Side spacer for Row 2 or shorter rows without modifiers:
                                // Exactly offsets Row 2 (e.g. A..L has 9 keys -> 0.5f spacer on each side, matching Gboard)
                                if (!isLastRow && rowKeys.size < 10) {
                                    val spacerWeight = ((10 - rowKeys.size) / 2f).coerceAtLeast(0f)
                                    if (spacerWeight > 0f) {
                                        Spacer(modifier = Modifier.weight(spacerWeight))
                                    }
                                }

                                // Left modifier on last row (Shift for alphabet, = / < for symbols)
                                if (isLastRow) {
                                    val modWeight = ((10f - rowKeys.size) / 2f).coerceIn(1.3f, 1.55f)
                                    when (keyboardMode) {
                                        KeyboardMode.ALPHABET -> {
                                            KeySpecialButton(
                                                icon = if (isCapsLock) Icons.Default.KeyboardCapslock else Icons.Default.KeyboardArrowUp,
                                                modifier = Modifier.weight(modWeight),
                                                theme = currentTheme,
                                                isSelected = isShifted || isCapsLock,
                                                height = currentKeyHeight,
                                                onClick = {
                                                    vibrate()
                                                    if (isShifted) {
                                                        isCapsLock = true
                                                        isShifted = false
                                                    } else if (isCapsLock) {
                                                        isCapsLock = false
                                                        isShifted = false
                                                    } else {
                                                        isShifted = true
                                                    }
                                                }
                                            )
                                        }
                                        KeyboardMode.NUMBERS -> {
                                            KeySpecialButton(
                                                text = "=/<",
                                                modifier = Modifier.weight(modWeight),
                                                theme = currentTheme,
                                                height = currentKeyHeight,
                                                onClick = {
                                                    vibrate()
                                                    keyboardMode = KeyboardMode.SYMBOLS
                                                }
                                            )
                                        }
                                        KeyboardMode.SYMBOLS -> {
                                            KeySpecialButton(
                                                text = "123",
                                                modifier = Modifier.weight(modWeight),
                                                theme = currentTheme,
                                                height = currentKeyHeight,
                                                onClick = {
                                                    vibrate()
                                                    keyboardMode = KeyboardMode.NUMBERS
                                                }
                                            )
                                        }
                                        else -> {}
                                    }
                                }

                                // Normal character keys with Gboard-style top hints on alphabet row 0
                                for (key in rowKeys) {
                                    val topHint = if (keyboardMode == KeyboardMode.ALPHABET && rowIndex == 0 && activeLanguage.layoutType == LayoutType.QWERTY) {
                                        when (key.lowercase()) {
                                             "q" -> "1"; "w" -> "2"; "e" -> "3"; "r" -> "4"; "t" -> "5"
                                            "y" -> "6"; "u" -> "7"; "i" -> "8"; "o" -> "9"; "p" -> "0"
                                            else -> null
                                        }
                                    } else null

                                    KeyButton(
                                        text = key,
                                        modifier = Modifier.weight(1f),
                                        theme = currentTheme,
                                        topHint = topHint,
                                        height = currentKeyHeight,
                                        showPreview = keyPreviewEnabled,
                                        onClick = { handleKeyPress(key) }
                                    )
                                }

                                // Right modifier on last row (Backspace with accelerating hold-to-delete)
                                if (isLastRow) {
                                    val modWeight = ((10f - rowKeys.size) / 2f).coerceIn(1.3f, 1.55f)
                                    KeyBackspaceButton(
                                        modifier = Modifier.weight(modWeight),
                                        theme = currentTheme,
                                        height = currentKeyHeight,
                                        onDelete = { handleBackspace() }
                                    )
                                }

                                // Right side spacer for Row 2 or shorter rows without modifiers:
                                if (!isLastRow && rowKeys.size < 10) {
                                    val spacerWeight = ((10 - rowKeys.size) / 2f).coerceAtLeast(0f)
                                    if (spacerWeight > 0f) {
                                        Spacer(modifier = Modifier.weight(spacerWeight))
                                    }
                                }
                            }
                        }

                        // BOTTOM ACTION ROW (Clean Gboard Layout with Cursor Scrubbing)
                        BottomActionRow(
                            theme = currentTheme,
                            keyboardMode = keyboardMode,
                            activeLanguage = activeLanguage,
                            enabledLanguages = enabledLanguages,
                            height = currentKeyHeight,
                            isTransliterationActive = transliterationEnabled,
                            editorInfo = currentEditorInfo,
                            onMoveCursor = onMoveCursor,
                            onSwitchMode = { newMode ->
                                vibrate()
                                keyboardMode = newMode
                            },
                            onSwitchLanguage = {
                                vibrate()
                                preferences.switchNextLanguage()
                            },
                            onSpace = { handleSpace() },
                            onOpenEmoji = {
                                vibrate()
                                keyboardMode = KeyboardMode.EMOJI
                            },
                            onEnter = {
                                vibrate()
                                val finishedWord = currentComposingWord.trim()
                                if (finishedWord.isNotEmpty()) {
                                    triggerWordTTS(finishedWord, isTranslated = realtimeAutoTranslate)
                                    currentComposingWord = ""
                                }
                                onPerformEditorAction()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmartToolbar(
    theme: KeyboardTheme,
    activeLanguage: Language,
    voiceGender: VoiceGender,
    realtimeTTSMode: RealtimeTTSMode,
    realtimeAutoTranslate: Boolean,
    targetTranslateLang: Language,
    autoDetectLanguage: Boolean,
    transliterationEnabled: Boolean = true,
    onToggleTransliteration: () -> Unit = {},
    showNumberRow: Boolean = true,
    onToggleNumberRow: () -> Unit = {},
    keyHeightDp: Int = 48,
    onCycleHeight: () -> Unit = {},
    onOpenLanguagePicker: () -> Unit,
    onOpenAI: () -> Unit,
    onOpenTranslate: () -> Unit,
    onOpenVoice: () -> Unit,
    onToggleVoiceGender: () -> Unit,
    onToggleRealtimeTTS: () -> Unit,
    onToggleRealtimeTranslate: () -> Unit,
    onOpenClipboard: () -> Unit,
    onOpenStickers: () -> Unit = {},
    onOpenEmoji: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTools: () -> Unit = {},
    onOpenSmartReply: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceColor.copy(alpha = 0.5f))
            .padding(horizontal = 4.dp, vertical = 3.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // MULTILINGUAL SELECTOR PILL: [🌐 Auto / EN ⇄ HI ▾]
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = theme.primaryColor.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.6f)),
            modifier = Modifier.clickable { onOpenLanguagePicker() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(if (autoDetectLanguage) "🌐 Auto" else activeLanguage.flagEmoji, fontSize = 12.sp)
                Text(
                    text = if (realtimeAutoTranslate) "→ ${targetTranslateLang.flagEmoji} ${targetTranslateLang.code.uppercase()}" else activeLanguage.code.uppercase(),
                    color = theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Select Language",
                    tint = theme.accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // REALTIME VOICE TTS SPEECH TOGGLE PILL
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (realtimeTTSMode != RealtimeTTSMode.OFF) Color(0xFF10B981).copy(alpha = 0.2f) else theme.keyColor,
            border = BorderStroke(1.dp, if (realtimeTTSMode != RealtimeTTSMode.OFF) Color(0xFF10B981) else theme.keyBorderColor),
            modifier = Modifier.clickable { onToggleRealtimeTTS() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (realtimeTTSMode != RealtimeTTSMode.OFF) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "TTS Voice",
                    tint = if (realtimeTTSMode != RealtimeTTSMode.OFF) Color(0xFF10B981) else theme.textSecondaryColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (realtimeTTSMode != RealtimeTTSMode.OFF) "Live Voice" else "Voice Muted",
                    color = if (realtimeTTSMode != RealtimeTTSMode.OFF) Color(0xFF10B981) else theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // REALTIME TRANSLATE TOGGLE PILL
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (realtimeAutoTranslate) theme.primaryColor else theme.keyColor,
            modifier = Modifier.clickable { onToggleRealtimeTranslate() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Translate,
                    contentDescription = "Translate",
                    tint = if (realtimeAutoTranslate) Color.White else theme.accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (realtimeAutoTranslate) "Translating" else "Translate",
                    color = if (realtimeAutoTranslate) Color.White else theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // TOOLS BUTTON: [ 🛠 Tools ]
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = theme.keySpecialColor,
            modifier = Modifier.clickable { onOpenTools() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.Widgets, contentDescription = "Tools", tint = theme.accentColor, modifier = Modifier.size(14.dp))
                Text("Tools", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // SMART REPLY QUICK BUTTON: [ ✨ Reply ]
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF6366F1).copy(alpha = 0.22f),
            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f)),
            modifier = Modifier.clickable { onOpenSmartReply() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Smart Reply", tint = Color(0xFF818CF8), modifier = Modifier.size(14.dp))
                Text("Smart Reply", color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // AI Assistant Button
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = theme.keySpecialColor,
            modifier = Modifier.clickable { onOpenAI() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = theme.accentColor, modifier = Modifier.size(15.dp))
                Text("AI", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Voice Mic
        IconButton(
            onClick = onOpenVoice,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = theme.accentColor, modifier = Modifier.size(16.dp))
        }

        // Clipboard
        IconButton(
            onClick = onOpenClipboard,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.ContentPaste, contentDescription = "Clipboard", tint = theme.textColor, modifier = Modifier.size(16.dp))
        }

        // Stickers & Festivals
        IconButton(
            onClick = onOpenStickers,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Celebration, contentDescription = "Stickers", tint = theme.accentColor, modifier = Modifier.size(16.dp))
        }

        // Emoji
        IconButton(
            onClick = onOpenEmoji,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.SentimentSatisfied, contentDescription = "Emoji", tint = theme.textColor, modifier = Modifier.size(16.dp))
        }

        // Settings
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SuggestionBar(
    suggestions: List<String>,
    currentComposingWord: String,
    activeLanguage: Language,
    targetLang: Language,
    realtimeAutoTranslate: Boolean,
    theme: KeyboardTheme,
    onSuggestionClick: (String, Boolean) -> Unit
) {
    var liveTrans by remember(currentComposingWord, activeLanguage.code, targetLang.code) {
        mutableStateOf<String?>(
            if (currentComposingWord.isNotBlank()) {
                val trans = GoogleTranslationEngine.translateFast(currentComposingWord, activeLanguage.code, targetLang.code)
                if (trans.isNotBlank() && trans.lowercase() != currentComposingWord.lowercase().trim()) trans else null
            } else null
        )
    }

    LaunchedEffect(currentComposingWord, activeLanguage.code, targetLang.code) {
        if (currentComposingWord.isNotBlank()) {
            val online = GoogleTranslationEngine.translate(currentComposingWord, activeLanguage.code, targetLang.code)
            if (online.isNotBlank() && online.lowercase() != currentComposingWord.lowercase().trim()) {
                liveTrans = online
            }
        } else {
            liveTrans = null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceColor.copy(alpha = 0.75f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Live Translation Highlight Candidate Chip
        val currentTrans = liveTrans
        if (currentTrans != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF6366F1),
                border = BorderStroke(1.dp, Color(0xFF818CF8)),
                modifier = Modifier
                    .weight(1.3f)
                    .clickable { onSuggestionClick(currentTrans, true) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ $liveTrans",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = targetLang.code.uppercase(),
                        color = Color(0xFFE0E7FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 2. Regular Suggestions
        val rawSuggestions = if (suggestions.isEmpty()) {
            listOf("I", "How", "Thank you")
        } else {
            suggestions
        }
        val displaySuggestions = if (liveTrans != null) rawSuggestions.take(3) else rawSuggestions.take(4)
        displaySuggestions.forEachIndexed { index, suggestion ->
            val isPrimary = if (displaySuggestions.size >= 3) index == 1 else index == 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPrimary) theme.primaryColor.copy(alpha = 0.16f)
                        else theme.keyColor.copy(alpha = 0.5f)
                    )
                    .border(
                        width = if (isPrimary) 1.dp else 0.dp,
                        color = if (isPrimary) theme.primaryColor.copy(alpha = 0.35f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSuggestionClick(suggestion, false) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = suggestion,
                    color = if (isPrimary) theme.textColor else theme.textColor.copy(alpha = 0.85f),
                    fontSize = if (isPrimary) 13.5.sp else 13.sp,
                    fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (index < displaySuggestions.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(theme.textSecondaryColor.copy(alpha = 0.25f))
                )
            }
        }
    }
}

@Composable
fun ThemedKeyBox(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isSelected: Boolean = false,
    isPressed: Boolean = false,
    height: Dp = 48.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cornerRadius = when (theme.themeStyle) {
        ThemeStyle.CLAYMORPHISM -> 11f
        ThemeStyle.NEOBRUTALISM -> 5f
        ThemeStyle.FLAT_DESIGN -> 4f
        else -> theme.keyCornerRadius
    }.dp

    val shape = if (isSelected && isSpecial) RoundedCornerShape(16.dp) else RoundedCornerShape(cornerRadius)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    val isTactile3D = theme.themeStyle == ThemeStyle.TACTILE_3D ||
            theme.themeStyle == ThemeStyle.SKEUOMORPHISM ||
            theme.themeStyle == ThemeStyle.NEUMORPHISM_LIGHT ||
            theme.themeStyle == ThemeStyle.NEUMORPHISM_DARK

    // Authentic mechanical spring depression: smoothly depresses to scale(0.96f) on touch
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) {
            if (isTactile3D) 0.95f else 0.97f
        } else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "mechanicalScale"
    )

    val baseModifier = modifier
        .height(height)
        .then(clickModifier)
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }

    // Outer elevation shadow when resting
    val elevationDp = if (isPressed) 0.dp else when (theme.themeStyle) {
        ThemeStyle.TACTILE_3D -> 3.5.dp
        ThemeStyle.SKEUOMORPHISM -> 3.5.dp
        ThemeStyle.NEUMORPHISM_LIGHT, ThemeStyle.NEUMORPHISM_DARK -> 2.5.dp
        ThemeStyle.CLAYMORPHISM -> 4.dp
        ThemeStyle.FLAT_2_0 -> 1.dp
        ThemeStyle.SEMI_TRANSPARENT, ThemeStyle.GLASSMORPHISM -> 0.dp
        ThemeStyle.MINIMALISM, ThemeStyle.FLAT_DESIGN, ThemeStyle.NEOBRUTALISM -> 0.dp
        else -> 1.5.dp
    }

    Box(
        modifier = baseModifier
            .shadow(elevation = elevationDp, shape = shape, clip = false)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        when (theme.themeStyle) {
            ThemeStyle.SEMI_TRANSPARENT -> {
                // THE USER'S BELOVED SEMI-TRANSPARENT STYLE: Translucent keys letting background gradients shine through
                val transBg = if (isSelected) theme.primaryColor.copy(alpha = 0.65f)
                else if (isPressed) theme.primaryColor.copy(alpha = 0.45f)
                else if (isSpecial) theme.keySpecialColor.copy(alpha = 0.45f)
                else theme.keyColor.copy(alpha = 0.32f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(transBg)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (isPressed) 0.22f else 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            if (isPressed) 1.2.dp else 0.85.dp,
                            if (isPressed) theme.accentColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.35f),
                            shape
                        )
                )
            }

            ThemeStyle.TACTILE_3D -> {
                // 3D MECHANICAL BUTTONS: Sculpted tactile keycaps with deep physical switch bevel
                val keyBg = if (isSelected) theme.primaryColor
                else if (isPressed) theme.keyPressedColor
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(
                            if (isPressed) 1.2.dp else 0.8.dp,
                            if (isPressed) theme.primaryColor.copy(alpha = 0.8f) else theme.keyBorderColor,
                            shape
                        )
                )
            }

            ThemeStyle.GLASSMORPHISM -> {
                val glassBg = if (isSelected) theme.primaryColor.copy(alpha = 0.70f)
                else if (isPressed) theme.primaryColor.copy(alpha = 0.45f)
                else if (isSpecial) theme.keySpecialColor.copy(alpha = 0.35f)
                else theme.keyColor.copy(alpha = 0.26f)

                val sheenGradient = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isPressed) 0.35f else 0.22f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(glassBg)
                        .background(sheenGradient)
                        .border(
                            if (isPressed) 1.2.dp else 0.85.dp,
                            Color.White.copy(alpha = if (isPressed) 0.7f else 0.45f),
                            shape
                        )
                )
            }

            ThemeStyle.NEUMORPHISM_LIGHT -> {
                val keyBg = if (isSelected) theme.primaryColor
                else if (isPressed) Color(0xFFD1D8E0)
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(
                            if (isPressed) 1.2.dp else 1.dp,
                            if (isPressed) Color(0xFF94A3B8) else Color.White.copy(alpha = 0.90f),
                            shape
                        )
                )
            }

            ThemeStyle.NEUMORPHISM_DARK -> {
                val keyBg = if (isSelected) theme.primaryColor
                else if (isPressed) Color(0xFF131518)
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(
                            if (isPressed) 1.dp else 0.75.dp,
                            if (isPressed) theme.primaryColor.copy(alpha = 0.7f) else Color(0xFF38404A),
                            shape
                        )
                )
            }

            ThemeStyle.CLAYMORPHISM -> {
                // PUFFY 3D CLAYMORPHISM: Marshmallow soft rounded volume with inner highlight
                val keyBg = if (isSelected) theme.primaryColor
                else if (isPressed) theme.keyPressedColor
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (theme.isDark) 0.20f else 0.45f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = if (theme.isDark) 0.25f else 0.08f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            if (isPressed) theme.primaryColor.copy(alpha = 0.7f) else Color.White.copy(alpha = if (theme.isDark) 0.18f else 0.65f),
                            shape
                        )
                )
            }

            ThemeStyle.SKEUOMORPHISM -> {
                val keyBg = if (isSelected) theme.primaryColor
                else if (isPressed) Color(0xFF16181B)
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(
                            if (isPressed) 1.2.dp else 1.dp,
                            if (isPressed) Color(0xFF0A0C0E) else Color(0xFF2C323A),
                            shape
                        )
                )
            }

            ThemeStyle.FLAT_DESIGN -> {
                val keyBg = if (isSelected) theme.accentColor
                else if (isPressed) theme.keyPressedColor
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                )
            }

            ThemeStyle.FLAT_2_0 -> {
                val keyBg = if (isSelected) theme.primaryColor
                else if (isPressed) theme.keyPressedColor
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(0.6.dp, theme.keyBorderColor.copy(alpha = 0.5f), shape)
                )
            }

            ThemeStyle.NEOBRUTALISM -> {
                // NEOBRUTALISM: Stark high-contrast pop art with thick 2dp black border
                val keyBg = if (isSelected) theme.accentColor
                else if (isPressed) theme.keyPressedColor
                else if (isSpecial) theme.keySpecialColor
                else theme.keyColor

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(1.8.dp, Color(0xFF111111), shape)
                )
            }

            ThemeStyle.MINIMALISM -> {
                val keyBg = if (isSelected) theme.accentColor.copy(alpha = 0.40f)
                else if (isPressed) theme.accentColor.copy(alpha = 0.28f)
                else if (isSpecial) theme.keySpecialColor.copy(alpha = theme.keyAlpha)
                else theme.keyColor.copy(alpha = theme.keyAlpha)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(0.5.dp, theme.keyBorderColor, shape)
                )
            }

            ThemeStyle.MATERIAL_YOU, ThemeStyle.STANDARD -> {
                val keyBg = if (isSelected) theme.primaryColor
                else if (isPressed) theme.keyPressedColor
                else if (isSpecial) theme.keySpecialColor.copy(alpha = theme.keyAlpha)
                else theme.keyColor.copy(alpha = theme.keyAlpha)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(keyBg)
                        .border(
                            0.6.dp,
                            if (isPressed) theme.primaryColor.copy(alpha = 0.4f) else theme.keyBorderColor,
                            shape
                        )
                )
            }
        }

        // Tactile 3D Inset Shadow and Top Sheen ONLY applied to 3D/tactile styles
        if (isTactile3D) {
            if (isPressed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = if (theme.isDark) 0.42f else 0.26f),
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (theme.isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.40f),
                                    Color.Transparent,
                                    if (theme.isDark) Color.Black.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.06f)
                                )
                            )
                        )
                )
            }
        }

        // Key inner content
        content()
    }
}

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    theme: KeyboardTheme,
    topHint: String? = null,
    height: Dp = 48.dp,
    showPreview: Boolean = true,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Auto-dismiss safety timer ensures key pop preview never freezes or stays stuck on screen!
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .pointerInput(text) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            ThemedKeyBox(
                theme = theme,
                modifier = Modifier.fillMaxSize(),
                isSpecial = false,
                isSelected = false,
                isPressed = isPressed,
                height = height,
                onClick = null
            ) {
                if (topHint != null) {
                    Text(
                        text = topHint,
                        color = theme.textSecondaryColor.copy(alpha = 0.65f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 3.5.dp)
                    )
                }
                Text(
                    text = text,
                    color = theme.textColor,
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Gboard-Style Floating 3D Key Preview Popup (Character Magnifier)
        if (isPressed && showPreview && text.length == 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-46).dp)
                    .width(52.dp)
                    .height(48.dp)
                    .zIndex(100f)
                    .shadow(10.dp, shape = RoundedCornerShape(12.dp))
                    .background(
                        if (theme.isDark) Color(0xFF282C37) else Color(0xFFF1F3F4),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        theme.primaryColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (theme.isDark) Color.White else Color(0xFF1E293B)
                )
            }
        }
    }
}

@Composable
fun KeySpecialButton(
    modifier: Modifier = Modifier,
    theme: KeyboardTheme,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSelected: Boolean = false,
    height: Dp = 48.dp,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .pointerInput(text ?: icon.toString()) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            ThemedKeyBox(
                theme = theme,
                modifier = Modifier.fillMaxSize(),
                isSpecial = true,
                isSelected = isSelected,
                isPressed = isPressed,
                height = height,
                onClick = null
            ) {
                val contentColor = if (isSelected) {
                    if (theme.themeStyle == ThemeStyle.MATERIAL_YOU || !theme.isDark) Color(0xFF042A5C) else Color.White
                } else theme.textColor

                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = text ?: "key",
                        tint = contentColor,
                        modifier = Modifier.size(19.dp)
                    )
                } else if (text != null) {
                    Text(
                        text = text,
                        color = contentColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun KeyBackspaceButton(
    modifier: Modifier = Modifier,
    theme: KeyboardTheme,
    height: Dp = 48.dp,
    onDelete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onDelete()
            // Wait 300ms initial long-press hold before fast repeat
            kotlinx.coroutines.delay(300)
            var deleteCount = 0
            while (isPressed) {
                onDelete()
                deleteCount++
                val delayMs = when {
                    deleteCount > 25 -> 20L
                    deleteCount > 10 -> 35L
                    else -> 48L
                }
                kotlinx.coroutines.delay(delayMs)
            }
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            ThemedKeyBox(
                theme = theme,
                modifier = Modifier.fillMaxSize(),
                isSpecial = true,
                isSelected = false,
                isPressed = isPressed,
                height = height,
                onClick = null
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = theme.textColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun BottomActionRow(
    theme: KeyboardTheme,
    keyboardMode: KeyboardMode,
    activeLanguage: Language,
    enabledLanguages: List<Language>,
    height: Dp = 48.dp,
    isTransliterationActive: Boolean = false,
    editorInfo: EditorInfo? = null,
    onMoveCursor: (Int) -> Unit = {},
    onSwitchMode: (KeyboardMode) -> Unit,
    onSwitchLanguage: () -> Unit,
    onSpace: () -> Unit,
    onOpenEmoji: () -> Unit,
    onEnter: () -> Unit
) {
    var isSpacePressed by remember { mutableStateOf(false) }
    var isDraggingCursor by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Switcher (?123 or ABC)
        KeySpecialButton(
            text = if (keyboardMode == KeyboardMode.ALPHABET) "?123" else "ABC",
            modifier = Modifier.weight(1.3f),
            theme = theme,
            height = height,
            onClick = {
                onSwitchMode(if (keyboardMode == KeyboardMode.ALPHABET) KeyboardMode.NUMBERS else KeyboardMode.ALPHABET)
            }
        )

        // Language Globe Button
        KeySpecialButton(
            icon = Icons.Default.Language,
            modifier = Modifier.weight(1f),
            theme = theme,
            height = height,
            onClick = onSwitchLanguage
        )

        // Space Bar with Cursor Scrubbing & Indic / Hinglish Label
        Box(
            modifier = Modifier
                .weight(4.7f)
                .height(height)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isSpacePressed = true
                        var totalDragX = 0f
                        var didDrag = false
                        val dragThreshold = 20f

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            val deltaX = change.position.x - change.previousPosition.x
                            totalDragX += deltaX

                            if (Math.abs(totalDragX) >= dragThreshold) {
                                didDrag = true
                                isDraggingCursor = true
                                val step = if (totalDragX > 0) 1 else -1
                                onMoveCursor(step)
                                totalDragX = 0f
                            }
                        }

                        if (!didDrag) {
                            onSpace()
                        }
                        isSpacePressed = false
                        isDraggingCursor = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                ThemedKeyBox(
                    theme = theme,
                    modifier = Modifier.fillMaxSize(),
                    isSpecial = false,
                    isSelected = isDraggingCursor,
                    isPressed = isSpacePressed,
                    height = height,
                    onClick = null
                ) {
                    if (isDraggingCursor) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = theme.accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "‹ Cursor Movement ›",
                                color = theme.accentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        val translitSuffix = if (isTransliterationActive) " (Hinglish)" else ""
                        Text(
                            text = "${activeLanguage.flagEmoji} ${activeLanguage.displayName}$translitSuffix",
                            color = theme.textSecondaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Emoji Button
        KeySpecialButton(
            icon = Icons.Default.SentimentSatisfied,
            modifier = Modifier.weight(1f),
            theme = theme,
            height = height,
            onClick = onOpenEmoji
        )

        // Enter / Action Key (Dynamic Pill / Primary Action based on field action)
        val imeAction = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        val isMultiline = editorInfo?.inputType?.let { inputType ->
            (inputType and android.text.InputType.TYPE_MASK_CLASS == android.text.InputType.TYPE_CLASS_TEXT) &&
                    (inputType and android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0)
        } ?: false

        val actionIcon = when {
            isMultiline -> Icons.AutoMirrored.Filled.KeyboardReturn
            imeAction == EditorInfo.IME_ACTION_SEARCH -> Icons.Default.Search
            imeAction == EditorInfo.IME_ACTION_GO -> Icons.AutoMirrored.Filled.ArrowForward
            imeAction == EditorInfo.IME_ACTION_SEND -> Icons.AutoMirrored.Filled.Send
            imeAction == EditorInfo.IME_ACTION_NEXT -> Icons.AutoMirrored.Filled.ArrowForward
            imeAction == EditorInfo.IME_ACTION_DONE -> Icons.Default.Done
            else -> Icons.AutoMirrored.Filled.KeyboardReturn
        }

        KeySpecialButton(
            icon = actionIcon,
            modifier = Modifier.weight(1.4f),
            theme = theme,
            isSelected = true,
            height = height,
            onClick = onEnter
        )
    }
}

@Composable
fun LanguagePanel(
    theme: KeyboardTheme,
    activeLanguage: Language,
    targetLanguage: Language,
    onSelectSourceLanguage: (Language) -> Unit,
    onSelectTargetLanguage: (Language) -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 265.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Language & Translation",
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = theme.primaryColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { onClose() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("✓ Done", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. INPUT / TYPING LANGUAGE (Horizontal Scroll)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📥 Typing Language",
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    "${activeLanguage.flagEmoji} ${activeLanguage.displayName}",
                    color = theme.accentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(Language.ALL_LANGUAGES) { lang ->
                    val isSelected = activeLanguage.id == lang.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) theme.primaryColor.copy(alpha = 0.25f) else theme.surfaceColor,
                        border = BorderStroke(1.dp, if (isSelected) theme.primaryColor else theme.keyBorderColor),
                        modifier = Modifier.clickable {
                            onSelectSourceLanguage(lang)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(lang.flagEmoji, fontSize = 16.sp)
                            Column {
                                Text(
                                    lang.displayName,
                                    color = if (isSelected) theme.accentColor else theme.textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                                Text(
                                    lang.nativeName,
                                    color = theme.textSecondaryColor,
                                    fontSize = 9.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. TRANSLATE TO / OUTPUT LANGUAGE (Horizontal Scroll)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📤 Translate Output",
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    "${targetLanguage.flagEmoji} ${targetLanguage.displayName}",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(Language.ALL_LANGUAGES) { lang ->
                    val isSelected = targetLanguage.id == lang.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.25f) else theme.surfaceColor,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981) else theme.keyBorderColor),
                        modifier = Modifier.clickable {
                            onSelectTargetLanguage(lang)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(lang.flagEmoji, fontSize = 16.sp)
                            Column {
                                Text(
                                    lang.displayName,
                                    color = if (isSelected) Color(0xFF34D399) else theme.textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                                Text(
                                    lang.nativeName,
                                    color = theme.textSecondaryColor,
                                    fontSize = 9.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Return to Keyboard Action Bar
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Back to Keyboard", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AIPanel(
    theme: KeyboardTheme,
    aiState: AIState,
    currentContextText: String,
    selectedTone: ToneType,
    smartReplies: List<SmartReplyOption>,
    onSelectTone: (ToneType, String) -> Unit,
    onFixGrammar: (String) -> Unit,
    onSmartReplies: (String) -> Unit,
    onContinueText: (String) -> Unit,
    onCustomPrompt: (String) -> Unit,
    onInsertResult: (String) -> Unit,
    onSpeakResult: (String) -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 265.dp
) {
    var promptInput by remember(currentContextText) { mutableStateOf(currentContextText) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                Text("LingoKey AI Assistant", color = theme.textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Gemini",
                        color = Color(0xFF818CF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondaryColor)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Prompt / Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(theme.surfaceColor)
                .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                textStyle = TextStyle(
                    color = theme.textColor,
                    fontSize = 13.sp
                ),
                decorationBox = { innerTextField ->
                    if (promptInput.isEmpty()) {
                        Text(
                            text = "Type text to rewrite, fix, or ask AI...",
                            color = theme.textSecondaryColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                },
                maxLines = 2
            )

            if (promptInput.isNotEmpty()) {
                IconButton(onClick = { promptInput = "" }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = theme.textSecondaryColor, modifier = Modifier.size(14.dp))
                }
            }

            IconButton(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        onCustomPrompt(promptInput)
                    } else {
                        onFixGrammar("hey sir i cannot come office today pls let me know")
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Submit",
                    tint = theme.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Starter Prompt Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val starterTemplates = listOf(
                "📝 Leave request" to "Dear Sir, I am writing to request 2 days leave due to personal matters.",
                "🙏 Thank you" to "Thank you so much for your assistance and continuous support!",
                "📅 Reschedule" to "Could we please reschedule our discussion to tomorrow morning?",
                "💼 Project update" to "All implementation tasks are complete and verified successfully."
            )
            for ((label, sample) in starterTemplates) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.keyColor.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.clickable {
                            promptInput = sample
                            onSelectTone(ToneType.PROFESSIONAL, sample)
                        }
                    ) {
                        Text(
                            text = label,
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // AI Quick Actions Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ActionChip(
                    text = "Fix Grammar",
                    icon = Icons.Default.Spellcheck,
                    theme = theme,
                    onClick = { onFixGrammar(promptInput) }
                )
            }
            item {
                ActionChip(
                    text = "Smart Reply",
                    icon = Icons.Default.QuestionAnswer,
                    theme = theme,
                    onClick = { onSmartReplies(promptInput) }
                )
            }
            item {
                ActionChip(
                    text = "Continue",
                    icon = Icons.Default.EditNote,
                    theme = theme,
                    onClick = { onContinueText(promptInput) }
                )
            }
            items(ToneType.values()) { tone ->
                ActionChip(
                    text = "${tone.iconEmoji} ${tone.displayName}",
                    theme = theme,
                    isSelected = selectedTone == tone,
                    onClick = { onSelectTone(tone, promptInput) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // AI Result Card
        when (aiState) {
            is AIState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .background(theme.surfaceColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = theme.primaryColor, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Gemini AI is generating...", color = theme.textSecondaryColor, fontSize = 12.sp)
                    }
                }
            }
            is AIState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ ${aiState.actionTitle}",
                                color = theme.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = aiState.resultText,
                            color = theme.textColor,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onSpeakResult(aiState.resultText) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { onInsertResult(aiState.resultText) },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Insert into Text", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            is AIState.Error -> {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ ${aiState.message}",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            AIState.Idle -> {
                if (smartReplies.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (reply in smartReplies) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.surfaceColor,
                                border = BorderStroke(1.dp, theme.keyColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onInsertResult(reply.text) }
                            ) {
                                Text(
                                    text = reply.text,
                                    color = theme.textColor,
                                    fontSize = 12.5.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(theme.surfaceColor, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💡 Type a prompt or tap any tone/action above to rewrite, fix, or generate!",
                            color = theme.textSecondaryColor,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionChip(
    text: String,
    theme: KeyboardTheme,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) theme.primaryColor else theme.keyColor,
        border = BorderStroke(0.5.dp, theme.keyBorderColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else theme.accentColor, modifier = Modifier.size(14.dp))
            }
            Text(text = text, color = if (isSelected) Color.White else theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TranslatePanel(
    theme: KeyboardTheme,
    sourceLang: Language,
    targetLang: Language,
    sourceText: String,
    resultText: String,
    isTranslating: Boolean,
    allLanguages: List<Language>,
    onSourceTextChange: (String) -> Unit,
    onTargetLangChange: (Language) -> Unit,
    onTranslate: () -> Unit,
    onReverseLanguages: () -> Unit,
    onInsert: () -> Unit,
    onSpeak: (String, Language) -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 265.dp
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Translate, contentDescription = "Translate", tint = theme.accentColor, modifier = Modifier.size(18.dp))
                Text("Live Neural Translator", color = theme.textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondaryColor)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Language Selectors Row (Source ⇄ Target)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = theme.surfaceColor,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${sourceLang.flagEmoji} ${sourceLang.displayName}",
                    color = theme.textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            IconButton(onClick = onReverseLanguages, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = theme.accentColor)
            }

            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.surfaceColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDropdown = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${targetLang.flagEmoji} ${targetLang.displayName}",
                            color = theme.textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                    }
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    for (lang in allLanguages) {
                        DropdownMenuItem(
                            text = { Text("${lang.flagEmoji} ${lang.displayName} (${lang.nativeName})") },
                            onClick = {
                                onTargetLangChange(lang)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input text field
        OutlinedTextField(
            value = sourceText,
            onValueChange = onSourceTextChange,
            placeholder = { Text("Type text to translate...", color = theme.textSecondaryColor, fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.primaryColor,
                unfocusedBorderColor = theme.keyBorderColor,
                focusedTextColor = theme.textColor,
                unfocusedTextColor = theme.textColor
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Result Card
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = resultText.ifBlank { "Translation will appear here in real-time" },
                    color = if (resultText.isNotBlank()) theme.textColor else theme.textSecondaryColor,
                    fontSize = 13.sp,
                    fontWeight = if (resultText.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (resultText.isNotBlank()) {
                        IconButton(onClick = { onSpeak(resultText, targetLang) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = theme.accentColor, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onInsert,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Insert Translation", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoicePanel(
    theme: KeyboardTheme,
    voiceStatus: VoiceTypingStatus,
    rmsLevel: Float,
    errorMessage: String?,
    activeLanguage: Language,
    allLanguages: List<Language>,
    recognizedText: String,
    targetLang: Language,
    onSelectLanguage: (Language) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onInsert: () -> Unit,
    onTranslateAndInsert: () -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 265.dp
) {
    val isListening = voiceStatus == VoiceTypingStatus.LISTENING || voiceStatus == VoiceTypingStatus.CONNECTING

    // Dynamic wave animation based on speech RMS
    val waveScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isListening) (1f + rmsLevel * 0.45f).coerceIn(1f, 1.55f) else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "rms_wave"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isListening) Color(0xFF10B981) else theme.accentColor,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    "Voice Typing",
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isListening) Color(0xFF10B981).copy(alpha = 0.2f) else theme.surfaceColor,
                    border = BorderStroke(1.dp, if (isListening) Color(0xFF10B981) else theme.keyBorderColor)
                ) {
                    Text(
                        text = "${activeLanguage.flagEmoji} ${activeLanguage.displayName}",
                        color = if (isListening) Color(0xFF10B981) else theme.textColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondaryColor)
            }
        }

        // Horizontal Language Switcher Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(allLanguages) { lang ->
                val isCurrent = lang.id == activeLanguage.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrent) theme.primaryColor else theme.surfaceColor,
                    border = BorderStroke(
                        1.dp,
                        if (isCurrent) theme.primaryColor else theme.keyBorderColor
                    ),
                    modifier = Modifier
                        .clickable {
                            if (!isCurrent) {
                                onSelectLanguage(lang)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(lang.flagEmoji, fontSize = 11.sp)
                        Text(
                            text = lang.nativeName,
                            color = if (isCurrent) Color.White else theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Center Animated Mic Ripple Indicator & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size((48 * waveScale).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.25f))
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isListening) Color(0xFF10B981) else if (errorMessage != null) Color(0xFFEF4444) else theme.surfaceColor,
                    border = BorderStroke(
                        1.5.dp,
                        if (isListening) Color.White.copy(alpha = 0.8f) else theme.keyBorderColor
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .clickable {
                            if (isListening) {
                                onStopListening()
                            } else {
                                onStartListening()
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (errorMessage != null) Icons.Default.Refresh else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = if (isListening || errorMessage != null) Color.White else theme.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Status & Transcription Preview Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.surfaceColor,
                border = BorderStroke(
                    1.dp,
                    if (errorMessage != null) Color(0xFFEF4444).copy(alpha = 0.5f)
                    else if (isListening) Color(0xFF10B981).copy(alpha = 0.4f)
                    else theme.keyBorderColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = "⚠️ $errorMessage\nTap mic to retry",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    } else if (recognizedText.isNotBlank()) {
                        Column {
                            Text(
                                text = "“$recognizedText”",
                                color = theme.textColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isListening) {
                                Text(
                                    text = "🟢 Listening in ${activeLanguage.displayName}...",
                                    color = Color(0xFF10B981),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isListening) "🎙️ Speak now in ${activeLanguage.displayName} (${activeLanguage.nativeName})..." else "Tap mic to start voice typing in ${activeLanguage.displayName}",
                            color = if (isListening) Color(0xFF10B981) else theme.textSecondaryColor,
                            fontSize = 11.5.sp,
                            fontWeight = if (isListening) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onInsert,
                enabled = recognizedText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.keyColor,
                    disabledContainerColor = theme.keyColor.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Insert Text", color = theme.textColor, fontSize = 12.sp)
            }

            Button(
                onClick = onTranslateAndInsert,
                enabled = recognizedText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.primaryColor,
                    disabledContainerColor = theme.primaryColor.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(34.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Translate → ${targetLang.displayName}",
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmojiPanel(
    theme: KeyboardTheme,
    onEmojiClick: (String) -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    panelHeight: Dp = 265.dp
) {
    var mediaMode by remember { mutableStateOf("EMOJI") } // EMOJI, STICKERS, KAOMOJI
    var selectedCategory by remember { mutableStateOf("Smileys") }
    var selectedStickerCategory by remember { mutableStateOf("🪔 Festivals") }
    val categories = KeyboardLayouts.EMOJI_CATEGORIES.keys.toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // 1. TOP MEDIA SEGMENT TABS (Emojis, Desi Stickers & GIFs, Kaomoji)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("EMOJI", "😊 Emojis", Icons.Default.SentimentSatisfied),
                Triple("STICKERS", "✨ Stickers & Desi", Icons.Default.AutoAwesome),
                Triple("KAOMOJI", "(◕‿◕) Kaomoji", Icons.Default.Mood)
            )

            for ((tabKey, tabLabel, tabIcon) in tabs) {
                val isSelected = mediaMode == tabKey
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) theme.primaryColor else theme.surfaceColor,
                    border = BorderStroke(
                        0.75.dp,
                        if (isSelected) theme.primaryColor else theme.keyBorderColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { mediaMode = tabKey }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tabLabel,
                            color = if (isSelected) Color.White else theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        when (mediaMode) {
            "STICKERS" -> {
                // Sticker Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (cat in StickerCatalog.STICKER_CATEGORIES) {
                        val isCatSelected = selectedStickerCategory == cat
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCatSelected) theme.accentColor else theme.surfaceColor,
                            modifier = Modifier.clickable { selectedStickerCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isCatSelected) Color.White else theme.textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stickers Grid
                val filteredStickers = StickerCatalog.ALL_STICKERS.filter { it.category == selectedStickerCategory }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredStickers) { sticker ->
                        val brush = Brush.linearGradient(
                            listOf(Color(sticker.gradientColors.first()), Color(sticker.gradientColors.last()))
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clickable { onEmojiClick(sticker.textToInsert) },
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(brush)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = sticker.iconBadge,
                                        fontSize = 24.sp
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = sticker.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (sticker.subtext.isNotEmpty()) {
                                            Text(
                                                text = sticker.subtext,
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "KAOMOJI" -> {
                val kaomojis = KeyboardLayouts.EMOJI_CATEGORIES["Kaomoji"] ?: emptyList()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(kaomojis) { kaomoji ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.surfaceColor,
                            border = BorderStroke(0.5.dp, theme.keyBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clickable { onEmojiClick(kaomoji) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = kaomoji,
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                // Category Tabs for Standard Emojis
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (category in categories) {
                        if (category == "Kaomoji") continue
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedCategory == category) theme.primaryColor else theme.surfaceColor,
                            modifier = Modifier.clickable { selectedCategory = category }
                        ) {
                            Text(
                                text = category,
                                color = if (selectedCategory == category) Color.White else theme.textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Emoji Grid
                val emojis = KeyboardLayouts.EMOJI_CATEGORIES[selectedCategory] ?: emptyList()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 38.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onEmojiClick(emoji) }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }

        // Bottom Bar (ABC, Backspace)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = theme.keySpecialColor),
                modifier = Modifier.height(34.dp)
            ) {
                Text("ABC", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = theme.textColor)
            }
        }
    }
}

@Composable
fun ClipboardPanel(
    theme: KeyboardTheme,
    clipboardRepository: ClipboardRepository,
    onPaste: (String) -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 265.dp
) {
    val items by clipboardRepository.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                Text("Clipboard History", color = theme.textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row {
                TextButton(onClick = { clipboardRepository.clearAll() }) {
                    Text("Clear", color = Color(0xFFEF4444), fontSize = 12.sp)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No copied clips yet", color = theme.textSecondaryColor, fontSize = 12.sp)
            }
        } else {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(160.dp)
                            .fillMaxHeight()
                            .clickable { onPaste(item.text) }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.text,
                                color = theme.textColor,
                                fontSize = 12.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.isPinned) {
                                    Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = theme.accentColor, modifier = Modifier.size(12.dp))
                                }
                                IconButton(
                                    onClick = { clipboardRepository.togglePin(item.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        if (item.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                                        contentDescription = "Pin",
                                        tint = if (item.isPinned) theme.primaryColor else theme.textSecondaryColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
