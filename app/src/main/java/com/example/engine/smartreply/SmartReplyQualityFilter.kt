package com.example.engine.smartreply

import com.example.model.SmartReplySuggestion

/**
 * Validates, filters, and ranks AI-generated reply suggestions according to strict quality and safety guidelines.
 */
object SmartReplyQualityFilter {

    private val BANNED_PATTERNS = listOf(
        Regex("\\b(password|pin|otp|credit card|cvv|account number)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(hack|steal|ddos|exploit|malware|virus)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(threat|kill|hurt|suicide|violence)\\b", RegexOption.IGNORE_CASE),
        Regex("^(system|assistant|ai|model):", RegexOption.IGNORE_CASE),
        Regex("^(as an ai|i am an ai|i cannot assist)", RegexOption.IGNORE_CASE)
    )

    /**
     * Filters, deduplicates, trims, and validates reply candidates.
     * Ensures exactly 3 to 5 high-quality, distinct, and safe suggestions.
     */
    fun filterAndValidate(
        candidates: List<SmartReplySuggestion>,
        desiredCount: Int = 4
    ): List<SmartReplySuggestion> {
        val cleanList = mutableListOf<SmartReplySuggestion>()
        val seenSignatures = mutableSetOf<String>()

        for (candidate in candidates) {
            val text = candidate.text.trim()
                .replace(Regex("^[\"'\u201C\u201D]+|[\"'\u201C\u201D]+$"), "") // strip surrounding quotes
                .replace(Regex("^[-*•0-9.]+\\s*"), "") // strip leading bullet points or numbers
                .trim()

            // 1. Length validation (3 to 220 characters)
            if (text.length < 3 || text.length > 220) continue

            // 2. Safety and prompt leakage check
            var isSafe = true
            for (pattern in BANNED_PATTERNS) {
                if (pattern.containsMatchIn(text)) {
                    isSafe = false
                    break
                }
            }
            if (!isSafe) continue

            // 3. Deduplication signature (lowercase without punctuation)
            val signature = text.lowercase().replace(Regex("[^a-z0-9\u0900-\u097F]"), "")
            if (signature.isEmpty() || seenSignatures.contains(signature)) continue

            seenSignatures.add(signature)
            cleanList.add(candidate.copy(text = text))
        }

        // Clamp to between 3 and 10 items (allowing users to explore up to 10 rich replies)
        val targetCount = desiredCount.coerceIn(3, 10)
        return if (cleanList.size > targetCount) {
            cleanList.take(targetCount)
        } else {
            cleanList
        }
    }
}
