package com.example.engine

import java.util.Locale

/**
 * High-performance offline phonetic transliterator for Hinglish to Hindi/Devanagari,
 * Marathi, and Banglish to Bengali.
 *
 * Converts typed Roman/English characters (e.g. "namaste", "kaisa", "bhai")
 * into authentic Indic script words in real-time.
 */
object TransliteratorEngine {

    // Curated high-frequency Hinglish to Hindi dictionary for 100% natural, accurate transliteration
    private val HINGLISH_DICTIONARY = mapOf(
        // Greetings & Respect
        "namaste" to "नमस्ते",
        "namaskar" to "नमस्कार",
        "pranam" to "प्रणाम",
        "pranaam" to "प्रणाम",
        "shukriya" to "शुक्रिया",
        "dhanyawad" to "धन्यवाद",
        "dhanyavad" to "धन्यवाद",
        "swagat" to "स्वागत",
        "badhai" to "बधाई",
        "shubh" to "शुभ",
        "subharambh" to "शुभारंभ",
        "alvida" to "अलविदा",
        "kripya" to "कृपया",
        "kripaya" to "कृपया",
        "ram" to "राम",
        "shyam" to "श्याम",
        "radhe" to "राधे",
        "krishna" to "कृष्ण",
        "jai" to "जय",
        "hind" to "हिन्द",
        "bharat" to "भारत",
        "vande" to "वन्दे",
        "mataram" to "मातरम्",

        // Question Words & Pronouns
        "kya" to "क्या",
        "kyun" to "क्यों",
        "kyu" to "क्यों",
        "kyon" to "क्यों",
        "kaise" to "कैसे",
        "kaisa" to "कैसा",
        "kaisi" to "कैसी",
        "kahan" to "कहाँ",
        "kaha" to "कहाँ",
        "kab" to "कब",
        "kaun" to "कौन",
        "kitna" to "कितना",
        "kitne" to "कितने",
        "kitni" to "कितनी",
        "kiska" to "किसका",
        "kiski" to "किसकी",
        "kiske" to "किसके",
        "kis" to "किस",
        "kise" to "किसे",

        // Pronouns
        "main" to "मैं",
        "mai" to "मैं",
        "hum" to "हम",
        "tum" to "तुम",
        "aap" to "आप",
        "apna" to "अपना",
        "apne" to "अपने",
        "apni" to "अपनी",
        "mera" to "मेरा",
        "meri" to "मेरी",
        "mere" to "मेरे",
        "tera" to "तेरा",
        "teri" to "तेरी",
        "tere" to "तेरे",
        "uska" to "उसका",
        "uski" to "उसकी",
        "uske" to "उसके",
        "unka" to "उनका",
        "unki" to "उनकी",
        "unke" to "उनके",
        "inhe" to "इन्हें",
        "unhe" to "उन्हें",
        "ye" to "ये",
        "yeh" to "यह",
        "wo" to "वो",
        "woh" to "वह",

        // Verbs & Auxiliaries
        "hai" to "है",
        "hain" to "हैं",
        "ho" to "हो",
        "hoon" to "हूँ",
        "hu" to "हूँ",
        "tha" to "था",
        "thi" to "थी",
        "the" to "थे",
        "hoga" to "होगा",
        "hogi" to "होगी",
        "honge" to "होंगे",
        "kar" to "कर",
        "karo" to "करो",
        "karna" to "करना",
        "karte" to "करते",
        "karti" to "करती",
        "karta" to "करता",
        "karenge" to "करेंगे",
        "kijiye" to "कीजिए",
        "raha" to "रहा",
        "rahe" to "रहे",
        "rahi" to "रही",
        "batao" to "बताओ",
        "bataiye" to "बताइए",
        "batana" to "बताना",
        "bolo" to "बोलो",
        "bolna" to "बोलना",
        "suno" to "सुनो",
        "sunna" to "सुनना",
        "dekho" to "देखो",
        "dekhna" to "देखना",
        "chalo" to "चलो",
        "chal" to "चल",
        "chalte" to "चलते",
        "aao" to "आओ",
        "aana" to "आना",
        "aaye" to "आए",
        "aayenge" to "आएंगे",
        "jao" to "जाओ",
        "jana" to "जाना",
        "gaye" to "गए",
        "jayenge" to "जाएंगे",
        "dena" to "देना",
        "do" to "दो",
        "denge" to "देंगे",
        "lena" to "लेना",
        "lo" to "लो",
        "lenge" to "लेंगे",
        "chahiye" to "चाहिए",
        "sakta" to "सकता",
        "sakte" to "सकते",
        "sakti" to "सकती",
        "milte" to "मिलते",
        "milenge" to "मिलेंगे",
        "samjhe" to "समझे",
        "samjha" to "समझा",
        "socho" to "सोचो",
        "soch" to "सोच",

        // Common Conversational Words
        "haan" to "हाँ",
        "ha" to "हाँ",
        "nahi" to "नहीं",
        "nahin" to "नहीं",
        "theek" to "ठीक",
        "thik" to "ठीक",
        "acha" to "अच्छा",
        "achha" to "अच्छा",
        "achhi" to "अच्छी",
        "achhe" to "अच्छे",
        "bahut" to "बहुत",
        "bht" to "बहुत",
        "zyada" to "ज़्यादा",
        "jyada" to "ज़्यादा",
        "kam" to "कम",
        "thoda" to "थोड़ा",
        "thodi" to "थोड़ी",
        "sab" to "सब",
        "sabhi" to "सभी",
        "kuch" to "कुछ",
        "koi" to "कोई",
        "har" to "हर",
        "bhi" to "भी",
        "aur" to "और",
        "ya" to "या",
        "par" to "पर",
        "lekin" to "लेकिन",
        "magar" to "मगर",
        "kyunki" to "क्योंकि",
        "isliye" to "इसलिए",
        "taki" to "ताकि",
        "bata" to "बता",
        "zaroor" to "ज़रूर",
        "jarur" to "ज़रूर",
        "hamesha" to "हमेशा",
        "kabhi" to "कभी",
        "sirf" to "सिर्फ",
        "bas" to "बस",
        "phir" to "फिर",
        "fir" to "फिर",
        "ab" to "अब",
        "tab" to "तब",
        "jab" to "जब",
        "yahan" to "यहाँ",
        "yaha" to "यहाँ",
        "wahan" to "वहाँ",
        "waha" to "वहाँ",
        "idhar" to "इधर",
        "udhar" to "उधर",
        "andar" to "अंदर",
        "bahar" to "बाहर",
        "upar" to "ऊपर",
        "neeche" to "नीचे",
        "aage" to "आगे",
        "peeche" to "पीछे",

        // People, Family, Friends
        "bhai" to "भाई",
        "bhaiya" to "भैया",
        "bro" to "भाई",
        "dost" to "दोस्त",
        "yaar" to "यार",
        "behan" to "बहन",
        "didi" to "दीदी",
        "mata" to "माता",
        "maa" to "माँ",
        "mummy" to "मम्मी",
        "pita" to "पिता",
        "papa" to "पापा",
        "chacha" to "चाचा",
        "chachi" to "चाची",
        "mama" to "मामा",
        "mami" to "मामी",
        "dada" to "दादा",
        "dadi" to "दादी",
        "nana" to "नाना",
        "nani" to "नानी",
        "beta" to "बेटा",
        "beti" to "बेटी",
        "bachhe" to "बच्चे",
        "bachha" to "बच्चा",
        "log" to "लोग",
        "parivar" to "परिवार",
        "ghar" to "घर",

        // Time, Days, Seasons
        "aaj" to "आज",
        "kal" to "कल",
        "parson" to "परसों",
        "subah" to "सुबह",
        "shaam" to "शाम",
        "raat" to "रात",
        "dophar" to "दोपहर",
        "samay" to "समय",
        "waqt" to "वक़्त",
        "din" to "दिन",
        "mahina" to "महीना",
        "saal" to "साल",
        "hafte" to "हफ़्ते",
        "ghanta" to "घंटा",
        "minute" to "मिनट",

        // Food, Objects, Lifestyle
        "khana" to "खाना",
        "paani" to "पानी",
        "pani" to "पानी",
        "chai" to "चाय",
        "coffee" to "कॉफ़ी",
        "roti" to "रोटी",
        "chawal" to "चावल",
        "sabzi" to "सब्ज़ी",
        "doodh" to "दूध",
        "mithai" to "मिठाई",
        "gaadi" to "गाड़ी",
        "phone" to "फ़ोन",
        "paisa" to "पैसा",
        "paise" to "पैसे",
        "kitab" to "किताब",
        "kamra" to "कमरा",
        "shehar" to "शहर",
        "gaon" to "गाँव",
        "kaam" to "काम",
        "naukri" to "नौकरी",
        "office" to "ऑफ़िस",
        "school" to "स्कूल",
        "college" to "कॉलेज",

        // Feelings & Qualities
        "pyar" to "प्यार",
        "pyaar" to "प्यार",
        "prem" to "प्रेम",
        "khush" to "खुश",
        "khushi" to "ख़ुशी",
        "dukhi" to "दुखी",
        "dukh" to "दुःख",
        "sundar" to "सुंदर",
        "khoobsurat" to "खूबसूरत",
        "bada" to "बड़ा",
        "badi" to "बड़ी",
        "bade" to "बड़े",
        "chota" to "छोटा",
        "choti" to "छोटी",
        "chote" to "छोटे",
        "naya" to "नया",
        "nayi" to "नई",
        "naye" to "नए",
        "purana" to "पुराना",
        "purani" to "पुरानी",
        "sahi" to "सही",
        "galat" to "गलत",
        "asli" to "असली",
        "nakli" to "नकली",
        "zaroori" to "ज़रूरी",
        "mushkil" to "मुश्किल",
        "aasan" to "आसान",
        "garam" to "गरम",
        "thanda" to "ठंडा",
        "meetha" to "मीठा",

        // Indian Slang / Expressive Phrases
        "arre" to "अरे",
        "wah" to "वाह",
        "shabaash" to "शाबाश",
        "shabash" to "शाबाश",
        "bindaas" to "बिंदास",
        "mast" to "मस्त",
        "masti" to "मस्ती",
        "zabardast" to "ज़बरदस्त",
        "jhakaas" to "झकास",
        "oyehoye" to "ओए होए",
        "oye" to "ओए",
        "suno" to "सुनो",
        "arrey" to "अरे"
    )

    // Common Banglish to Bengali dictionary
    private val BANGLISH_DICTIONARY = mapOf(
        "kemon" to "কেমন",
        "achhen" to "আছেন",
        "achho" to "আছো",
        "bhalo" to "ভালো",
        "dhonnobad" to "ধন্যবাদ",
        "dhanyabad" to "ধন্যবাদ",
        "shubho" to "শুভ",
        "shokal" to "সকাল",
        "sokal" to "সকাল",
        "raat" to "রাত",
        "bondhu" to "বন্ধু",
        "tumi" to "তুমি",
        "apni" to "আপনি",
        "ami" to "আমি",
        "amra" to "আমরা",
        "ki" to "কী",
        "kothay" to "কোথায়",
        "khabar" to "খাবার",
        "jol" to "জল",
        "pani" to "পানি",
        "bari" to "বাড়ি",
        "kaj" to "কাজ",
        "somoy" to "সময়",
        "bhalobasha" to "ভালোবাসা",
        "khub" to "খুব",
        "shobai" to "সবাই",
        "aaj" to "আজ",
        "kal" to "কাল",
        "haan" to "হ্যাঁ",
        "na" to "না"
    )

    // Consonant map for phonetic parsing
    private val CONSONANT_MAP: List<Pair<String, String>> = listOf(
        "ksha" to "क्ष", "ksh" to "क्ष्", "gya" to "ज्ञ", "gy" to "ज्ञ्", "tra" to "त्र", "tr" to "त्र्", "shra" to "श्र", "shr" to "श्र्",
        "chh" to "छ्", "ch" to "च्", "kh" to "ख्", "gh" to "घ्", "jh" to "झ्", "th" to "थ्", "dh" to "ध्", "ph" to "फ्", "bh" to "भ्",
        "sh" to "श्", "shh" to "ष्", "k" to "क्", "g" to "ग्", "c" to "क्", "j" to "ज्", "t" to "त्", "d" to "द्", "n" to "न्",
        "p" to "प्", "f" to "फ्", "b" to "ब्", "m" to "म्", "y" to "य्", "r" to "र्", "l" to "ल्", "v" to "व्", "w" to "व्",
        "s" to "स्", "h" to "ह्", "z" to "ज़्", "q" to "क़्", "x" to "क्स्"
    )

    // Vowel & Matra map
    private val VOWEL_MAP: List<Triple<String, String, String>> = listOf(
        // (roman, independentVowel, matra)
        Triple("aa", "आ", "ा"),
        Triple("ee", "ई", "ी"),
        Triple("oo", "ऊ", "ू"),
        Triple("ai", "ऐ", "ै"),
        Triple("au", "औ", "ौ"),
        Triple("ei", "ऐ", "ै"),
        Triple("ou", "औ", "ौ"),
        Triple("a", "अ", ""),
        Triple("i", "इ", "ि"),
        Triple("u", "उ", "ु"),
        Triple("e", "ए", "े"),
        Triple("o", "ओ", "ो")
    )

    fun transliterate(input: String, langCode: String = "hi"): String {
        return getTransliterationCandidates(input, langCode).firstOrNull() ?: input
    }

    /**
     * Translates a Roman/Hinglish input into Indic candidates.
     * Returns a list of candidates: [transliteratedScript, secondChoice, originalRoman]
     */
    fun getTransliterationCandidates(input: String, langCode: String = "hi"): List<String> {
        val clean = input.trim().lowercase(Locale.ROOT)
        if (clean.isBlank()) return emptyList()

        // 1. Direct dictionary match for Bengali
        if (langCode == "bn") {
            val bnMatch = BANGLISH_DICTIONARY[clean]
            if (bnMatch != null) {
                return listOf(bnMatch, clean)
            }
        }

        // 2. Direct dictionary match for Hindi / Marathi
        val exactMatch = HINGLISH_DICTIONARY[clean]
        if (exactMatch != null) {
            val capitalized = if (input.firstOrNull()?.isUpperCase() == true) clean else clean
            return listOf(exactMatch, capitalized)
        }

        // 3. Prefix lookup in dictionary for fast typing completion
        val prefixMatches = HINGLISH_DICTIONARY.filterKeys { it.startsWith(clean) }.values.take(2)
        if (prefixMatches.isNotEmpty()) {
            val list = mutableListOf<String>()
            list.addAll(prefixMatches)
            list.add(clean)
            return list.distinct()
        }

        // 4. Algorithmic phonetic transliterator fallback
        val algorithmic = transliteratePhonetic(clean)
        return if (algorithmic.isNotBlank() && algorithmic != clean) {
            listOf(algorithmic, clean)
        } else {
            listOf(clean)
        }
    }

    /**
     * Algorithmic transliterator for any arbitrary Hinglish text
     */
    fun transliteratePhonetic(input: String): String {
        val str = input.lowercase(Locale.ROOT)
        val sb = StringBuilder()
        var i = 0
        var lastWasConsonant = false

        while (i < str.length) {
            // Check for vowels first
            var matchedVowel: Triple<String, String, String>? = null
            for (v in VOWEL_MAP) {
                if (str.startsWith(v.first, i)) {
                    matchedVowel = v
                    break
                }
            }

            if (matchedVowel != null) {
                if (lastWasConsonant) {
                    // Remove halant from previous consonant if applying matra
                    if (sb.isNotEmpty() && sb.last() == '्') {
                        sb.deleteCharAt(sb.length - 1)
                    }
                    sb.append(matchedVowel.third)
                } else {
                    sb.append(matchedVowel.second)
                }
                i += matchedVowel.first.length
                lastWasConsonant = false
                continue
            }

            // Check for consonants
            var matchedConsonant: Pair<String, String>? = null
            for (c in CONSONANT_MAP) {
                if (str.startsWith(c.first, i)) {
                    matchedConsonant = c
                    break
                }
            }

            if (matchedConsonant != null) {
                sb.append(matchedConsonant.second)
                i += matchedConsonant.first.length
                lastWasConsonant = true
                continue
            }

            // Other character (numbers, punctuation, etc.)
            sb.append(str[i])
            i++
            lastWasConsonant = false
        }

        // If the word ends with a consonant and has a halant, in standard Hindi speech
        // the final letter is pronounced with inherent schwa (अ), so remove trailing halant
        var result = sb.toString()
        if (result.endsWith("्")) {
            result = result.dropLast(1)
        }

        return result
    }
}
