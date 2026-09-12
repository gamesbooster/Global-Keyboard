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
    private val MODEL = "gemini-2.5-flash"
    private val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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
            // Graceful fallback to genuine local rule & intent-based reply engine
            Log.d(TAG, "No Gemini API key present, using offline smart reply engine")
            return@withContext offlineFallback.analyzeAndGenerateReplies(request)
        }

        try {
            val systemInstruction = """
                You are the Smart Reply engine for Global MKeyboard Dynamic.
                Your ONLY task is to analyze a user-supplied received message and generate 3 to 5 natural, relevant reply options that the user could send.
                Do not act as a general chatbot.
                Do not answer questions for the user directly.
                Do not explain the received message.
                Do not provide analysis to the user.
                Do not generate unrelated content.

                Determine the language and communication style of the supplied message automatically.
                Preserve the dominant language and natural style unless another output language is explicitly requested.
                Handle multilingual and code-mixed messages when possible (such as Hinglish, Hindi, Marathi, Bengali, Tamil, Telugu, etc.).

                Generate replies that logically match the received message.
                Do not invent facts that are not present in the message.
                Provide 3 to 5 different, reasonable, high-quality response choices rather than identical sentences.

                STYLE & TONE DIRECTIVE:
                The user has selected a specific Reply Style from the Reply Style & Tone library.
                The selected style MUST genuinely influence and define all generated replies.
                - If the style is a specific TONE (e.g. Professional, Romantic, Witty, Sarcastic, Firm, Caring, Chill), all replies must be voiced in that distinct personality.
                - If the style is a RESPONSE PURPOSE (e.g. Confirm, Decline, Ask for Clarification, Schedule, Apologize, Thank), all replies must carry out that exact purpose with varied phrasing.
                - If the style is a LENGTH constraint (e.g. Very Short, Short, Elaborate), the length constraint must be strictly respected.
                - If the style is 'Smart Match' or 'Auto', automatically select the most appropriate tone, purpose, and length matching the conversation context.

                SAFETY RULE:
                Under no circumstances generate abusive language, threats, insults, profanity, harassment, intimidation, or illegal content.
                If the requested style is 'Aggressive', 'Firm', or 'Boundary Setting', generate assertive, unequivocal, firm professional or personal boundaries without abuse or insults.

                Return ONLY structured JSON matching this schema:
                {
                  "detectedLanguage": "string (e.g. English, Hindi, Hinglish, Marathi)",
                  "dominantLanguage": "string language code (e.g. en, hi, mr, bn)",
                  "isCodeMixed": boolean,
                  "detectedIntent": "string (e.g. question, invitation, meeting, gratitude, greeting)",
                  "replies": [
                    {
                      "text": "string (the ready-to-send reply text)",
                      "tone": "string (the tone name matching the style)",
                      "confidence": float (0.0 to 1.0)
                    }
                  ]
                }
            """.trimIndent()

            val effectiveStyle = request.style
            val structuredStyleJson = effectiveStyle.toStructuredJson()

            val styleInstruction = if (effectiveStyle.id == "smart_match" || effectiveStyle.id == "auto") {
                "Smart Match: Automatically deduce the most fitting tone, intent, and length from the incoming message."
            } else {
                buildString {
                    append("Selected Style: ${effectiveStyle.displayName} (${effectiveStyle.category.displayName}). ")
                    append("Prompt Description: ${effectiveStyle.promptDescription}. ")
                    effectiveStyle.structuredTone?.let { append("Target Tone: $it. ") }
                    effectiveStyle.structuredPurpose?.let { append("Target Purpose: $it (all replies must perform this exact intent). ") }
                    effectiveStyle.structuredLength?.let { append("Length: $it. ") }
                }
            }

            val languageInstruction = if (request.requestedLanguage != "auto") {
                "Force generated replies to be in language code: ${request.requestedLanguage}."
            } else {
                "Automatically preserve the sender's language and style (including code-mixing/Hinglish)."
            }

            // Structured prompt with defense against prompt injection
            val userPrompt = """
                STRUCTURED STYLE PARAMETER:
                $structuredStyleJson

                STYLE INSTRUCTION:
                $styleInstruction

                LANGUAGE INSTRUCTION:
                $languageInstruction

                Maximum replies requested: ${request.maxReplies.coerceIn(3, 5)}.

                --- BEGIN RECEIVED MESSAGE DATA ---
                ${request.message}
                --- END RECEIVED MESSAGE DATA ---
            """.trimIndent()

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

            // Generation config with JSON response format
            val genConfig = JSONObject()
                .put("temperature", 0.7)
                .put("maxOutputTokens", 1024)
                .put("responseMimeType", "application/json")
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val httpRequest = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(httpRequest).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.w(TAG, "Gemini API error code: ${response.code}, body: $responseString")
                    if (response.code == 429) {
                        return@withContext Result.failure(RuntimeException("Rate limit reached. Please wait a moment and try again."))
                    } else if (response.code == 401 || response.code == 403) {
                        return@withContext Result.failure(RuntimeException("API authentication error. Please check your credentials."))
                    }
                    // Fallback to offline engine
                    return@withContext offlineFallback.analyzeAndGenerateReplies(request)
                }

                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (text.isNullOrBlank()) {
                    return@withContext offlineFallback.analyzeAndGenerateReplies(request)
                }

                try {
                    val cleanJson = text.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val parsed = JSONObject(cleanJson)
                    val detectedLang = parsed.optString("detectedLanguage", "English")
                    val dominantLang = parsed.optString("dominantLanguage", "en")
                    val isCodeMixed = parsed.optBoolean("isCodeMixed", false)
                    val detectedIntent = parsed.optString("detectedIntent", "general")
                    val rawRepliesArray = parsed.optJSONArray("replies") ?: JSONArray()

                    val suggestionList = mutableListOf<SmartReplySuggestion>()
                    for (i in 0 until rawRepliesArray.length()) {
                        val replyObj = rawRepliesArray.optJSONObject(i) ?: continue
                        val replyText = replyObj.optString("text", "").trim()
                        val tone = replyObj.optString("tone", effectiveStyle.displayName)
                        val confidence = replyObj.optDouble("confidence", 0.9).toFloat()

                        if (replyText.isNotBlank()) {
                            suggestionList.add(SmartReplySuggestion(replyText, tone, dominantLang, confidence))
                        }
                    }

                    val filtered = SmartReplyQualityFilter.filterAndValidate(suggestionList, request.maxReplies)

                    if (filtered.isNotEmpty()) {
                        Result.success(
                            SmartReplyResponse(
                                detectedLanguage = detectedLang,
                                dominantLanguage = dominantLang,
                                isCodeMixed = isCodeMixed,
                                replies = filtered,
                                detectedIntent = detectedIntent,
                                appliedStyle = effectiveStyle
                            )
                        )
                    } else {
                        offlineFallback.analyzeAndGenerateReplies(request)
                    }
                } catch (pe: Exception) {
                    Log.w(TAG, "Failed to parse structured JSON from Gemini: ${pe.message}")
                    offlineFallback.analyzeAndGenerateReplies(request)
                }
            }
        } catch (te: SocketTimeoutException) {
            Log.w(TAG, "Gemini request timed out: ${te.message}")
            offlineFallback.analyzeAndGenerateReplies(request)
        } catch (nhe: UnknownHostException) {
            Log.w(TAG, "No network connection: ${nhe.message}")
            Result.failure(IOException("No internet connection."))
        } catch (e: Exception) {
            Log.w(TAG, "Gemini execution exception: ${e.message}")
            offlineFallback.analyzeAndGenerateReplies(request)
        }
    }
}
