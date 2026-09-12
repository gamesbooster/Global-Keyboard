package com.example

import com.example.engine.GoogleTranslationEngine
import com.example.engine.TranslationMatrix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationEngineTest {

    @Test
    fun testOfflineTranslationHelloToMarathi() {
        val translated = TranslationMatrix.translateOffline("hello", "en", "mr")
        assertEquals("नमस्कार", translated)
    }

    @Test
    fun testOfflineTranslationHelloToHindi() {
        val translated = TranslationMatrix.translateOffline("hello", "en", "hi")
        assertEquals("नमस्ते", translated)
    }

    @Test
    fun testCacheAndFastTranslation() {
        GoogleTranslationEngine.cacheResult("water", "en", "mr", "पाणी")
        val cached = GoogleTranslationEngine.translateFast("water", "en", "mr")
        assertEquals("पाणी", cached)
    }

    @Test
    fun testLanguageDetection() {
        val langHindi = TranslationMatrix.detectLanguage("नमस्ते")
        assertEquals("hi", langHindi)

        val langEnglish = TranslationMatrix.detectLanguage("Hello world")
        assertEquals("en", langEnglish)
    }

    @Test
    fun testSpatialTypoCorrection() {
        // 'u' is adjacent to 'y', test mistouch "tuping" -> suggests "typing"
        val suggestions = com.example.engine.SuggestionEngine.getSuggestions("tuping", "en")
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.contains("typing"))

        // 'v' is adjacent to 'b', test mistouch "keyvoard" -> suggests "keyboard"
        val kbSuggestions = com.example.engine.SuggestionEngine.getSuggestions("keyvoard", "en")
        assertTrue(kbSuggestions.contains("keyboard") || kbSuggestions.contains("keyboards"))
    }

    @Test
    fun testUserReportedTyposAutoCorrection() {
        val correctionEntered = com.example.engine.SuggestionEngine.getAutoCorrection("eneterd", "en")
        assertEquals("entered", correctionEntered)

        val suggestionsSugset = com.example.engine.SuggestionEngine.getSuggestions("sugset", "", "en")
        assertTrue(suggestionsSugset.contains("suggest"))

        val correctionFeatures = com.example.engine.SuggestionEngine.getAutoCorrection("feataues", "en")
        assertEquals("features", correctionFeatures)
    }

    @Test
    fun testNextWordPrediction() {
        val nextWords = com.example.engine.SuggestionEngine.getSuggestions("", contextBefore = "how are", langCode = "en")
        assertTrue("Expected 'you' in next word predictions: $nextWords", nextWords.contains("you"))

        val nextAfterThank = com.example.engine.SuggestionEngine.getSuggestions("", contextBefore = "thank", langCode = "en")
        assertTrue("Expected 'you' in next word predictions after 'thank': $nextAfterThank", nextAfterThank.contains("you"))
    }

    @Test
    fun testSmartAIProviderFallback() = kotlinx.coroutines.runBlocking {
        val smartAi = com.example.engine.SmartAIProvider()
        val grammarResult = smartAi.fixGrammar("i has went to office and did not saw him")
        assertTrue(grammarResult.isSuccess)
        assertNotNull(grammarResult.getOrNull())

        val rewriteResult = smartAi.rewrite("cannot come today", com.example.model.ToneType.PROFESSIONAL)
        assertTrue(rewriteResult.isSuccess)
        assertTrue(rewriteResult.getOrNull()?.isNotBlank() == true)

        val replies = smartAi.generateSmartReplies("Are you coming?")
        assertTrue(replies.isSuccess)
        assertTrue(replies.getOrNull()?.isNotEmpty() == true)
    }

    @Test
    fun testPhoneticTransliterationEngine() {
        val hindiCandidates = com.example.engine.TransliteratorEngine.getTransliterationCandidates("namaste", "hi")
        assertTrue(hindiCandidates.isNotEmpty())
        assertEquals("नमस्ते", hindiCandidates.first())

        val dhanyawadCandidates = com.example.engine.TransliteratorEngine.getTransliterationCandidates("dhanyawad", "hi")
        assertTrue(dhanyawadCandidates.isNotEmpty())
        assertEquals("धन्यवाद", dhanyawadCandidates.first())

        val kaiseCandidates = com.example.engine.TransliteratorEngine.getTransliterationCandidates("kaise", "hi")
        assertTrue(kaiseCandidates.isNotEmpty())
        assertEquals("कैसे", kaiseCandidates.first())
    }

    @Test
    fun testEmojiSuggestionEngine() {
        val chaiEmojis = com.example.engine.EmojiSuggestionEngine.getMatchingEmojis("chai")
        assertTrue(chaiEmojis.contains("☕"))

        val cricketEmojis = com.example.engine.EmojiSuggestionEngine.getMatchingEmojis("cricket")
        assertTrue(cricketEmojis.contains("🏏"))

        val fireEmojis = com.example.engine.EmojiSuggestionEngine.getMatchingEmojis("fire")
        assertTrue(fireEmojis.contains("🔥"))
    }

    @Test
    fun testStickerCatalogIntegrity() {
        val stickers = com.example.model.StickerData.ALL_STICKERS
        assertTrue(stickers.isNotEmpty())
        assertTrue(stickers.any { it.tag == "diwali" })
        assertTrue(stickers.any { it.tag == "holi" })
        assertTrue(stickers.any { it.title.contains("Arre Yaar") })
    }
}
