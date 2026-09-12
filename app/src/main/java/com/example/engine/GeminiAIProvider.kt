package com.example.engine

import android.util.Log
import com.example.BuildConfig
import com.example.model.SmartReplyOption
import com.example.model.ToneType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAIProvider(
    private val offlineFallback: SmartAIProvider = SmartAIProvider()
) : AIProvider {

    private val TAG = "GeminiAIProvider"
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

    private suspend fun callGemini(prompt: String, systemInstruction: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey == null) {
            return@withContext Result.failure(IllegalStateException("No Gemini API key configured"))
        }

        try {
            val rootJson = JSONObject()

            if (!systemInstruction.isNullOrBlank()) {
                val sysPart = JSONObject().put("text", systemInstruction)
                val sysParts = JSONArray().put(sysPart)
                val sysContent = JSONObject().put("parts", sysParts)
                rootJson.put("systemInstruction", sysContent)
            }

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val genConfig = JSONObject()
                .put("temperature", 0.7)
                .put("maxOutputTokens", 512)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.w(TAG, "Gemini API error code: ${response.code}, body: $responseString")
                    return@withContext Result.failure(RuntimeException("Gemini API call failed with code ${response.code}"))
                }

                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    Result.success(text.trim())
                } else {
                    Result.failure(RuntimeException("Empty text from Gemini response"))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API execution error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun rewrite(text: String, tone: ToneType): Result<String> {
        val input = text.trim()
        if (input.isBlank()) {
            return offlineFallback.rewrite(text, tone)
        }

        val prompt = "Rewrite the following text in a ${tone.displayName} tone. Maintain the core message but adjust the vocabulary, structure, and expression appropriately. Output ONLY the rewritten text:\n\n\"$input\""
        val systemInstruction = "You are a writing assistant in an intelligent mobile keyboard. Provide polished, direct rewritten responses without markdown ticks, quotes, or preambles."

        val geminiResult = callGemini(prompt, systemInstruction)
        return if (geminiResult.isSuccess) {
            geminiResult
        } else {
            offlineFallback.rewrite(input, tone)
        }
    }

    override suspend fun fixGrammar(text: String): Result<String> {
        val input = text.trim()
        if (input.isBlank()) {
            return offlineFallback.fixGrammar(text)
        }

        val prompt = "Correct all spelling, grammatical, punctuation, and capitalization errors in the following text. Preserve the original meaning and natural tone. Output ONLY the corrected text:\n\n\"$input\""
        val systemInstruction = "You are a professional grammar proofreader. Return only the cleaned and corrected text."

        val geminiResult = callGemini(prompt, systemInstruction)
        return if (geminiResult.isSuccess) {
            geminiResult
        } else {
            offlineFallback.fixGrammar(input)
        }
    }

    override suspend fun translate(text: String, sourceLangCode: String, targetLangCode: String): Result<String> {
        val input = text.trim()
        if (input.isBlank()) {
            return offlineFallback.translate(text, sourceLangCode, targetLangCode)
        }

        val prompt = "Translate the following text from language code '$sourceLangCode' to language code '$targetLangCode'. Return ONLY the translated text:\n\n\"$input\""
        val geminiResult = callGemini(prompt)
        return if (geminiResult.isSuccess) {
            geminiResult
        } else {
            offlineFallback.translate(input, sourceLangCode, targetLangCode)
        }
    }

    override suspend fun generateSmartReplies(contextText: String): Result<List<SmartReplyOption>> {
        val input = contextText.trim()
        if (input.isBlank()) {
            return offlineFallback.generateSmartReplies(contextText)
        }

        val prompt = "Context message: \"$input\"\nGenerate 3-4 short, natural, ready-to-send reply messages to this context. Format each on a single line separated by a newline."
        val geminiResult = callGemini(prompt)

        if (geminiResult.isSuccess) {
            val lines = geminiResult.getOrNull()?.split("\n")
                ?.map { it.replace(Regex("^[-*0-9.]+\\s*"), "").trim() }
                ?.filter { it.isNotBlank() && it.length < 80 }
                ?: emptyList()

            if (lines.isNotEmpty()) {
                val replies = lines.take(4).map { SmartReplyOption(it, "Quick Reply") }
                return Result.success(replies)
            }
        }

        return offlineFallback.generateSmartReplies(input)
    }

    override suspend fun continueText(text: String): Result<String> {
        val input = text.trim()
        if (input.isBlank()) {
            return offlineFallback.continueText(text)
        }

        val prompt = "Continue writing the next 1 or 2 natural, concise sentences that logically follow this text:\n\n\"$input\""
        val systemInstruction = "You are an intelligent auto-completion assistant in a mobile keyboard. Continue the thought smoothly."

        val geminiResult = callGemini(prompt, systemInstruction)
        return if (geminiResult.isSuccess) {
            val extension = geminiResult.getOrNull() ?: ""
            Result.success("$input $extension")
        } else {
            offlineFallback.continueText(input)
        }
    }

    override suspend fun customPrompt(prompt: String): Result<String> {
        val input = prompt.trim()
        if (input.isBlank()) {
            return Result.success("Please provide an instruction or question.")
        }

        val geminiResult = callGemini(input)
        return if (geminiResult.isSuccess) {
            geminiResult
        } else {
            offlineFallback.customPrompt(input)
        }
    }

    override suspend fun detectLanguage(text: String): String {
        return offlineFallback.detectLanguage(text)
    }
}
