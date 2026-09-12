package com.example.engine.smartreply

/**
 * Intelligent Language & Code-Mixing Detector for Smart Reply.
 * Identifies primary language, script, and code-mixed patterns (such as Hinglish, Marathish, or mixed English).
 */
object SmartReplyLanguageDetector {

    data class DetectionResult(
        val detectedLanguageCode: String,
        val detectedLanguageName: String,
        val isCodeMixed: Boolean,
        val script: String
    )

    private val HINGLISH_MARKERS = setOf(
        "bhai", "bhaiya", "kal", "aaj", "parso", "kya", "kyu", "kyun", "hai", "hain", "ho", "hoga", "hogi",
        "aa", "aao", "raha", "rahe", "rahi", "kar", "karo", "karna", "nahi", "nhi", "haan", "han",
        "theek", "thik", "kaise", "kaisa", "kaisi", "kaha", "kahan", "kitne", "kitna", "baje", "milte",
        "milna", "chalo", "yaar", "chal", "baat", "bol", "bolo", "samajh", "aaya", "dekh", "dekho",
        "mujhe", "tujhe", "hum", "aap", "tum", "mera", "meri", "tere", "teri", "apna", "apni", "ab", "kab",
        "abhi", "kuch", "sab", "mat", "kr", "krna", "krte", "hoga", "tha", "thi", "the"
    )

    private val MARATHI_ROMAN_MARKERS = setOf(
        "tu", "uudya", "udya", "aaj", "parva", "aahes", "ahe", "ahet", "kay", "kasa", "kashi",
        "yenar", "janar", "karaycha", "karto", "karte", "bol", "sang", "kuthe", "kiti", "vajta",
        "nako", "ho", "nahi", "ye", "ja", "aple", "mazha", "tuzha", "bhetu", "bhetuya"
    )

    private val ENGLISH_COMMON_WORDS = setOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with",
        "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her",
        "she", "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up",
        "out", "if", "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time",
        "no", "just", "him", "know", "take", "people", "into", "year", "your", "good", "some", "could",
        "them", "see", "other", "than", "then", "now", "look", "only", "come", "its", "over", "think",
        "also", "back", "after", "use", "two", "how", "our", "work", "first", "well", "way", "even",
        "new", "want", "because", "any", "these", "give", "day", "most", "us", "are", "meeting", "tomorrow",
        "call", "project", "urgent", "thanks", "hello", "hi", "hey", "sorry", "please", "free", "tonight"
    )

    fun detect(text: String): DetectionResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return DetectionResult("en", "English", false, "Latin")
        }

        var devanagariCount = 0
        var bengaliCount = 0
        var tamilCount = 0
        var teluguCount = 0
        var kannadaCount = 0
        var gujaratiCount = 0
        var punjabiCount = 0
        var malayalamCount = 0
        var odiaCount = 0
        var latinCount = 0

        for (ch in trimmed) {
            val code = ch.code
            when (code) {
                in 0x0900..0x097F -> devanagariCount++
                in 0x0980..0x09FF -> bengaliCount++
                in 0x0B80..0x0BFF -> tamilCount++
                in 0x0C00..0x0C7F -> teluguCount++
                in 0x0C80..0x0CFF -> kannadaCount++
                in 0x0A80..0x0AFF -> gujaratiCount++
                in 0x0A00..0x0A7F -> punjabiCount++
                in 0x0D00..0x0D7F -> malayalamCount++
                in 0x0B00..0x0B7F -> odiaCount++
                in 0x0041..0x005A, in 0x0061..0x007A -> latinCount++
            }
        }

        // Script-based detection
        val indicTotal = devanagariCount + bengaliCount + tamilCount + teluguCount +
                kannadaCount + gujaratiCount + punjabiCount + malayalamCount + odiaCount

        // Native Indic Script Checks
        if (indicTotal > 0 && indicTotal >= latinCount / 2) {
            val hasLatin = latinCount > 3
            if (devanagariCount >= indicTotal * 0.7) {
                // Check if Marathi-specific characters or words exist
                val isMarathi = trimmed.contains("आहे") || trimmed.contains("आहात") ||
                        trimmed.contains("नाही") || trimmed.contains("कसा") || trimmed.contains("येणार") ||
                        trimmed.contains("होय") || trimmed.contains("बघ") || trimmed.contains("उद्या")
                val langCode = if (isMarathi) "mr" else "hi"
                val langName = if (isMarathi) "Marathi" else "Hindi"
                return DetectionResult(langCode, langName, hasLatin, "Devanagari")
            } else if (bengaliCount > 0) {
                return DetectionResult("bn", "Bengali", hasLatin, "Bengali")
            } else if (tamilCount > 0) {
                return DetectionResult("ta", "Tamil", hasLatin, "Tamil")
            } else if (teluguCount > 0) {
                return DetectionResult("te", "Telugu", hasLatin, "Telugu")
            } else if (kannadaCount > 0) {
                return DetectionResult("kn", "Kannada", hasLatin, "Kannada")
            } else if (gujaratiCount > 0) {
                return DetectionResult("gu", "Gujarati", hasLatin, "Gujarati")
            } else if (punjabiCount > 0) {
                return DetectionResult("pa", "Punjabi", hasLatin, "Gurmukhi")
            } else if (malayalamCount > 0) {
                return DetectionResult("ml", "Malayalam", hasLatin, "Malayalam")
            } else if (odiaCount > 0) {
                return DetectionResult("or", "Odia", hasLatin, "Odia")
            }
        }

        // Latin script: Check for Hinglish or Marathi-English Code-Mixing
        val tokens = trimmed.lowercase()
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.isNotBlank() }

        var hinglishHits = 0
        var marathiHits = 0
        var englishHits = 0

        for (token in tokens) {
            if (HINGLISH_MARKERS.contains(token)) hinglishHits++
            if (MARATHI_ROMAN_MARKERS.contains(token)) marathiHits++
            if (ENGLISH_COMMON_WORDS.contains(token)) englishHits++
        }

        val hasHinglish = hinglishHits > 0
        val hasMarathi = marathiHits > 0
        val hasEnglish = englishHits > 0

        if (hasMarathi && (hasEnglish || marathiHits >= 2)) {
            return DetectionResult("mr", "Marathi (Romanized)", true, "Latin")
        }

        if (hasHinglish && hasEnglish) {
            return DetectionResult("hi", "Hinglish (Hindi + English)", true, "Latin")
        }

        if (hasHinglish && hinglishHits >= 2) {
            return DetectionResult("hi", "Hinglish (Hindi)", false, "Latin")
        }

        // Default to English
        return DetectionResult("en", "English", false, "Latin")
    }
}
