package com.example.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.*
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.ClipboardRepository
import com.example.data.LingoKeyPreferences
import com.example.engine.AIProvider
import com.example.engine.GeminiAIProvider
import com.example.engine.SmartAIProvider
import com.example.engine.VoiceTTSEngine
import java.util.Locale

class LingoKeyInputMethodService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private lateinit var preferences: LingoKeyPreferences
    private lateinit var clipboardRepository: ClipboardRepository
    private lateinit var aiProvider: AIProvider
    private lateinit var voiceTTSEngine: VoiceTTSEngine

    private var speechRecognizer: SpeechRecognizer? = null
    private var inputComposeView: ComposeView? = null
    private var onVoiceStatusUpdate: ((VoiceTypingStatus, Float, String) -> Unit)? = null
    private var lastPartialTextLength: Int = 0
    private var isVoiceSessionActive: Boolean = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val accumulatedSpeechText = StringBuilder()
    private val restartListeningRunnable = Runnable {
        if (isVoiceSessionActive) {
            launchSpeechRecognizerInternal()
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performAttach()
            savedStateRegistryController.performRestore(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        preferences = LingoKeyPreferences.getInstance(this)
        clipboardRepository = ClipboardRepository.getInstance(this)
        aiProvider = GeminiAIProvider()
        voiceTTSEngine = VoiceTTSEngine.getInstance(this)
    }

    /**
     * Ensures the soft keyboard ALWAYS appears whenever any search bar,
     * text field, web input, or password box is tapped.
     */
    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    /**
     * Prevent fullscreen extract mode in search bars or landscape orientations,
     * which would otherwise cover the screen and hide the custom keyboard UI.
     */
    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onCreateInputView(): View {
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        val composeView = ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleRegistry))
            setViewTreeLifecycleOwner(this@LingoKeyInputMethodService)
            setViewTreeViewModelStoreOwner(this@LingoKeyInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@LingoKeyInputMethodService)

            setContent {
                ComposeKeyboardView(
                    preferences = preferences,
                    clipboardRepository = clipboardRepository,
                    aiProvider = aiProvider,
                    voiceTTSEngine = voiceTTSEngine,
                    onCommitText = { text ->
                        commitTextSafe(text)
                    },
                    onDeleteSurroundingText = { before, after ->
                        deleteSurroundingTextSafe(before, after)
                    },
                    onMoveCursor = { offset ->
                        moveCursorSafe(offset)
                    },
                    onPerformEditorAction = {
                        performEditorActionSafe()
                    },
                    onGetContextText = {
                        getContextTextSafe()
                    },
                    onReplaceAllText = { newText ->
                        replaceAllTextSafe(newText)
                    },
                    onOpenSettings = {
                        openSettingsActivity()
                    },
                    onOpenThemesStore = {
                        openSettingsActivity("themes")
                    },
                    onStartSpeechRecognition = {
                        startSpeechRecognition()
                    },
                    onStopSpeechRecognition = {
                        stopSpeechRecognition()
                    },
                    registerVoiceListener = { listener ->
                        onVoiceStatusUpdate = listener
                    },
                    onGetEditorInfo = { currentInputEditorInfo },
                    isInputConnectionActive = { currentInputConnection != null }
                )
            }
        }
        inputComposeView = composeView
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        voiceTTSEngine.stop()
        stopSpeechRecognition()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        voiceTTSEngine.stop()
        stopSpeechRecognition()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        stopSpeechRecognition()
        inputComposeView = null
    }

    private fun commitTextSafe(text: String) {
        try {
            val ic: InputConnection? = currentInputConnection
            ic?.commitText(text, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteSurroundingTextSafe(beforeLength: Int, afterLength: Int) {
        try {
            val ic: InputConnection? = currentInputConnection
            if (ic != null) {
                val selected = ic.getSelectedText(0)
                if (!selected.isNullOrEmpty()) {
                    ic.commitText("", 1)
                } else {
                    val deleted = ic.deleteSurroundingText(beforeLength, afterLength)
                    if (!deleted) {
                        // Fallback key event for apps/search bars that ignore deleteSurroundingText
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun moveCursorSafe(offset: Int) {
        try {
            val ic = currentInputConnection ?: return
            if (offset < 0) {
                repeat(-offset) {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
                }
            } else if (offset > 0) {
                repeat(offset) {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performEditorActionSafe() {
        try {
            val ic: InputConnection? = currentInputConnection
            val info = currentInputEditorInfo
            val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_DONE
            if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                val handled = ic?.performEditorAction(action) ?: false
                if (!handled) {
                    ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            } else {
                ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getContextTextSafe(): String {
        return try {
            val ic: InputConnection? = currentInputConnection
            val selected = ic?.getSelectedText(0)?.toString()
            if (!selected.isNullOrBlank()) {
                selected
            } else {
                val before = ic?.getTextBeforeCursor(300, 0)?.toString() ?: ""
                val after = ic?.getTextAfterCursor(100, 0)?.toString() ?: ""
                (before + after).trim()
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun replaceAllTextSafe(newText: String) {
        try {
            val ic = currentInputConnection ?: return
            val before = ic.getTextBeforeCursor(2000, 0)?.length ?: 0
            val after = ic.getTextAfterCursor(1000, 0)?.length ?: 0
            ic.deleteSurroundingText(before, after)
            ic.commitText(newText, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openSettingsActivity(destination: String? = null) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                if (destination != null) {
                    putExtra("destination", destination)
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            onVoiceStatusUpdate?.invoke(VoiceTypingStatus.ERROR, 0f, "Voice typing is not supported on this system")
            return
        }

        isVoiceSessionActive = true
        accumulatedSpeechText.clear()
        launchSpeechRecognizerInternal()
    }

    private fun launchSpeechRecognizerInternal() {
        if (!isVoiceSessionActive) return

        mainHandler.removeCallbacks(restartListeningRunnable)
        lastPartialTextLength = 0

        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore cleanup
        }

        try {
            onVoiceStatusUpdate?.invoke(VoiceTypingStatus.CONNECTING, 0f, "Connecting microphone...")

            val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (!isVoiceSessionActive) return
                    val displayText = if (accumulatedSpeechText.isNotEmpty()) accumulatedSpeechText.toString() else "Listening... Speak now"
                    onVoiceStatusUpdate?.invoke(VoiceTypingStatus.LISTENING, 0.2f, displayText)
                }

                override fun onBeginningOfSpeech() {
                    if (!isVoiceSessionActive) return
                    onVoiceStatusUpdate?.invoke(VoiceTypingStatus.LISTENING, 0.5f, "Hearing your voice...")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    if (!isVoiceSessionActive) return
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                    onVoiceStatusUpdate?.invoke(VoiceTypingStatus.LISTENING, normalized, "")
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    if (!isVoiceSessionActive) return
                    onVoiceStatusUpdate?.invoke(VoiceTypingStatus.LISTENING, 0.15f, "Listening continuously...")
                }

                override fun onError(error: Int) {
                    if (!isVoiceSessionActive) return

                    // If it is speech timeout, no match, or recognizer busy, seamlessly restart to keep voice typing continuous like Gboard
                    if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                        error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ||
                        error == SpeechRecognizer.ERROR_CLIENT) {
                        
                        lastPartialTextLength = 0
                        // Short delay before auto-resuming listening loop
                        mainHandler.postDelayed(restartListeningRunnable, 250)
                        return
                    }

                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue for voice"
                        SpeechRecognizer.ERROR_SERVER -> "Voice server error"
                        else -> "Voice recognition stopped (Code: $error)"
                    }
                    onVoiceStatusUpdate?.invoke(VoiceTypingStatus.ERROR, 0f, message)
                    isVoiceSessionActive = false
                    lastPartialTextLength = 0
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val phraseText = matches?.firstOrNull()?.trim()

                    if (!phraseText.isNullOrBlank()) {
                        // Replace the last stream partial chunk with the finalized sentence chunk
                        if (lastPartialTextLength > 0) {
                            deleteSurroundingTextSafe(lastPartialTextLength, 0)
                        }
                        commitTextSafe("$phraseText ")

                        if (accumulatedSpeechText.isNotEmpty()) {
                            accumulatedSpeechText.append(" ")
                        }
                        accumulatedSpeechText.append(phraseText)
                        val fullSummary = accumulatedSpeechText.toString()
                        onVoiceStatusUpdate?.invoke(VoiceTypingStatus.LISTENING, 0.3f, fullSummary)
                    }
                    lastPartialTextLength = 0

                    // Continuously resume listening if voice session is active (continuous voice typing)
                    if (isVoiceSessionActive) {
                        mainHandler.postDelayed(restartListeningRunnable, 100)
                    } else {
                        onVoiceStatusUpdate?.invoke(VoiceTypingStatus.IDLE, 0f, accumulatedSpeechText.toString())
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    if (!isVoiceSessionActive) return
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partialText = matches?.firstOrNull()
                    if (!partialText.isNullOrBlank()) {
                        // Real-time Gboard style streaming into the text field:
                        if (lastPartialTextLength > 0) {
                            deleteSurroundingTextSafe(lastPartialTextLength, 0)
                        }
                        commitTextSafe(partialText)
                        lastPartialTextLength = partialText.length

                        val preview = if (accumulatedSpeechText.isNotEmpty()) {
                            "${accumulatedSpeechText} $partialText"
                        } else {
                            partialText
                        }
                        onVoiceStatusUpdate?.invoke(VoiceTypingStatus.LISTENING, 0.8f, preview)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val currentLocaleTag = preferences.activeLanguage.value.ttsLocaleTag
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocaleTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLocaleTag)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, currentLocaleTag)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(
                    "hi-IN", "bn-IN", "mr-IN", "gu-IN", "ta-IN", "te-IN", "kn-IN", "ml-IN", "pa-IN", "ur-PK", "en-US", "es-ES", "fr-FR", "de-DE", "ja-JP"
                ))
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra("android.speech.extra.DICTATION_MODE", true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
            }
            recognizer.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            onVoiceStatusUpdate?.invoke(VoiceTypingStatus.ERROR, 0f, "Could not start voice recognition: ${e.localizedMessage ?: "Unknown error"}")
            isVoiceSessionActive = false
        }
    }

    private fun stopSpeechRecognition() {
        isVoiceSessionActive = false
        mainHandler.removeCallbacks(restartListeningRunnable)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore
        }
        lastPartialTextLength = 0
        val finalResult = accumulatedSpeechText.toString()
        onVoiceStatusUpdate?.invoke(VoiceTypingStatus.IDLE, 0f, finalResult)
    }
}
