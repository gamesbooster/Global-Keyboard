package com.example.engine.smartreply

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.example.model.*
import java.io.IOException

/**
 * Main coordinator and repository for Smart Reply operations.
 * Validates input, guards sensitive input fields, tracks quotas, invokes provider,
 * applies quality filtering, and produces state for the UI.
 */
class SmartReplyRepository(
    val provider: SmartReplyProvider = GeminiSmartReplyProvider(),
    val usageRepository: SmartReplyUsageRepository
) {

    companion object {
        const val MAX_INPUT_LENGTH = 8000
    }

    /**
     * Checks if the active Android editor represents a sensitive field (password, PIN, OTP, credentials).
     */
    fun isSensitiveField(editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) return false
        val inputType = editorInfo.inputType
        val classType = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        if (classType == InputType.TYPE_CLASS_TEXT) {
            if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            ) {
                return true
            }
        }
        if (classType == InputType.TYPE_CLASS_NUMBER) {
            if (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
                return true
            }
        }
        return false
    }

    suspend fun processSmartReply(
        message: String,
        style: SmartReplyStyle = SmartReplyLibrary.SMART_MATCH,
        tone: SmartReplyTone = SmartReplyTone.AUTO,
        language: String = "auto",
        maxReplies: Int = 4,
        editorInfo: EditorInfo? = null,
        isInputConnectionActive: Boolean = true
    ): SmartReplyUiState {
        // 1. Guard against sensitive editors
        if (isSensitiveField(editorInfo)) {
            return SmartReplyUiState.SensitiveFieldBlocked
        }

        val trimmed = message.trim()
        if (trimmed.isBlank()) {
            return SmartReplyUiState.EmptyResult("Please paste or type a received message first.")
        }

        if (trimmed.length > MAX_INPUT_LENGTH) {
            return SmartReplyUiState.EmptyResult("Message is too long (over $MAX_INPUT_LENGTH characters). Please shorten it.")
        }

        // 2. Check usage limit / free quota
        if (!usageRepository.canGenerate()) {
            return SmartReplyUiState.RateLimited("Free Smart Reply limit reached (0 left). Watch a short ad or spin to get more free replies!")
        }

        val effectiveStyle = if (style != SmartReplyLibrary.SMART_MATCH) {
            style
        } else if (tone != SmartReplyTone.AUTO) {
            tone.toSmartReplyStyle()
        } else {
            style
        }

        val request = SmartReplyRequest(
            message = trimmed,
            requestedLanguage = language,
            style = effectiveStyle,
            tone = tone,
            maxReplies = maxReplies
        )

        val result = provider.analyzeAndGenerateReplies(request)

        return result.fold(
            onSuccess = { response ->
                if (response.replies.isEmpty()) {
                    SmartReplyUiState.EmptyResult("No reply suggestions could be generated for this message.")
                } else {
                    usageRepository.recordSuccessfulGeneration()
                    SmartReplyUiState.Success(response)
                }
            },
            onFailure = { error ->
                // Always try offline provider so user never gets blocked by external API outages
                val offlineProvider = OfflineSmartReplyProvider()
                kotlinx.coroutines.runBlocking {
                    val fallbackResult = offlineProvider.analyzeAndGenerateReplies(request)
                    fallbackResult.fold(
                        onSuccess = { offlineResponse ->
                            if (offlineResponse.replies.isNotEmpty()) {
                                usageRepository.recordSuccessfulGeneration()
                                SmartReplyUiState.Success(offlineResponse)
                            } else {
                                SmartReplyUiState.EmptyResult("No reply suggestions could be generated for this message.")
                            }
                        },
                        onFailure = {
                            SmartReplyUiState.ProviderError(error.message ?: "Smart Reply is temporarily unavailable.")
                        }
                    )
                }
            }
        )
    }
}
