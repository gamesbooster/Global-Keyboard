package com.example.engine

import com.example.model.Language

object LanguageDetector {

    /**
     * Detect language based on Unicode script blocks and characteristic vocabulary patterns.
     */
    fun detectLanguage(text: String): Language {
        if (text.isBlank()) return Language.getByCode("en")

        var devanagariCount = 0
        var bengaliCount = 0
        var gujaratiCount = 0
        var tamilCount = 0
        var teluguCount = 0
        var kannadaCount = 0
        var malayalamCount = 0
        var gurmukhiCount = 0
        var arabicCount = 0
        var japaneseCount = 0
        var latinCount = 0

        for (char in text) {
            val codePoint = char.code
            when (codePoint) {
                in 0x0900..0x097F -> devanagariCount++
                in 0x0980..0x09FF -> bengaliCount++
                in 0x0A80..0x0AFF -> gujaratiCount++
                in 0x0B80..0x0BFF -> tamilCount++
                in 0x0C00..0x0C7F -> teluguCount++
                in 0x0C80..0x0CFF -> kannadaCount++
                in 0x0D00..0x0D7F -> malayalamCount++
                in 0x0A00..0x0A7F -> gurmukhiCount++
                in 0x0600..0x06FF, in 0x0750..0x077F, in 0xFB50..0xFDFF, in 0xFE70..0xFEFF -> arabicCount++
                in 0x3040..0x309F, in 0x30A0..0x30FF, in 0x4E00..0x9FAF -> japaneseCount++
                in 0x0041..0x005A, in 0x0061..0x007A, in 0x00C0..0x00FF, in 0x0100..0x017F -> latinCount++
            }
        }

        // Script based matching
        val counts = listOf(
            "devanagari" to devanagariCount,
            "bn" to bengaliCount,
            "gu" to gujaratiCount,
            "ta" to tamilCount,
            "te" to teluguCount,
            "kn" to kannadaCount,
            "ml" to malayalamCount,
            "pa" to gurmukhiCount,
            "ur" to arabicCount,
            "ja" to japaneseCount,
            "latin" to latinCount
        ).sortedByDescending { it.second }

        val topScript = counts.firstOrNull()
        if (topScript != null && topScript.second > 0) {
            when (topScript.first) {
                "bn" -> return Language.getByCode("bn")
                "gu" -> return Language.getByCode("gu")
                "ta" -> return Language.getByCode("ta")
                "te" -> return Language.getByCode("te")
                "kn" -> return Language.getByCode("kn")
                "ml" -> return Language.getByCode("ml")
                "pa" -> return Language.getByCode("pa")
                "ur" -> return Language.getByCode("ur")
                "ja" -> return Language.getByCode("ja")
                "devanagari" -> {
                    // Distinguish Marathi from Hindi using characteristic Marathi characters/words
                    val lower = text.lowercase()
                    if (lower.contains("आहे") || lower.contains("नाही") || lower.contains("कसे") || lower.contains("तुम्ही") || lower.contains("नमस्कार") || lower.contains("झाले")) {
                        return Language.getByCode("mr")
                    }
                    return Language.getByCode("hi")
                }
                "latin" -> {
                    val lower = text.lowercase()
                    // French patterns
                    if (lower.contains("bonjour") || lower.contains("merci") || lower.contains("vous") || lower.contains("est") || lower.contains("le ") || lower.contains("la ")) {
                        return Language.getByCode("fr")
                    }
                    // Spanish patterns
                    if (lower.contains("hola") || lower.contains("gracias") || lower.contains("amigo") || lower.contains("como") || lower.contains("por favor") || lower.contains("bueno")) {
                        return Language.getByCode("es")
                    }
                    // German patterns
                    if (lower.contains("hallo") || lower.contains("danke") || lower.contains("bitte") || lower.contains("guten") || lower.contains("ich ") || lower.contains("und ")) {
                        return Language.getByCode("de")
                    }
                    return Language.getByCode("en")
                }
            }
        }

        return Language.getByCode("en")
    }
}
