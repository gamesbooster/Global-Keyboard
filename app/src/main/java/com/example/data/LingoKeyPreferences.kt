package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LingoKeyPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lingokey_ai_prefs", Context.MODE_PRIVATE)

    // Keyboard Settings
    val autoCapitalization = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CAP, true))
    val autoCorrection = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CORRECT, true))
    val showSuggestions = MutableStateFlow(prefs.getBoolean(KEY_SUGGESTIONS, true))
    val showNumberRow = MutableStateFlow(prefs.getBoolean(KEY_NUMBER_ROW, true))
    val transliterationEnabled = MutableStateFlow(prefs.getBoolean(KEY_TRANSLITERATION, true))
    val emojiSuggestionsEnabled = MutableStateFlow(prefs.getBoolean(KEY_EMOJI_SUGGESTIONS, true))
    val keyVibration = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    val keySound = MutableStateFlow(prefs.getBoolean(KEY_SOUND, false))
    val keyVibrationStrength = MutableStateFlow(prefs.getInt(KEY_VIBRATION_STRENGTH, 25))

    // Languages
    val enabledLanguages = MutableStateFlow(loadEnabledLanguages())
    val activeLanguage = MutableStateFlow(loadActiveLanguage())

    // Themes
    val currentTheme = MutableStateFlow(loadCurrentTheme())
    val keyRoundnessDp = MutableStateFlow(prefs.getInt(KEY_KEY_ROUNDNESS, 8))
    val keyHeightDp = MutableStateFlow(prefs.getInt(KEY_KEY_HEIGHT, 48))
    val keyPreviewEnabled = MutableStateFlow(prefs.getBoolean(KEY_KEY_PREVIEW, true))

    // AI & Real-time Translation
    val aiEnabled = MutableStateFlow(prefs.getBoolean(KEY_AI_ENABLED, true))
    val defaultTone = MutableStateFlow(ToneType.valueOf(prefs.getString(KEY_DEFAULT_TONE, ToneType.PROFESSIONAL.name) ?: ToneType.PROFESSIONAL.name))
    val autoDetectLanguage = MutableStateFlow(prefs.getBoolean(KEY_AUTO_DETECT_LANG, true))
    val realtimeAutoTranslate = MutableStateFlow(prefs.getBoolean(KEY_REALTIME_AUTO_TRANSLATE, true))
    val targetTranslationLanguage = MutableStateFlow(Language.getById(prefs.getString(KEY_TARGET_TRANSLATE_LANG, "hi") ?: "hi"))
    val saveAIHistory = MutableStateFlow(prefs.getBoolean(KEY_SAVE_AI_HISTORY, true))

    // Voice & Realtime TTS
    val voiceGender = MutableStateFlow(
        try {
            VoiceGender.valueOf(prefs.getString(KEY_VOICE_GENDER, VoiceGender.NAMASTE.name) ?: VoiceGender.NAMASTE.name)
        } catch (e: Exception) {
            VoiceGender.NAMASTE
        }
    )
    val realtimeTTSMode = MutableStateFlow(RealtimeTTSMode.valueOf(prefs.getString(KEY_REALTIME_TTS_MODE, RealtimeTTSMode.MANUAL_TAP.name) ?: RealtimeTTSMode.MANUAL_TAP.name))
    val voicePitch = MutableStateFlow(prefs.getFloat(KEY_VOICE_PITCH, 1.0f))
    val voiceSpeed = MutableStateFlow(prefs.getFloat(KEY_VOICE_SPEED, 1.0f))
    val voiceGuidanceEnabled = MutableStateFlow(prefs.getBoolean(KEY_VOICE_GUIDANCE, true))

    // Layout & Typing Style
    val typingStyle = MutableStateFlow(prefs.getString(KEY_TYPING_STYLE, "transliteration") ?: "transliteration")
    val showKeyBorders = MutableStateFlow(prefs.getBoolean(KEY_SHOW_KEY_BORDERS, true))

    // Onboarding
    val onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))

    // Theme Mode (White / Light Mode vs Dark Mode)
    val isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_IS_DARK_MODE, false))

    // Premium / VIP In-App Purchase Status
    val isPremiumUser = MutableStateFlow(prefs.getBoolean(KEY_IS_PREMIUM_USER, false))
    val aiUsageCount = MutableStateFlow(prefs.getInt(KEY_AI_USAGE_COUNT, 0))

    // Smart Reply Preferences
    val smartReplyEnabled = MutableStateFlow(prefs.getBoolean(KEY_SMART_REPLY_ENABLED, true))
    val smartReplyDefaultStyleId = MutableStateFlow(prefs.getString(KEY_SMART_REPLY_DEFAULT_STYLE, "smart_match") ?: "smart_match")
    val smartReplyDefaultTone = MutableStateFlow(prefs.getString(KEY_SMART_REPLY_DEFAULT_TONE, "AUTO") ?: "AUTO")
    val smartReplyLanguage = MutableStateFlow(prefs.getString(KEY_SMART_REPLY_LANG, "auto") ?: "auto")
    val smartReplyMaxSuggestions = MutableStateFlow(prefs.getInt(KEY_SMART_REPLY_MAX_SUGGESTIONS, 4))
    val smartReplyUsageCount = MutableStateFlow(prefs.getInt(KEY_SMART_REPLY_USAGE_COUNT, 0))

    private fun loadCurrentTheme(): KeyboardTheme {
        val themeId = prefs.getString(KEY_THEME, "midnight") ?: "midnight"
        if (themeId == "custom_user") {
            val bg1 = prefs.getInt("custom_bg_1", 0xFF0F0C29.toInt())
            val bg2 = prefs.getInt("custom_bg_2", 0xFF302B63.toInt())
            val keyCol = prefs.getInt("custom_key_col", 0x44FFFFFF.toInt())
            val textCol = prefs.getInt("custom_text_col", 0xFFFFFFFF.toInt())
            val accentCol = prefs.getInt("custom_accent_col", 0xFF6366F1.toInt())
            val alpha = prefs.getFloat("custom_alpha", 0.85f)
            return KeyboardTheme(
                id = "custom_user",
                name = "My Custom Theme",
                isDark = true,
                primaryColor = Color(accentCol),
                backgroundColor = Color(bg1),
                surfaceColor = Color(bg2),
                keyColor = Color(keyCol),
                keySpecialColor = Color(keyCol),
                keyPressedColor = Color(accentCol),
                textColor = Color(textCol),
                textSecondaryColor = Color(textCol).copy(alpha = 0.7f),
                accentColor = Color(accentCol),
                keyBorderColor = Color(accentCol).copy(alpha = 0.35f),
                backgroundGradient = listOf(Color(bg1), Color(bg2)),
                keyAlpha = alpha,
                backdropType = "custom"
            )
        }
        return KeyboardTheme.getById(themeId)
    }

    fun setCustomTheme(
        name: String,
        bgGradient: List<Color>,
        keyColor: Color,
        textColor: Color,
        accentColor: Color,
        keyAlpha: Float
    ) {
        val bg1 = (bgGradient.firstOrNull() ?: Color(0xFF0F0C29)).value.toLong().toInt()
        val bg2 = (if (bgGradient.size > 1) bgGradient[1] else bgGradient.firstOrNull() ?: Color(0xFF302B63)).value.toLong().toInt()
        val keyInt = keyColor.value.toLong().toInt()
        val textInt = textColor.value.toLong().toInt()
        val accentInt = accentColor.value.toLong().toInt()

        prefs.edit()
            .putString(KEY_THEME, "custom_user")
            .putInt("custom_bg_1", bg1)
            .putInt("custom_bg_2", bg2)
            .putInt("custom_key_col", keyInt)
            .putInt("custom_text_col", textInt)
            .putInt("custom_accent_col", accentInt)
            .putFloat("custom_alpha", keyAlpha)
            .apply()

        currentTheme.value = KeyboardTheme(
            id = "custom_user",
            name = name,
            isDark = true,
            primaryColor = accentColor,
            backgroundColor = Color(bg1),
            surfaceColor = Color(bg2),
            keyColor = keyColor,
            keySpecialColor = keyColor,
            keyPressedColor = accentColor,
            textColor = textColor,
            textSecondaryColor = textColor.copy(alpha = 0.7f),
            accentColor = accentColor,
            keyBorderColor = accentColor.copy(alpha = 0.35f),
            backgroundGradient = bgGradient,
            keyAlpha = keyAlpha,
            backdropType = "custom"
        )
    }

    private fun loadEnabledLanguages(): List<Language> {
        val raw = prefs.getString(KEY_ENABLED_LANGUAGES, "en,hi") ?: "en,hi"
        val ids = raw.split(",").filter { it.isNotBlank() }
        val list = ids.map { Language.getById(it) }
        return if (list.isEmpty()) listOf(Language.getById("en"), Language.getById("hi")) else list
    }

    private fun loadActiveLanguage(): Language {
        val activeId = prefs.getString(KEY_ACTIVE_LANGUAGE, "en") ?: "en"
        return Language.getById(activeId)
    }

    fun setAutoCapitalization(value: Boolean) {
        autoCapitalization.value = value
        prefs.edit().putBoolean(KEY_AUTO_CAP, value).apply()
    }

    fun setAutoCorrection(value: Boolean) {
        autoCorrection.value = value
        prefs.edit().putBoolean(KEY_AUTO_CORRECT, value).apply()
    }

    fun setShowSuggestions(value: Boolean) {
        showSuggestions.value = value
        prefs.edit().putBoolean(KEY_SUGGESTIONS, value).apply()
    }

    fun setShowNumberRow(value: Boolean) {
        showNumberRow.value = value
        prefs.edit().putBoolean(KEY_NUMBER_ROW, value).apply()
    }

    fun setTransliterationEnabled(value: Boolean) {
        transliterationEnabled.value = value
        prefs.edit().putBoolean(KEY_TRANSLITERATION, value).apply()
    }

    fun setEmojiSuggestionsEnabled(value: Boolean) {
        emojiSuggestionsEnabled.value = value
        prefs.edit().putBoolean(KEY_EMOJI_SUGGESTIONS, value).apply()
    }

    fun setKeyVibration(value: Boolean) {
        keyVibration.value = value
        prefs.edit().putBoolean(KEY_VIBRATION, value).apply()
    }

    fun setKeySound(value: Boolean) {
        keySound.value = value
        prefs.edit().putBoolean(KEY_SOUND, value).apply()
    }

    fun setEnabledLanguages(languages: List<Language>) {
        val distinct = languages.distinctBy { it.id }
        enabledLanguages.value = distinct
        val joined = distinct.joinToString(",") { it.id }
        prefs.edit().putString(KEY_ENABLED_LANGUAGES, joined).apply()
        if (activeLanguage.value !in distinct) {
            setActiveLanguage(distinct.firstOrNull() ?: Language.getById("en"))
        }
    }

    fun setActiveLanguage(language: Language) {
        activeLanguage.value = language
        prefs.edit().putString(KEY_ACTIVE_LANGUAGE, language.id).apply()
    }

    fun switchNextLanguage() {
        val currentList = enabledLanguages.value
        if (currentList.size <= 1) return
        val currentIndex = currentList.indexOfFirst { it.id == activeLanguage.value.id }
        val nextIndex = (currentIndex + 1) % currentList.size
        setActiveLanguage(currentList[nextIndex])
    }

    fun setTheme(theme: KeyboardTheme) {
        currentTheme.value = theme
        prefs.edit().putString(KEY_THEME, theme.id).apply()
    }

    fun setAIEnabled(value: Boolean) {
        aiEnabled.value = value
        prefs.edit().putBoolean(KEY_AI_ENABLED, value).apply()
    }

    fun setDefaultTone(tone: ToneType) {
        defaultTone.value = tone
        prefs.edit().putString(KEY_DEFAULT_TONE, tone.name).apply()
    }

    fun setAutoDetectLanguage(value: Boolean) {
        autoDetectLanguage.value = value
        prefs.edit().putBoolean(KEY_AUTO_DETECT_LANG, value).apply()
    }

    fun setRealtimeAutoTranslate(value: Boolean) {
        realtimeAutoTranslate.value = value
        prefs.edit().putBoolean(KEY_REALTIME_AUTO_TRANSLATE, value).apply()
    }

    fun setTargetTranslationLanguage(language: Language) {
        targetTranslationLanguage.value = language
        realtimeAutoTranslate.value = true
        prefs.edit()
            .putString(KEY_TARGET_TRANSLATE_LANG, language.id)
            .putBoolean(KEY_REALTIME_AUTO_TRANSLATE, true)
            .apply()
    }

    fun setVoiceGender(gender: VoiceGender) {
        voiceGender.value = gender
        prefs.edit().putString(KEY_VOICE_GENDER, gender.name).apply()
    }

    fun setRealtimeTTSMode(mode: RealtimeTTSMode) {
        realtimeTTSMode.value = mode
        prefs.edit().putString(KEY_REALTIME_TTS_MODE, mode.name).apply()
    }

    fun setVoicePitch(pitch: Float) {
        voicePitch.value = pitch
        prefs.edit().putFloat(KEY_VOICE_PITCH, pitch).apply()
    }

    fun setVoiceSpeed(speed: Float) {
        voiceSpeed.value = speed
        prefs.edit().putFloat(KEY_VOICE_SPEED, speed).apply()
    }

    fun setOnboardingCompleted(value: Boolean) {
        onboardingCompleted.value = value
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    }

    fun setKeyHeightDp(height: Int) {
        keyHeightDp.value = height
        prefs.edit().putInt(KEY_KEY_HEIGHT, height).apply()
    }

    fun setKeyPreviewEnabled(enabled: Boolean) {
        keyPreviewEnabled.value = enabled
        prefs.edit().putBoolean(KEY_KEY_PREVIEW, enabled).apply()
    }

    fun setTypingStyle(style: String) {
        typingStyle.value = style
        prefs.edit().putString(KEY_TYPING_STYLE, style).apply()
        if (style == "transliteration") {
            setTransliterationEnabled(true)
        } else if (style == "english") {
            setTransliterationEnabled(false)
        }
    }

    fun setShowKeyBorders(show: Boolean) {
        showKeyBorders.value = show
        prefs.edit().putBoolean(KEY_SHOW_KEY_BORDERS, show).apply()
    }

    fun setVoiceGuidanceEnabled(enabled: Boolean) {
        voiceGuidanceEnabled.value = enabled
        prefs.edit().putBoolean(KEY_VOICE_GUIDANCE, enabled).apply()
    }

    // Theme Mode Methods (Light vs Dark)
    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, enabled).apply()
    }

    fun toggleDarkMode() {
        setDarkMode(!isDarkMode.value)
    }

    // In-App Purchase & VIP Membership Methods
    fun setPremiumUser(premium: Boolean) {
        isPremiumUser.value = premium
        prefs.edit().putBoolean(KEY_IS_PREMIUM_USER, premium).apply()
    }

    fun activatePro() {
        setPremiumUser(true)
    }

    fun restorePurchases(): Boolean {
        // Simulates queryPurchasesAsync from Google Play Billing Client
        val wasActive = prefs.getBoolean(KEY_IS_PREMIUM_USER, false)
        if (!wasActive) {
            // Restore demo purchase for tester
            setPremiumUser(true)
            return true
        }
        return true
    }

    fun resetToFreeTier() {
        setPremiumUser(false)
        aiUsageCount.value = 0
        prefs.edit().putInt(KEY_AI_USAGE_COUNT, 0).apply()
    }

    fun isThemeUnlocked(theme: KeyboardTheme): Boolean {
        if (!theme.isPro) return true
        return isPremiumUser.value
    }

    fun canUseTone(tone: ToneType): Boolean {
        if (isPremiumUser.value) return true
        // Confident & Funny are VIP exclusive
        return tone != ToneType.CONFIDENT && tone != ToneType.FUNNY
    }

    fun recordAiUsage(): Boolean {
        if (isPremiumUser.value) return true
        val current = aiUsageCount.value + 1
        aiUsageCount.value = current
        prefs.edit().putInt(KEY_AI_USAGE_COUNT, current).apply()
        return current <= 10 // 10 free AI generations for free tier
    }

    fun hasRemainingAiCredits(): Boolean {
        if (isPremiumUser.value) return true
        return aiUsageCount.value < 10
    }

    fun setSmartReplyEnabled(enabled: Boolean) {
        smartReplyEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SMART_REPLY_ENABLED, enabled).apply()
    }

    fun setSmartReplyDefaultStyle(styleId: String) {
        smartReplyDefaultStyleId.value = styleId
        prefs.edit().putString(KEY_SMART_REPLY_DEFAULT_STYLE, styleId).apply()
    }

    fun setSmartReplyDefaultTone(tone: String) {
        smartReplyDefaultTone.value = tone
        prefs.edit().putString(KEY_SMART_REPLY_DEFAULT_TONE, tone).apply()
    }

    fun setSmartReplyLanguage(language: String) {
        smartReplyLanguage.value = language
        prefs.edit().putString(KEY_SMART_REPLY_LANG, language).apply()
    }

    fun setSmartReplyMaxSuggestions(max: Int) {
        val clamped = max.coerceIn(3, 5)
        smartReplyMaxSuggestions.value = clamped
        prefs.edit().putInt(KEY_SMART_REPLY_MAX_SUGGESTIONS, clamped).apply()
    }

    fun recordSmartReplyUsage(): Boolean {
        val current = smartReplyUsageCount.value + 1
        smartReplyUsageCount.value = current
        prefs.edit().putInt(KEY_SMART_REPLY_USAGE_COUNT, current).apply()
        return true
    }

    companion object {
        @Volatile
        private var instance: LingoKeyPreferences? = null

        fun getInstance(context: Context): LingoKeyPreferences {
            return instance ?: synchronized(this) {
                instance ?: LingoKeyPreferences(context.applicationContext).also { instance = it }
            }
        }

        private const val KEY_AUTO_CAP = "pref_auto_cap"
        private const val KEY_AUTO_CORRECT = "pref_auto_correct"
        private const val KEY_SUGGESTIONS = "pref_suggestions"
        private const val KEY_NUMBER_ROW = "pref_number_row"
        private const val KEY_TRANSLITERATION = "pref_transliteration"
        private const val KEY_EMOJI_SUGGESTIONS = "pref_emoji_suggestions"
        private const val KEY_VIBRATION = "pref_vibration"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_VIBRATION_STRENGTH = "pref_vibration_strength"
        private const val KEY_ENABLED_LANGUAGES = "pref_enabled_languages"
        private const val KEY_ACTIVE_LANGUAGE = "pref_active_language"
        private const val KEY_THEME = "pref_theme"
        private const val KEY_KEY_ROUNDNESS = "pref_key_roundness"
        private const val KEY_KEY_HEIGHT = "pref_key_height"
        private const val KEY_KEY_PREVIEW = "pref_key_preview"
        private const val KEY_AI_ENABLED = "pref_ai_enabled"
        private const val KEY_DEFAULT_TONE = "pref_default_tone"
        private const val KEY_AUTO_DETECT_LANG = "pref_auto_detect_lang"
        private const val KEY_REALTIME_AUTO_TRANSLATE = "pref_realtime_auto_translate"
        private const val KEY_TARGET_TRANSLATE_LANG = "pref_target_translate_lang"
        private const val KEY_SAVE_AI_HISTORY = "pref_save_ai_history"
        private const val KEY_VOICE_GENDER = "pref_voice_gender"
        private const val KEY_REALTIME_TTS_MODE = "pref_realtime_tts_mode"
        private const val KEY_VOICE_PITCH = "pref_voice_pitch"
        private const val KEY_VOICE_SPEED = "pref_voice_speed"
        private const val KEY_VOICE_GUIDANCE = "pref_voice_guidance"
        private const val KEY_TYPING_STYLE = "pref_typing_style"
        private const val KEY_SHOW_KEY_BORDERS = "pref_show_key_borders"
        private const val KEY_ONBOARDING_COMPLETED = "pref_onboarding_completed"
        private const val KEY_IS_DARK_MODE = "pref_is_dark_mode"
        private const val KEY_IS_PREMIUM_USER = "pref_is_premium_user"
        private const val KEY_AI_USAGE_COUNT = "pref_ai_usage_count"
        private const val KEY_SMART_REPLY_ENABLED = "pref_smart_reply_enabled"
        private const val KEY_SMART_REPLY_DEFAULT_STYLE = "pref_smart_reply_default_style"
        private const val KEY_SMART_REPLY_DEFAULT_TONE = "pref_smart_reply_default_tone"
        private const val KEY_SMART_REPLY_LANG = "pref_smart_reply_lang"
        private const val KEY_SMART_REPLY_MAX_SUGGESTIONS = "pref_smart_reply_max_suggestions"
        private const val KEY_SMART_REPLY_USAGE_COUNT = "pref_smart_reply_usage_count"
    }
}
