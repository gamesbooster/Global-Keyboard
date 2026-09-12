package com.example.model

enum class VoiceGender(
    val displayName: String,
    val iconEmoji: String,
    val pitchModifier: Float,
    val speedModifier: Float = 1.0f,
    val description: String = ""
) {
    NAMASTE("Namaste Voice", "🙏", 0.95f, 0.90f, "Warm, polite & gentle Indian greeting tone"),
    SOFT("Soft Gentle", "🍃", 0.90f, 0.85f, "Quiet, relaxed & very easy on the ears"),
    FEMALE("Female Voice", "👩", 1.05f, 0.98f, "Clear, crisp, melodic tone"),
    MALE("Male Voice", "👨", 0.88f, 0.95f, "Deep, warm, resonant tone")
}

enum class RealtimeTTSMode(val displayName: String, val description: String) {
    OFF("Off (Muted)", "No automatic voice output"),
    WORD_BY_WORD("Real-time Word", "Speaks each word automatically after typing"),
    SENTENCE_BY_SENTENCE("Real-time Sentence", "Speaks full sentence on punctuation (. ? ! or enter)"),
    MANUAL_TAP("Tap to Speak", "Only speaks when you tap the voice/speaker icon")
}

data class VoiceSettings(
    val gender: VoiceGender = VoiceGender.NAMASTE,
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
    val realtimeTTSMode: RealtimeTTSMode = RealtimeTTSMode.MANUAL_TAP,
    val autoSpeakTranslation: Boolean = true
)
