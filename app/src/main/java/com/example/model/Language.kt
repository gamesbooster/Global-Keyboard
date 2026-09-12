package com.example.model

enum class LayoutType {
    QWERTY,
    DEVANAGARI,
    BENGALI,
    GUJARATI,
    TAMIL,
    TELUGU,
    KANNADA,
    MALAYALAM,
    GURMUKHI,
    ARABIC,
    JAPANESE
}

data class Language(
    val id: String,
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val layoutType: LayoutType = LayoutType.QWERTY,
    val isEnabledByDefault: Boolean = false,
    val ttsLocaleTag: String = "en-US"
) {
    companion object {
        val ALL_LANGUAGES = listOf(
            Language("en", "en", "English", "English", "🇺🇸", LayoutType.QWERTY, true, "en-US"),
            Language("hi", "hi", "Hindi", "हिन्दी", "🇮🇳", LayoutType.DEVANAGARI, true, "hi-IN"),
            Language("mr", "mr", "Marathi", "मराठी", "🇮🇳", LayoutType.DEVANAGARI, false, "mr-IN"),
            Language("bn", "bn", "Bengali", "বাংলা", "🇮🇳", LayoutType.BENGALI, false, "bn-IN"),
            Language("gu", "gu", "Gujarati", "ગુજરાતી", "🇮🇳", LayoutType.GUJARATI, false, "gu-IN"),
            Language("ta", "ta", "Tamil", "தமிழ்", "🇮🇳", LayoutType.TAMIL, false, "ta-IN"),
            Language("te", "te", "Telugu", "తెలుగు", "🇮🇳", LayoutType.TELUGU, false, "te-IN"),
            Language("kn", "kn", "Kannada", "ಕನ್ನಡ", "🇮🇳", LayoutType.KANNADA, false, "kn-IN"),
            Language("ml", "ml", "Malayalam", "മലയാളം", "🇮🇳", LayoutType.MALAYALAM, false, "ml-IN"),
            Language("pa", "pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳", LayoutType.GURMUKHI, false, "pa-IN"),
            Language("ur", "ur", "Urdu", "اردو", "🇵🇰", LayoutType.ARABIC, false, "ur-PK"),
            Language("es", "es", "Spanish", "Español", "🇪🇸", LayoutType.QWERTY, false, "es-ES"),
            Language("fr", "fr", "French", "Français", "🇫🇷", LayoutType.QWERTY, false, "fr-FR"),
            Language("de", "de", "German", "Deutsch", "🇩🇪", LayoutType.QWERTY, false, "de-DE"),
            Language("ja", "ja", "Japanese", "日本語", "🇯🇵", LayoutType.JAPANESE, false, "ja-JP")
        )

        fun getById(id: String): Language {
            return ALL_LANGUAGES.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALL_LANGUAGES.first()
        }

        fun getByCode(code: String): Language {
            return ALL_LANGUAGES.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ALL_LANGUAGES.first()
        }
    }
}
