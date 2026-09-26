package com.example.keyboard

import com.example.model.LayoutType

object KeyboardLayouts {

    val NUMBER_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val NUMBERS_TOP_ROW = listOf("[", "]", "{", "}", "<", ">", "^", "%", "°", "=")
    val SYMBOLS_TOP_ROW = listOf("€", "¥", "£", "¢", "©", "®", "™", "✓", "•", "∆")

    // GBOARD 3x4 DEDICATED NUMERIC PAD LAYOUT
    val NUMERIC_PAD_LEFT_OPERATORS = listOf("+", "-", "*", "/")
    val NUMERIC_PAD_DIGIT_GRID = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    val NUMERIC_PAD_BOTTOM_ROW = listOf("ABC", ",", "!?#", "0", "=", ".", "ENTER")

    val QWERTY_ROWS = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    val QWERTY_SHIFTED_ROWS = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("Z", "X", "C", "V", "B", "N", "M")
    )

    // DEVANAGARI (Hindi & Marathi / हिन्दी आणि मराठी)
    val DEVANAGARI_ROWS = listOf(
        listOf("क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ"),
        listOf("ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न"),
        listOf("प", "फ", "ब", "भ", "म", "य", "र", "ल", "व", "श", "ष", "स", "ह")
    )

    val DEVANAGARI_SHIFTED_ROWS = listOf(
        listOf("ा", "ि", "ी", "ु", "ू", "ृ", "े", "ै", "ो", "ौ"),
        listOf("अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ", "औ", "ऍ", "ऑ"),
        listOf("्", "ं", "ः", "ँ", "़", "ळ", "क्ष", "त्र", "ज्ञ", "श्र")
    )

    val DEVANAGARI_QUICK_MATRAS = listOf(
        "ा", "ि", "ी", "ु", "ू", "ृ", "े", "ै", "ो", "ौ", "्", "ं", "ः", "ँ", "़", "ळ"
    )

    // BENGALI (বাংলা)
    val BENGALI_ROWS = listOf(
        listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ"),
        listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন"),
        listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ", "স", "হ")
    )

    val BENGALI_SHIFTED_ROWS = listOf(
        listOf("া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ"),
        listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও", "ঔ"),
        listOf("্", "ং", "ঃ", "ঁ", "ৎ", "ড়", "ঢ়", "য়", "্য", "্র")
    )

    val BENGALI_QUICK_MATRAS = listOf(
        "া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ", "্", "ং", "ঃ", "ঁ", "ৎ", "্য", "্র"
    )

    // GUJARATI (ગુજરાતી)
    val GUJARATI_ROWS = listOf(
        listOf("ક", "ખ", "ગ", "ઘ", "ચ", "છ", "જ", "ઝ", "ટ", "ઠ"),
        listOf("ડ", "ઢ", "ણ", "ત", "થ", "દ", "ધ", "ન", "પ", "ફ"),
        listOf("બ", "ભ", "મ", "ય", "ર", "લ", "વ", "શ", "ષ", "સ", "હ")
    )

    val GUJARATI_SHIFTED_ROWS = listOf(
        listOf("ા", "િ", "ી", "ુ", "ૂ", "ૃ", "ે", "ૈ", "ો", "ૌ"),
        listOf("અ", "આ", "ઇ", "ઈ", "ઉ", "ઊ", "ઋ", "એ", "ઐ", "ઓ", "ઔ"),
        listOf("્", "ં", "ઃ", "ઁ", "ળ", "ક્ષ", "જ્ઞ", "ત્ર", "શ્ર")
    )

    val GUJARATI_QUICK_MATRAS = listOf(
        "ા", "િ", "ી", "ુ", "ૂ", "ૃ", "ે", "ૈ", "ો", "ૌ", "્", "ં", "ઃ", "ઁ", "ળ"
    )

    // TAMIL (தமிழ்)
    val TAMIL_ROWS = listOf(
        listOf("க", "ங", "ச", "ஞ", "ட", "ண", "த", "ந", "ப", "ம"),
        listOf("ய", "ர", "ல", "வ", "ழ", "ள", "ற", "ன", "ஜ", "ஷ"),
        listOf("ஸ", "ஹ", "க்ஷ", "அ", "ஆ", "இ", "ஈ", "உ", "ஊ")
    )

    val TAMIL_SHIFTED_ROWS = listOf(
        listOf("ா", "ி", "ீ", "ு", "ூ", "ெ", "ே", "ை", "ொ", "ோ", "ௌ"),
        listOf("எ", "ஏ", "ஐ", "ஒ", "ஓ", "ஔ", "ஃ"),
        listOf("்", "ஸ்ரீ", "ௐ", "௧", "௨", "௩", "௪", "௫")
    )

    val TAMIL_QUICK_MATRAS = listOf(
        "ா", "ி", "ீ", "ு", "ூ", "ெ", "ே", "ை", "ொ", "ோ", "ௌ", "்", "ஃ"
    )

    // TELUGU (తెలుగు)
    val TELUGU_ROWS = listOf(
        listOf("క", "ఖ", "గ", "ఘ", "చ", "ఛ", "జ", "ఝ", "ట", "ఠ"),
        listOf("డ", "ఢ", "ణ", "త", "థ", "ద", "ధ", "న", "ప", "ఫ"),
        listOf("బ", "భ", "మ", "య", "ర", "ల", "వ", "శ", "ష", "స", "హ")
    )

    val TELUGU_SHIFTED_ROWS = listOf(
        listOf("ా", "ి", "ీ", "ు", "ూ", "ృ", "ె", "ే", "ై", "ొ", "ో", "ౌ"),
        listOf("అ", "ఆ", "ఇ", "ఈ", "ఉ", "ఊ", "ఋ", "ఎ", "ఏ", "ఐ", "ఒ", "ఓ", "ఔ"),
        listOf("్", "ం", "ః", "ళ", "క్ష", "జ్ఞ", "ఱ")
    )

    val TELUGU_QUICK_MATRAS = listOf(
        "ా", "ి", "ీ", "ు", "ూ", "ృ", "ె", "ే", "ై", "ొ", "ో", "ౌ", "్", "ం", "ః"
    )

    // KANNADA (ಕನ್ನಡ)
    val KANNADA_ROWS = listOf(
        listOf("ಕ", "ಖ", "ಗ", "ಘ", "ಚ", "ಛ", "ಜ", "ಝ", "ಟ", "ಠ"),
        listOf("ಡ", "ಢ", "ಣ", "ತ", "ಥ", "ದ", "ಧ", "ನ", "ಪ", "ಫ"),
        listOf("ಬ", "ಭ", "ಮ", "ಯ", "ರ", "ಲ", "ವ", "ಶ", "ಷ", "ಸ", "ಹ")
    )

    val KANNADA_SHIFTED_ROWS = listOf(
        listOf("ಾ", "ಿ", "ೀ", "ು", "ೂ", "ೃ", "ೆ", "ೇ", "ೈ", "ೊ", "ೋ", "ೌ"),
        listOf("ಅ", "ಆ", "ಇ", "ಈ", "ಉ", "ಊ", "ಋ", "ಎ", "ಏ", "ಐ", "ಒ", "ಓ", "ಔ"),
        listOf("್", "ಂ", "ಃ", "ಳ", "ಕ್ಷ", "ಜ್ಞ", "ಱ")
    )

    val KANNADA_QUICK_MATRAS = listOf(
        "ಾ", "ಿ", "ೀ", "ು", "ೂ", "ೃ", "ೆ", "ೇ", "ೈ", "ೊ", "ೋ", "ೌ", "್", "ಂ", "ಃ"
    )

    // MALAYALAM (മലയാളം)
    val MALAYALAM_ROWS = listOf(
        listOf("ക", "ഖ", "ഗ", "ഘ", "ങ", "ച", "ഛ", "ജ", "ഝ", "ഞ"),
        listOf("ട", "ഠ", "ഡ", "ഢ", "ണ", "ത", "ഥ", "ദ", "ധ", "ന"),
        listOf("പ", "ഫ", "ബ", "ഭ", "മ", "യ", "ര", "ല", "വ", "ശ", "ഷ", "സ", "ഹ")
    )

    val MALAYALAM_SHIFTED_ROWS = listOf(
        listOf("ാ", "ി", "ീ", "ു", "ൂ", "ൃ", "െ", "േ", "ൈ", "ൊ", "ോ", "ൌ"),
        listOf("അ", "ആ", "ഇ", "ഈ", "ഉ", "ഊ", "ഋ", "എ", "ഏ", "ഐ", "ഒ", "ഓ", "ഔ"),
        listOf("്", "ം", "ഃ", "ള", "ഴ", "റ", "ൺ", "ൻ", "ർ", "ൽ", "ൾ")
    )

    val MALAYALAM_QUICK_MATRAS = listOf(
        "ാ", "ി", "ീ", "ു", "ൂ", "ൃ", "െ", "േ", "ൈ", "ൊ", "ോ", "ൌ", "്", "ം", "ഃ"
    )

    // GURMUKHI / PUNJABI (ਪੰਜਾਬੀ)
    val GURMUKHI_ROWS = listOf(
        listOf("ਕ", "ਖ", "ਗ", "ਘ", "ਙ", "ਚ", "ਛ", "ਜ", "ਝ", "ਞ"),
        listOf("ਟ", "ਠ", "ਡ", "ਢ", "ਣ", "ਤ", "ਥ", "ਦ", "ਧ", "ਨ"),
        listOf("ਪ", "ਫ", "ਬ", "ਭ", "ਮ", "ਯ", "ਰ", "ਲ", "ਵ", "ਸ਼", "ਸ", "ਹ")
    )

    val GURMUKHI_SHIFTED_ROWS = listOf(
        listOf("ਾ", "ਿ", "ੀ", "ੁ", "ੂ", "ੇ", "ੈ", "ੋ", "ੌ"),
        listOf("ਅ", "ਆ", "ਇ", "ਈ", "ਉ", "ਊ", "ਏ", "ਐ", "ਓ", "ਔ"),
        listOf("੍", "ਂ", "ੱ", "਼", "ੜ", "ਖ਼", "ਗ਼", "ਜ਼", "ਫ਼", "ੴ")
    )

    val GURMUKHI_QUICK_MATRAS = listOf(
        "ਾ", "ਿ", "ੀ", "ੁ", "ੂ", "ੇ", "ੈ", "ੋ", "ੌ", "੍", "ਂ", "ੱ", "਼"
    )

    // ARABIC / URDU (العربية / اردو)
    val ARABIC_ROWS = listOf(
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
        listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
        listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ", "د", "ذ")
    )

    val ARABIC_SHIFTED_ROWS = listOf(
        listOf("َ", "ُ", "ِ", "ً", "ٌ", "ٍ", "ْ", "ّ", "ٓ"),
        listOf("ٹ", "ڈ", "ڑ", "ں", "ے", "ہ", "ھ", "چ", "پ", "ژ", "گ"),
        listOf("آ", "أ", "إ", "ٱ", "ؤ", "ئ", "ۓ", "ۂ", "،", "؛", "؟")
    )

    val ARABIC_QUICK_MATRAS = listOf(
        "َ", "ُ", "ِ", "ً", "ٌ", "ٍ", "ْ", "ّ", "ٓ", "ٹ", "ڈ", "ڑ", "ں", "ے", "چ", "پ", "گ"
    )

    val NUMBERS_PAGE_ROWS = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "₹", "$", "_", "&", "-", "+", "(", ")", "/"),
        listOf("*", "\"", "'", ":", ";", "!", "?")
    )

    val SYMBOLS_PAGE_ROWS = listOf(
        listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"),
        listOf("₹", "£", "¥", "€", "¢", "^", "°", "=", "{", "}", "\\"),
        listOf("%", "©", "®", "™", "✓", "[", "]", "<", ">")
    )

    // CYRILLIC (Russian / Русский)
    val CYRILLIC_ROWS = listOf(
        listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "ъ"),
        listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э"),
        listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю")
    )

    val CYRILLIC_SHIFTED_ROWS = listOf(
        listOf("Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З", "Х", "Ъ"),
        listOf("Ф", "Ы", "В", "А", "П", "Р", "О", "Л", "Д", "Ж", "Э"),
        listOf("Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю")
    )

    fun getRowsForLayout(layoutType: LayoutType, isShifted: Boolean = false): List<List<String>> {
        return if (isShifted) {
            when (layoutType) {
                LayoutType.QWERTY -> QWERTY_SHIFTED_ROWS
                LayoutType.DEVANAGARI -> DEVANAGARI_SHIFTED_ROWS
                LayoutType.BENGALI -> BENGALI_SHIFTED_ROWS
                LayoutType.GUJARATI -> GUJARATI_SHIFTED_ROWS
                LayoutType.TAMIL -> TAMIL_SHIFTED_ROWS
                LayoutType.TELUGU -> TELUGU_SHIFTED_ROWS
                LayoutType.KANNADA -> KANNADA_SHIFTED_ROWS
                LayoutType.MALAYALAM -> MALAYALAM_SHIFTED_ROWS
                LayoutType.GURMUKHI -> GURMUKHI_SHIFTED_ROWS
                LayoutType.ARABIC -> ARABIC_SHIFTED_ROWS
                LayoutType.JAPANESE -> QWERTY_SHIFTED_ROWS
                LayoutType.CYRILLIC -> CYRILLIC_SHIFTED_ROWS
            }
        } else {
            when (layoutType) {
                LayoutType.QWERTY -> QWERTY_ROWS
                LayoutType.DEVANAGARI -> DEVANAGARI_ROWS
                LayoutType.BENGALI -> BENGALI_ROWS
                LayoutType.GUJARATI -> GUJARATI_ROWS
                LayoutType.TAMIL -> TAMIL_ROWS
                LayoutType.TELUGU -> TELUGU_ROWS
                LayoutType.KANNADA -> KANNADA_ROWS
                LayoutType.MALAYALAM -> MALAYALAM_ROWS
                LayoutType.GURMUKHI -> GURMUKHI_ROWS
                LayoutType.ARABIC -> ARABIC_ROWS
                LayoutType.JAPANESE -> QWERTY_ROWS
                LayoutType.CYRILLIC -> CYRILLIC_ROWS
            }
        }
    }

    fun getQuickMatrasForLayout(layoutType: LayoutType): List<String>? {
        return when (layoutType) {
            LayoutType.DEVANAGARI -> DEVANAGARI_QUICK_MATRAS
            LayoutType.BENGALI -> BENGALI_QUICK_MATRAS
            LayoutType.GUJARATI -> GUJARATI_QUICK_MATRAS
            LayoutType.TAMIL -> TAMIL_QUICK_MATRAS
            LayoutType.TELUGU -> TELUGU_QUICK_MATRAS
            LayoutType.KANNADA -> KANNADA_QUICK_MATRAS
            LayoutType.MALAYALAM -> MALAYALAM_QUICK_MATRAS
            LayoutType.GURMUKHI -> GURMUKHI_QUICK_MATRAS
            LayoutType.ARABIC -> ARABIC_QUICK_MATRAS
            LayoutType.QWERTY, LayoutType.JAPANESE, LayoutType.CYRILLIC -> null
        }
    }

    fun getSubSymbolForQWERTY(key: String): String? {
        return when (key.lowercase()) {
            "q" -> "1"
            "w" -> "2"
            "e" -> "3"
            "r" -> "4"
            "t" -> "5"
            "y" -> "6"
            "u" -> "7"
            "i" -> "8"
            "o" -> "9"
            "p" -> "0"
            "a" -> "@"
            "s" -> "#"
            "d" -> "$"
            "f" -> "%"
            "g" -> "&"
            "h" -> "-"
            "j" -> "+"
            "k" -> "("
            "l" -> ")"
            "z" -> "*"
            "x" -> "\""
            "c" -> "'"
            "v" -> ":"
            "b" -> ";"
            "n" -> "!"
            "m" -> "?"
            else -> null
        }
    }

    val EMOJI_CATEGORIES = mapOf(
        "Smileys" to listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕"),
        "Desi & Festivals" to listOf("🇮🇳", "🪔", "☕", "🍛", "🫓", "🏏", "🌸", "🙏", "🎨", "🎆", "🎇", "🔔", "🪕", "🐘", "🐅", "🥭", "🥮", "🧵", "🕌", "🛕", "₹", "🫡", "🤝", "💐", "✨", "🌺", "🥥", "🎊", "🎉"),
        "Gestures" to listOf("👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞", "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✍️", "💅", "🤳", "💪"),
        "Hearts" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "💌", "💐", "🌸", "💮", "🌹", "🌺", "🌻", "🌼", "🌷"),
        "Vibe & Tech" to listOf("✨", "⭐", "🌟", "💫", "🔥", "💥", "⚡", "🌈", "☀️", "🌙", "🎉", "🎊", "🚀", "💡", "🧠", "🤖", "💻", "📱", "🎯", "🏆", "💎", "💯", "🔥", "🪄"),
        "Kaomoji" to listOf("(◕‿◕)", "(づ｡◕‿‿◕｡)づ", "(¬‿¬)", "(╯°□°)╯", "(•‿•)", "¯\\_(ツ)_/¯", "(^o^)/", "(>‿<)", "(｡♥‿♥｡)", "(⁄ ⁄•⁄ω⁄•⁄ ⁄)", "(╥﹏╥)", "(T_T)", "(≧◡≦)", "(ʘ‿ʘ)", "(•̀o•́)ง", "( ͡° ͜ʖ ͡°)", "(っ˘з(˘⌣˘ )", "＼(＾O＾)／")
    )
}
