package com.example.model

enum class KeyboardMode {
    ALPHABET,
    NUMBERS,
    SYMBOLS,
    EMOJI,
    CLIPBOARD,
    AI_PANEL,
    TRANSLATE_PANEL,
    VOICE_PANEL,
    LANGUAGE_PANEL,
    TOOLS_PANEL,
    SMART_REPLY_PANEL,
    THEMES_PANEL,
    TOOLBAR_CUSTOMIZE
}

sealed class AIState {
    object Idle : AIState()
    object Loading : AIState()
    data class Success(val originalText: String, val resultText: String, val actionTitle: String) : AIState()
    data class Error(val message: String) : AIState()
}

data class SmartReplyOption(
    val text: String,
    val category: String = "Quick Reply"
)
