package com.example.keyboard

import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import com.example.engine.smartreply.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.ClipboardRepository
import com.example.data.LingoKeyPreferences
import com.example.engine.*
import com.example.model.*
import kotlinx.coroutines.launch

data class AutoCorrectionRevertRecord(
    val originalTyped: String,
    val correctedWord: String,
    val committedLength: Int
)

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
    onStartSpeechRecognition: ((String?, String?, Boolean, Boolean) -> Unit)? = null,
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
    val autoFixGrammar by preferences.autoFixGrammar.collectAsState()
    val longPressSubSymbols by preferences.longPressSubSymbols.collectAsState()
    val keyVibration by preferences.keyVibration.collectAsState()
    val voiceGender by preferences.voiceGender.collectAsState()
    val realtimeTTSMode by preferences.realtimeTTSMode.collectAsState()
    val realtimeAutoTranslate by preferences.realtimeAutoTranslate.collectAsState()
    val sourceTranslateLang by preferences.sourceTranslationLanguage.collectAsState()
    val targetTranslateLang by preferences.targetTranslationLanguage.collectAsState()
    val autoDetectLanguage by preferences.autoDetectLanguage.collectAsState()
    val keyHeightDp by preferences.keyHeightDp.collectAsState()
    val keyPreviewEnabled by preferences.keyPreviewEnabled.collectAsState()
    val toolbarItems by preferences.toolbarItems.collectAsState()
    val latestClip by clipboardRepository.latestClip.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()

    var isFloatingTranslateBarVisible by remember { mutableStateOf(false) }
    var keyboardMode by remember { mutableStateOf(KeyboardMode.ALPHABET) }
    var isShifted by remember { mutableStateOf(false) }
    var isCapsLock by remember { mutableStateOf(false) }
    var currentComposingWord by remember { mutableStateOf("") }
    var lastCommittedWord by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf("I", "Hello", "How", "Thank you")) }
    var currentCandidateStrip by remember {
        mutableStateOf(SuggestionEngine.getCandidateStrip("", "", "en"))
    }
    var lastAutoCorrection by remember { mutableStateOf<AutoCorrectionRevertRecord?>(null) }
    var lastSpaceTapTime by remember { mutableLongStateOf(0L) }

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
    var emojiPanelInitialTab by remember { mutableStateOf("EMOJI") }
    var languagePanelInitialTab by remember { mutableStateOf("SOURCE") }

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

    var suggestionJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val isTyping by remember { derivedStateOf { currentComposingWord.isNotEmpty() } }

    // Refresh suggestions asynchronously on Dispatchers.Default (Zero-Latency Typing)
    fun refreshSuggestions(composingWord: String) {
        suggestionJob?.cancel()
        val context = onGetContextText().ifEmpty { lastCommittedWord }
        val langCode = activeLanguage.code
        val isTranslit = transliterationEnabled
        val isEmoji = emojiSuggestionsEnabled

        if (composingWord.isEmpty()) {
            val strip = SuggestionEngine.getCandidateStrip("", context, langCode)
            val preds = SuggestionEngine.getSuggestions("", context, langCode)
            currentCandidateStrip = strip
            suggestions = preds
            return
        }

        suggestionJob = coroutineScope.launch(Dispatchers.Default) {
            val strip = SuggestionEngine.getCandidateStrip(composingWord, context, langCode)
            val base = SuggestionEngine.getSuggestions(composingWord, context, langCode)

            val translitCandidates = if (isTranslit && composingWord.isNotBlank()) {
                TransliteratorEngine.getTransliterationCandidates(composingWord, langCode)
            } else emptyList()

            val emojiCandidates = if (isEmoji && composingWord.isNotBlank()) {
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

            withContext(Dispatchers.Main) {
                currentCandidateStrip = strip
                suggestions = combined
            }
        }
    }

    // Update suggestions when active language or preferences change
    LaunchedEffect(activeLanguage.code, transliterationEnabled, emojiSuggestionsEnabled) {
        refreshSuggestions(currentComposingWord)
    }

    // Fast debounced real-time translation for Translate Bar
    LaunchedEffect(translateSourceText, sourceTranslateLang.code, targetTranslateLang.code, isFloatingTranslateBarVisible) {
        if (!isFloatingTranslateBarVisible) return@LaunchedEffect
        val clean = translateSourceText.trim()
        if (clean.isEmpty()) {
            translateResultText = ""
            isTranslating = false
            return@LaunchedEffect
        }
        val sl = sourceTranslateLang.code
        val tl = targetTranslateLang.code

        // 1. Fast cache/offline match in 0ms
        val cached = GoogleTranslationEngine.getCached(clean, sl, tl)
        if (!cached.isNullOrBlank()) {
            translateResultText = cached
            isTranslating = false
            return@LaunchedEffect
        }
        val offline = TranslationMatrix.translateOffline(clean, sl, tl)
        if (offline != clean && offline.isNotBlank()) {
            translateResultText = offline
        }

        // 2. Debounce 90ms before hitting Google Translate online
        delay(90)
        isTranslating = true
        try {
            val online = GoogleTranslationEngine.translate(clean, sl, tl)
            if (online.isNotBlank()) {
                translateResultText = online
            }
        } catch (_: Exception) {
            // Keep offline fallback
        } finally {
            isTranslating = false
        }
    }

    // Sync voice recognition with floating translation bar
    LaunchedEffect(voiceRecognizedText, isFloatingTranslateBarVisible) {
        if (isFloatingTranslateBarVisible && voiceRecognizedText.isNotBlank()) {
            if (voiceRecognizedText.contains("➔")) {
                val parts = voiceRecognizedText.split("➔")
                val src = parts.getOrNull(0)?.trim() ?: ""
                val tgt = parts.getOrNull(1)?.trim() ?: ""
                if (src.isNotBlank()) {
                    translateSourceText = src
                }
                if (tgt.isNotBlank() && tgt != "...") {
                    translateResultText = tgt
                }
            } else {
                translateSourceText = voiceRecognizedText
            }
        }
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
        lastAutoCorrection = null
        if (isFloatingTranslateBarVisible) {
            translateSourceText += char
            return
        }
        val textToCommit = char

        // INSTANT COMMIT TO INPUT CONNECTION (<1ms on UI thread)
        onCommitText(textToCommit)
        if (isShifted && !isCapsLock) {
            isShifted = false
        }

        // Check if punctuation completes a word
        if (char in "\n\t.,!?") {
            // Auto-fix grammar slips on punctuation if enabled (e.g. "dont." -> "don't.", "he go." -> "he goes.")
            if (autoFixGrammar && char in ".,!?") {
                val context = onGetContextText()
                val fix = GrammarEngine.autoFixOnPunctuation(context)
                if (fix != null) {
                    onDeleteSurroundingText(fix.charsToReplace + textToCommit.length, 0)
                    onCommitText("${fix.correctedPhrase}$char")
                }
            }

            val finishedWord = currentComposingWord.trim()
            if (finishedWord.isNotEmpty()) {
                lastCommittedWord = finishedWord
                coroutineScope.launch(Dispatchers.Default) {
                    SuggestionEngine.learnWord(finishedWord)
                }
                if (autoDetectLanguage && finishedWord.length >= 2) {
                    coroutineScope.launch(Dispatchers.Default) {
                        val detected = LanguageDetector.detectLanguage(finishedWord)
                        if (detected.id != activeLanguage.id) {
                            withContext(Dispatchers.Main) {
                                preferences.setActiveLanguage(detected)
                            }
                        }
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
            // Prefetch translation in background while user types
            if (realtimeAutoTranslate && currentComposingWord.isNotBlank()) {
                coroutineScope.launch(Dispatchers.Default) {
                    GoogleTranslationEngine.prefetch(currentComposingWord, activeLanguage.code, targetTranslateLang.code)
                }
            }
            // Auto-detect language dynamically while typing on background dispatcher
            if (autoDetectLanguage && currentComposingWord.length >= 3) {
                coroutineScope.launch(Dispatchers.Default) {
                    val detected = LanguageDetector.detectLanguage(currentComposingWord)
                    if (detected.id != activeLanguage.id && detected.layoutType == activeLanguage.layoutType) {
                        withContext(Dispatchers.Main) {
                            preferences.setActiveLanguage(detected)
                        }
                    }
                }
            }
            refreshSuggestions(currentComposingWord)
        }
    }

    fun handleBackspace() {
        vibrate()
        if (isFloatingTranslateBarVisible) {
            if (translateSourceText.isNotEmpty()) {
                translateSourceText = translateSourceText.dropLast(1)
            }
            return
        }
        if (lastAutoCorrection != null) {
            // Gboard-grade Backspace Revert: revert autocorrection back to user's literal typed input
            val revert = lastAutoCorrection!!
            lastAutoCorrection = null
            onDeleteSurroundingText(revert.committedLength, 0)
            onCommitText(revert.originalTyped)
            currentComposingWord = revert.originalTyped
            refreshSuggestions(currentComposingWord)
            return
        }

        onDeleteSurroundingText(1, 0)
        if (currentComposingWord.isNotEmpty()) {
            currentComposingWord = currentComposingWord.dropLast(1)
            refreshSuggestions(currentComposingWord)
        } else {
            refreshSuggestions("")
        }
    }

    fun handleDeleteWord() {
        vibrate()
        lastAutoCorrection = null
        if (isFloatingTranslateBarVisible) {
            if (translateSourceText.isNotEmpty()) {
                val trimmed = translateSourceText.trimEnd()
                val trailingSpaces = translateSourceText.length - trimmed.length
                val lastWordLen = trimmed.takeLastWhile { !it.isWhitespace() }.length
                val toDrop = (trailingSpaces + lastWordLen).coerceAtLeast(1)
                translateSourceText = translateSourceText.dropLast(toDrop)
            }
            return
        }
        val contextBefore = onGetContextText()
        if (contextBefore.isNotEmpty()) {
            val trimmed = contextBefore.trimEnd()
            val trailingSpaces = contextBefore.length - trimmed.length
            val lastWordLen = trimmed.takeLastWhile { !it.isWhitespace() }.length
            val toDelete = (trailingSpaces + lastWordLen).coerceIn(1, 60)
            onDeleteSurroundingText(toDelete, 0)
        } else {
            onDeleteSurroundingText(1, 0)
        }
        currentComposingWord = ""
        refreshSuggestions("")
    }

    fun handleSpace() {
        vibrate()
        if (isFloatingTranslateBarVisible) {
            if (translateSourceText.isNotEmpty() && !translateSourceText.endsWith(" ")) {
                translateSourceText += " "
            }
            return
        }
        var wordToProcess = currentComposingWord.trim()

        if (wordToProcess.isEmpty()) {
            val now = System.currentTimeMillis()
            if (now - lastSpaceTapTime < 360L && lastSpaceTapTime > 0L) {
                // Gboard double-space shortcut: converts previous space into period and space
                lastSpaceTapTime = 0L
                onDeleteSurroundingText(1, 0)
                onCommitText(". ")
                if (!isCapsLock) {
                    isShifted = true
                }
                refreshSuggestions("")
                return
            }
            lastSpaceTapTime = now
            onCommitText(" ")
            refreshSuggestions("")
            return
        }
        lastSpaceTapTime = System.currentTimeMillis()

        // 1. Auto-detection on space offloaded to background
        if (autoDetectLanguage && wordToProcess.length >= 2) {
            coroutineScope.launch(Dispatchers.Default) {
                val detected = LanguageDetector.detectLanguage(wordToProcess)
                if (detected.id != activeLanguage.id) {
                    withContext(Dispatchers.Main) {
                        preferences.setActiveLanguage(detected)
                    }
                }
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
                lastAutoCorrection = null
            }
        } else if (autoCorrection && wordToProcess.isNotEmpty()) {
            // 3. Spacebar Auto-Commit (Gboard-grade zero-latency auto-correction & grammar fix)
            val candidate = currentCandidateStrip
            val grammarFix = candidate.grammarFix
            if (autoFixGrammar && grammarFix != null && (grammarFix.explanation.contains("contraction", ignoreCase = true) || grammarFix.explanation.contains("apostrophe", ignoreCase = true) || grammarFix.originalPhrase.equals("i", ignoreCase = true))) {
                onDeleteSurroundingText(grammarFix.charsToReplace, 0)
                onCommitText("${grammarFix.correctedPhrase} ")
                lastAutoCorrection = AutoCorrectionRevertRecord(
                    originalTyped = grammarFix.originalPhrase,
                    correctedWord = grammarFix.correctedPhrase,
                    committedLength = grammarFix.correctedPhrase.length + 1
                )
                lastCommittedWord = grammarFix.correctedPhrase
                coroutineScope.launch(Dispatchers.Default) {
                    SuggestionEngine.learnWord(grammarFix.correctedPhrase)
                }
                currentComposingWord = ""
                refreshSuggestions("")
                return
            } else if (candidate.isAutoCorrection && candidate.primary.isNotBlank() && !candidate.primary.equals(wordToProcess, ignoreCase = true)) {
                val corrected = candidate.primary
                onDeleteSurroundingText(currentComposingWord.length, 0)
                onCommitText("$corrected ")
                // Save state for Gboard-grade Backspace Revert!
                lastAutoCorrection = AutoCorrectionRevertRecord(
                    originalTyped = currentComposingWord,
                    correctedWord = corrected,
                    committedLength = corrected.length + 1
                )
                triggerWordTTS(corrected, isTranslated = realtimeAutoTranslate)
                lastCommittedWord = corrected
                coroutineScope.launch(Dispatchers.Default) {
                    SuggestionEngine.learnWord(corrected)
                }
                currentComposingWord = ""
                refreshSuggestions("")
                return
            } else {
                lastAutoCorrection = null
            }
        } else {
            lastAutoCorrection = null
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

    var activeLangDropdown by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(backgroundModifier)
            .navigationBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
        // ACTIVE TOOL STATE (Determines which 3D Neumorphic chip is glowing and handles 1st-click open / 2nd-click close)
        val activeToolId = when {
            isFloatingTranslateBarVisible -> "translate"
            keyboardMode == KeyboardMode.TOOLS_PANEL -> "tools"
            keyboardMode == KeyboardMode.AI_PANEL -> "ai"
            keyboardMode == KeyboardMode.TRANSLATE_PANEL -> "translate"
            keyboardMode == KeyboardMode.VOICE_PANEL -> "voice"
            keyboardMode == KeyboardMode.CLIPBOARD -> "clipboard"
            keyboardMode == KeyboardMode.SMART_REPLY_PANEL -> "smart_reply"
            keyboardMode == KeyboardMode.EMOJI -> if (emojiPanelInitialTab == "STICKER") "stickers" else "emoji"
            keyboardMode == KeyboardMode.LANGUAGE_PANEL -> "languages"
            keyboardMode == KeyboardMode.NUMERIC_PAD -> "numpad"
            keyboardMode == KeyboardMode.TOOLBAR_CUSTOMIZE -> "customize"
            else -> null
        }

        // Gboard-Style Floating Translation Bar (Rendered ABOVE Toolbar, exactly like Gboard)
        if (isFloatingTranslateBarVisible) {
            GboardFloatingTranslateBar(
                theme = currentTheme,
                sourceLang = sourceTranslateLang,
                targetLang = targetTranslateLang,
                sourceText = translateSourceText,
                resultText = translateResultText,
                isTranslating = isTranslating,
                voiceStatus = voiceStatus,
                voiceRmsLevel = voiceRmsLevel,
                allLanguages = Language.ALL_LANGUAGES,
                onSourceLangChange = { lang: Language ->
                    preferences.setSourceTranslationLanguage(lang)
                    if (translateSourceText.isNotBlank()) {
                        translateResultText = GoogleTranslationEngine.translateFast(translateSourceText, lang.code, targetTranslateLang.code)
                    }
                },
                onTargetLangChange = { lang: Language ->
                    preferences.setTargetTranslationLanguage(lang)
                    if (translateSourceText.isNotBlank()) {
                        translateResultText = GoogleTranslationEngine.translateFast(translateSourceText, sourceTranslateLang.code, lang.code)
                    }
                },
                onSwapLanguages = {
                    activeLangDropdown = null
                    if (sourceTranslateLang.id == "auto") {
                        val newSource = targetTranslateLang
                        val newTarget = Language.getById("en")
                        preferences.setSourceTranslationLanguage(newSource)
                        preferences.setTargetTranslationLanguage(newTarget)
                    } else {
                        val newSource = targetTranslateLang
                        val newTarget = sourceTranslateLang
                        preferences.setSourceTranslationLanguage(newSource)
                        preferences.setTargetTranslationLanguage(newTarget)
                    }
                    val oldRes = translateResultText
                    translateResultText = translateSourceText
                    translateSourceText = oldRes
                    if (translateSourceText.isNotBlank()) {
                        translateResultText = GoogleTranslationEngine.translateFast(translateSourceText, sourceTranslateLang.code, targetTranslateLang.code)
                    }
                },
                onSourceTextChange = { newText: String ->
                    translateSourceText = newText
                    if (newText.isNotBlank()) {
                        translateResultText = GoogleTranslationEngine.translateFast(newText, sourceTranslateLang.code, targetTranslateLang.code)
                    } else {
                        translateResultText = ""
                    }
                },
                onCommitTranslation = { textToInsert: String ->
                    onCommitText("$textToInsert ")
                    translateSourceText = ""
                    translateResultText = ""
                },
                onToggleVoiceTranslate = {
                    if (voiceStatus == VoiceTypingStatus.LISTENING || voiceStatus == VoiceTypingStatus.CONNECTING) {
                        onStopSpeechRecognition?.invoke()
                    } else {
                        onStartSpeechRecognition?.invoke(sourceTranslateLang.id, targetTranslateLang.id, true, false)
                    }
                },
                onClear = {
                    translateSourceText = ""
                    translateResultText = ""
                },
                onClose = {
                    activeLangDropdown = null
                    isFloatingTranslateBarVisible = false
                    onStopSpeechRecognition?.invoke()
                    translateSourceText = ""
                    translateResultText = ""
                },
                activeDropdown = activeLangDropdown,
                onToggleDropdown = { type ->
                    activeLangDropdown = if (activeLangDropdown == type) null else type
                }
            )
        }

        // UNIFIED 3D NEUMORPHIC TOOLBAR & GBOARD CANDIDATE STRIP
        UnifiedGboardTopStrip(
            theme = currentTheme,
            isTyping = isTyping,
            candidateResult = currentCandidateStrip,
            currentComposingWord = currentComposingWord,
            activeLanguage = activeLanguage,
            targetLang = targetTranslateLang,
            realtimeAutoTranslate = realtimeAutoTranslate,
            toolbarItems = toolbarItems,
            latestClip = latestClip,
            activeToolId = activeToolId,
            voiceStatus = voiceStatus,
            onToggleLeftGrid = {
                vibrate()
                if (keyboardMode != KeyboardMode.ALPHABET) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    keyboardMode = KeyboardMode.TOOLS_PANEL
                }
            },
            onPasteClip = { clip ->
                vibrate()
                lastAutoCorrection = null
                onCommitText(clip)
                clipboardRepository.dismissLatestClip()
            },
            onCommitGrammarFix = { fix ->
                vibrate()
                onDeleteSurroundingText(fix.charsToReplace, 0)
                onCommitText("${fix.correctedPhrase} ")
                lastAutoCorrection = null
                currentComposingWord = ""
                refreshSuggestions("")
            },
            onCommitCandidate = { word, isDirectTranslation, isLiteral ->
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
                    if (!isLiteral && currentCandidateStrip.isAutoCorrection && word == currentCandidateStrip.primary) {
                        lastAutoCorrection = AutoCorrectionRevertRecord(
                            originalTyped = currentComposingWord,
                            correctedWord = textToInsert,
                            committedLength = textToInsert.length + suffix.length
                        )
                    } else {
                        lastAutoCorrection = null
                    }
                    triggerWordTTS(textToInsert, isTranslated = isDirectTranslation || realtimeAutoTranslate)
                    lastCommittedWord = textToInsert
                    coroutineScope.launch(Dispatchers.Default) {
                        SuggestionEngine.learnWord(textToInsert)
                    }
                } else {
                    lastAutoCorrection = null
                }
                currentComposingWord = ""
                refreshSuggestions("")
            },
            onOpenEmoji = {
                vibrate()
                if (keyboardMode == KeyboardMode.EMOJI && emojiPanelInitialTab == "EMOJI") {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    emojiPanelInitialTab = "EMOJI"
                    keyboardMode = KeyboardMode.EMOJI
                }
            },
            onOpenTools = {
                vibrate()
                if (keyboardMode == KeyboardMode.TOOLS_PANEL) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    keyboardMode = KeyboardMode.TOOLS_PANEL
                }
            },
            onOpenAI = {
                vibrate()
                if (keyboardMode == KeyboardMode.AI_PANEL) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    keyboardMode = KeyboardMode.AI_PANEL
                    aiActionState = AIState.Idle
                }
            },
            onOpenTranslate = {
                vibrate()
                isFloatingTranslateBarVisible = !isFloatingTranslateBarVisible
                if (isFloatingTranslateBarVisible) {
                    keyboardMode = KeyboardMode.ALPHABET
                    translateSourceText = onGetContextText()
                    if (translateSourceText.isNotBlank()) {
                        translateResultText = GoogleTranslationEngine.translateFast(translateSourceText, sourceTranslateLang.code, targetTranslateLang.code)
                        coroutineScope.launch {
                            isTranslating = true
                            val online = GoogleTranslationEngine.translate(translateSourceText, sourceTranslateLang.code, targetTranslateLang.code)
                            translateResultText = online
                            isTranslating = false
                        }
                    }
                } else {
                    onStopSpeechRecognition?.invoke()
                }
            },
            onOpenVoice = {
                vibrate()
                if (isFloatingTranslateBarVisible) {
                    if (voiceStatus == VoiceTypingStatus.LISTENING || voiceStatus == VoiceTypingStatus.CONNECTING) {
                        onStopSpeechRecognition?.invoke()
                    } else {
                        onStartSpeechRecognition?.invoke(sourceTranslateLang.id, targetTranslateLang.id, true, false)
                    }
                } else {
                    voiceRecognizedText = ""
                    voiceErrorMessage = null
                    if (voiceStatus == VoiceTypingStatus.LISTENING || voiceStatus == VoiceTypingStatus.CONNECTING) {
                        onStopSpeechRecognition?.invoke()
                    } else {
                        onStartSpeechRecognition?.invoke(activeLanguage.id, targetTranslateLang.id, realtimeAutoTranslate, true)
                    }
                }
            },
            onOpenSettings = onOpenSettings,
            onOpenClipboard = {
                vibrate()
                if (keyboardMode == KeyboardMode.CLIPBOARD) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    keyboardMode = KeyboardMode.CLIPBOARD
                }
            },
            onOpenSmartReply = {
                vibrate()
                if (keyboardMode == KeyboardMode.SMART_REPLY_PANEL) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    keyboardMode = KeyboardMode.SMART_REPLY_PANEL
                }
            },
            onOpenTemplates = {
                vibrate()
                if (keyboardMode == KeyboardMode.EMOJI && emojiPanelInitialTab == "STICKER") {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    emojiPanelInitialTab = "STICKER"
                    keyboardMode = KeyboardMode.EMOJI
                }
            },
            onOpenThemes = { onOpenThemesStore() },
            onOpenLanguages = {
                vibrate()
                if (keyboardMode == KeyboardMode.LANGUAGE_PANEL) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    languagePanelInitialTab = "SOURCE"
                    keyboardMode = KeyboardMode.LANGUAGE_PANEL
                }
            },
            onOpenToolbarCustomize = {
                vibrate()
                if (keyboardMode == KeyboardMode.TOOLBAR_CUSTOMIZE) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    keyboardMode = KeyboardMode.TOOLBAR_CUSTOMIZE
                }
            },
            onOpenNumericPad = {
                vibrate()
                if (keyboardMode == KeyboardMode.NUMERIC_PAD) {
                    keyboardMode = KeyboardMode.ALPHABET
                } else {
                    keyboardMode = KeyboardMode.NUMERIC_PAD
                }
            }
        )

        // DYNAMIC KEYBOARD PANEL HEIGHT (Driven by user preference keyHeightDp, default 45dp row height + natural spacing)
        val currentKeyHeight = remember(keyHeightDp) {
            keyHeightDp.dp.coerceIn(38.dp, 68.dp)
        }
        val standardPanelHeight = remember(currentKeyHeight, showNumberRow) {
            var h = (currentKeyHeight * 4) + 22.dp
            if (showNumberRow) h += (currentKeyHeight * 0.88f).coerceAtLeast(36.dp)
            h.coerceAtLeast(240.dp)
        }

        // CONTENT AREA (Panels OR Main Keyboard)
        AnimatedContent(
            targetState = keyboardMode,
            label = "keyboard_mode_anim"
        ) { mode ->
            when (mode) {
                KeyboardMode.TOOLBAR_CUSTOMIZE -> {
                    ToolbarCustomizePanel(
                        theme = currentTheme,
                        currentOrder = toolbarItems,
                        onSaveOrder = { preferences.setToolbarItems(it) },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.TOOLS_PANEL -> {
                    ToolsPanel(
                        theme = currentTheme,
                        onOpenSmartReply = { keyboardMode = KeyboardMode.SMART_REPLY_PANEL },
                        onOpenTranslate = {
                            keyboardMode = KeyboardMode.ALPHABET
                            isFloatingTranslateBarVisible = true
                            translateSourceText = onGetContextText()
                            if (translateSourceText.isNotBlank()) {
                                translateResultText = GoogleTranslationEngine.translateFast(translateSourceText, sourceTranslateLang.code, targetTranslateLang.code)
                            }
                        },
                        onOpenVoice = {
                            keyboardMode = KeyboardMode.ALPHABET
                            isFloatingTranslateBarVisible = true
                            voiceRecognizedText = ""
                            voiceErrorMessage = null
                            onStartSpeechRecognition?.invoke(sourceTranslateLang.id, targetTranslateLang.id, true, false)
                        },
                        onOpenClipboard = { keyboardMode = KeyboardMode.CLIPBOARD },
                        onOpenAI = { keyboardMode = KeyboardMode.AI_PANEL; aiActionState = AIState.Idle },
                        onOpenThemes = {
                            onOpenThemesStore()
                        },
                        onOpenLanguages = {
                            languagePanelInitialTab = "SOURCE"
                            keyboardMode = KeyboardMode.LANGUAGE_PANEL
                        },
                        onOpenSettings = onOpenSettings,
                        onOpenCustomizeToolbar = { keyboardMode = KeyboardMode.TOOLBAR_CUSTOMIZE },
                        onOpenNumericPad = { keyboardMode = KeyboardMode.NUMERIC_PAD },
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
                            onStartSpeechRecognition?.invoke(newLang.id, targetTranslateLang.id, false, false)
                        },
                        onStartListening = {
                            voiceErrorMessage = null
                            onStartSpeechRecognition?.invoke(activeLanguage.id, targetTranslateLang.id, false, false)
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
                        preferences = preferences,
                        onEmojiClick = { emoji ->
                            vibrate()
                            onCommitText(emoji)
                        },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        onDelete = { handleBackspace() },
                        initialMode = emojiPanelInitialTab,
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
                        initialTab = languagePanelInitialTab,
                        onSelectSourceLanguage = { lang ->
                            preferences.setAutoDetectLanguage(false)
                            preferences.setActiveLanguage(lang)
                            try {
                                voiceTTSEngine.speak("Language set to ${lang.displayName}", lang.ttsLocaleTag)
                            } catch (_: Exception) {}
                        },
                        onSelectTargetLanguage = { lang ->
                            preferences.setTargetTranslationLanguage(lang)
                            try {
                                voiceTTSEngine.speak("Translating to ${lang.displayName}", lang.ttsLocaleTag)
                            } catch (_: Exception) {}
                        },
                        onClose = { keyboardMode = KeyboardMode.ALPHABET },
                        panelHeight = standardPanelHeight
                    )
                }

                KeyboardMode.NUMERIC_PAD -> {
                    DedicatedNumericPad(
                        theme = currentTheme,
                        panelHeight = standardPanelHeight,
                        editorInfo = currentEditorInfo,
                        onKeyPress = { key -> handleKeyPress(key) },
                        onDelete = { handleBackspace() },
                        onDeleteWord = { handleDeleteWord() },
                        onSpace = { handleSpace() },
                        onEnter = {
                            vibrate()
                            lastAutoCorrection = null
                            if (isFloatingTranslateBarVisible) {
                                if (translateResultText.isNotBlank()) {
                                    onCommitText("$translateResultText ")
                                    triggerWordTTS(translateResultText, isTranslated = true)
                                    translateSourceText = ""
                                    translateResultText = ""
                                } else if (translateSourceText.isNotBlank()) {
                                    onCommitText("$translateSourceText ")
                                    translateSourceText = ""
                                } else {
                                    onPerformEditorAction()
                                }
                            } else {
                                val finishedWord = currentComposingWord.trim()
                                if (finishedWord.isNotEmpty()) {
                                    triggerWordTTS(finishedWord, isTranslated = realtimeAutoTranslate)
                                    currentComposingWord = ""
                                }
                                onPerformEditorAction()
                            }
                        },
                        onSwitchMode = { newMode ->
                            vibrate()
                            keyboardMode = newMode
                        }
                    )
                }

                else -> {
                    // MAIN TYPING KEYBOARD (ALPHABET / NUMBERS / SYMBOLS)
                    Column {
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

                        // TOP QUICK ROW (Ensures 100% constant keypad height across Alphabet, Numbers, and Symbols modes)
                        if (showNumberRow) {
                            val topRowKeys = when (keyboardMode) {
                                KeyboardMode.NUMBERS -> KeyboardLayouts.NUMBERS_TOP_ROW
                                KeyboardMode.SYMBOLS -> KeyboardLayouts.SYMBOLS_TOP_ROW
                                else -> KeyboardLayouts.NUMBER_ROW
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 1.dp, vertical = 0.5.dp),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                for (keyChar in topRowKeys) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 1.5.dp, vertical = 2.75.dp)
                                    ) {
                                        ThemedKeyBox(
                                            theme = currentTheme,
                                            modifier = Modifier.fillMaxWidth(),
                                            isSpecial = false,
                                            isSelected = false,
                                            height = (currentKeyHeight * 0.88f).coerceAtLeast(39.dp),
                                            onClick = { handleKeyPress(keyChar) }
                                        ) {
                                            Text(
                                                text = keyChar,
                                                color = currentTheme.textColor,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.align(Alignment.Center)
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
                                    .padding(horizontal = 1.dp, vertical = 0.5.dp),
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
                                // Calibrated to 1.5f weight (matching Gboard 7 alphabet keys + 1.5f shift + 1.5f backspace = 10.0f)
                                if (isLastRow) {
                                    val modWeight = if (rowKeys.size == 7) 1.5f else ((10f - rowKeys.size) / 2f).coerceIn(1.3f, 1.55f)
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

                                // Normal character keys with Gboard-style top hints on alphabet keys
                                for (key in rowKeys) {
                                    val topHint = if (keyboardMode == KeyboardMode.ALPHABET && activeLanguage.layoutType == LayoutType.QWERTY && longPressSubSymbols) {
                                        KeyboardLayouts.getSubSymbolForQWERTY(key)
                                    } else null

                                    KeyButton(
                                        text = key,
                                        modifier = Modifier.weight(1f),
                                        theme = currentTheme,
                                        topHint = topHint,
                                        height = currentKeyHeight,
                                        showPreview = keyPreviewEnabled,
                                        enableLongPressSubSymbol = longPressSubSymbols,
                                        onClick = { handleKeyPress(key) },
                                        onInsertSubSymbol = { symbol ->
                                            vibrate()
                                            onDeleteSurroundingText(1, 0)
                                            handleKeyPress(symbol)
                                        }
                                    )
                                }

                                // Right modifier on last row (Backspace with accelerating hold-to-delete & swipe-to-delete-word)
                                if (isLastRow) {
                                    val modWeight = if (rowKeys.size == 7) 1.5f else ((10f - rowKeys.size) / 2f).coerceIn(1.3f, 1.55f)
                                    KeyBackspaceButton(
                                        modifier = Modifier.weight(modWeight),
                                        theme = currentTheme,
                                        height = currentKeyHeight,
                                        onDelete = { handleBackspace() },
                                        onDeleteWord = { handleDeleteWord() }
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

                        // BOTTOM ACTION ROW (Clean Gboard Layout: Spacebar 50% width, flanked symmetrically by ?123, comma, period, enter)
                        BottomActionRow(
                            theme = currentTheme,
                            keyboardMode = keyboardMode,
                            activeLanguage = activeLanguage,
                            enabledLanguages = enabledLanguages,
                            height = currentKeyHeight,
                            isTransliterationActive = transliterationEnabled,
                            editorInfo = currentEditorInfo,
                            onMoveCursor = { offset ->
                                lastAutoCorrection = null
                                onMoveCursor(offset)
                            },
                            onSwitchMode = { newMode ->
                                vibrate()
                                lastAutoCorrection = null
                                keyboardMode = newMode
                            },
                            onComma = {
                                vibrate()
                                lastAutoCorrection = null
                                handleKeyPress(",")
                            },
                            onPeriod = {
                                vibrate()
                                lastAutoCorrection = null
                                handleKeyPress(".")
                            },
                            onInsertSymbol = { symbol ->
                                vibrate()
                                lastAutoCorrection = null
                                handleKeyPress(symbol)
                            },
                            onSwitchLanguage = {
                                vibrate()
                                preferences.switchNextLanguage()
                            },
                            onOpenLanguagePanel = {
                                vibrate()
                                keyboardMode = KeyboardMode.LANGUAGE_PANEL
                            },
                            onSpace = { handleSpace() },
                            onOpenEmoji = {
                                vibrate()
                                keyboardMode = KeyboardMode.EMOJI
                            },
                            onEnter = {
                                vibrate()
                                lastAutoCorrection = null
                                if (isFloatingTranslateBarVisible) {
                                    if (translateResultText.isNotBlank()) {
                                        onCommitText("$translateResultText ")
                                        triggerWordTTS(translateResultText, isTranslated = true)
                                        translateSourceText = ""
                                        translateResultText = ""
                                    } else if (translateSourceText.isNotBlank()) {
                                        onCommitText("$translateSourceText ")
                                        translateSourceText = ""
                                    } else {
                                        onPerformEditorAction()
                                    }
                                } else {
                                    val finishedWord = currentComposingWord.trim()
                                    if (finishedWord.isNotEmpty()) {
                                        triggerWordTTS(finishedWord, isTranslated = realtimeAutoTranslate)
                                        currentComposingWord = ""
                                    }
                                    onPerformEditorAction()
                                }
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
    onOpenSmartReply: () -> Unit = {},
    onOpenThemes: () -> Unit = {},
    onOpenLanguages: () -> Unit = {},
    toolbarItems: List<String> = emptyList(),
    onOpenCustomizeToolbar: () -> Unit = {}
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
        // DYNAMIC CUSTOMIZABLE TOOLBAR ITEMS
        val effectiveItems = if (toolbarItems.isEmpty()) {
            listOf("translate", "smart_reply", "ai", "tools", "voice", "stickers", "settings")
        } else {
            toolbarItems
        }

        for (itemId in effectiveItems) {
            when (itemId) {
                "translate" -> {
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
                }
                "smart_reply" -> {
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
                }
                "ai" -> {
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
                }
                "tools" -> {
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
                }
                "voice" -> {
                    IconButton(
                        onClick = onOpenVoice,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                    }
                }
                "stickers" -> {
                    IconButton(
                        onClick = onOpenStickers,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = "Templates", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                    }
                }
                "settings" -> {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                    }
                }
                "lang_selector" -> {
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
                }
                "tts_speech" -> {
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
                }
                "clipboard" -> {
                    IconButton(
                        onClick = onOpenClipboard,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Clipboard", tint = theme.textColor, modifier = Modifier.size(16.dp))
                    }
                }
                "emoji" -> {
                    IconButton(
                        onClick = onOpenEmoji,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.SentimentSatisfied, contentDescription = "Emoji", tint = theme.textColor, modifier = Modifier.size(16.dp))
                    }
                }
                "themes" -> {
                    IconButton(
                        onClick = onOpenThemes,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Themes", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                    }
                }
                "languages" -> {
                    IconButton(
                        onClick = onOpenLanguages,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Languages", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // TOOLBAR CUSTOMIZE SHORTCUT
        IconButton(
            onClick = onOpenCustomizeToolbar,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = "Customize Toolbar", tint = theme.accentColor, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SuggestionBar(
    candidateResult: com.example.engine.CandidateStripResult,
    currentComposingWord: String,
    activeLanguage: Language,
    targetLang: Language,
    realtimeAutoTranslate: Boolean,
    theme: KeyboardTheme,
    latestClip: String? = null,
    onPasteClip: (String) -> Unit = {},
    onCommitCandidate: (word: String, isDirectTranslation: Boolean, isLiteral: Boolean) -> Unit
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
            .height(44.dp)
            .background(theme.surfaceColor.copy(alpha = 0.85f))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val hasSmartPaste = currentComposingWord.isBlank() && !latestClip.isNullOrBlank()

        if (hasSmartPaste && latestClip != null) {
            // Smart Paste chip takes left slot
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.primaryColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.55f)),
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .clickable { onPasteClip(latestClip) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = latestClip.replace("\n", " ").trim(),
                        color = theme.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Paste",
                        color = theme.primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Center slot (Primary next word)
            val primaryText = candidateResult.primary.ifEmpty { "the" }
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.primaryColor.copy(alpha = 0.12f))
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { onCommitCandidate(primaryText, false, false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = primaryText,
                    color = theme.textColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right slot (Alternative next word)
            val altText = candidateResult.alternative.ifEmpty { "to" }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.keyColor.copy(alpha = 0.45f))
                    .clickable { onCommitCandidate(altText, false, false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = altText,
                    color = theme.textColor.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            // STANDARD GBOARD 3-SLOT CANDIDATE STRIP
            // 1. LEFT SLOT: Literal typed text (italicized in quotes so user can easily preserve exact input)
            val literalText = candidateResult.literal.ifEmpty { currentComposingWord.ifEmpty { "I" } }
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.keyColor.copy(alpha = 0.45f))
                    .clickable { onCommitCandidate(literalText, false, true) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentComposingWord.isNotEmpty()) "“$literalText”" else literalText,
                    color = theme.textColor.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    fontStyle = if (currentComposingWord.isNotEmpty()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Divider 1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(theme.textSecondaryColor.copy(alpha = 0.22f))
            )

            // 2. CENTER SLOT: Primary Auto-Correction / High-Probability Prediction
            val primaryText = candidateResult.primary.ifEmpty { currentComposingWord.ifEmpty { "Hello" } }
            val isAutoCorrection = candidateResult.isAutoCorrection
            Box(
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isAutoCorrection) theme.primaryColor.copy(alpha = 0.22f)
                        else theme.primaryColor.copy(alpha = 0.12f)
                    )
                    .border(
                        width = if (isAutoCorrection) 1.2.dp else 1.dp,
                        color = if (isAutoCorrection) theme.primaryColor.copy(alpha = 0.65f) else theme.primaryColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onCommitCandidate(primaryText, false, false) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = primaryText,
                        color = theme.textColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isAutoCorrection) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                        )
                    }
                }
            }

            // Divider 2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(theme.textSecondaryColor.copy(alpha = 0.22f))
            )

            // 3. RIGHT SLOT: Alternative Candidate OR Live Translation
            val currentTrans = liveTrans
            if (currentTrans != null && currentComposingWord.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6366F1),
                    border = BorderStroke(1.dp, Color(0xFF818CF8)),
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                        .clickable { onCommitCandidate(currentTrans, true, false) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ $currentTrans",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                val altText = candidateResult.alternative.ifEmpty { if (currentComposingWord.isEmpty()) "Thanks" else "" }
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (altText.isNotEmpty()) theme.keyColor.copy(alpha = 0.45f)
                            else Color.Transparent
                        )
                        .clickable(enabled = altText.isNotEmpty()) { onCommitCandidate(altText, false, false) }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (altText.isNotEmpty()) {
                        Text(
                            text = altText,
                            color = theme.textColor.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // FLOATING DROPDOWN LANGUAGE SLIP DRAWER (Compact frosted-glass slip directly beneath pressed chip)
    if (activeLangDropdown != null && isFloatingTranslateBarVisible) {
        // Invisible touch barrier outside the slip to close on tap-outside
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(99f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    activeLangDropdown = null
                }
        )

        val isTarget = activeLangDropdown == "TARGET"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(100f)
                .padding(
                    top = 44.dp, // Positioned directly beneath the translate bar chip row
                    start = if (!isTarget) 10.dp else 0.dp,
                    end = if (isTarget) 10.dp else 0.dp
                ),
            contentAlignment = if (isTarget) Alignment.TopEnd else Alignment.TopStart
        ) {
            FloatingDropdownLanguageSlip(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Consume click so tap-outside dismiss doesn't trigger on the drawer itself
                },
                isTarget = isTarget,
                currentLanguage = if (isTarget) targetTranslateLang else sourceTranslateLang,
                languages = Language.ALL_LANGUAGES,
                theme = currentTheme,
                onSelectLanguage = { selected: Language ->
                    vibrate()
                    if (isTarget) {
                        preferences.setTargetTranslationLanguage(selected)
                        if (translateSourceText.isNotBlank()) {
                            translateResultText = GoogleTranslationEngine.translateFast(
                                translateSourceText,
                                sourceTranslateLang.code,
                                selected.code
                            )
                        }
                    } else {
                        preferences.setSourceTranslationLanguage(selected)
                        if (translateSourceText.isNotBlank()) {
                            translateResultText = GoogleTranslationEngine.translateFast(
                                translateSourceText,
                                selected.code,
                                targetTranslateLang.code
                            )
                        }
                    }
                    activeLangDropdown = null
                },
                onDismiss = { activeLangDropdown = null }
            )
        }
    }
}
}

@Composable
fun UnifiedGboardTopStrip(
    theme: KeyboardTheme,
    isTyping: Boolean,
    candidateResult: com.example.engine.CandidateStripResult,
    currentComposingWord: String,
    activeLanguage: Language,
    targetLang: Language,
    realtimeAutoTranslate: Boolean,
    toolbarItems: List<String>,
    latestClip: String? = null,
    activeToolId: String? = null,
    voiceStatus: VoiceTypingStatus = VoiceTypingStatus.IDLE,
    onToggleLeftGrid: () -> Unit = {},
    onPasteClip: (String) -> Unit = {},
    onCommitGrammarFix: ((com.example.engine.GrammarEngine.GrammarSuggestion) -> Unit)? = null,
    onCommitCandidate: (word: String, isDirectTranslation: Boolean, isLiteral: Boolean) -> Unit,
    onOpenEmoji: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenAI: () -> Unit,
    onOpenTranslate: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenClipboard: () -> Unit = {},
    onOpenSmartReply: () -> Unit = {},
    onOpenTemplates: () -> Unit = {},
    onOpenThemes: () -> Unit = {},
    onOpenLanguages: () -> Unit = {},
    onOpenToolbarCustomize: () -> Unit = {},
    onOpenNumericPad: () -> Unit = {}
) {
    var liveTrans by remember(currentComposingWord, activeLanguage.code, targetLang.code) {
        mutableStateOf<String?>(
            if (currentComposingWord.isNotBlank()) {
                val trans = GoogleTranslationEngine.translateFast(currentComposingWord, activeLanguage.code, targetLang.code)
                if (trans.isNotBlank() && trans.lowercase() != currentComposingWord.lowercase().trim()) trans else null
            } else null
        )
    }

    var forceShowTools by remember { mutableStateOf(false) }

    LaunchedEffect(currentComposingWord, activeLanguage.code, targetLang.code) {
        if (currentComposingWord.isNotBlank()) {
            val online = GoogleTranslationEngine.translate(currentComposingWord, activeLanguage.code, targetLang.code)
            if (online.isNotBlank() && online.lowercase() != currentComposingWord.lowercase().trim()) {
                liveTrans = online
            }
        } else {
            liveTrans = null
            forceShowTools = false
        }
    }

    // Dynamic Theme-Aware Toolbar Styling
    val topStripBgBrush = Brush.verticalGradient(
        colors = listOf(
            theme.surfaceColor.copy(alpha = 0.95f),
            theme.backgroundColor.copy(alpha = 0.98f)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(topStripBgBrush)
            .border(
                width = 0.8.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        theme.accentColor.copy(alpha = 0.15f),
                        theme.accentColor.copy(alpha = 0.35f),
                        theme.accentColor.copy(alpha = 0.15f)
                    )
                ),
                shape = RectangleShape
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!forceShowTools && activeToolId == null) {
            // Left 3D Grid Toggle + Gboard 3-Candidate Strip + Right 3D Voice Mic
            NeumorphicToggleGridButton(
                isActive = false,
                theme = theme,
                onClick = { forceShowTools = true }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // 3D Gboard 3-Candidate Suggestion Strip (paste capsule completely removed per user request)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT SLOT: Literal typed text
                    val literalText = candidateResult.literal.ifEmpty { currentComposingWord }
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.keyColor.copy(alpha = 0.6f))
                            .clickable { onCommitCandidate(literalText, false, true) }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentComposingWord.isNotEmpty()) "“$literalText”" else literalText,
                            color = theme.textColor.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Divider 1
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(theme.textColor.copy(alpha = 0.15f))
                    )

                    // CENTER SLOT: Real-time Grammar Repair Chip OR Primary Candidate / Auto-Correction
                    val grammarFix = candidateResult.grammarFix
                    if (grammarFix != null && onCommitGrammarFix != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F766E).copy(alpha = 0.95f),
                            border = BorderStroke(1.2.dp, Color(0xFF2DD4BF)),
                            modifier = Modifier
                                .weight(1.35f)
                                .fillMaxHeight()
                                .clickable { onCommitGrammarFix(grammarFix) }
                                .padding(horizontal = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ ${grammarFix.correctedPhrase}",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        val primaryText = candidateResult.primary.ifEmpty { currentComposingWord }
                        val isAutoCorrection = candidateResult.isAutoCorrection
                        Box(
                            modifier = Modifier
                                .weight(1.35f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isAutoCorrection) theme.accentColor.copy(alpha = 0.22f)
                                    else theme.keyColor.copy(alpha = 0.85f)
                                )
                                .border(
                                    width = if (isAutoCorrection) 1.2.dp else 0.8.dp,
                                    color = if (isAutoCorrection) theme.accentColor.copy(alpha = 0.7f) else theme.textColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onCommitCandidate(primaryText, false, false) }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = primaryText,
                                    color = theme.textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isAutoCorrection) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(theme.accentColor)
                                    )
                                }
                            }
                        }
                    }

                    // Divider 2
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(theme.textColor.copy(alpha = 0.15f))
                    )

                    // RIGHT SLOT: Live Translation OR Alternative Candidate
                    val currentTrans = liveTrans
                    if (currentTrans != null && currentComposingWord.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.accentColor,
                            border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .weight(1.05f)
                                .fillMaxHeight()
                                .clickable { onCommitCandidate(currentTrans, true, false) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ $currentTrans",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        val altText = candidateResult.alternative.ifEmpty { "" }
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (altText.isNotEmpty()) theme.keyColor.copy(alpha = 0.6f)
                                    else Color.Transparent
                                )
                                .clickable(enabled = altText.isNotEmpty()) { onCommitCandidate(altText, false, false) }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (altText.isNotEmpty()) {
                                Text(
                                    text = altText,
                                    color = theme.textColor.copy(alpha = 0.75f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: 3D Voice Mic
            val isVoiceActive = voiceStatus == VoiceTypingStatus.LISTENING || voiceStatus == VoiceTypingStatus.CONNECTING
            NeumorphicToggleGridButton(
                isActive = isVoiceActive,
                theme = theme,
                icon = Icons.Default.Mic,
                onClick = { onOpenVoice() }
            )
        } else {
            // FULL 3D NEUMORPHIC TOOLBAR:
            // Left dedicated Tools toggle + Center scrollable quick action chips + Far-right Settings gear
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Far Left: Dedicated 3D Tools Toggle Button
                NeumorphicToggleGridButton(
                    isActive = (activeToolId == "tools"),
                    theme = theme,
                    icon = Icons.Default.GridView,
                    onClick = {
                        onToggleLeftGrid()
                        if (forceShowTools) forceShowTools = false
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Center: Dynamic scrollable container for actions & chips
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Render ordered toolbar items without redundant "tools" or "settings"
                    for (toolId in toolbarItems) {
                        when (toolId) {
                            "tools" -> {
                                // Redundant tools button removed from scrollable bar since dedicated left tools button exists
                            }

                            "smart_reply" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.AutoAwesome,
                                    label = "Smart Reply",
                                    isSelected = (activeToolId == "smart_reply"),
                                    iconColor = Color(0xFF818CF8),
                                    theme = theme,
                                    onClick = onOpenSmartReply
                                )
                            }

                            "ai" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.SmartToy,
                                    label = "AI",
                                    isSelected = (activeToolId == "ai"),
                                    iconColor = Color(0xFFF472B6),
                                    theme = theme,
                                    onClick = onOpenAI
                                )
                            }

                            "voice" -> {
                                val isListening = voiceStatus == VoiceTypingStatus.LISTENING || voiceStatus == VoiceTypingStatus.CONNECTING
                                NeumorphicToolChip(
                                    icon = Icons.Default.Mic,
                                    label = if (isListening) "Listening…" else "Voice",
                                    isSelected = (activeToolId == "voice") || isListening,
                                    iconColor = theme.accentColor,
                                    theme = theme,
                                    onClick = onOpenVoice
                                )
                            }

                            "translate" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.Translate,
                                    label = "Translate",
                                    isSelected = (activeToolId == "translate"),
                                    iconColor = Color(0xFF60A5FA),
                                    theme = theme,
                                    onClick = onOpenTranslate
                                )
                            }

                            "clipboard" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.ContentPaste,
                                    label = "Clipboard",
                                    isSelected = (activeToolId == "clipboard"),
                                    iconColor = Color(0xFF38BDF8),
                                    theme = theme,
                                    onClick = onOpenClipboard
                                )
                            }

                            "emoji" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.SentimentSatisfied,
                                    label = "Emoji",
                                    isSelected = (activeToolId == "emoji"),
                                    iconColor = Color(0xFFFBBF24),
                                    theme = theme,
                                    onClick = onOpenEmoji
                                )
                            }

                            "themes" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.Palette,
                                    label = "Themes",
                                    isSelected = false,
                                    iconColor = Color(0xFFA78BFA),
                                    theme = theme,
                                    onClick = onOpenThemes
                                )
                            }

                            "settings" -> {
                                // Dedicated settings gear is pinned to the far right
                            }

                            "lang_selector" -> {
                                // Language pill removed from main top bar per user instructions
                            }

                            "stickers" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.Celebration,
                                    label = "Templates",
                                    isSelected = (activeToolId == "stickers"),
                                    iconColor = Color(0xFFF43F5E),
                                    theme = theme,
                                    onClick = onOpenTemplates
                                )
                            }

                            "languages" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.Language,
                                    label = "Languages",
                                    isSelected = (activeToolId == "languages"),
                                    iconColor = Color(0xFF14B8A6),
                                    theme = theme,
                                    onClick = onOpenLanguages
                                )
                            }

                            "numpad" -> {
                                NeumorphicToolChip(
                                    icon = Icons.Default.Dialpad,
                                    label = "1234",
                                    isSelected = (activeToolId == "numpad"),
                                    iconColor = Color(0xFF2DD4BF),
                                    theme = theme,
                                    onClick = onOpenNumericPad
                                )
                            }
                        }
                    }

                    NeumorphicToolChip(
                        icon = Icons.Default.Tune,
                        label = "Edit",
                        isSelected = (activeToolId == "customize"),
                        iconColor = theme.textColor.copy(alpha = 0.7f),
                        theme = theme,
                        onClick = onOpenToolbarCustomize
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Far Right: Pinned Dedicated Settings Gear Button
                NeumorphicToggleGridButton(
                    isActive = (activeToolId == "settings"),
                    theme = theme,
                    icon = Icons.Default.Settings,
                    onClick = { onOpenSettings() }
                )
            }
        }
    }
}

/**
 * 3D Neumorphic Extruded Tool Squircle Chip
 * Uses dynamic theme background, accent and key text color.
 */
@Composable
fun NeumorphicToolChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    iconColor: Color = Color(0xFF94A3B8),
    theme: KeyboardTheme,
    contentDescription: String = label,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    // 3D Neumorphic Background Brush derived from theme
    val backgroundBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                theme.accentColor,
                theme.accentColor.copy(alpha = 0.85f),
                theme.accentColor.copy(alpha = 0.7f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                theme.keyColor.copy(alpha = 0.9f),
                theme.keyColor.copy(alpha = 0.98f)
            )
        )
    }

    // 3D Specular Rim / Glowing Highlight Border
    val borderBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                theme.accentColor.copy(alpha = 0.9f),
                theme.accentColor.copy(alpha = 0.4f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    }

    Box(
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 52.dp)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = theme.accentColor,
                        spotColor = theme.accentColor
                    )
                } else {
                    Modifier.shadow(
                        elevation = 2.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    )
                }
            )
            .clip(shape)
            .background(brush = backgroundBrush)
            .border(
                width = if (isSelected) 1.2.dp else 0.8.dp,
                brush = borderBrush,
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) Color.White else iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else theme.textColor.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Standalone 3D Neumorphic Square Apps / Tools / Action Button
 */
@Composable
fun NeumorphicToggleGridButton(
    isActive: Boolean,
    theme: KeyboardTheme,
    icon: ImageVector = Icons.Default.GridView,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundBrush = if (isActive) {
        Brush.verticalGradient(
            listOf(
                theme.accentColor,
                theme.accentColor.copy(alpha = 0.85f),
                theme.accentColor.copy(alpha = 0.7f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                theme.keyColor.copy(alpha = 0.9f),
                theme.keyColor.copy(alpha = 0.98f)
            )
        )
    }
    val borderBrush = if (isActive) {
        Brush.verticalGradient(
            listOf(
                theme.accentColor.copy(alpha = 0.9f),
                theme.accentColor.copy(alpha = 0.4f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .then(
                if (isActive) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = theme.accentColor,
                        spotColor = theme.accentColor
                    )
                } else {
                    Modifier.shadow(
                        elevation = 2.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    )
                }
            )
            .clip(shape)
            .background(brush = backgroundBrush)
            .border(
                width = if (isActive) 1.2.dp else 0.8.dp,
                brush = borderBrush,
                shape = shape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Tool Action",
            tint = if (isActive) Color.White else theme.textColor.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ThemedKeyBox(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isSelected: Boolean = false,
    isPressed: Boolean = false,
    height: Dp = 45.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cornerRadius = when (theme.themeStyle) {
        ThemeStyle.CLAYMORPHISM -> 10f
        ThemeStyle.NEOBRUTALISM -> 4f
        ThemeStyle.FLAT_DESIGN -> 7f
        else -> 7f
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
    height: Dp = 45.dp,
    showPreview: Boolean = true,
    enableLongPressSubSymbol: Boolean = true,
    onClick: () -> Unit,
    onInsertSubSymbol: ((String) -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    var showingSubSymbolPreview by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnInsertSubSymbol by rememberUpdatedState(onInsertSubSymbol)

    // Auto-dismiss safety timer ensures key pop preview never freezes or stays stuck on screen!
    LaunchedEffect(isPressed, showingSubSymbolPreview) {
        if (isPressed && !showingSubSymbolPreview) {
            delay(120)
            if (!showingSubSymbolPreview) {
                isPressed = false
            }
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .pointerInput(topHint, enableLongPressSubSymbol) {
                kotlinx.coroutines.coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        showingSubSymbolPreview = false
                        currentOnClick()

                        val longPressJob = if (topHint != null && enableLongPressSubSymbol && currentOnInsertSubSymbol != null) {
                            launch {
                                delay(185)
                                showingSubSymbolPreview = true
                                currentOnInsertSubSymbol?.invoke(topHint)
                            }
                        } else null

                        waitForUpOrCancellation()
                        longPressJob?.cancel()
                        isPressed = false
                        showingSubSymbolPreview = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.5.dp, vertical = 2.75.dp)
        ) {
            ThemedKeyBox(
                theme = theme,
                modifier = Modifier.fillMaxSize(),
                isSpecial = false,
                isSelected = showingSubSymbolPreview,
                isPressed = isPressed,
                height = height,
                onClick = null
            ) {
                if (topHint != null) {
                    Text(
                        text = topHint,
                        color = if (showingSubSymbolPreview) theme.accentColor else theme.textSecondaryColor.copy(alpha = 0.65f),
                        fontSize = 9.sp,
                        fontWeight = if (showingSubSymbolPreview) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 3.5.dp)
                    )
                }
                val keyFontSize = (height.value * 0.46f).coerceIn(18f, 26f).sp
                Text(
                    text = text,
                    color = theme.textColor,
                    fontSize = keyFontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Gboard-Style Floating 3D Key Preview Popup (Character Magnifier)
        if (isPressed && showPreview && text.length == 1) {
            val previewHeight = (height * 0.95f).coerceIn(42.dp, 56.dp)
            val previewOffset = -(previewHeight - 2.dp)
            val previewFontSize = (previewHeight.value * 0.50f).coerceIn(21f, 29f).sp
            val previewDisplayText = if (showingSubSymbolPreview && topHint != null) topHint else text
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = previewOffset)
                    .width(50.dp)
                    .height(previewHeight)
                    .zIndex(100f)
                    .shadow(8.dp, shape = RoundedCornerShape(10.dp))
                    .background(
                        if (showingSubSymbolPreview) theme.accentColor
                        else if (theme.isDark) Color(0xFF282C37) else Color(0xFFF1F3F4),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        if (showingSubSymbolPreview) Color.White.copy(alpha = 0.8f) else theme.primaryColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = previewDisplayText,
                    fontSize = previewFontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (showingSubSymbolPreview) Color.White else if (theme.isDark) Color.White else Color(0xFF1E293B)
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
    height: Dp = 45.dp,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    currentOnClick()
                    waitForUpOrCancellation()
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.5.dp, vertical = 2.75.dp)
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
    height: Dp = 45.dp,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    var isSwipingWord by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed && !isSwipingWord) {
            onDelete()
            // Wait 300ms initial long-press hold before fast repeat
            kotlinx.coroutines.delay(300)
            var deleteCount = 0
            while (isPressed && !isSwipingWord) {
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
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    isSwipingWord = false
                    var totalDragX = 0f
                    var wordsDeleted = 0
                    val wordSwipeThreshold = 38f // px threshold to trigger word deletion

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        val deltaX = change.position.x - change.previousPosition.x
                        totalDragX += deltaX

                        // Left swipe detection (negative X)
                        if (totalDragX < -wordSwipeThreshold) {
                            isSwipingWord = true
                            val steps = ((-totalDragX) / wordSwipeThreshold).toInt()
                            if (steps > wordsDeleted) {
                                val toDelete = steps - wordsDeleted
                                repeat(toDelete) {
                                    onDeleteWord()
                                }
                                wordsDeleted = steps
                            }
                        }
                    }

                    isPressed = false
                    isSwipingWord = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.5.dp, vertical = 2.75.dp)
        ) {
            ThemedKeyBox(
                theme = theme,
                modifier = Modifier.fillMaxSize(),
                isSpecial = true,
                isSelected = isSwipingWord,
                isPressed = isPressed,
                height = height,
                onClick = null
            ) {
                if (isSwipingWord) {
                    Text(
                        text = "‹ Word",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
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
}

@Composable
fun BottomActionRow(
    theme: KeyboardTheme,
    keyboardMode: KeyboardMode,
    activeLanguage: Language,
    enabledLanguages: List<Language>,
    height: Dp = 45.dp,
    isTransliterationActive: Boolean = false,
    editorInfo: EditorInfo? = null,
    onMoveCursor: (Int) -> Unit = {},
    onSwitchMode: (KeyboardMode) -> Unit,
    onComma: () -> Unit = {},
    onPeriod: () -> Unit = {},
    onInsertSymbol: (String) -> Unit = {},
    onSwitchLanguage: () -> Unit,
    onOpenLanguagePanel: () -> Unit = {},
    onSpace: () -> Unit,
    onOpenEmoji: () -> Unit,
    onEnter: () -> Unit
) {
    val view = LocalView.current
    var isSpacePressed by remember { mutableStateOf(false) }
    var isDraggingCursor by remember { mutableStateOf(false) }
    var isPeriodPressed by remember { mutableStateOf(false) }

    // Period Key Floating Symbol Bubble State (Gboard Punctuation Popup)
    var isSymbolPopupVisible by remember { mutableStateOf(false) }
    var selectedSymbol by remember { mutableStateOf<String?>(null) }
    var periodKeyOffsetInRow by remember { mutableStateOf(Offset.Zero) }
    var bubbleOffsetInRow by remember { mutableStateOf(Offset.Zero) }
    var bubbleSizeInRow by remember { mutableStateOf(IntSize.Zero) }

    val symbolsRow1 = remember { listOf("&", "%", "+", "\"", "-", ":", "'", "@") }
    val symbolsRow2 = remember { listOf(";", "/", "(", ")", "#", "!", ",", "?") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(200f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp, vertical = 0.5.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Mode Switcher (?123 or ABC) - Weight 1.2f
            KeySpecialButton(
                text = if (keyboardMode == KeyboardMode.ALPHABET) "?123" else "ABC",
                modifier = Modifier.weight(1.2f),
                theme = theme,
                height = height,
                onClick = {
                    onSwitchMode(if (keyboardMode == KeyboardMode.ALPHABET) KeyboardMode.NUMBERS else KeyboardMode.ALPHABET)
                }
            )

            // 2. Comma or Numpad Switcher Key - Weight 1.0f
            if (keyboardMode == KeyboardMode.NUMBERS) {
                KeySpecialButton(
                    text = "1234",
                    icon = Icons.Default.Dialpad,
                    modifier = Modifier.weight(1.0f),
                    theme = theme,
                    height = height,
                    onClick = {
                        onSwitchMode(KeyboardMode.NUMERIC_PAD)
                    }
                )
            } else {
                KeySpecialButton(
                    modifier = Modifier.weight(1.0f),
                    theme = theme,
                    text = ",",
                    height = height,
                    onClick = onComma
                )
            }

            // 3. Dedicated Emoji Key (Distinct side-by-side key) - Weight 1.0f
            KeySpecialButton(
                modifier = Modifier.weight(1.0f),
                theme = theme,
                icon = Icons.Default.SentimentSatisfied,
                height = height,
                onClick = onOpenEmoji
            )

            // 4. Space Bar (Weight 4.8f -> authentic Gboard spacious footprint) with Cursor Scrubbing & Language Label
            Box(
                modifier = Modifier
                    .weight(4.8f)
                    .height(height)
                    .pointerInput(Unit) {
                        kotlinx.coroutines.coroutineScope {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                isSpacePressed = true
                                var totalDragX = 0f
                                var didDrag = false
                                var didLongPress = false
                                val dragThreshold = 18f

                                val longPressTimer = this@coroutineScope.launch {
                                    kotlinx.coroutines.delay(380)
                                    if (!didDrag) {
                                        didLongPress = true
                                        onOpenLanguagePanel()
                                    }
                                }

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break

                                    val deltaX = change.position.x - change.previousPosition.x
                                    totalDragX += deltaX

                                    if (Math.abs(totalDragX) >= dragThreshold) {
                                        longPressTimer.cancel()
                                        didDrag = true
                                        isDraggingCursor = true
                                        val step = if (totalDragX > 0) 1 else -1
                                        onMoveCursor(step)
                                        totalDragX = 0f
                                    }
                                }

                                longPressTimer.cancel()
                                if (!didDrag && !didLongPress) {
                                    onSpace()
                                }
                                isSpacePressed = false
                                isDraggingCursor = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 1.5.dp, vertical = 2.75.dp)
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
                            val translitSuffix = if (isTransliterationActive && activeLanguage.id != "en") " (${activeLanguage.nativeName})" else ""
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${activeLanguage.flagEmoji} ${activeLanguage.displayName}$translitSuffix",
                                    color = theme.textSecondaryColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch or select language",
                                    tint = theme.textSecondaryColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Period Key with Floating Symbol Bubble on Long-Press / Drag - Weight 0.8f
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .height(height)
                    .onGloballyPositioned { coords ->
                        periodKeyOffsetInRow = coords.positionInParent()
                    }
                    .pointerInput(Unit) {
                        kotlinx.coroutines.coroutineScope {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                isPeriodPressed = true
                                var isLongPress = false
                                selectedSymbol = null
                                var currentTouch = down.position

                                val longPressJob = this@coroutineScope.launch {
                                    kotlinx.coroutines.delay(220)
                                    isLongPress = true
                                    isSymbolPopupVisible = true
                                    try {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    } catch (e: Exception) {}
                                }

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    currentTouch = change.position

                                    if (isLongPress) {
                                        val effectiveBubbleWidth = if (bubbleSizeInRow.width > 0) bubbleSizeInRow.width.toFloat() else 800f
                                        val effectiveBubbleHeight = if (bubbleSizeInRow.height > 0) bubbleSizeInRow.height.toFloat() else 220f

                                        val touchXInRow = currentTouch.x + periodKeyOffsetInRow.x
                                        val touchYInRow = currentTouch.y + periodKeyOffsetInRow.y

                                        val localX = touchXInRow - bubbleOffsetInRow.x
                                        val localY = touchYInRow - bubbleOffsetInRow.y

                                        val margin = 32f
                                        if (localX in -margin..(effectiveBubbleWidth + margin) &&
                                            localY in -margin..(effectiveBubbleHeight + margin)) {
                                            val clampedX = localX.coerceIn(0f, effectiveBubbleWidth - 1f)
                                            val clampedY = localY.coerceIn(0f, effectiveBubbleHeight - 1f)
                                            val colWidth = effectiveBubbleWidth / 8f
                                            val rowHeight = effectiveBubbleHeight / 2f

                                            val col = (clampedX / colWidth).toInt().coerceIn(0, 7)
                                            val row = (clampedY / rowHeight).toInt().coerceIn(0, 1)

                                            val newSelected = if (row == 0) symbolsRow1[col] else symbolsRow2[col]
                                            if (newSelected != selectedSymbol) {
                                                selectedSymbol = newSelected
                                                try {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                } catch (e: Exception) {}
                                            }
                                        }
                                    }
                                }

                                longPressJob.cancel()
                                if (isLongPress) {
                                    if (selectedSymbol != null) {
                                        onInsertSymbol(selectedSymbol!!)
                                        try {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        } catch (e: Exception) {}
                                    } else {
                                        onPeriod()
                                    }
                                } else {
                                    onPeriod()
                                }
                                isPeriodPressed = false
                                isSymbolPopupVisible = false
                                selectedSymbol = null
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 1.5.dp, vertical = 2.75.dp)
                ) {
                    ThemedKeyBox(
                        theme = theme,
                        modifier = Modifier.fillMaxSize(),
                        isSpecial = true,
                        isSelected = isPeriodPressed || isSymbolPopupVisible,
                        height = height,
                        onClick = null
                    ) {
                        Text(
                            text = ".",
                            color = theme.textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // 6. Enter / Action Key (Dynamic Search / Send / Go / Next / Done) - Weight 1.2f
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
                modifier = Modifier.weight(1.2f),
                theme = theme,
                isSelected = true,
                height = height,
                onClick = onEnter
            )
        }

        // GBOARD FLOATING SYMBOL BUBBLE POPUP (Period Long-Press / Drag Punctuation Grid)
        if (isSymbolPopupVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = (-94).dp)
                    .width(316.dp)
                    .height(88.dp)
                    .zIndex(1000f)
                    .onGloballyPositioned { coords ->
                        bubbleOffsetInRow = coords.positionInParent()
                        bubbleSizeInRow = coords.size
                    }
                    .shadow(12.dp, shape = RoundedCornerShape(16.dp))
                    .background(
                        if (theme.isDark) Color(0xFF262A33) else Color(0xFFF1F3F4),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = theme.primaryColor.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Row 1: &   %   +   "   -   :   '   @
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (symbol in symbolsRow1) {
                            val isSelected = symbol == selectedSymbol
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) theme.primaryColor else Color.Transparent)
                                    .clickable {
                                        onInsertSymbol(symbol)
                                        isSymbolPopupVisible = false
                                        selectedSymbol = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = symbol,
                                    color = if (isSelected) Color.White else theme.textColor,
                                    fontSize = if (isSelected) 21.sp else 17.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Row 2: ;   /   (   )   #   !   ,   ?
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (symbol in symbolsRow2) {
                            val isSelected = symbol == selectedSymbol
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) theme.primaryColor else Color.Transparent)
                                    .clickable {
                                        onInsertSymbol(symbol)
                                        isSymbolPopupVisible = false
                                        selectedSymbol = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = symbol,
                                    color = if (isSelected) Color.White else theme.textColor,
                                    fontSize = if (isSelected) 21.sp else 17.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguagePanel(
    theme: KeyboardTheme,
    activeLanguage: Language,
    targetLanguage: Language,
    initialTab: String = "SOURCE",
    onSelectSourceLanguage: (Language) -> Unit,
    onSelectTargetLanguage: (Language) -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 265.dp
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }

    val allLanguages = Language.ALL_LANGUAGES
    val displayLanguages = remember(searchQuery, selectedTab) {
        val baseList = if (selectedTab == "TARGET") {
            listOf(Language.AUTO) + allLanguages.filter { it.id != "auto" }
        } else {
            allLanguages
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim().lowercase()
            baseList.filter {
                it.displayName.lowercase().contains(q) ||
                it.nativeName.lowercase().contains(q) ||
                it.code.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // 1. Header Bar: Title, Search Field, Done Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = "Language Drawer",
                    tint = theme.accentColor,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    "Languages",
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }

            // Compact Search Bar inside keyboard
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.surfaceColor,
                border = BorderStroke(0.8.dp, theme.keyBorderColor),
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = theme.textSecondaryColor,
                        modifier = Modifier.size(13.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search 30+ languages...",
                                    color = theme.textSecondaryColor.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = theme.textSecondaryColor,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            // Done Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.primaryColor,
                modifier = Modifier
                    .height(30.dp)
                    .clickable { onClose() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("✓ Done", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Tab Segmented Control: Typing Language vs Translation Target
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Tab 1: Typing / Input Language
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedTab == "SOURCE") theme.primaryColor.copy(alpha = 0.22f) else theme.surfaceColor.copy(alpha = 0.5f),
                border = BorderStroke(
                    width = if (selectedTab == "SOURCE") 1.4.dp else 0.8.dp,
                    color = if (selectedTab == "SOURCE") theme.primaryColor else theme.keyBorderColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = "SOURCE" }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📥 Typing: ${activeLanguage.flagEmoji} ${activeLanguage.displayName}",
                        color = if (selectedTab == "SOURCE") theme.accentColor else theme.textSecondaryColor,
                        fontSize = 10.5.sp,
                        fontWeight = if (selectedTab == "SOURCE") FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tab 2: Translation Target Language
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedTab == "TARGET") Color(0xFF10B981).copy(alpha = 0.22f) else theme.surfaceColor.copy(alpha = 0.5f),
                border = BorderStroke(
                    width = if (selectedTab == "TARGET") 1.4.dp else 0.8.dp,
                    color = if (selectedTab == "TARGET") Color(0xFF10B981) else theme.keyBorderColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = "TARGET" }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📤 Translate: ${targetLanguage.flagEmoji} ${targetLanguage.displayName}",
                        color = if (selectedTab == "TARGET") Color(0xFF34D399) else theme.textSecondaryColor,
                        fontSize = 10.5.sp,
                        fontWeight = if (selectedTab == "TARGET") FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3. Scrollable Language Grid (LazyVerticalGrid with 2 columns: smooth, native within keyboard)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(displayLanguages, key = { "${it.id}_${selectedTab}" }) { lang ->
                val isSelected = if (selectedTab == "SOURCE") {
                    lang.id == activeLanguage.id
                } else {
                    lang.id == targetLanguage.id
                }
                val activeTint = if (selectedTab == "TARGET") Color(0xFF10B981) else theme.accentColor

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) activeTint.copy(alpha = 0.18f) else theme.surfaceColor,
                    border = BorderStroke(
                        width = if (isSelected) 1.4.dp else 0.7.dp,
                        color = if (isSelected) activeTint else theme.keyBorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedTab == "SOURCE") {
                                onSelectSourceLanguage(lang)
                            } else {
                                onSelectTargetLanguage(lang)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(lang.flagEmoji, fontSize = 16.sp)
                            Column {
                                Text(
                                    text = lang.displayName,
                                    color = if (isSelected) activeTint else theme.textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = lang.nativeName,
                                    color = theme.textSecondaryColor,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = activeTint,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
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
    preferences: LingoKeyPreferences,
    onEmojiClick: (String) -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    initialMode: String = "EMOJI",
    panelHeight: Dp = 265.dp
) {
    var mediaMode by remember(initialMode) {
        mutableStateOf(if (initialMode == "STICKERS") "TEMPLATES" else initialMode)
    }
    var selectedCategory by remember { mutableStateOf("Smileys") }
    var selectedStickerCategory by remember { mutableStateOf(StickerCatalog.STICKER_CATEGORIES.first()) }
    val categories = KeyboardLayouts.EMOJI_CATEGORIES.keys.toList()

    val userCustomCards by preferences.customCardTemplates.collectAsState()
    var isCreatingTemplate by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newSubtext by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("📌 My Templates") }
    var newBadgeEmoji by remember { mutableStateOf("🪔") }
    val gradientPalette = remember {
        listOf(
            listOf(0xFFF59E0B, 0xFFD97706), // Amber Golden
            listOf(0xFFF97316, 0xFFEA580C), // Sunset Orange
            listOf(0xFF6366F1, 0xFF4F46E5), // Royal Indigo
            listOf(0xFF8B5CF6, 0xFF6D28D9), // Purple Violet
            listOf(0xFF10B981, 0xFF059669), // Emerald Green
            listOf(0xFFEC4899, 0xFFBE185D), // Rose Pink
            listOf(0xFF0EA5E9, 0xFF0284C7), // Ocean Cyan
            listOf(0xFFEF4444, 0xFFDC2626)  // Crimson Red
        )
    }
    var selectedGradientIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // 1. TOP MEDIA SEGMENT TABS (Emojis & Templates)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("EMOJI", "😊 Emojis", Icons.Default.SentimentSatisfied),
                Triple("TEMPLATES", "🎨 Templates", Icons.Default.Celebration)
            )

            for ((tabKey, tabLabel, _) in tabs) {
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
                        .clickable {
                            mediaMode = tabKey
                            isCreatingTemplate = false
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tabLabel,
                            color = if (isSelected) Color.White else theme.textColor,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        when (mediaMode) {
            "TEMPLATES" -> {
                if (isCreatingTemplate) {
                    // Inline Card Template Creator
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "➕ Create Card Template",
                                color = theme.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            IconButton(
                                onClick = { isCreatingTemplate = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Live Card Preview
                        val activeGradient = gradientPalette[selectedGradientIndex]
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(listOf(Color(activeGradient[0]), Color(activeGradient[1]))))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = newBadgeEmoji, fontSize = 22.sp)
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = if (newTitle.isBlank()) "Card Template Title" else newTitle,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (newSubtext.isBlank()) (if (newContent.isBlank()) "Preview text to insert..." else newContent) else newSubtext,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 9.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Title Input
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.surfaceColor,
                            border = BorderStroke(0.75.dp, theme.keyBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                if (newTitle.isEmpty()) {
                                    Text("Title (e.g. Festival Wish, Slang Reply)", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                BasicTextField(
                                    value = newTitle,
                                    onValueChange = { newTitle = it },
                                    textStyle = TextStyle(color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        // Subtext / Tagline Input
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.surfaceColor,
                            border = BorderStroke(0.75.dp, theme.keyBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                if (newSubtext.isEmpty()) {
                                    Text("Subtext (optional e.g. Greeting, Fun Tag)", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                BasicTextField(
                                    value = newSubtext,
                                    onValueChange = { newSubtext = it },
                                    textStyle = TextStyle(color = theme.textColor, fontSize = 11.sp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        // Content Input (Message text to insert)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.surfaceColor,
                            border = BorderStroke(0.75.dp, theme.keyBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                if (newContent.isEmpty()) {
                                    Text("Message text to insert when tapped...", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                BasicTextField(
                                    value = newContent,
                                    onValueChange = { newContent = it },
                                    textStyle = TextStyle(color = theme.textColor, fontSize = 11.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 38.dp)
                                )
                            }
                        }

                        // Badge Emoji Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeList = listOf("🪔", "✨", "🌟", "🎂", "❤️", "💼", "☕", "🚀", "💬", "🎁", "⚡", "🥳", "🔥", "💰", "🌸", "🙏", "🎉", "💐")
                            for (b in badgeList) {
                                val isBadgeSelected = newBadgeEmoji == b
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isBadgeSelected) theme.primaryColor.copy(alpha = 0.3f) else theme.surfaceColor,
                                    border = BorderStroke(0.8.dp, if (isBadgeSelected) theme.primaryColor else Color.Transparent),
                                    modifier = Modifier.clickable { newBadgeEmoji = b }
                                ) {
                                    Text(b, fontSize = 15.sp, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }

                        // Gradient Palette Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for ((idx, grad) in gradientPalette.withIndex()) {
                                val isGradSelected = selectedGradientIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Color(grad[0]), Color(grad[1]))))
                                        .border(
                                            width = if (isGradSelected) 2.dp else 0.5.dp,
                                            color = if (isGradSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedGradientIndex = idx }
                                )
                            }
                        }

                        // Category selection chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (cat in StickerCatalog.STICKER_CATEGORIES) {
                                val isSelected = newCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) theme.accentColor else theme.surfaceColor,
                                    modifier = Modifier.clickable { newCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color.White else theme.textColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.surfaceColor,
                                modifier = Modifier
                                    .clickable { isCreatingTemplate = false }
                                    .padding(end = 6.dp)
                            ) {
                                Text("Cancel", color = theme.textSecondaryColor, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }

                            val canSave = newTitle.isNotBlank() && (newContent.isNotBlank() || newSubtext.isNotBlank())
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (canSave) theme.primaryColor else theme.primaryColor.copy(alpha = 0.4f),
                                modifier = Modifier.clickable(enabled = canSave) {
                                    val textToSave = newContent.ifBlank { newTitle }
                                    val subtextToSave = newSubtext.ifBlank { newTitle }
                                    preferences.saveCustomCardTemplate(
                                        title = newTitle,
                                        textToInsert = textToSave,
                                        category = newCategory,
                                        iconBadge = newBadgeEmoji,
                                        gradientColors = gradientPalette[selectedGradientIndex],
                                        subtext = subtextToSave
                                    )
                                    selectedStickerCategory = newCategory
                                    newTitle = ""
                                    newSubtext = ""
                                    newContent = ""
                                    isCreatingTemplate = false
                                }
                            ) {
                                Text("💾 Save Template", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                            }
                        }
                    }
                } else {
                    // Category Chips Bar
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

                    // Cards Container with Floating '+' FAB
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val displayCards = remember(selectedStickerCategory, userCustomCards) {
                            if (selectedStickerCategory == "📌 My Templates") {
                                userCustomCards
                            } else {
                                val userCardsInCat = userCustomCards.filter { it.category == selectedStickerCategory }
                                val builtInCardsInCat = StickerCatalog.ALL_STICKERS.filter { it.category == selectedStickerCategory }
                                userCardsInCat + builtInCardsInCat
                            }
                        }

                        if (displayCards.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("No templates in this category yet", color = theme.textSecondaryColor, fontSize = 12.sp)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = theme.primaryColor.copy(alpha = 0.15f),
                                        modifier = Modifier.clickable { isCreatingTemplate = true }
                                    ) {
                                        Text("➕ Create your first card template", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(bottom = 54.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(displayCards, key = { it.id }) { sticker ->
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
                                                    modifier = Modifier.weight(1f),
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

                                            if (sticker.isUserCreated) {
                                                IconButton(
                                                    onClick = { preferences.deleteCustomCardTemplate(sticker.id) },
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .align(Alignment.TopEnd)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete Template",
                                                        tint = Color.White.copy(alpha = 0.85f),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Floating '+' Icon fixed at the keyboard right bottom corner
                        Surface(
                            shape = CircleShape,
                            color = theme.primaryColor,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 6.dp)
                                .size(46.dp)
                                .clickable { isCreatingTemplate = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Create Card Template",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
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
fun DedicatedNumericPad(
    theme: KeyboardTheme,
    panelHeight: Dp,
    editorInfo: EditorInfo? = null,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSwitchMode: (KeyboardMode) -> Unit
) {
    val rowHeight = ((panelHeight - 16.dp) / 5).coerceIn(48.dp, 56.dp)

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // ROW 1: [ + ] [ 1 ] [ 2 ] [ 3 ] [ ⌫ ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeySpecialButton(
                text = "+",
                modifier = Modifier.weight(1f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress("+") }
            )
            KeyButton(
                text = "1",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("1") }
            )
            KeyButton(
                text = "2",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("2") }
            )
            KeyButton(
                text = "3",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("3") }
            )
            KeyBackspaceButton(
                modifier = Modifier.weight(1.2f),
                theme = theme,
                height = rowHeight,
                onDelete = onDelete,
                onDeleteWord = onDeleteWord
            )
        }

        // ROW 2: [ - ] [ 4 ] [ 5 ] [ 6 ] [ ( ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeySpecialButton(
                text = "-",
                modifier = Modifier.weight(1f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress("-") }
            )
            KeyButton(
                text = "4",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("4") }
            )
            KeyButton(
                text = "5",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("5") }
            )
            KeyButton(
                text = "6",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("6") }
            )
            KeySpecialButton(
                text = "(",
                modifier = Modifier.weight(1.2f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress("(") }
            )
        }

        // ROW 3: [ * ] [ 7 ] [ 8 ] [ 9 ] [ ) ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeySpecialButton(
                text = "*",
                modifier = Modifier.weight(1f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress("*") }
            )
            KeyButton(
                text = "7",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("7") }
            )
            KeyButton(
                text = "8",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("8") }
            )
            KeyButton(
                text = "9",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("9") }
            )
            KeySpecialButton(
                text = ")",
                modifier = Modifier.weight(1.2f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress(")") }
            )
        }

        // ROW 4: [ / ] [ , ] [ 0 ] [ . ] [ = ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeySpecialButton(
                text = "/",
                modifier = Modifier.weight(1f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress("/") }
            )
            KeySpecialButton(
                text = ",",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress(",") }
            )
            KeyButton(
                text = "0",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                showPreview = true,
                onClick = { onKeyPress("0") }
            )
            KeySpecialButton(
                text = ".",
                modifier = Modifier.weight(1.3f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress(".") }
            )
            KeySpecialButton(
                text = "=",
                modifier = Modifier.weight(1.2f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress("=") }
            )
        }

        // ROW 5: Bottom Action Bar [ ABC ] [ !?# ] [ Space ] [ % ] [ ⏎ Enter ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeySpecialButton(
                text = "ABC",
                modifier = Modifier.weight(1.4f),
                theme = theme,
                height = rowHeight,
                onClick = { onSwitchMode(KeyboardMode.ALPHABET) }
            )
            KeySpecialButton(
                text = "!?#",
                modifier = Modifier.weight(1.2f),
                theme = theme,
                height = rowHeight,
                onClick = { onSwitchMode(KeyboardMode.NUMBERS) }
            )
            // Spacebar
            Box(
                modifier = Modifier
                    .weight(2.4f)
                    .height(rowHeight)
                    .clickable { onSpace() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 2.25.dp, vertical = 2.dp)
                ) {
                    ThemedKeyBox(
                        theme = theme,
                        modifier = Modifier.fillMaxSize(),
                        isSpecial = false,
                        isSelected = false,
                        height = rowHeight,
                        onClick = null
                    ) {
                        Text(
                            text = "space",
                            color = theme.textSecondaryColor.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            KeySpecialButton(
                text = "%",
                modifier = Modifier.weight(1f),
                theme = theme,
                height = rowHeight,
                onClick = { onKeyPress("%") }
            )
            KeySpecialButton(
                icon = actionIcon,
                modifier = Modifier.weight(1.4f),
                theme = theme,
                isSelected = true,
                height = rowHeight,
                onClick = onEnter
            )
        }
    }
}

@Composable
fun GboardFloatingTranslateBar(
    theme: KeyboardTheme,
    sourceLang: Language,
    targetLang: Language,
    sourceText: String,
    resultText: String,
    isTranslating: Boolean,
    voiceStatus: VoiceTypingStatus,
    voiceRmsLevel: Float,
    allLanguages: List<Language>,
    onSourceLangChange: (Language) -> Unit,
    onTargetLangChange: (Language) -> Unit,
    onSwapLanguages: () -> Unit,
    onSourceTextChange: (String) -> Unit,
    onCommitTranslation: (String) -> Unit,
    onToggleVoiceTranslate: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    activeDropdown: String? = null,
    onToggleDropdown: ((String) -> Unit)? = null
) {
    val isListening = voiceStatus == VoiceTypingStatus.LISTENING || voiceStatus == VoiceTypingStatus.CONNECTING

    val pulseScale by animateFloatAsState(
        targetValue = if (isListening) (1f + (voiceRmsLevel.coerceIn(0f, 10f) / 25f)).coerceIn(1f, 1.25f) else 1f,
        animationSpec = tween(150),
        label = "mic_pulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = theme.surfaceColor,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Row 1: Source Language Pill ⇄ Target Language Pill | Mic | Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Source Language Pill
                val isSourceActive = activeDropdown == "SOURCE"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSourceActive) theme.accentColor.copy(alpha = 0.15f) else theme.backgroundColor.copy(alpha = 0.85f),
                    border = BorderStroke(
                        if (isSourceActive) 1.5.dp else 0.5.dp,
                        if (isSourceActive) theme.accentColor else theme.accentColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onToggleDropdown?.invoke("SOURCE")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${sourceLang.flagEmoji} ${sourceLang.displayName}",
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = if (isSourceActive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Select Source Language",
                            tint = if (isSourceActive) theme.accentColor else theme.textSecondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Swap Direction Button (⇄)
                IconButton(
                    onClick = onSwapLanguages,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Languages",
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Target Language Pill
                val isTargetActive = activeDropdown == "TARGET"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isTargetActive) theme.accentColor.copy(alpha = 0.15f) else theme.backgroundColor.copy(alpha = 0.85f),
                    border = BorderStroke(
                        if (isTargetActive) 1.5.dp else 0.5.dp,
                        if (isTargetActive) theme.accentColor else theme.accentColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onToggleDropdown?.invoke("TARGET")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${targetLang.flagEmoji} ${targetLang.displayName}",
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = if (isTargetActive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Select Target Language",
                            tint = if (isTargetActive) theme.accentColor else theme.textSecondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Live Audio Mic Button with Pulsing Scale
                Surface(
                    shape = CircleShape,
                    color = if (isListening) theme.accentColor else theme.backgroundColor,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(pulseScale)
                        .clickable { onToggleVoiceTranslate() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Voice Translate",
                            tint = if (isListening) Color.White else theme.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Close Button (✕)
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Floating Translate Bar",
                        tint = theme.textSecondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Live Translation Stream & Direct Commit Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.backgroundColor.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    // Source Input / Speech Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sourceLang.flagEmoji,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = if (sourceText.isNotBlank()) sourceText else if (isListening) "Listening... speak now" else "Type or speak to translate...",
                            color = if (sourceText.isNotBlank()) theme.textColor else theme.textSecondaryColor.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (sourceText.isNotBlank() || resultText.isNotBlank()) {
                            IconButton(
                                onClick = onClear,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = theme.textSecondaryColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Real-time Live Translation Card
                    if (resultText.isNotBlank() || isTranslating) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = theme.accentColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, theme.accentColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = resultText.isNotBlank()) {
                                    onCommitTranslation(resultText)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = "➔",
                                        color = theme.accentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = targetLang.flagEmoji,
                                        fontSize = 12.sp
                                    )
                                    if (resultText.isNotBlank()) {
                                        Text(
                                            text = resultText,
                                            color = theme.textColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                    } else if (isTranslating) {
                                        Text(
                                            text = "Translating...",
                                            color = theme.textSecondaryColor,
                                            fontSize = 11.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isTranslating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(13.dp),
                                            strokeWidth = 1.5.dp,
                                            color = theme.accentColor
                                        )
                                    }
                                    if (resultText.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = theme.accentColor,
                                            modifier = Modifier.clickable { onCommitTranslation(resultText) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Insert",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Insert",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
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
        }
    }
}

@Composable
fun FloatingDropdownLanguageSlip(
    isTarget: Boolean,
    currentLanguage: Language,
    languages: List<Language>,
    theme: KeyboardTheme,
    onSelectLanguage: (Language) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // Popular language IDs upfront matching Gboard & the user's reference:
    // Bengali, Chinese, French, German, Russian, English, Spanish, Hindi, Arabic, Portuguese, Japanese, Italian, Korean
    val popularIds = remember(isTarget) {
        if (isTarget) {
            listOf("bn", "zh", "fr", "de", "ru", "en", "es", "hi", "ar", "pt", "ja", "it", "ko")
        } else {
            listOf("auto", "bn", "zh", "fr", "de", "ru", "en", "es", "hi", "ar", "pt", "ja", "it", "ko")
        }
    }

    val displayLanguages = remember(searchQuery, isTarget) {
        val allList = mutableListOf<Language>()
        if (!isTarget) {
            allList.add(Language.AUTO)
        }
        allList.addAll(Language.ALL_LANGUAGES.filter { it.id != "auto" })

        val q = searchQuery.trim().lowercase()
        if (q.isNotEmpty()) {
            allList.filter {
                it.displayName.lowercase().contains(q) ||
                it.nativeName.lowercase().contains(q) ||
                it.code.lowercase().contains(q)
            }
        } else {
            val popularList = popularIds.mapNotNull { id ->
                if (id == "auto") Language.AUTO
                else allList.firstOrNull { it.id.equals(id, ignoreCase = true) }
            }
            val remainingList = allList.filter { lang ->
                popularIds.none { it.equals(lang.id, ignoreCase = true) }
            }.sortedBy { it.displayName }
            popularList + remainingList
        }
    }

    // Frosted-Glass Floating Dropdown Slip: Width ~176dp, Height ~210dp, 14dp rounded corners
    Surface(
        modifier = modifier
            .width(176.dp)
            .height(210.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(14.dp),
        color = theme.surfaceColor.copy(alpha = 0.98f),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.5f),
                    theme.accentColor.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            // Inline Micro-Search Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.backgroundColor.copy(alpha = 0.85f))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = theme.textSecondaryColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        color = theme.textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search...",
                                color = theme.textSecondaryColor.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = theme.textSecondaryColor,
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = theme.textSecondaryColor.copy(alpha = 0.15f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Scrollable Language List with smooth one-touch instant selection
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(displayLanguages, key = { "${it.id}_${it.code}" }) { lang ->
                    val isSelected = lang.id.equals(currentLanguage.id, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLanguage(lang)
                            }
                            .background(
                                if (isSelected) theme.accentColor.copy(alpha = 0.14f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = lang.displayName,
                            color = if (isSelected) theme.accentColor else theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = theme.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
