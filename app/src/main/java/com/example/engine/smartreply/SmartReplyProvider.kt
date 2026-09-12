package com.example.engine.smartreply

import com.example.model.SmartReplyRequest
import com.example.model.SmartReplyResponse

/**
 * Abstraction layer for Smart Reply AI generation.
 * This decouples the keyboard UI and business logic from any specific AI provider (Gemini, OpenAI, local, backend).
 */
interface SmartReplyProvider {
    suspend fun analyzeAndGenerateReplies(request: SmartReplyRequest): Result<SmartReplyResponse>
}
