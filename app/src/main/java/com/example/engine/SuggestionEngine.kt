package com.example.engine

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object SuggestionEngine {

    // Dynamic user vocabulary learned during session
    private val learnedWords = ConcurrentHashMap.newKeySet<String>()

    fun learnWord(word: String) {
        val clean = word.trim().lowercase()
        if (clean.length in 2..25 && clean.all { it.isLetter() }) {
            learnedWords.add(clean)
        }
    }

    // Spatial adjacency map for standard QWERTY layout - used for calculating physical key distance
    private val QWERTY_NEIGHBORS: Map<Char, Set<Char>> = mapOf(
        'q' to setOf('w', 'a', 's', '1', '2'),
        'w' to setOf('q', 'e', 'a', 's', 'd', '2', '3'),
        'e' to setOf('w', 'r', 's', 'd', 'f', '3', '4'),
        'r' to setOf('e', 't', 'd', 'f', 'g', '4', '5'),
        't' to setOf('r', 'y', 'f', 'g', 'h', '5', '6'),
        'y' to setOf('t', 'u', 'g', 'h', 'j', '6', '7'),
        'u' to setOf('y', 'i', 'h', 'j', 'k', '7', '8'),
        'i' to setOf('u', 'o', 'j', 'k', 'l', '8', '9'),
        'o' to setOf('i', 'p', 'k', 'l', '9', '0'),
        'p' to setOf('o', 'l', '0'),
        'a' to setOf('q', 'w', 's', 'z'),
        's' to setOf('q', 'w', 'e', 'a', 'd', 'z', 'x'),
        'd' to setOf('w', 'e', 'r', 's', 'f', 'x', 'c'),
        'f' to setOf('e', 'r', 't', 'd', 'g', 'c', 'v'),
        'g' to setOf('r', 't', 'y', 'f', 'h', 'v', 'b'),
        'h' to setOf('t', 'y', 'u', 'g', 'j', 'b', 'n'),
        'j' to setOf('y', 'u', 'i', 'h', 'k', 'n', 'm'),
        'k' to setOf('u', 'i', 'o', 'j', 'l', 'm'),
        'l' to setOf('i', 'o', 'p', 'k'),
        'z' to setOf('a', 's', 'x'),
        'x' to setOf('z', 's', 'd', 'c'),
        'c' to setOf('x', 'd', 'f', 'v'),
        'v' to setOf('c', 'f', 'g', 'b'),
        'b' to setOf('v', 'g', 'h', 'n'),
        'n' to setOf('b', 'h', 'j', 'm'),
        'm' to setOf('n', 'j', 'k')
    )

    // Extensive dictionary with high-frequency everyday words
    private val commonWords = mapOf(
        "en" to listOf(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "I", "it", "for", "not", "on", "with",
            "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her",
            "she", "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out",
            "if", "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just",
            "him", "know", "take", "people", "into", "year", "your", "good", "some", "could", "them", "see",
            "other", "than", "then", "now", "look", "only", "come", "its", "over", "think", "also", "back",
            "after", "use", "two", "how", "our", "work", "first", "well", "way", "even", "new", "want", "because",
            "any", "these", "give", "day", "most", "us", "great", "tell", "need", "feel", "try", "leave", "call",
            "should", "world", "still", "something", "keyboard", "message", "today", "tomorrow", "tonight",
            "yesterday", "please", "thanks", "thank", "welcome", "friend", "happy", "love", "awesome", "perfect",
            "phone", "android", "app", "application", "game", "typing", "type", "smart", "quick", "fast", "simple",
            "easy", "easily", "help", "sure", "fine", "okay", "alright", "meeting", "office", "home", "family",
            "school", "project", "email", "send", "receive", "ready", "start", "finish", "done", "check", "update",
            "online", "together", "beautiful", "wonderful", "amazing", "brother", "sister", "mother", "father",
            "food", "dinner", "lunch", "morning", "evening", "night", "travel", "place", "placement", "city",
            "country", "music", "video", "photo", "picture", "question", "answer", "reason", "problem", "solution",
            "idea", "remember", "forget", "always", "never", "sometimes", "often", "usually", "almost", "enough",
            "already", "again", "around", "without", "alphabet", "alignment", "align", "distance", "extra", "means",
            "touch", "clear", "button", "buttons", "analyze", "size", "sizes", "best", "better", "spell",
            "mismatch", "inspiration", "gboard", "letters", "stagger", "space", "spacing", "screen", "display",
            "tap", "taps", "press", "preview", "height", "width", "accurate", "accuracy", "device", "sound",
            "vibration", "theme", "themes", "style", "language", "translate", "translation", "voice", "speak",
            "speed", "pitch", "write", "writing", "read", "reading", "input", "output", "system", "entered",
            "enter", "entry", "suggest", "suggestion", "suggestions", "feature", "features", "save", "saving",
            "saved", "lot", "lots", "fix", "fixed", "fixing", "look", "looking", "working", "work", "worked",
            "free", "busy", "available", "schedule", "reschedule", "discuss", "discussion", "important", "urgent",
            "priority", "document", "file", "folder", "link", "address", "number", "profile", "account",
            "password", "security", "setting", "settings", "option", "options", "share", "shared", "sharing",
            "download", "install", "test", "testing", "tester", "tested", "response", "reply", "replies",
            "connect", "connection", "connected", "internet", "network", "battery", "power", "charge", "charger",
            "level", "sound", "volume", "music", "camera", "media", "storage", "cloud", "backup", "restore",
            "status", "progress", "forward", "backward", "next", "previous", "continue", "cancel", "confirm",
            "agree", "accept", "reject", "delete", "remove", "insert", "copy", "paste", "cut", "undo", "redo",
            "select", "search", "find", "replace", "clear", "reset", "enable", "disable", "active", "inactive",
            "high", "low", "medium", "light", "dark", "mode", "color", "colors", "font", "bold", "italic",
            "underlined", "header", "title", "subtitle", "note", "notes", "memo", "calendar", "event", "alarm",
            "clock", "timer", "reminder", "task", "tasks", "todo", "done", "pending", "inbox", "sent", "draft",
            "trash", "spam", "archive", "favorite", "star", "bookmark", "history", "recent", "clear", "search",
            "google", "bing", "search", "web", "browser", "page", "site", "website", "link", "url", "code",
            "developer", "coding", "studio", "gemini", "artificial", "intelligence", "assistant", "agent",
            "model", "prompt", "generate", "generated", "generating", "creation", "creative", "rewrite", "tone",
            "grammar", "formal", "casual", "polite", "confident", "funny", "short", "expand", "summarize",
            "summary", "listen", "listener", "speaking", "mic", "microphone", "headphone", "bluetooth",
            "wifi", "cellular", "mobile", "tablet", "desktop", "laptop", "device", "hardware", "software",
            "version", "update", "upgrade", "latest", "modern", "design", "layout", "neumorphism", "glassmorphism",
            "flat", "minimal", "amoled", "gradient", "cyberpunk", "material", "smooth", "fluid", "responsive",
            "touchscreen", "gesture", "swipe", "glide", "drag", "drop", "scroll", "fling", "pinch", "zoom"
        ),
        "hi" to listOf(
            "नमस्ते", "आप", "कैसे", "हैं", "धन्यवाद", "हाँ", "नहीं", "कहाँ", "जा", "रहे", "सुप्रभात", "शुभ", "दोस्त",
            "काम", "समय", "घर", "पानी", "खाना", "प्यार", "बहुत", "अच्छा", "भारत", "सब", "लोग", "क्या", "कब", "कौन",
            "यहाँ", "वहाँ", "ठीक", "भाई", "बहन", "माता", "पिता", "आज", "कल", "रात", "दिन", "खुश", "सुंदर", "मदद",
            "फोन", "गाड़ी", "किताब", "पैसा", "बात", "बधाई", "स्वागत", "मिलते", "जरूर", "हमेशा", "कभी", "कीजिए",
            "बताइए", "चाहिए", "सकते", "सकता", "सकती", "होगा", "होगी", "देंगे", "लेंगे", "करेंगे", "करते", "करती"
        ),
        "mr" to listOf(
            "नमस्कार", "तुम्ही", "कसे", "आहात", "धन्यवाद", "हो", "नाही", "कुठे", "जात", "शुभ", "सकाळ", "मित्र",
            "काम", "वेळ", "घर", "पाणी", "जेवण", "प्रेम", "खूप", "छान", "महाराष्ट्र", "सर्व", "लोग", "काय", "कधी",
            "कोण", "येथे", "तेथे", "ठीक", "भाऊ", "बहीण", "आई", "बाबा", "आज", "उद्या", "रात्र", "दिवस", "आनंद", "सुंदर"
        ),
        "bn" to listOf(
            "হ্যালো", "আপনি", "কেमन", "আছেন", "ধন্যবাদ", "হ্যাঁ", "না", "কোথায়", "যাচ্ছেন", "শুভ", "সকাল", "বন্ধু",
            "কাজ", "সময়", "বাড়ি", "জল", "খাবার", "ভালোবাসা", "খুব", "ভালো", "সবাই", "আজ", "কাল", "রাত", "দিন"
        ),
        "es" to listOf(
            "hola", "gracias", "por", "favor", "buenos", "días", "cómo", "estás", "bien", "amigo", "trabajo",
            "tiempo", "casa", "agua", "comida", "amor", "mucho", "todo", "donde", "cuando", "quien", "si", "no",
            "siempre", "nunca", "hoy", "mañana", "noche", "tarde", "familia", "hermano", "hermana", "feliz",
            "necesito", "quiero", "puedo", "hacer", "decir", "ir", "venir", "estar", "tener", "ser", "dar"
        ),
        "fr" to listOf(
            "bonjour", "merci", "salut", "comment", "allez", "vous", "bien", "ami", "travail", "temps", "maison",
            "eau", "nourriture", "amour", "beaucoup", "tout", "où", "quand", "qui", "oui", "non", "toujours",
            "jamais", "aujourd'hui", "demain", "soir", "famille", "frère", "soeur", "heureux", "bonne", "journée"
        )
    )

    // Extensive typo correction dictionary mapping mistyped words to intended words
    private val commonTypos = mapOf(
        // User specific typos & keyboard mistouches
        "thye" to "the",
        "autoi" to "auto",
        "nbot" to "not",
        "woek" to "work",
        "propeelky" to "properly",
        "propely" to "properly",
        "posible" to "possible",
        "psosuible" to "possible",
        "posbbile" to "possible",
        "sson" to "soon",
        "anfd" to "and",
        "aanfd" to "and",
        "cahaneg" to "change",
        "chaneg" to "change",
        "chagne" to "change",
        "keyopad" to "keypad",
        "fist" to "first",
        "frist" to "first",
        "featurra" to "feature",
        "feataues" to "features",
        "feataue" to "feature",
        "fetures" to "features",
        "iutsd" to "it's",
        "lasnuage" to "language",
        "lahuage" to "language",
        "laguafe" to "language",
        "laguage" to "language",
        "lanugage" to "language",
        "langauge" to "language",
        "voce" to "voice",
        "voise" to "voice",
        "thata" to "that",
        "aftrew" to "after",
        "cliking" to "clicking",
        "trala" to "translate",
        "trasnlate" to "translate",
        "tranlate" to "translate",
        "ouput" to "output",
        "inpu" to "input",
        "cueerenly" to "currently",
        "curently" to "currently",
        "horizontala" to "horizontal",
        "horizantal" to "horizontal",
        "scrolloing" to "scrolling",
        "scroling" to "scrolling",
        "rthaty" to "that",
        "blook" to "look",
        "ggods" to "good",
        "prfetionba" to "professional",
        "profesional" to "professional",
        "eneterd" to "entered",
        "eneter" to "enter",
        "sugset" to "suggest",
        "sugsets" to "suggests",
        "suggestons" to "suggestions",
        "esay" to "easy",
        "menas" to "means",
        "meand" to "means",
        "keybord" to "keyboard",
        "keyborad" to "keyboard",
        "keborad" to "keyboard",
        "kybord" to "keyboard",
        "palcemnt" to "placement",
        "palacemts" to "placement",
        "placemnt" to "placement",
        "typoing" to "typing",
        "typign" to "typing",
        "alaiment" to "alignment",
        "alignemnt" to "alignment",
        "aligment" to "alignment",
        "alfabet" to "alphabet",
        "eaxtrs" to "extra",
        "extr" to "extra",
        "calarra" to "clear",
        "cler" to "clear",
        "bauttaon" to "button",
        "botton" to "button",
        "buttn" to "button",
        "analays" to "analyze",
        "analyz" to "analyze",
        "thinkd" to "things",
        "thigns" to "things",
        "beast" to "best",
        "insprstion" to "inspiration",
        "insperation" to "inspiration",
        "themas" to "themes",
        "thimesa" to "themes",
        "gloassmorphisusm" to "glassmorphism",
        "psosible" to "possible",
        "soze" to "size",
        "sze" to "size",
        "hight" to "height",
        "heigth" to "height",
        "widht" to "width",
        // Common conversational typos
        "teh" to "the",
        "hte" to "the",
        "adn" to "and",
        "nad" to "and",
        "waht" to "what",
        "wht" to "what",
        "thsi" to "this",
        "tihs" to "this",
        "thier" to "their",
        "ther" to "there",
        "becuase" to "because",
        "becasue" to "because",
        "bcuz" to "because",
        "bcoz" to "because",
        "soemthing" to "something",
        "somthing" to "something",
        "wrk" to "work",
        "wokr" to "work",
        "recieve" to "receive",
        "recive" to "receive",
        "seperate" to "separate",
        "untill" to "until",
        "wierd" to "weird",
        "accomodate" to "accommodate",
        "definately" to "definitely",
        "definetly" to "definitely",
        "goverment" to "government",
        "occured" to "occurred",
        "tommorrow" to "tomorrow",
        "tomorow" to "tomorrow",
        "tmrw" to "tomorrow",
        "truely" to "truly",
        "alot" to "a lot",
        "alway" to "always",
        "alaways" to "always",
        "thx" to "thanks",
        "thnk" to "thank",
        "tysm" to "thank you so much",
        "pls" to "please",
        "plz" to "please",
        "peopl" to "people",
        "peopel" to "people",
        "helpo" to "hello",
        "helo" to "hello",
        "hlo" to "hello",
        "hlw" to "hello",
        "gm" to "Good morning",
        "gn" to "Good night",
        "omw" to "on my way",
        "idk" to "I don't know",
        "btw" to "by the way",
        "alright" to "all right",
        "dont" to "don't",
        "cant" to "can't",
        "wont" to "won't",
        "im" to "I'm",
        "youre" to "you're",
        "theyre" to "they're",
        "didnt" to "didn't",
        "isnt" to "isn't",
        "arent" to "aren't",
        "shouldnt" to "shouldn't",
        "couldnt" to "couldn't",
        "wouldnt" to "wouldn't",
        "whos" to "who's",
        "whats" to "what's",
        "hows" to "how's",
        "thats" to "that's",
        "wher" to "where",
        "whre" to "where",
        "wanna" to "want to",
        "gonna" to "going to",
        "gotta" to "got to",
        "messege" to "message",
        "msg" to "message",
        "awsum" to "awesome",
        "beautifull" to "beautiful",
        "freind" to "friend",
        "frnd" to "friend",
        "pic" to "picture",
        "sry" to "sorry",
        "srry" to "sorry",
        "diffrent" to "different",
        "diferent" to "different"
    )

    // Next-Word Prediction (Language Model N-Gram Matrix like Google Keyboard)
    private val NEXT_WORD_PREDICTIONS: Map<String, List<String>> = mapOf(
        // Trigrams / Compound phrases
        "how are" to listOf("you", "things", "they", "we"),
        "how is" to listOf("it", "everything", "your", "the", "work"),
        "how about" to listOf("you", "tomorrow", "this", "today"),
        "how to" to listOf("use", "make", "get", "do", "fix"),
        "how can" to listOf("I", "we", "you", "this"),
        "how do" to listOf("you", "we", "I"),
        "what are" to listOf("you", "the", "we", "they"),
        "what is" to listOf("the", "your", "this", "that", "it"),
        "what do" to listOf("you", "we", "they"),
        "what about" to listOf("you", "this", "tomorrow", "it"),
        "where are" to listOf("you", "we", "the", "they"),
        "where is" to listOf("the", "my", "your", "it"),
        "where can" to listOf("I", "we", "you"),
        "thank you" to listOf("so", "very", "much", "for", "again"),
        "thanks for" to listOf("the", "your", "sharing", "help", "coming"),
        "please let" to listOf("me", "us", "know", "them"),
        "let me" to listOf("know", "see", "check", "help", "think", "do"),
        "let us" to listOf("know", "go", "start", "proceed", "meet"),
        "can you" to listOf("please", "send", "call", "help", "check", "come"),
        "could you" to listOf("please", "send", "let", "help", "share"),
        "would you" to listOf("like", "mind", "be", "please"),
        "do you" to listOf("have", "want", "think", "know", "need", "like"),
        "good morning" to listOf("have", "hope", "sir", "all", "everyone"),
        "good night" to listOf("sweet", "sleep", "take", "dreams"),
        "good afternoon" to listOf("hope", "everyone", "all"),
        "see you" to listOf("soon", "tomorrow", "later", "there", "then"),
        "nice to" to listOf("meet", "see", "hear", "talk"),
        "have a" to listOf("great", "good", "nice", "wonderful", "safe"),
        "have a great" to listOf("day", "weekend", "time", "evening"),
        "looking forward" to listOf("to", "hearing", "seeing"),
        "looking forward to" to listOf("hearing", "meeting", "seeing", "working", "it"),
        "on my" to listOf("way", "phone", "own", "mind"),
        "at the" to listOf("moment", "office", "same", "end", "meeting"),
        "in the" to listOf("morning", "evening", "office", "future"),
        "to the" to listOf("office", "meeting", "next", "best", "airport"),
        "for the" to listOf("update", "help", "information", "support"),
        "call me" to listOf("when", "back", "later", "if", "today"),
        "tell me" to listOf("about", "more", "when", "how"),
        "give me" to listOf("a", "some", "the", "one", "minute"),
        "send me" to listOf("the", "your", "details", "a", "file"),
        "no problem" to listOf("at", "all", "my", "friend"),
        "of course" to listOf("you", "I", "we", "it"),
        "sounds good" to listOf("see", "let", "talk", "thanks"),
        "talk to" to listOf("you", "me", "them", "later"),
        "talk to you" to listOf("soon", "later", "tomorrow"),
        "as soon as" to listOf("possible", "I", "you", "we"),
        "sorry for" to listOf("the", "being", "my", "that"),
        "sorry for the" to listOf("delay", "inconvenience", "late"),
        "are you" to listOf("ready", "there", "sure", "coming", "free", "okay"),
        "we are" to listOf("ready", "going", "here", "waiting", "happy"),
        "we will" to listOf("be", "see", "meet", "have", "discuss"),
        "it is" to listOf("a", "very", "not", "so", "going", "really"),
        "this is" to listOf("a", "the", "very", "great", "awesome"),
        "that is" to listOf("great", "good", "awesome", "true", "fine"),
        "i am" to listOf("going", "not", "so", "glad", "ready", "here"),
        "i will" to listOf("be", "call", "send", "check", "come", "do"),
        "i have" to listOf("been", "already", "to", "a", "no", "received"),
        "i want" to listOf("to", "you", "a", "more", "that"),
        "i need" to listOf("to", "help", "a", "your", "some"),
        "i think" to listOf("that", "it", "we", "you", "so"),
        "i don't" to listOf("know", "think", "have", "want", "like"),
        "i can" to listOf("do", "help", "see", "come", "make"),
        "i would" to listOf("like", "love", "be", "suggest"),
        "want to" to listOf("know", "go", "see", "be", "have"),
        "need to" to listOf("know", "go", "do", "check", "talk"),
        "going to" to listOf("be", "the", "do", "have", "get"),
        "able to" to listOf("make", "attend", "come", "do", "help"),
        "all the" to listOf("best", "time", "way", "things"),
        "take care" to listOf("of", "and", "see", "always"),
        "welcome to" to listOf("the", "our", "my"),

        // Bigrams / Single word followers
        "how" to listOf("are", "is", "about", "to", "was", "can", "do"),
        "what" to listOf("is", "are", "do", "about", "time", "happened"),
        "where" to listOf("are", "is", "can", "do", "will"),
        "when" to listOf("will", "are", "is", "can", "do", "you"),
        "why" to listOf("not", "are", "did", "is", "do"),
        "who" to listOf("is", "are", "was", "can"),
        "i" to listOf("am", "will", "have", "can", "want", "think", "need"),
        "you" to listOf("are", "can", "have", "will", "know", "want"),
        "he" to listOf("is", "was", "will", "has", "can"),
        "she" to listOf("is", "was", "will", "has", "can"),
        "it" to listOf("is", "was", "will", "has", "can", "looks"),
        "we" to listOf("are", "will", "can", "have", "should"),
        "they" to listOf("are", "will", "were", "have", "can"),
        "thank" to listOf("you", "so", "very"),
        "thanks" to listOf("for", "a", "lot", "again", "bro"),
        "please" to listOf("let", "help", "send", "find", "call", "check"),
        "let" to listOf("me", "us", "you", "them"),
        "can" to listOf("you", "I", "we", "it", "be"),
        "could" to listOf("you", "we", "I", "be"),
        "would" to listOf("you", "like", "be", "have"),
        "should" to listOf("we", "I", "be", "have"),
        "do" to listOf("you", "not", "we", "I"),
        "did" to listOf("you", "not", "he", "she"),
        "is" to listOf("there", "it", "this", "that", "he"),
        "are" to listOf("you", "there", "they", "we"),
        "will" to listOf("be", "you", "we", "call", "come"),
        "have" to listOf("a", "to", "you", "been", "done"),
        "good" to listOf("morning", "afternoon", "evening", "night", "luck", "job"),
        "happy" to listOf("birthday", "new", "to", "for"),
        "see" to listOf("you", "what", "it", "how"),
        "nice" to listOf("to", "meeting", "day", "one"),
        "sounds" to listOf("good", "great", "like", "awesome"),
        "sorry" to listOf("for", "about", "I", "to"),
        "hello" to listOf("everyone", "sir", "how", "friend", "there"),
        "hey" to listOf("there", "how", "what", "bro", "friend"),
        "hi" to listOf("there", "how", "all", "everyone"),
        "ok" to listOf("thanks", "sure", "got", "sounds"),
        "okay" to listOf("thanks", "sounds", "sure", "I"),
        "yes" to listOf("I", "sure", "please", "absolutely"),
        "no" to listOf("problem", "worries", "thanks", "I")
    )

    private fun areNeighbors(c1: Char, c2: Char): Boolean {
        if (c1 == c2) return true
        val lower1 = c1.lowercaseChar()
        val lower2 = c2.lowercaseChar()
        return QWERTY_NEIGHBORS[lower1]?.contains(lower2) == true
    }

    /**
     * Calculates Levenshtein edit distance with spatial keyboard awareness:
     * Tapping an adjacent physical QWERTY key incurs only 0.45 cost instead of 1.0.
     * Transposition of adjacent letters incurs only 0.5 cost.
     */
    fun calculateSpatialDistance(typed: String, candidate: String): Float {
        val s1 = typed.lowercase()
        val s2 = candidate.lowercase()
        val len1 = s1.length
        val len2 = s2.length
        if (Math.abs(len1 - len2) > 2) return 99f

        // Check transposition (e.g. "teh" -> "the", "adn" -> "and", "palce" -> "place")
        if (len1 == len2 && len1 >= 2) {
            for (i in 0 until len1 - 1) {
                if (s1[i] == s2[i + 1] && s1[i + 1] == s2[i] &&
                    s1.substring(0, i) == s2.substring(0, i) &&
                    s1.substring(i + 2) == s2.substring(i + 2)
                ) {
                    return 0.5f
                }
            }
        }

        val dp = Array(len1 + 1) { FloatArray(len2 + 1) }
        for (i in 0..len1) dp[i][0] = i * 1.0f
        for (j in 0..len2) dp[0][j] = j * 1.0f

        for (i in 1..len1) {
            val c1 = s1[i - 1]
            for (j in 1..len2) {
                val c2 = s2[j - 1]
                if (c1 == c2) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    val subCost = if (areNeighbors(c1, c2)) 0.45f else 1.0f
                    val insertCost = 0.85f
                    val deleteCost = 0.85f
                    dp[i][j] = minOf(
                        dp[i - 1][j - 1] + subCost,
                        dp[i - 1][j] + deleteCost,
                        dp[i][j - 1] + insertCost
                    )
                }
            }
        }
        return dp[len1][len2]
    }

    /**
     * Primary suggestion function with Next-Word Prediction and typo correction like Google Keyboard.
     * When currentPrefix is non-empty: provides completions and typo fixes.
     * When currentPrefix is empty: provides next-word predictions based on preceding text!
     */
    fun getSuggestions(currentPrefix: String, contextBefore: String = "", langCode: String = "en"): List<String> {
        val prefix = currentPrefix.trim()

        // CASE 1: Word completion & active typing
        if (prefix.isNotEmpty()) {
            val prefixLower = prefix.lowercase()
            val results = mutableListOf<String>()

            val langWords = (commonWords[langCode] ?: commonWords["en"] ?: emptyList())
            val allWords = (learnedWords + langWords).distinct()
            val isValidExactWord = allWords.any { it.equals(prefixLower, ignoreCase = true) }

            // 1. Direct typo exact match check (instant fix for typos)
            commonTypos[prefixLower]?.let {
                results.add(it)
            }

            // 2. If typed word is already valid, include it as primary choice
            if (isValidExactWord && !results.contains(prefixLower)) {
                results.add(prefixLower)
            }

            // 3. Spatial QWERTY fuzzy neighbor match for mistouches & transpositions
            if (prefixLower.length >= 2) {
                val fuzzyCandidates = allWords
                    .map { word -> word to calculateSpatialDistance(prefixLower, word) }
                    .filter { it.second <= 1.42f }
                    .sortedBy { it.second }
                    .map { it.first }

                for (cand in fuzzyCandidates) {
                    if (!results.contains(cand)) {
                        results.add(cand)
                    }
                    if (results.size >= 4) break
                }
            }

            // 4. Exact word prefix completions (e.g. "typ" -> "type", "typing")
            val prefixMatches = allWords
                .filter { it.lowercase().startsWith(prefixLower) }
                .sortedBy { it.length }

            for (match in prefixMatches) {
                if (!results.contains(match)) {
                    results.add(match)
                }
                if (results.size >= 5) break
            }

            // 5. Fill fallback with English if needed
            if (results.size < 3 && langCode != "en") {
                val general = (commonWords["en"] ?: emptyList()).filter { it.lowercase().startsWith(prefixLower) }
                results.addAll(general.take(3 - results.size))
            }

            // Preserve capitalization of input if user typed uppercase first letter
            val isCapitalized = prefix.firstOrNull()?.isUpperCase() == true
            val formatted = results.distinct().map { word ->
                if (isCapitalized && word.length > 1 && word.first().isLetter()) {
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                } else {
                    word
                }
            }

            return formatted.take(4)
        }

        // CASE 2: Next-Word Prediction when user pressed space or completed word
        val words = contextBefore.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val results = mutableListOf<String>()

        if (words.isNotEmpty()) {
            val lastWord = words.last().lowercase().replace(Regex("[^a-zA-Z0-9']"), "")
            val secondLastWord = if (words.size >= 2) {
                words[words.size - 2].lowercase().replace(Regex("[^a-zA-Z0-9']"), "")
            } else ""

            // Try trigram key: "how are" -> ["you", "things", "we"]
            if (secondLastWord.isNotEmpty()) {
                val trigramKey = "$secondLastWord $lastWord"
                NEXT_WORD_PREDICTIONS[trigramKey]?.let {
                    results.addAll(it)
                }
            }

            // Try bigram key: "how" -> ["are", "is", "about"]
            if (results.size < 4 && lastWord.isNotEmpty()) {
                NEXT_WORD_PREDICTIONS[lastWord]?.let { predictions ->
                    for (pred in predictions) {
                        if (!results.contains(pred)) {
                            results.add(pred)
                            if (results.size >= 4) break
                        }
                    }
                }
            }
        }

        // Default sentence starters if no predictions matched or context is blank
        if (results.isEmpty()) {
            results.addAll(
                when (langCode) {
                    "hi" -> listOf("नमस्ते", "आप", "धन्यवाद", "हाँ", "ठीक है")
                    "mr" -> listOf("नमस्कार", "तुम्ही", "धन्यवाद", "हो", "ठीक आहे")
                    "bn" -> listOf("হ্যালো", "আপনি", "ধন্যবাদ", "হ্যাঁ", "ভালো")
                    "es" -> listOf("Hola", "Cómo", "Gracias", "Por favor", "Bien")
                    "fr" -> listOf("Bonjour", "Merci", "Comment", "Oui", "S'il vous plaît")
                    else -> listOf("I", "Hello", "How", "Thank you", "Please", "What")
                }
            )
        }

        return results.distinct().take(4)
    }

    /**
     * Backward-compatible overload for existing callers.
     */
    fun getSuggestions(currentPrefix: String, langCode: String): List<String> {
        return getSuggestions(currentPrefix, "", langCode)
    }

    /**
     * Finds the best auto-correction candidate when user taps space after typing a mistouched word.
     */
    fun getAutoCorrection(word: String, langCode: String = "en"): String? {
        val lower = word.lowercase().trim()
        if (lower.length < 2) return null

        // 1. Check exact typo dictionary
        val typoMatch = commonTypos[lower]
        if (typoMatch != null) {
            val isCapitalized = word.firstOrNull()?.isUpperCase() == true
            return if (isCapitalized && typoMatch.length > 1) {
                typoMatch.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            } else {
                typoMatch
            }
        }

        // 2. If it's already a valid dictionary word or learned word, do NOT over-correct it
        val langWords = (commonWords[langCode] ?: commonWords["en"] ?: emptyList())
        if (langWords.any { it.equals(lower, ignoreCase = true) } || learnedWords.contains(lower)) {
            return null
        }

        // 3. Spatial QWERTY neighbor check
        if (lower.length >= 3) {
            val bestMatch = langWords
                .map { it to calculateSpatialDistance(lower, it) }
                .filter { it.second <= 1.35f }
                .minByOrNull { it.second }

            if (bestMatch != null) {
                val corrected = bestMatch.first
                val isCapitalized = word.firstOrNull()?.isUpperCase() == true
                return if (isCapitalized && corrected.length > 1) {
                    corrected.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                } else {
                    corrected
                }
            }
        }

        return null
    }
}
