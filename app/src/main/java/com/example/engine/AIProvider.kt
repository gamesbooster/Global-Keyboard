package com.example.engine

import com.example.model.SmartReplyOption
import com.example.model.ToneType

interface AIProvider {
    suspend fun rewrite(text: String, tone: ToneType): Result<String>
    suspend fun fixGrammar(text: String): Result<String>
    suspend fun translate(text: String, sourceLangCode: String, targetLangCode: String): Result<String>
    suspend fun generateSmartReplies(contextText: String): Result<List<SmartReplyOption>>
    suspend fun continueText(text: String): Result<String>
    suspend fun detectLanguage(text: String): String
    suspend fun customPrompt(prompt: String): Result<String> = Result.success(prompt)
}
