package com.example.engine

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Universal Translation Engine powered by Google Translate.
 * Translates any word, phrase, sentence, or paragraph across all languages.
 * Includes in-memory caching, pre-warmed vocabulary, Google Input Tools transliteration,
 * and seamless offline fallback.
 */
object GoogleTranslationEngine {
    private const val TAG = "GoogleTranslationEngine"
    private const val CLIENT_ENDPOINT = "https://clients5.google.com/translate_a/t"
    private const val INPUT_TOOLS_ENDPOINT = "https://inputtools.google.com/request"

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Fast in-memory cache: "sl->tl:text" to "translatedText"
    private val translationCache = ConcurrentHashMap<String, String>()

    init {
        prewarmCache()
    }

    private fun cacheKey(text: String, sourceLang: String, targetLang: String): String {
        val s = if (sourceLang.isBlank() || sourceLang.equals("auto", ignoreCase = true)) "auto" else sourceLang.lowercase()
        val t = targetLang.lowercase()
        return "$s->$t:${text.trim().lowercase()}"
    }

    fun getCached(text: String, sourceLang: String, targetLang: String): String? {
        if (text.isBlank()) return text
        return translationCache[cacheKey(text, sourceLang, targetLang)]
    }

    fun cacheResult(text: String, sourceLang: String, targetLang: String, result: String) {
        if (text.isNotBlank() && result.isNotBlank()) {
            translationCache[cacheKey(text, sourceLang, targetLang)] = result
        }
    }

    /**
     * Synchronous fast translation (0ms):
     * Checks in-memory cache first, then checks local offline dictionary.
     * Also schedules a background prefetch for any uncached words.
     */
    fun translateFast(text: String, sourceLang: String, targetLang: String): String {
        if (text.isBlank()) return text
        val clean = text.trim()
        val sl = if (sourceLang.equals("auto", ignoreCase = true)) TranslationMatrix.detectLanguage(clean) else sourceLang.lowercase()
        val tl = targetLang.lowercase()
        if (sl == tl) return clean

        // 1. In-memory cache hit
        val cached = translationCache[cacheKey(clean, sl, tl)]
            ?: translationCache[cacheKey(clean, "auto", tl)]
        if (cached != null && cached.isNotBlank()) return cached

        // 2. Offline dictionary lookup
        val offline = TranslationMatrix.translateOffline(clean, sl, tl)
        if (offline != clean) {
            translationCache[cacheKey(clean, sl, tl)] = offline
            return offline
        }

        // 3. Trigger asynchronous background translation so future calls are instant
        prefetch(clean, sl, tl)
        return clean
    }

    /**
     * Prefetches translation asynchronously in the background.
     */
    fun prefetch(text: String, sourceLang: String, targetLang: String) {
        if (text.isBlank()) return
        val clean = text.trim()
        val key = cacheKey(clean, sourceLang, targetLang)
        if (translationCache.containsKey(key)) return

        engineScope.launch {
            try {
                translate(clean, sourceLang, targetLang)
            } catch (e: Exception) {
                // Background prefetch error ignored
            }
        }
    }

    /**
     * Asynchronously translates ANY text (single words, complex phrases, full sentences)
     * using the real Google Translate engine.
     */
    suspend fun translate(text: String, sourceLang: String, targetLang: String): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text
        val clean = text.trim()
        val sl = if (sourceLang.equals("auto", ignoreCase = true)) "auto" else sourceLang.lowercase()
        val tl = targetLang.lowercase()

        if (sl != "auto" && sl == tl) return@withContext clean

        val key = cacheKey(clean, sl, tl)
        val cached = translationCache[key]
        if (cached != null && cached.isNotBlank()) return@withContext cached

        // 1. Try Google Translate primary client endpoint
        try {
            val encodedQuery = URLEncoder.encode(clean, StandardCharsets.UTF_8.name())
            val url = "$CLIENT_ENDPOINT?client=dict-chrome-ex&sl=$sl&tl=$tl&q=$encodedQuery"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                .header("Accept", "*/*")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val parsed = parseGoogleTranslateResponse(body)
                    if (!parsed.isNullOrBlank()) {
                        // Check if Google Translate left Romanized text unchanged for Indic languages
                        val finalResult = if (parsed.equals(clean, ignoreCase = true) && tl in listOf("hi", "mr", "bn", "gu", "pa", "ta", "te", "kn", "ml", "ur")) {
                            transliterateIndic(clean, tl) ?: parsed
                        } else {
                            parsed
                        }
                        translationCache[key] = finalResult
                        return@withContext finalResult
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google Translate endpoint error: ${e.message}")
        }

        // 2. Google Input Tools transliteration fallback for Indic languages if source was Romanized
        if (tl in listOf("hi", "mr", "bn", "gu", "pa", "ta", "te", "kn", "ml", "ur")) {
            val transliterated = transliterateIndic(clean, tl)
            if (!transliterated.isNullOrBlank()) {
                translationCache[key] = transliterated
                return@withContext transliterated
            }
        }

        // 3. Fallback to offline dictionary
        val fallback = TranslationMatrix.translateOffline(clean, sl, tl)
        translationCache[key] = fallback
        return@withContext fallback
    }

    /**
     * Parses the JSON array response from Google Translate endpoint:
     * Handles:
     * - ["translation"]
     * - [["translation", "detectedLang"]]
     * - ["Sentence 1. ", "Sentence 2."]
     */
    private fun parseGoogleTranslateResponse(jsonString: String): String? {
        return try {
            val jsonArray = JSONArray(jsonString)
            val sb = StringBuilder()
            for (i in 0 until jsonArray.length()) {
                when (val item = jsonArray.get(i)) {
                    is String -> sb.append(item)
                    is JSONArray -> {
                        if (item.length() > 0) {
                            val inner = item.get(0)
                            if (inner is String) {
                                sb.append(inner)
                            }
                        }
                    }
                }
            }
            val result = sb.toString().trim()
            if (result.isNotEmpty()) result else null
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing Google Translate JSON: ${e.message}")
            null
        }
    }

    /**
     * Google Input Tools transliteration for Indic languages
     * e.g. "namaskar" -> "नमस्कार", "aap kaise ho" -> "आप कैसे हो"
     */
    suspend fun transliterateIndic(text: String, targetLang: String): String? = withContext(Dispatchers.IO) {
        val itcCode = when (targetLang.lowercase()) {
            "hi" -> "hi-t-i0-und"
            "mr" -> "mr-t-i0-und"
            "bn" -> "bn-t-i0-und"
            "gu" -> "gu-t-i0-und"
            "pa" -> "pa-t-i0-und"
            "ta" -> "ta-t-i0-und"
            "te" -> "te-t-i0-und"
            "kn" -> "kn-t-i0-und"
            "ml" -> "ml-t-i0-und"
            "ur" -> "ur-t-i0-und"
            else -> return@withContext null
        }
        try {
            val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.name())
            val url = "$INPUT_TOOLS_ENDPOINT?text=$encoded&itc=$itcCode&num=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .get()
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val root = JSONArray(body)
                    if (root.length() >= 2 && root.getString(0) == "SUCCESS") {
                        val dataArray = root.getJSONArray(1)
                        val sb = StringBuilder()
                        for (i in 0 until dataArray.length()) {
                            val entry = dataArray.getJSONArray(i)
                            val candidates = entry.getJSONArray(1)
                            if (candidates.length() > 0) {
                                if (i > 0) sb.append(" ")
                                sb.append(candidates.getString(0))
                            }
                        }
                        val result = sb.toString().trim()
                        if (result.isNotEmpty()) return@withContext result
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Input Tools error: ${e.message}")
        }
        null
    }

    private fun prewarmCache() {
        try {
            for ((phrase, transMap) in TranslationMatrix.phraseDictionary) {
                for ((lang, transText) in transMap) {
                    translationCache["en->$lang:$phrase"] = transText
                    translationCache["auto->$lang:$phrase"] = transText
                }
            }
            for ((word, transMap) in TranslationMatrix.singleWordDictionary) {
                for ((lang, transText) in transMap) {
                    translationCache["en->$lang:$word"] = transText
                    translationCache["auto->$lang:$word"] = transText
                }
            }
            for ((rom, transMap) in TranslationMatrix.romanizedIndicLookup) {
                for ((lang, transText) in transMap) {
                    translationCache["auto->$lang:$rom"] = transText
                    translationCache["en->$lang:$rom"] = transText
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Prewarm cache error: ${e.message}")
        }
    }
}
