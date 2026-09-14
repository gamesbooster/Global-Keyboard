package com.example.engine.smartreply

import android.util.Log
import com.example.BuildConfig
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Production Gemini API Smart Reply Provider.
 * Integrates directly with Google Gemini API via secure REST, parses structured JSON replies,
 * supports the rich Reply Style & Tone library with structured parameters,
 * defends against prompt injections, and falls back to offline engine when needed.
 */
class GeminiSmartReplyProvider(
    private val offlineFallback: SmartReplyProvider = OfflineSmartReplyProvider()
) : SmartReplyProvider {

    private val TAG = "GeminiSmartReply"
    private val CANDIDATE_MODELS = listOf("gemini-3.6-flash", "gemini-3.8-flash", "gemini-flash-latest", "gemini-3.5-flash")

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String? {
        val key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            null
        }
        return if (!key.isNullOrBlank() && key != "MY_GEMINI_API_KEY") key else null
    }

    override suspend fun analyzeAndGenerateReplies(request: SmartReplyRequest): Result<SmartReplyResponse> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey == null) {
            // Graceful fallback to local rule & intent-based reply engine
            Log.d(TAG, "No Gemini API key present, using offline smart reply engine")
            return@withContext offlineFallback.analyzeAndGenerateReplies(request)
        }

        val effectiveStyle = request.style
        val structuredStyleJson = effectiveStyle.toStructuredJson()
        val targetRepliesCount = request.maxReplies.coerceIn(3, 10)

        val systemInstruction = """
            You are an advanced, human-like Smart Reply AI engine embedded in a next-generation mobile keyboard (Global MKeyboard Dynamic).
            Your PRIMARY task is to deeply analyze the incoming message and generate $targetRepliesCount realistic, intelligent, human-like reply choices that a real person would actually send in messaging apps (such as WhatsApp, Instagram, Telegram, Slack, SMS).

            CORE PRINCIPLES:
            1. HUMAN THINKING: Think like an empathetic, witty, and contextual human conversationalist. Do not give robotic, canned, or formulaic templates.
            2. UNIQUE VARIETY: Every suggested reply must be distinctly phrased and present different viable angles or intents (e.g. enthusiastic agreement, confirming specific logistics, asking a relevant follow-up question, polite decline, casual remark, warm acknowledgment, humorous banter, alternative suggestion).
            3. EXACT LANGUAGE & CODE-MIXING PRESERVATION:
               - If the input message is in Hinglish (Roman Hindi), reply in natural, authentic Hinglish (e.g. "Haan bhai, bilkul!", "Kal kitne baje milna hai?", "Nahi yaar, thoda busy hoon.").
               - If in Hindi (Devanagari), reply in natural Hindi.
               - If in Marathi, Bengali, Tamil, Telugu, Gujarati, Spanish, etc., mirror the natural language and dialect.
               - If English, reply in natural conversational English.
            4. STYLE & TONE DIRECTIVE:
               - Selected Style: ${effectiveStyle.displayName} (${effectiveStyle.category.displayName}).
               - Prompt Directive: ${effectiveStyle.promptDescription}.
               - If a specific tone or purpose is defined, let all reply variations reflect that distinct nuance while maintaining natural diversity.
            5. SAFETY & POLICY:
               - Under no circumstances produce hate speech, harassment, threats, abusive slurs, sexually explicit content, or dangerous instructions.
               - If the style is 'Firm', 'Assertive', or 'Boundary Setting', generate polite but unequivocal boundaries without abusive insults.

            OUTPUT FORMAT:
            You MUST return ONLY valid JSON matching this schema:
            {
              "detectedLanguage": "string (e.g. Hinglish, Hindi, English, Marathi, Bengali)",
              "dominantLanguage": "string language code (e.g. en, hi, mr, bn, ta, te)",
              "isCodeMixed": boolean,
              "detectedIntent": "string (e.g. meeting_inquiry, invitation, greeting, casual_checkin)",
              "replies": [
                {
                  "text": "string (the ready-to-send reply message)",
                  "tone": "string (the specific nuance, e.g. enthusiastic, casual, inquisitive, polite, witty)",
                  "confidence": float (0.85 to 1.0)
                }
              ]
            }
        """.trimIndent()

        val styleInstruction = if (effectiveStyle.id == "smart_match" || effectiveStyle.id == "auto") {
            "Smart Match: Deduce the ideal conversational tone and diverse response paths from the incoming message context."
        } else {
            buildString {
                append("Selected Style: ${effectiveStyle.displayName} (${effectiveStyle.category.displayName}). ")
                append("Description: ${effectiveStyle.promptDescription}. ")
                effectiveStyle.structuredTone?.let { append("Tone: $it. ") }
                effectiveStyle.structuredPurpose?.let { append("Purpose: $it. ") }
                effectiveStyle.structuredLength?.let { append("Length constraint: $it. ") }
            }
        }

        val languageInstruction = if (request.requestedLanguage != "auto") {
            "Force output replies to be in language code: ${request.requestedLanguage}."
        } else {
            "Automatically preserve and match the incoming message's language and style (including Hinglish/code-mixing)."
        }

        val userPrompt = """
            STRUCTURED STYLE:
            $structuredStyleJson

            STYLE INSTRUCTION:
            $styleInstruction

            LANGUAGE INSTRUCTION:
            $languageInstruction

            Please generate exactly $targetRepliesCount diverse, natural, human-like replies.

            --- INCOMING MESSAGE ---
            ${request.message}
            --- END MESSAGE ---
        """.trimIndent()

        // Attempt generation with candidate models (gemini-3.6-flash -> gemini-3.8-flash -> gemini-flash-latest)
        var lastException: Exception? = null

        for (modelName in CANDIDATE_MODELS) {
            try {
                val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

                val rootJson = JSONObject()

                // System instruction
                val sysPart = JSONObject().put("text", systemInstruction)
                val sysParts = JSONArray().put(sysPart)
                val sysContent = JSONObject().put("parts", sysParts)
                rootJson.put("systemInstruction", sysContent)

                // Content
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", userPrompt))
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                rootJson.put("contents", contentsArray)

                // Generation config
                val genConfig = JSONObject()
                    .put("temperature", 0.75)
                    .put("maxOutputTokens", 2048)
                    .put("responseMimeType", "application/json")
                rootJson.put("generationConfig", genConfig)

                val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val httpRequest = Request.Builder()
                    .url("$baseUrl?key=$apiKey")
                    .post(requestBody)
                    .build()

                client.newCall(httpRequest).execute().use { response ->
                    val responseString = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Gemini ($modelName) API error code: ${response.code}, body: $responseString")
                        if (response.code == 429) {
                            return@withContext Result.failure(RuntimeException("Rate limit reached. Please wait a moment and try again."))
                        } else if (response.code == 401 || response.code == 403) {
                            return@withContext Result.failure(RuntimeException("API authentication error. Please check your credentials."))
                        }
                        // Try next model if 404 or 5xx
                        return@use
                    }

                    val jsonResponse = JSONObject(responseString)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")

                    var text: String? = null
                    if (parts != null) {
                        for (p in 0 until parts.length()) {
                            val partObj = parts.optJSONObject(p)
                            val t = partObj?.optString("text")
                            if (!t.isNullOrBlank() && (t.contains("\"replies\"") || t.trim().startsWith("{"))) {
                                text = t
                                break
                            }
                        }
                        if (text == null && parts.length() > 0) {
                            text = parts.optJSONObject(0)?.optString("text")
                        }
                    }

                    if (text.isNullOrBlank()) {
                        return@use
                    }

                    val cleanJson = text.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val parsed = JSONObject(cleanJson)
                    val detectedLang = parsed.optString("detectedLanguage", "Auto-detected")
                    val dominantLang = parsed.optString("dominantLanguage", "auto")
                    val isCodeMixed = parsed.optBoolean("isCodeMixed", false)
                    val detectedIntent = parsed.optString("detectedIntent", "chat")
                    val rawRepliesArray = parsed.optJSONArray("replies") ?: JSONArray()

                    val suggestionList = mutableListOf<SmartReplySuggestion>()
                    for (i in 0 until rawRepliesArray.length()) {
                        val replyObj = rawRepliesArray.optJSONObject(i) ?: continue
                        val replyText = replyObj.optString("text", "").trim()
                        val tone = replyObj.optString("tone", effectiveStyle.displayName)
                        val confidence = replyObj.optDouble("confidence", 0.95).toFloat()

                        if (replyText.isNotBlank()) {
                            suggestionList.add(SmartReplySuggestion(replyText, tone, dominantLang, confidence))
                        }
                    }

                    val filtered = SmartReplyQualityFilter.filterAndValidate(suggestionList, targetRepliesCount)

                    if (filtered.isNotEmpty()) {
                        return@withContext Result.success(
                            SmartReplyResponse(
                                detectedLanguage = detectedLang,
                                dominantLanguage = dominantLang,
                                isCodeMixed = isCodeMixed,
                                replies = filtered,
                                detectedIntent = detectedIntent,
                                appliedStyle = effectiveStyle
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Attempt with model $modelName failed: ${e.message}")
                lastException = e
            }
        }

        // If network error or any failure occurred, fall back to offline provider so user still gets intelligent suggestions
        Log.w(TAG, "Gemini online request did not succeed, falling back to offline smart reply engine")
        return@withContext offlineFallback.analyzeAndGenerateReplies(request)
    }
}
