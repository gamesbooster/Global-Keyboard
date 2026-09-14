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

enum class LanguageCategory(val displayName: String) {
    ALL("All Languages"),
    INDIAN("Indian Languages"),
    GLOBAL("Global & World")
}

data class Language(
    val id: String,
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val layoutType: LayoutType = LayoutType.QWERTY,
    val isEnabledByDefault: Boolean = false,
    val ttsLocaleTag: String = "en-US",
    val isGlobal: Boolean = false
) {
    companion object {
        val ALL_LANGUAGES = listOf(
            // --- Primary & Indian Languages ---
            Language("en", "en", "English", "English", "🇺🇸", LayoutType.QWERTY, true, "en-US", isGlobal = true),
            Language("hi", "hi", "Hindi", "हिन्दी", "🇮🇳", LayoutType.DEVANAGARI, true, "hi-IN", isGlobal = false),
            Language("mr", "mr", "Marathi", "मराठी", "🇮🇳", LayoutType.DEVANAGARI, false, "mr-IN", isGlobal = false),
            Language("bn", "bn", "Bengali", "বাংলা", "🇮🇳", LayoutType.BENGALI, false, "bn-IN", isGlobal = false),
            Language("gu", "gu", "Gujarati", "ગુજરાતી", "🇮🇳", LayoutType.GUJARATI, false, "gu-IN", isGlobal = false),
            Language("ta", "ta", "Tamil", "தமிழ்", "🇮🇳", LayoutType.TAMIL, false, "ta-IN", isGlobal = false),
            Language("te", "te", "Telugu", "తెలుగు", "🇮🇳", LayoutType.TELUGU, false, "te-IN", isGlobal = false),
            Language("kn", "kn", "Kannada", "ಕನ್ನಡ", "🇮🇳", LayoutType.KANNADA, false, "kn-IN", isGlobal = false),
            Language("ml", "ml", "Malayalam", "മലയാളം", "🇮🇳", LayoutType.MALAYALAM, false, "ml-IN", isGlobal = false),
            Language("pa", "pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳", LayoutType.GURMUKHI, false, "pa-IN", isGlobal = false),
            Language("ur", "ur", "Urdu", "اردو", "🇵🇰", LayoutType.ARABIC, false, "ur-PK", isGlobal = false),
            Language("sa", "sa", "Sanskrit", "संस्कृतम्", "🇮🇳", LayoutType.DEVANAGARI, false, "hi-IN", isGlobal = false),
            Language("ne", "ne", "Nepali", "नेपाली", "🇳🇵", LayoutType.DEVANAGARI, false, "ne-NP", isGlobal = false),
            Language("or", "or", "Odia", "ଓଡ଼ିଆ", "🇮🇳", LayoutType.BENGALI, false, "bn-IN", isGlobal = false),
            Language("as", "as", "Assamese", "অসমীয়া", "🇮🇳", LayoutType.BENGALI, false, "bn-IN", isGlobal = false),

            // --- Foreign & World Languages ---
            Language("es", "es", "Spanish", "Español", "🇪🇸", LayoutType.QWERTY, false, "es-ES", isGlobal = true),
            Language("fr", "fr", "French", "Français", "🇫🇷", LayoutType.QWERTY, false, "fr-FR", isGlobal = true),
            Language("de", "de", "German", "Deutsch", "🇩🇪", LayoutType.QWERTY, false, "de-DE", isGlobal = true),
            Language("ar", "ar", "Arabic", "العربية", "🇸🇦", LayoutType.ARABIC, false, "ar-SA", isGlobal = true),
            Language("pt", "pt", "Portuguese", "Português", "🇧🇷", LayoutType.QWERTY, false, "pt-BR", isGlobal = true),
            Language("ru", "ru", "Russian", "Русский", "🇷🇺", LayoutType.QWERTY, false, "ru-RU", isGlobal = true),
            Language("it", "it", "Italian", "Italiano", "🇮🇹", LayoutType.QWERTY, false, "it-IT", isGlobal = true),
            Language("ja", "ja", "Japanese", "日本語", "🇯🇵", LayoutType.JAPANESE, false, "ja-JP", isGlobal = true),
            Language("ko", "ko", "Korean", "한국어", "🇰🇷", LayoutType.QWERTY, false, "ko-KR", isGlobal = true),
            Language("zh", "zh", "Chinese", "中文 (简体)", "🇨🇳", LayoutType.QWERTY, false, "zh-CN", isGlobal = true),
            Language("tr", "tr", "Turkish", "Türkçe", "🇹🇷", LayoutType.QWERTY, false, "tr-TR", isGlobal = true),
            Language("id", "id", "Indonesian", "Bahasa Indonesia", "🇮🇩", LayoutType.QWERTY, false, "id-ID", isGlobal = true),
            Language("vi", "vi", "Vietnamese", "Tiếng Việt", "🇻🇳", LayoutType.QWERTY, false, "vi-VN", isGlobal = true),
            Language("nl", "nl", "Dutch", "Nederlands", "🇳🇱", LayoutType.QWERTY, false, "nl-NL", isGlobal = true),
            Language("pl", "pl", "Polish", "Polski", "🇵🇱", LayoutType.QWERTY, false, "pl-PL", isGlobal = true),
            Language("th", "th", "Thai", "ไทย", "🇹🇭", LayoutType.QWERTY, false, "th-TH", isGlobal = true),
            Language("fa", "fa", "Persian", "فارسی", "🇮🇷", LayoutType.ARABIC, false, "fa-IR", isGlobal = true),
            Language("fil", "fil", "Filipino", "Tagalog", "🇵🇭", LayoutType.QWERTY, false, "fil-PH", isGlobal = true)
        )

        fun getById(id: String): Language {
            return ALL_LANGUAGES.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALL_LANGUAGES.first()
        }

        fun getByCode(code: String): Language {
            return ALL_LANGUAGES.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ALL_LANGUAGES.first()
        }
    }
}
