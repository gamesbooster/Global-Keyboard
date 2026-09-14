package com.example.engine

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import com.example.model.Language
import com.example.model.VoiceGender
import java.util.Locale

class VoiceTTSEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentGender = VoiceGender.NAMASTE
    private var currentPitch = 1.0f
    private var currentSpeed = 1.0f

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceTTSEngine", "Failed to init TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            val indianEnglish = Locale.forLanguageTag("en-IN")
            if (tts?.isLanguageAvailable(indianEnglish) ?: -1 >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = indianEnglish
            } else {
                tts?.language = Locale.US
            }
            applyVoiceConfig()
        } else {
            isInitialized = false
        }
    }

    fun setVoiceGender(gender: VoiceGender) {
        currentGender = gender
        applyVoiceConfig()
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        applyVoiceConfig()
    }

    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
        applyVoiceConfig()
    }

    private fun applyVoiceConfig() {
        val engine = tts ?: return
        if (!isInitialized) return

        val effectivePitch = (currentPitch * currentGender.pitchModifier).coerceIn(0.5f, 2.0f)
        val effectiveSpeed = (currentSpeed * currentGender.speedModifier).coerceIn(0.5f, 2.0f)
        engine.setPitch(effectivePitch)
        engine.setSpeechRate(effectiveSpeed)

        // Try to match pleasant natural voices if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val voices = engine.voices
                if (voices != null && voices.isNotEmpty()) {
                    val targetVoice = when (currentGender) {
                        VoiceGender.NAMASTE -> {
                            // Prioritize Indian natural voice / smooth female or calm voice
                            voices.firstOrNull { voice ->
                                val name = voice.name.lowercase()
                                val country = voice.locale.country.uppercase()
                                (country == "IN" || country == "IND") && (name.contains("natural") || name.contains("female") || name.contains("cxx"))
                            } ?: voices.firstOrNull { voice ->
                                val country = voice.locale.country.uppercase()
                                country == "IN" || country == "IND"
                            }
                        }
                        VoiceGender.SOFT -> {
                            voices.firstOrNull { voice ->
                                val name = voice.name.lowercase()
                                name.contains("soft") || name.contains("calm") || name.contains("natural") || name.contains("low")
                            }
                        }
                        VoiceGender.MALE -> {
                            voices.firstOrNull { voice ->
                                val name = voice.name.lowercase()
                                (name.contains("male") || name.contains("man") || name.contains("#male") || name.contains("-m-")) && !name.contains("female")
                            }
                        }
                        VoiceGender.FEMALE -> {
                            voices.firstOrNull { voice ->
                                val name = voice.name.lowercase()
                                name.contains("female") || name.contains("woman") || name.contains("#female") || name.contains("-f-")
                            }
                        }
                    }
                    if (targetVoice != null) {
                        engine.voice = targetVoice
                    }
                }
            } catch (e: Exception) {
                // Fallback to pitch modulation
            }
        }
    }

    fun speak(text: String, languageTag: String? = null, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        val engine = tts ?: return
        if (!isInitialized || text.isBlank()) return

        try {
            if (!languageTag.isNullOrBlank()) {
                val locale = Locale.forLanguageTag(languageTag)
                val result = engine.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val inLocale = Locale.forLanguageTag("en-IN")
                    if (engine.isLanguageAvailable(inLocale) >= TextToSpeech.LANG_AVAILABLE) {
                        engine.language = inLocale
                    } else {
                        engine.language = Locale.US
                    }
                }
            }
            applyVoiceConfig()
            engine.speak(text, queueMode, null, "lingokey_tts_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("VoiceTTSEngine", "Error in speak", e)
        }
    }

    fun speakWord(word: String, languageTag: String? = null) {
        val clean = word.trim()
        if (clean.length > 1) {
            speak(clean, languageTag, TextToSpeech.QUEUE_FLUSH)
        }
    }

    fun speakSentence(sentence: String, languageTag: String? = null) {
        val clean = sentence.trim()
        if (clean.length > 2) {
            speak(clean, languageTag, TextToSpeech.QUEUE_FLUSH)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        @Volatile
        private var instance: VoiceTTSEngine? = null

        fun getInstance(context: Context): VoiceTTSEngine {
            return instance ?: synchronized(this) {
                instance ?: VoiceTTSEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
