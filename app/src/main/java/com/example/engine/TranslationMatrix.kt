package com.example.engine

import java.util.Locale

object TranslationMatrix {

    // Common typos and phonetic shorthand mappings
    private val typoNormalizations = mapOf(
        "gello" to "hello",
        "gelo" to "hello",
        "helo" to "hello",
        "hlo" to "hello",
        "hlw" to "hello",
        "hy" to "hi",
        "hye" to "hi",
        "hii" to "hi",
        "hiii" to "hi",
        "thx" to "thank you",
        "thnx" to "thank you",
        "thanx" to "thank you",
        "tq" to "thank you",
        "tysm" to "thank you",
        "ty" to "thank you",
        "pls" to "please",
        "plz" to "please",
        "gm" to "good morning",
        "gn" to "good night",
        "hru" to "how are you",
        "wlcm" to "welcome",
        "sry" to "sorry",
        "srry" to "sorry",
        "np" to "no problem",
        "tc" to "take care",
        "bbye" to "bye",
        "cya" to "see you",
        "namskar" to "namaskar",
        "namste" to "namaste",
        "dhnyavad" to "dhanyavad",
        "dhanyawad" to "dhanyavad",
        "shukriyaa" to "shukriya",
        "suprbhat" to "suprabhat"
    )

    // Conversational & Daily Life Phrases Matrix
    internal val phraseDictionary = mapOf(
        "hello" to mapOf(
            "en" to "Hello",
            "hi" to "नमस्ते",
            "mr" to "नमस्कार",
            "bn" to "হ্যালো",
            "gu" to "નમસ્તે",
            "ta" to "வணக்கம்",
            "te" to "నమస్కారం",
            "kn" to "ನಮಸ್ಕಾರ",
            "ml" to "നമസ്കാരം",
            "pa" to "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ",
            "ur" to "ہیلو",
            "es" to "Hola",
            "fr" to "Bonjour",
            "de" to "Hallo",
            "ja" to "こんにちは"
        ),
        "hi" to mapOf(
            "en" to "Hi",
            "hi" to "नमस्ते",
            "mr" to "नमस्कार",
            "bn" to "হাই",
            "gu" to "નમસ્તે",
            "ta" to "வணக்கம்",
            "te" to "హాయ్",
            "kn" to "ಹಾಯ್",
            "ml" to "ഹായ്",
            "pa" to "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ",
            "ur" to "سلام",
            "es" to "Hola",
            "fr" to "Salut",
            "de" to "Hallo",
            "ja" to "やあ"
        ),
        "good morning" to mapOf(
            "en" to "Good morning",
            "hi" to "शुभ प्रभात",
            "mr" to "शुभ सकाळ",
            "bn" to "সুপ্রভাত",
            "gu" to "શુભ સવાર",
            "ta" to "காலை வணக்கம்",
            "te" to "శుభోదయం",
            "kn" to "ಶುಭೋದಯ",
            "ml" to "സുപ്രഭാതം",
            "pa" to "ਸ਼ੁਭ ਸਵੇਰ",
            "ur" to "صبح بخیر",
            "es" to "Buenos días",
            "fr" to "Bonjour",
            "de" to "Guten Morgen",
            "ja" to "おはようございます"
        ),
        "good afternoon" to mapOf(
            "en" to "Good afternoon",
            "hi" to "शुभ दोपहर",
            "mr" to "शुभ दुपार",
            "bn" to "শুভ দুপুর",
            "gu" to "શુભ બપોર",
            "ta" to "மதிய வணக்கம்",
            "te" to "శుభ మధ్యాహ్నం",
            "kn" to "ಶುಭ ಮಧ್ಯಾಹ್ನ",
            "ml" to "ശുഭ ഉച്ചതിരിഞ്ഞ്",
            "pa" to "ਸ਼ੁਭ ਦੁਪਹਿਰ",
            "ur" to "سہ پہر بخیر",
            "es" to "Buenas tardes",
            "fr" to "Bon après-midi",
            "de" to "Guten Tag",
            "ja" to "こんにちは"
        ),
        "good evening" to mapOf(
            "en" to "Good evening",
            "hi" to "शुभ संध्या",
            "mr" to "शुभ संध्याकाळ",
            "bn" to "শুভ সন্ধ্যা",
            "gu" to "શુભ સાંજ",
            "ta" to "மாலை வணக்கம்",
            "te" to "శుభ సాయంత్రం",
            "kn" to "ಶುಭ ಸಂಜೆ",
            "ml" to "ശുഭ സായാഹ്നം",
            "pa" to "ਸ਼ੁਭ ਸ਼ਾਮ",
            "ur" to "شام بخیر",
            "es" to "Buenas tardes",
            "fr" to "Bonsoir",
            "de" to "Guten Abend",
            "ja" to "こんばんは"
        ),
        "good night" to mapOf(
            "en" to "Good night",
            "hi" to "शुभ रात्रि",
            "mr" to "शुभ रात्री",
            "bn" to "শুভ রাত্রি",
            "gu" to "શુભ રાત્રી",
            "ta" to "இனிய இரவு",
            "te" to "శుభ రాత్రి",
            "kn" to "ಶುಭ ರಾತ್ರಿ",
            "ml" to "ശുഭരാത്രി",
            "pa" to "ਸ਼ੁਭ ਰਾਤ",
            "ur" to "شب بخیر",
            "es" to "Buenas noches",
            "fr" to "Bonne nuit",
            "de" to "Gute Nacht",
            "ja" to "おやすみなさい"
        ),
        "thank you" to mapOf(
            "en" to "Thank you",
            "hi" to "धन्यवाद",
            "mr" to "धन्यवाद",
            "bn" to "ধন্যবাদ",
            "gu" to "આભાર",
            "ta" to "நன்றி",
            "te" to "ధన్యవాదాలు",
            "kn" to "ಧನ್ಯವಾದಗಳು",
            "ml" to "നന്ദി",
            "pa" to "ਧੰਨਵਾਦ",
            "ur" to "شکریہ",
            "es" to "Gracias",
            "fr" to "Merci",
            "de" to "Danke",
            "ja" to "ありがとうございます"
        ),
        "thanks" to mapOf(
            "en" to "Thanks",
            "hi" to "धन्यवाद",
            "mr" to "धन्यवाद",
            "bn" to "ধন্যবাদ",
            "gu" to "આભાર",
            "ta" to "நன்றி",
            "te" to "ధన్యవాదాలు",
            "kn" to "ಧನ್ಯವಾದಗಳು",
            "ml" to "നന്ദി",
            "pa" to "ਧੰਨਵਾਦ",
            "ur" to "شکریہ",
            "es" to "Gracias",
            "fr" to "Merci",
            "de" to "Danke",
            "ja" to "ありがとう"
        ),
        "how are you" to mapOf(
            "en" to "How are you?",
            "hi" to "आप कैसे हैं?",
            "mr" to "तुम्ही कसे आहात?",
            "bn" to "আপনি কেমন আছেন?",
            "gu" to "તમે કેમ છો?",
            "ta" to "எப்படி இருக்கிறீர்கள்?",
            "te" to "మీరు ఎలా ఉన్నారు?",
            "kn" to "ನೀವು ಹೇಗಿದ್ದೀರಿ?",
            "ml" to "സുഖമാണോ?",
            "pa" to "ਤੁਸੀਂ ਕਿਵੇਂ ਹੋ?",
            "ur" to "آپ کیسے ہیں؟",
            "es" to "¿Cómo estás?",
            "fr" to "Comment allez-vous?",
            "de" to "Wie geht es dir?",
            "ja" to "お元気ですか？"
        ),
        "how are you doing" to mapOf(
            "en" to "How are you doing?",
            "hi" to "आप कैसे हैं?",
            "mr" to "तुम्ही कसे आहात?",
            "bn" to "আপনি কেমন আছেন?",
            "gu" to "તમે કેમ છો?",
            "ta" to "எப்படி இருக்கிறீர்கள்?",
            "te" to "మీరు ఎలా ఉన్నారు?",
            "kn" to "ನೀವು ಹೇಗಿದ್ದೀರಿ?",
            "ml" to "സുഖമാണോ?",
            "pa" to "ਤੁਸੀਂ ਕਿਵੇਂ ਹੋ?",
            "ur" to "آپ کیسے ہیں؟",
            "es" to "¿Cómo te va?",
            "fr" to "Comment ça va?",
            "de" to "Wie geht's?",
            "ja" to "調子はどうですか？"
        ),
        "i am fine" to mapOf(
            "en" to "I am fine",
            "hi" to "मैं ठीक हूँ",
            "mr" to "मी ठीक आहे",
            "bn" to "আমি ভালো আছি",
            "gu" to "હું મજામાં છું",
            "ta" to "நான் நலமாக இருக்கிறேன்",
            "te" to "నేను బాగున్నాను",
            "kn" to "ನಾನು ಆರಾಮಾಗಿದ್ದೇನೆ",
            "ml" to "എനിക്ക് സുഖമാണ്",
            "pa" to "ਮੈਂ ਠੀਕ ਹਾਂ",
            "ur" to "میں ٹھیک ہوں",
            "es" to "Estoy bien",
            "fr" to "Je vais bien",
            "de" to "Mir geht es gut",
            "ja" to "私は元気です"
        ),
        "welcome" to mapOf(
            "en" to "Welcome",
            "hi" to "स्वागत है",
            "mr" to "स्वागत आहे",
            "bn" to "স্বাগতম",
            "gu" to "સ્વાગત છે",
            "ta" to "வரவேற்கிறோம்",
            "te" to "స్వాగతం",
            "kn" to "ಸ್ವಾಗತ",
            "ml" to "സ്വാഗതം",
            "pa" to "ਜੀ ਆਇਆਂ ਨੂੰ",
            "ur" to "خوش آمدید",
            "es" to "Bienvenido",
            "fr" to "Bienvenue",
            "de" to "Willkommen",
            "ja" to "ようこそ"
        ),
        "what is your name" to mapOf(
            "en" to "What is your name?",
            "hi" to "आपका नाम क्या है?",
            "mr" to "तुमचे नाव काय आहे?",
            "bn" to "আপনার নাম কী?",
            "gu" to "તમારું નામ શું છે?",
            "ta" to "உங்கள் பெயர் என்ன?",
            "te" to "మీ పేరు ఏమిటి?",
            "kn" to "ನಿಮ್ಮ ಹೆಸರೇನು?",
            "ml" to "നിങ്ങളുടെ പേരെന്താണ്?",
            "pa" to "ਤੁਹਾਡਾ ਨਾਮ ਕੀ ਹੈ?",
            "ur" to "آپ کا نام کیا ہے؟",
            "es" to "¿Cómo te llamas?",
            "fr" to "Comment vous appelez-vous?",
            "de" to "Wie heißt du?",
            "ja" to "お名前は何ですか？"
        ),
        "my name is" to mapOf(
            "en" to "My name is",
            "hi" to "मेरा नाम है",
            "mr" to "माझे नाव आहे",
            "bn" to "আমার নাম",
            "gu" to "મારું નામ છે",
            "ta" to "என் பெயர்",
            "te" to "నా పేరు",
            "kn" to "ನನ್ನ ಹೆಸರು",
            "ml" to "എന്റെ പേര്",
            "pa" to "ਮੇਰਾ ਨਾਮ ਹੈ",
            "ur" to "میرا نام ہے",
            "es" to "Mi nombre es",
            "fr" to "Je m'appelle",
            "de" to "Mein Name ist",
            "ja" to "私の名前は"
        ),
        "where are you going" to mapOf(
            "en" to "Where are you going?",
            "hi" to "आप कहाँ जा रहे हैं?",
            "mr" to "तुम्ही कुठे जात आहात?",
            "bn" to "আপনি কোথায় যাচ্ছেন?",
            "gu" to "તમે ક્યાં જાઓ છો?",
            "ta" to "நீங்கள் எங்கே போகிறீர்கள்?",
            "te" to "మీరు ఎక్కడికి వెళ్తున్నారు?",
            "kn" to "ನೀವು ಎಲ್ಲಿಗೆ ಹೋಗುತ್ತಿದ್ದೀರಿ?",
            "ml" to "നിങ്ങൾ എവിടെ പോകുന്നു?",
            "pa" to "ਤੁਸੀਂ ਕਿੱਥੇ ਜਾ ਰਹੇ ਹੋ?",
            "ur" to "آپ کہاں جا رہے ہیں؟",
            "es" to "¿A dónde vas?",
            "fr" to "Où vas-tu?",
            "de" to "Wohin gehst du?",
            "ja" to "どこに行きますか？"
        ),
        "what are you doing" to mapOf(
            "en" to "What are you doing?",
            "hi" to "आप क्या कर रहे हैं?",
            "mr" to "तुम्ही काय करत आहात?",
            "bn" to "আপনি কী করছেন?",
            "gu" to "તમે શું કરી રહ્યા છો?",
            "ta" to "நீங்கள் என்ன செய்கிறீர்கள்?",
            "te" to "మీరు ఏమి చేస్తున్నారు?",
            "kn" to "ನೀವು ಏನು ಮಾಡುತ್ತಿದ್ದೀರಿ?",
            "ml" to "നിങ്ങൾ എന്താണ് ചെയ്യുന്നത്?",
            "pa" to "ਤੁਸੀਂ ਕੀ ਕਰ ਰਹੇ ਹੋ?",
            "ur" to "آپ کیا کر رہے ہیں؟",
            "es" to "¿Qué estás haciendo?",
            "fr" to "Que fais-tu?",
            "de" to "Was machst du?",
            "ja" to "何をしていますか？"
        ),
        "i love you" to mapOf(
            "en" to "I love you",
            "hi" to "मैं तुमसे प्यार करता हूँ",
            "mr" to "माझं तुझ्यावर प्रेम आहे",
            "bn" to "আমি তোমাকে ভালবাসি",
            "gu" to "હું તમને પ્રેમ કરું છું",
            "ta" to "நான் உன்னை காதலிக்கிறேன்",
            "te" to "నేను నిన్ను ప్రేమిస్తున్నాను",
            "kn" to "ನಾನು ನಿನ್ನನ್ನು ಪ್ರೀತಿಸುತ್ತೇನೆ",
            "ml" to "ഞാൻ നിന്നെ സ്നേഹിക്കുന്നു",
            "pa" to "ਮੈਂ ਤੈਨੂੰ ਪਿਆਰ ਕਰਦਾ ਹਾਂ",
            "ur" to "میں تم سے پیار کرتا ہوں",
            "es" to "Te quiero",
            "fr" to "Je t'aime",
            "de" to "Ich liebe dich",
            "ja" to "愛しています"
        ),
        "congratulations" to mapOf(
            "en" to "Congratulations",
            "hi" to "बधाई हो",
            "mr" to "अभिनंदन",
            "bn" to "অভিনন্দন",
            "gu" to "અભિનંદન",
            "ta" to "வாழ்த்துக்கள்",
            "te" to "అభినందనలు",
            "kn" to "ಅಭಿನಂದನೆಗಳು",
            "ml" to "അഭിനന്ദനങ്ങൾ",
            "pa" to "ਮੁਬਾਰਕਾਂ",
            "ur" to "مبارک ہو",
            "es" to "Felicidades",
            "fr" to "Félicitations",
            "de" to "Herzlichen Glückwunsch",
            "ja" to "おめでとうございます"
        ),
        "see you soon" to mapOf(
            "en" to "See you soon",
            "hi" to "जल्द मिलते हैं",
            "mr" to "लवकरच भेटू",
            "bn" to "শীঘ্রই দেখা হবে",
            "gu" to "જલ્દી મળીશું",
            "ta" to "விரைவில் சந்திப்போம்",
            "te" to "త్వరలో కలుద్దాం",
            "kn" to "ಬೇಗ ಸಿಗೋಣ",
            "ml" to "ഉടൻ കാണാം",
            "pa" to "ਜਲਦੀ ਮਿਲਦੇ ਹਾਂ",
            "ur" to "جلد ملیں گے",
            "es" to "Hasta pronto",
            "fr" to "À bientôt",
            "de" to "Bis bald",
            "ja" to "また会いましょう"
        ),
        "bye" to mapOf(
            "en" to "Bye",
            "hi" to "अलविदा",
            "mr" to "बाय",
            "bn" to "বিদায়",
            "gu" to "આવજો",
            "ta" to "வருகிறேன்",
            "te" to "వీడ్కోలు",
            "kn" to "ಬೈ",
            "ml" to "വിട",
            "pa" to "ਅਲਵਿਦਾ",
            "ur" to "خدا حافظ",
            "es" to "Adiós",
            "fr" to "Au revoir",
            "de" to "Tschüss",
            "ja" to "さようなら"
        ),
        "goodbye" to mapOf(
            "en" to "Goodbye",
            "hi" to "अलविदा",
            "mr" to "निरोप",
            "bn" to "বিদায়",
            "gu" to "આવજો",
            "ta" to "போய் வருகிறேன்",
            "te" to "వీడ్కోలు",
            "kn" to "ವಿದಾಯ",
            "ml" to "വിട",
            "pa" to "ਅਲਵਿਦਾ",
            "ur" to "الوداع",
            "es" to "Adiós",
            "fr" to "Au revoir",
            "de" to "Auf Wiedersehen",
            "ja" to "さようなら"
        ),
        "take care" to mapOf(
            "en" to "Take care",
            "hi" to "अपना ख्याल रखना",
            "mr" to "काळजी घ्या",
            "bn" to "নিজের যত্ন নিও",
            "gu" to "કાળજી રાખજો",
            "ta" to "பார்த்துக் கொள்ளுங்கள்",
            "te" to "జాగ్రత్త",
            "kn" to "ಕಾಳಜಿ ವಹಿಸಿ",
            "ml" to "ശ്രദ്ധിക്കുക",
            "pa" to "ਆਪਣਾ ਖਿਆਲ ਰੱਖਣਾ",
            "ur" to "اپنا خیال رکھنا",
            "es" to "Cuídate",
            "fr" to "Prenez soin de vous",
            "de" to "Pass auf dich auf",
            "ja" to "気をつけて"
        ),
        "yes" to mapOf(
            "en" to "Yes",
            "hi" to "हाँ",
            "mr" to "हो",
            "bn" to "হ্যাঁ",
            "gu" to "હા",
            "ta" to "ஆம்",
            "te" to "అవును",
            "kn" to "ಹೌದು",
            "ml" to "അതെ",
            "pa" to "ਹਾਂ",
            "ur" to "ہاں",
            "es" to "Sí",
            "fr" to "Oui",
            "de" to "Ja",
            "ja" to "はい"
        ),
        "no" to mapOf(
            "en" to "No",
            "hi" to "नहीं",
            "mr" to "नाही",
            "bn" to "না",
            "gu" to "ના",
            "ta" to "இல்லை",
            "te" to "కాదు",
            "kn" to "ಇಲ್ಲ",
            "ml" to "അല്ല",
            "pa" to "ਨਹੀਂ",
            "ur" to "نہیں",
            "es" to "No",
            "fr" to "Non",
            "de" to "Nein",
            "ja" to "いいえ"
        ),
        "please" to mapOf(
            "en" to "Please",
            "hi" to "कृपया",
            "mr" to "कृपया",
            "bn" to "দয়া করে",
            "gu" to "કૃપા કરીને",
            "ta" to "தயவுசெய்து",
            "te" to "దయచేసి",
            "kn" to "ದಯವಿಟ್ಟು",
            "ml" to "ദയവായി",
            "pa" to "ਕਿਰਪਾ ਕਰਕੇ",
            "ur" to "براہ کرم",
            "es" to "Por favor",
            "fr" to "S'il vous plaît",
            "de" to "Bitte",
            "ja" to "お願いします"
        ),
        "sorry" to mapOf(
            "en" to "Sorry",
            "hi" to "माफ़ कीजिए",
            "mr" to "माफ करा",
            "bn" to "দুঃখিত",
            "gu" to "માફ કરશો",
            "ta" to "மன்னிக்கவும்",
            "te" to "క్షమించండి",
            "kn" to "ಕ್ಷಮಿಸಿ",
            "ml" to "ക്ഷമിക്കണം",
            "pa" to "ਮਾਫ਼ ਕਰਨਾ",
            "ur" to "معاف کیجئے",
            "es" to "Lo siento",
            "fr" to "Pardon",
            "de" to "Entschuldigung",
            "ja" to "ごめんなさい"
        ),
        "excuse me" to mapOf(
            "en" to "Excuse me",
            "hi" to "माफ़ कीजिए",
            "mr" to "माफ करा",
            "bn" to "মাফ করবেন",
            "gu" to "માફ કરશો",
            "ta" to "மன்னிக்கவும்",
            "te" to "క్షమించండి",
            "kn" to "ಕ್ಷಮಿಸಿ",
            "ml" to "ക്ഷമിക്കണം",
            "pa" to "ਮਾਫ਼ ਕਰਨਾ",
            "ur" to "معاف کیجئے",
            "es" to "Disculpe",
            "fr" to "Excusez-moi",
            "de" to "Entschuldigung",
            "ja" to "すみません"
        ),
        "ok" to mapOf(
            "en" to "OK",
            "hi" to "ठीक है",
            "mr" to "ठीक आहे",
            "bn" to "ঠিক আছে",
            "gu" to "બરાબર",
            "ta" to "சரி",
            "te" to "సరే",
            "kn" to "ಸರಿ",
            "ml" to "ശരി",
            "pa" to "ਠੀਕ ਹੈ",
            "ur" to "ٹھیک ہے",
            "es" to "Está bien",
            "fr" to "D'accord",
            "de" to "In Ordnung",
            "ja" to "わかりました"
        ),
        "okay" to mapOf(
            "en" to "Okay",
            "hi" to "ठीक है",
            "mr" to "ठीक आहे",
            "bn" to "ঠিক আছে",
            "gu" to "બરાબર",
            "ta" to "சரி",
            "te" to "సరే",
            "kn" to "ಸರಿ",
            "ml" to "ശരി",
            "pa" to "ਠੀਕ ਹੈ",
            "ur" to "ٹھیک ہے",
            "es" to "De acuerdo",
            "fr" to "D'accord",
            "de" to "Okay",
            "ja" to "オーケー"
        )
    )

    // Single words translation map
    internal val singleWordDictionary = mapOf(
        "water" to mapOf("hi" to "पानी", "mr" to "पाणी", "bn" to "জল", "gu" to "પાણી", "ta" to "தண்ணீர்", "te" to "నీరు", "kn" to "ನೀರು", "ml" to "വെള്ളം", "pa" to "ਪਾਣੀ", "ur" to "پانی", "es" to "agua", "fr" to "eau", "de" to "Wasser", "ja" to "水"),
        "food" to mapOf("hi" to "खाना", "mr" to "जेवण", "bn" to "খাবার", "gu" to "ખોરાક", "ta" to "உணவு", "te" to "ఆహారం", "kn" to "ಆಹಾರ", "ml" to "ഭക്ഷണം", "pa" to "ਭੋਜਨ", "ur" to "کھانا", "es" to "comida", "fr" to "nourriture", "de" to "Essen", "ja" to "食べ物"),
        "tea" to mapOf("hi" to "चाय", "mr" to "चहा", "bn" to "চা", "gu" to "ચા", "ta" to "தேநீர்", "te" to "టీ", "kn" to "ಟೀ", "ml" to "ചായ", "pa" to "ਚਾਹ", "ur" to "چائے", "es" to "té", "fr" to "thé", "de" to "Tee", "ja" to "お茶"),
        "coffee" to mapOf("hi" to "कॉफ़ी", "mr" to "कॉफी", "bn" to "কফি", "gu" to "કોફી", "ta" to "காபி", "te" to "కాఫీ", "kn" to "ಕಾಫಿ", "ml" to "കോഫി", "pa" to "ਕੌਫੀ", "ur" to "کافی", "es" to "café", "fr" to "café", "de" to "Kaffee", "ja" to "コーヒー"),
        "home" to mapOf("hi" to "घर", "mr" to "घर", "bn" to "বাড়ি", "gu" to "ઘર", "ta" to "வீடு", "te" to "ఇల్లు", "kn" to "ಮನೆ", "ml" to "വീട്", "pa" to "ਘਰ", "ur" to "گھر", "es" to "casa", "fr" to "maison", "de" to "Zuhause", "ja" to "家"),
        "house" to mapOf("hi" to "घर", "mr" to "घर", "bn" to "বাড়ি", "gu" to "ઘર", "ta" to "வீடு", "te" to "ఇల్లు", "kn" to "ಮನೆ", "ml" to "വീട്", "pa" to "ਘਰ", "ur" to "گھر", "es" to "casa", "fr" to "maison", "de" to "Haus", "ja" to "家"),
        "friend" to mapOf("hi" to "दोस्त", "mr" to "मित्र", "bn" to "বন্ধু", "gu" to "મિત્ર", "ta" to "நண்பன்", "te" to "స్నేహితుడు", "kn" to "ಸ್ನೇಹಿತ", "ml" to "സുഹൃത്ത്", "pa" to "ਦੋਸਤ", "ur" to "دوست", "es" to "amigo", "fr" to "ami", "de" to "Freund", "ja" to "友達"),
        "work" to mapOf("hi" to "काम", "mr" to "काम", "bn" to "কাজ", "gu" to "કામ", "ta" to "வேலை", "te" to "పని", "kn" to "ಕೆಲಸ", "ml" to "ജോലി", "pa" to "ਕੰਮ", "ur" to "کام", "es" to "trabajo", "fr" to "travail", "de" to "Arbeit", "ja" to "仕事"),
        "job" to mapOf("hi" to "नौकरी", "mr" to "नोकरी", "bn" to "চাকরি", "gu" to "નોકરી", "ta" to "வேலை", "te" to "ఉద్యోగం", "kn" to "ಉದ್ಯೋಗ", "ml" to "ജോലി", "pa" to "ਨੌਕਰੀ", "ur" to "نوکری", "es" to "trabajo", "fr" to "emploi", "de" to "Job", "ja" to "仕事"),
        "money" to mapOf("hi" to "पैसे", "mr" to "पैसे", "bn" to "টাকা", "gu" to "પૈસા", "ta" to "பணம்", "te" to "డబ్బు", "kn" to "ಹಣ", "ml" to "പണം", "pa" to "ਪੈਸੇ", "ur" to "پیسے", "es" to "dinero", "fr" to "argent", "de" to "Geld", "ja" to "お金"),
        "time" to mapOf("hi" to "समय", "mr" to "वेळ", "bn" to "সময়", "gu" to "સમય", "ta" to "நேரம்", "te" to "సమయం", "kn" to "ಸಮಯ", "ml" to "സമയം", "pa" to "ਸਮਾਂ", "ur" to "وقت", "es" to "tiempo", "fr" to "temps", "de" to "Zeit", "ja" to "時間"),
        "happy" to mapOf("hi" to "खुश", "mr" to "आनंदी", "bn" to "খুশি", "gu" to "ખુશ", "ta" to "மகிழ்ச்சி", "te" to "సంతోషం", "kn" to "ಸಂತೋಷ", "ml" to "സന്തോഷം", "pa" to "ਖੁਸ਼", "ur" to "خوش", "es" to "feliz", "fr" to "heureux", "de" to "glücklich", "ja" to "幸せ"),
        "love" to mapOf("hi" to "प्यार", "mr" to "प्रेम", "bn" to "ভালোবাসা", "gu" to "પ્રેમ", "ta" to "அன்பு", "te" to "ప్రేమ", "kn" to "ಪ್ರೀತಿ", "ml" to "സ്നേഹം", "pa" to "ਪਿਆਰ", "ur" to "محبت", "es" to "amor", "fr" to "amour", "de" to "Liebe", "ja" to "愛"),
        "beautiful" to mapOf("hi" to "सुंदर", "mr" to "सुंदर", "bn" to "সুন্দর", "gu" to "સુંદર", "ta" to "அழகான", "te" to "అందమైన", "kn" to "ಸುಂದರ", "ml" to "സുന്ദരമായ", "pa" to "ਸੁੰਦਰ", "ur" to "خوبصورت", "es" to "hermoso", "fr" to "beau", "de" to "schön", "ja" to "美しい"),
        "good" to mapOf("hi" to "अच्छा", "mr" to "चांगले", "bn" to "ভালো", "gu" to "સારું", "ta" to "நல்ல", "te" to "మంచి", "kn" to "ಒಳ್ಳೆಯದು", "ml" to "നല്ലത്", "pa" to "ਚੰਗਾ", "ur" to "اچھا", "es" to "bueno", "fr" to "bon", "de" to "gut", "ja" to "良い"),
        "bad" to mapOf("hi" to "बुरा", "mr" to "वाईट", "bn" to "খারাপ", "gu" to "ખરાબ", "ta" to "மோசமான", "te" to "చెడు", "kn" to "ಕೆಟ್ಟ", "ml" to "മോശം", "pa" to "ਬੁਰਾ", "ur" to "برا", "es" to "malo", "fr" to "mauvais", "de" to "schlecht", "ja" to "悪い"),
        "big" to mapOf("hi" to "बड़ा", "mr" to "मोठा", "bn" to "বড়", "gu" to "મોટું", "ta" to "பெரிய", "te" to "పెద్ద", "kn" to "ದೊಡ್ಡ", "ml" to "വലിയ", "pa" to "ਵੱਡਾ", "ur" to "بڑا", "es" to "grande", "fr" to "grand", "de" to "groß", "ja" to "大きい"),
        "small" to mapOf("hi" to "छोटा", "mr" to "लहान", "bn" to "ছোট", "gu" to "નાનું", "ta" to "சிறிய", "te" to "చిన్న", "kn" to "ಚಿಕ್ಕ", "ml" to "ചെറിയ", "pa" to "ਛੋਟਾ", "ur" to "چھوٹا", "es" to "pequeño", "fr" to "petit", "de" to "klein", "ja" to "小さい"),
        "today" to mapOf("hi" to "आज", "mr" to "आज", "bn" to "আজ", "gu" to "આજે", "ta" to "இன்று", "te" to "ఈ రోజు", "kn" to "ಇಂದು", "ml" to "ഇന്ന്", "pa" to "ਅੱਜ", "ur" to "آج", "es" to "hoy", "fr" to "aujourd'hui", "de" to "heute", "ja" to "今日"),
        "tomorrow" to mapOf("hi" to "कल", "mr" to "उद्या", "bn" to "কাল", "gu" to "આવતીકાલે", "ta" to "நாளை", "te" to "రేపు", "kn" to "ನಾಳೆ", "ml" to "നാളെ", "pa" to "ਕੱਲ੍ਹ", "ur" to "کل", "es" to "mañana", "fr" to "demain", "de" to "morgen", "ja" to "明日"),
        "yesterday" to mapOf("hi" to "कल", "mr" to "काल", "bn" to "গতকাল", "gu" to "ગઈકાલે", "ta" to "நேற்று", "te" to "నిన్న", "kn" to "ನಿನ್ನೆ", "ml" to "ഇന്നലെ", "pa" to "ਕੱਲ੍ਹ", "ur" to "کل", "es" to "ayer", "fr" to "hier", "de" to "gestern", "ja" to "昨日"),
        "day" to mapOf("hi" to "दिन", "mr" to "दिवस", "bn" to "দিন", "gu" to "દિવસ", "ta" to "நாள்", "te" to "రోజు", "kn" to "ದಿನ", "ml" to "ദിവസം", "pa" to "ਦਿਨ", "ur" to "دن", "es" to "día", "fr" to "jour", "de" to "Tag", "ja" to "日"),
        "night" to mapOf("hi" to "रात", "mr" to "रात्र", "bn" to "রাত", "gu" to "રાત", "ta" to "இரவு", "te" to "రాత్రి", "kn" to "ರಾತ್ರಿ", "ml" to "രാത്രി", "pa" to "ਰਾਤ", "ur" to "رات", "es" to "noche", "fr" to "nuit", "de" to "Nacht", "ja" to "夜"),
        "morning" to mapOf("hi" to "सुबह", "mr" to "सकाळ", "bn" to "সকাল", "gu" to "સવાર", "ta" to "காலை", "te" to "ఉదయం", "kn" to "ಬೆಳಗ್ಗೆ", "ml" to "രാവിലെ", "pa" to "ਸਵੇਰ", "ur" to "صبح", "es" to "mañana", "fr" to "matin", "de" to "Morgen", "ja" to "朝"),
        "mother" to mapOf("hi" to "माँ", "mr" to "आई", "bn" to "মা", "gu" to "માતા", "ta" to "அம்மா", "te" to "అమ్మ", "kn" to "ಅಮ್ಮ", "ml" to "അമ്മ", "pa" to "ਮਾਂ", "ur" to "ماں", "es" to "madre", "fr" to "mère", "de" to "Mutter", "ja" to "母"),
        "father" to mapOf("hi" to "पिता", "mr" to "बाबा", "bn" to "বাবা", "gu" to "પિતા", "ta" to "அப்பா", "te" to "నాన్న", "kn" to "ಅಪ್ಪ", "ml" to "അച്ഛൻ", "pa" to "ਪਿਤਾ", "ur" to "والد", "es" to "padre", "fr" to "père", "de" to "Vater", "ja" to "父"),
        "brother" to mapOf("hi" to "भाई", "mr" to "भाऊ", "bn" to "ভাই", "gu" to "ભાઈ", "ta" to "சகோதரன்", "te" to "సోదరుడు", "kn" to "ಸಹೋದರ", "ml" to "സഹോദരൻ", "pa" to "ਭਰਾ", "ur" to "بھائی", "es" to "hermano", "fr" to "frère", "de" to "Bruder", "ja" to "兄弟"),
        "sister" to mapOf("hi" to "बहन", "mr" to "बहीण", "bn" to "বোন", "gu" to "બહેન", "ta" to "சகோதரி", "te" to "సోదరి", "kn" to "ಸಹೋದರಿ", "ml" to "സഹോദരി", "pa" to "ਭੈਣ", "ur" to "بہن", "es" to "hermana", "fr" to "soeur", "de" to "Schwester", "ja" to "姉妹"),
        "family" to mapOf("hi" to "परिवार", "mr" to "कुटुंब", "bn" to "পরিবার", "gu" to "પરિવાર", "ta" to "குடும்பம்", "te" to "కుటుంబం", "kn" to "ಕುಟುಂಬ", "ml" to "കുടുംബം", "pa" to "ਪਰਿਵਾਰ", "ur" to "خاندان", "es" to "familia", "fr" to "famille", "de" to "Familie", "ja" to "家族"),
        "book" to mapOf("hi" to "किताब", "mr" to "पुस्तक", "bn" to "বই", "gu" to "પુસ્તક", "ta" to "புத்தகம்", "te" to "పుస్తకం", "kn" to "ಪುಸ್ತಕ", "ml" to "പുസ്തകം", "pa" to "ਕਿਤਾਬ", "ur" to "کتاب", "es" to "libro", "fr" to "livre", "de" to "Buch", "ja" to "本"),
        "car" to mapOf("hi" to "गाड़ी", "mr" to "गाडी", "bn" to "গাড়ি", "gu" to "ગાડી", "ta" to "கார்", "te" to "కారు", "kn" to "ಕಾರು", "ml" to "കാർ", "pa" to "ਕਾਰ", "ur" to "گاڑی", "es" to "coche", "fr" to "voiture", "de" to "Auto", "ja" to "車"),
        "phone" to mapOf("hi" to "फ़ोन", "mr" to "फोन", "bn" to "ফোন", "gu" to "ફોન", "ta" to "தொலைபேசி", "te" to "ఫోన్", "kn" to "ಫೋನ್", "ml" to "ഫോൺ", "pa" to "ਫੋਨ", "ur" to "فون", "es" to "teléfono", "fr" to "téléphone", "de" to "Telefon", "ja" to "電話"),
        "city" to mapOf("hi" to "शहर", "mr" to "शहर", "bn" to "শহর", "gu" to "શહેર", "ta" to "நகரம்", "te" to "నగరం", "kn" to "ನಗರ", "ml" to "നഗരം", "pa" to "ਸ਼ਹਿਰ", "ur" to "شہر", "es" to "ciudad", "fr" to "ville", "de" to "Stadt", "ja" to "街"),
        "country" to mapOf("hi" to "देश", "mr" to "देश", "bn" to "দেশ", "gu" to "દેશ", "ta" to "நாடு", "te" to "దేశం", "kn" to "ದೇಶ", "ml" to "രാജ്യം", "pa" to "ਦੇਸ਼", "ur" to "ملک", "es" to "país", "fr" to "pays", "de" to "Land", "ja" to "国"),
        "india" to mapOf("hi" to "भारत", "mr" to "भारत", "bn" to "ভারত", "gu" to "ભારત", "ta" to "இந்தியா", "te" to "భారతదేశం", "kn" to "ಭಾರತ", "ml" to "ഇന്ത്യ", "pa" to "ਭਾਰਤ", "ur" to "بھارت", "es" to "India", "fr" to "Inde", "de" to "Indien", "ja" to "インド"),
        "help" to mapOf("hi" to "मदद", "mr" to "मदत", "bn" to "সাহায্য", "gu" to "મદદ", "ta" to "உதவி", "te" to "సహాయం", "kn" to "ಸಹಾಯ", "ml" to "സഹായം", "pa" to "ਮਦਦ", "ur" to "مدد", "es" to "ayuda", "fr" to "aide", "de" to "Hilfe", "ja" to "助け"),
        "who" to mapOf("hi" to "कौन", "mr" to "कोण", "bn" to "কে", "gu" to "કોણ", "ta" to "யார்", "te" to "ఎవరు", "kn" to "ಯಾರು", "ml" to "ആര്", "pa" to "ਕੌਣ", "ur" to "کون", "es" to "quién", "fr" to "qui", "de" to "wer", "ja" to "誰"),
        "what" to mapOf("hi" to "क्या", "mr" to "काय", "bn" to "কী", "gu" to "શું", "ta" to "என்ன", "te" to "ఏమిటి", "kn" to "ಏನು", "ml" to "എന്ത്", "pa" to "ਕੀ", "ur" to "کیا", "es" to "qué", "fr" to "quoi", "de" to "was", "ja" to "何"),
        "where" to mapOf("hi" to "कहाँ", "mr" to "कुठे", "bn" to "কোথায়", "gu" to "ક્યાં", "ta" to "எங்கே", "te" to "ఎక్కడ", "kn" to "ಎಲ್ಲಿ", "ml" to "എവിടെ", "pa" to "ਕਿੱਥੇ", "ur" to "کہاں", "es" to "dónde", "fr" to "où", "de" to "wo", "ja" to "どこ"),
        "when" to mapOf("hi" to "कब", "mr" to "कधी", "bn" to "কখন", "gu" to "ક્યારે", "ta" to "எப்போது", "te" to "ఎప్పుడు", "kn" to "ಯಾವಾಗ", "ml" to "എപ്പോൾ", "pa" to "ਕਦੋਂ", "ur" to "کب", "es" to "cuándo", "fr" to "quand", "de" to "wann", "ja" to "いつ"),
        "why" to mapOf("hi" to "क्यों", "mr" to "का", "bn" to "কেন", "gu" to "કેમ", "ta" to "ஏன்", "te" to "ఎందుకు", "kn" to "ಏಕೆ", "ml" to "എന്തുകൊണ്ട്", "pa" to "ਕਿਉਂ", "ur" to "کیوں", "es" to "por qué", "fr" to "pourquoi", "de" to "warum", "ja" to "なぜ"),
        "how" to mapOf("hi" to "कैसे", "mr" to "कसे", "bn" to "কীভাবে", "gu" to "કેવી રીતે", "ta" to "எப்படி", "te" to "ఎలా", "kn" to "ಹೇಗೆ", "ml" to "എങ്ങനെ", "pa" to "ਕਿਵੇਂ", "ur" to "کیسے", "es" to "cómo", "fr" to "comment", "de" to "wie", "ja" to "どのように"),
        "come" to mapOf("hi" to "आओ", "mr" to "या", "bn" to "আসুন", "gu" to "આવો", "ta" to "வாருங்கள்", "te" to "రండి", "kn" to "ಬನ್ನಿ", "ml" to "വരൂ", "pa" to "ਆਓ", "ur" to "آؤ", "es" to "ven", "fr" to "viens", "de" to "komm", "ja" to "来て"),
        "go" to mapOf("hi" to "जाओ", "mr" to "जा", "bn" to "যান", "gu" to "જાઓ", "ta" to "போங்கள்", "te" to "వెళ్ళండి", "kn" to "ಹೋಗಿ", "ml" to "പോകൂ", "pa" to "ਜਾਓ", "ur" to "جاؤ", "es" to "ve", "fr" to "va", "de" to "geh", "ja" to "行って"),
        "eat" to mapOf("hi" to "खाओ", "mr" to "खा", "bn" to "খান", "gu" to "ખાઓ", "ta" to "சாப்பிடுங்கள்", "te" to "తినండి", "kn" to "ತಿನ್ನಿ", "ml" to "കഴിക്കുക", "pa" to "ਖਾਓ", "ur" to "کھاؤ", "es" to "come", "fr" to "mange", "de" to "iss", "ja" to "食べて"),
        "drink" to mapOf("hi" to "पीओ", "mr" to "प्या", "bn" to "পান করুন", "gu" to "પીવો", "ta" to "குடியுங்கள்", "te" to "త్రాగండి", "kn" to "ಕುಡಿಯಿರಿ", "ml" to "കുടിക്കുക", "pa" to "ਪੀਓ", "ur" to "پیو", "es" to "bebe", "fr" to "bois", "de" to "trink", "ja" to "飲んで"),
        "sleep" to mapOf("hi" to "सो जाओ", "mr" to "झोपा", "bn" to "ঘুমান", "gu" to "સુઈ જાઓ", "ta" to "தூங்குங்கள்", "te" to "పడుకోండి", "kn" to "ಮಲಗಿ", "ml" to "ഉറങ്ങുക", "pa" to "ਸੌਂ ਜਾਓ", "ur" to "سو جاؤ", "es" to "duerme", "fr" to "dors", "de" to "schlaf", "ja" to "寝て"),
        "see" to mapOf("hi" to "देखो", "mr" to "पहा", "bn" to "দেখুন", "gu" to "જુઓ", "ta" to "பாருங்கள்", "te" to "చూడండి", "kn" to "ನೋಡಿ", "ml" to "കാണുക", "pa" to "ਵੇਖੋ", "ur" to "دیکھو", "es" to "mira", "fr" to "regarde", "de" to "sieh", "ja" to "見て"),
        "speak" to mapOf("hi" to "बोलो", "mr" to "बोला", "bn" to "বলুন", "gu" to "બોલો", "ta" to "பேசுங்கள்", "te" to "మాట్లాడండి", "kn" to "ಮಾತನಾಡಿ", "ml" to "സംസാരിക്കുക", "pa" to "ਬੋਲੋ", "ur" to "بولو", "es" to "habla", "fr" to "parle", "de" to "sprich", "ja" to "話して")
    )

    // Romanized / Hinglish / Marathlish transliteration & lookup dictionary
    internal val romanizedIndicLookup = mapOf(
        "namaskar" to mapOf("hi" to "नमस्ते", "mr" to "नमस्कार", "en" to "Hello", "bn" to "হ্যালো", "gu" to "નમસ્તે", "ta" to "வணக்கம்", "es" to "Hola", "fr" to "Bonjour"),
        "namaste" to mapOf("hi" to "नमस्ते", "mr" to "नमस्कार", "en" to "Hello", "bn" to "হ্যালো", "gu" to "નમસ્તે", "ta" to "வணக்கம்", "es" to "Hola", "fr" to "Bonjour"),
        "dhanyavad" to mapOf("hi" to "धन्यवाद", "mr" to "धन्यवाद", "en" to "Thank you", "bn" to "ধন্যবাদ", "gu" to "આભાર", "es" to "Gracias", "fr" to "Merci"),
        "dhanyawad" to mapOf("hi" to "धन्यवाद", "mr" to "धन्यवाद", "en" to "Thank you", "bn" to "ধন্যবাদ", "gu" to "આભાર", "es" to "Gracias", "fr" to "Merci"),
        "shukriya" to mapOf("hi" to "धन्यवाद", "mr" to "धन्यवाद", "en" to "Thank you", "ur" to "شکریہ", "es" to "Gracias", "fr" to "Merci"),
        "kasa ahes" to mapOf("mr" to "कसे आहात?", "hi" to "कैसे हो?", "en" to "How are you?"),
        "kasa kay" to mapOf("mr" to "कसे काय?", "hi" to "कैसा चल रहा है?", "en" to "How is it going?"),
        "kase ahat" to mapOf("mr" to "तुम्ही कसे आहात?", "hi" to "आप कैसे हैं?", "en" to "How are you?"),
        "kay kartoy" to mapOf("mr" to "काय करतोय?", "hi" to "क्या कर रहे हो?", "en" to "What are you doing?"),
        "kay challay" to mapOf("mr" to "काय चाललंय?", "hi" to "क्या चल रहा है?", "en" to "What's going on?"),
        "kaise ho" to mapOf("hi" to "कैसे हो?", "mr" to "कसे आहात?", "en" to "How are you?"),
        "kaisa hai" to mapOf("hi" to "कैसा है?", "mr" to "कसा आहेस?", "en" to "How are you?"),
        "kya hal hai" to mapOf("hi" to "क्या हाल है?", "mr" to "काय चाललंय?", "en" to "How are you doing?"),
        "kya kar rahe ho" to mapOf("hi" to "आप क्या कर रहे हैं?", "mr" to "तुम्ही काय करत आहात?", "en" to "What are you doing?"),
        "thik ahe" to mapOf("mr" to "ठीक आहे", "hi" to "ठीक है", "en" to "Okay"),
        "theek hai" to mapOf("hi" to "ठीक है", "mr" to "ठीक आहे", "en" to "Okay"),
        "thik hai" to mapOf("hi" to "ठीक है", "mr" to "ठीक आहे", "en" to "Okay"),
        "barobar ahe" to mapOf("mr" to "बरोबर आहे", "hi" to "सही है", "en" to "That's right"),
        "suprabhat" to mapOf("hi" to "शुभ प्रभात", "mr" to "शुभ सकाळ", "en" to "Good morning"),
        "shubh sakal" to mapOf("mr" to "शुभ सकाळ", "hi" to "शुभ प्रभात", "en" to "Good morning"),
        "shubh ratri" to mapOf("mr" to "शुभ रात्री", "hi" to "शुभ रात्रि", "en" to "Good night"),
        "subh ratri" to mapOf("mr" to "शुभ रात्री", "hi" to "शुभ रात्रि", "en" to "Good night"),
        "alvida" to mapOf("hi" to "अलविदा", "mr" to "अलविदा", "en" to "Goodbye"),
        "swagat" to mapOf("hi" to "स्वागत है", "mr" to "स्वागत आहे", "en" to "Welcome"),
        "kripya" to mapOf("hi" to "कृपया", "mr" to "कृपया", "en" to "Please"),
        "pani" to mapOf("hi" to "पानी", "mr" to "पाणी", "en" to "Water"),
        "paani" to mapOf("hi" to "पानी", "mr" to "पाणी", "en" to "Water"),
        "ghar" to mapOf("hi" to "घर", "mr" to "घर", "en" to "Home"),
        "jevan" to mapOf("mr" to "जेवण", "hi" to "खाना", "en" to "Food"),
        "khana" to mapOf("hi" to "खाना", "mr" to "जेवण", "en" to "Food"),
        "chaha" to mapOf("mr" to "चहा", "hi" to "चाय", "en" to "Tea"),
        "chai" to mapOf("hi" to "चाय", "mr" to "चहा", "en" to "Tea"),
        "kaam" to mapOf("hi" to "काम", "mr" to "काम", "en" to "Work"),
        "paise" to mapOf("hi" to "पैसे", "mr" to "पैसे", "en" to "Money"),
        "paisa" to mapOf("hi" to "पैसा", "mr" to "पैसे", "en" to "Money"),
        "prem" to mapOf("mr" to "प्रेम", "hi" to "प्यार", "en" to "Love"),
        "pyar" to mapOf("hi" to "प्यार", "mr" to "प्रेम", "en" to "Love"),
        "mitra" to mapOf("mr" to "मित्र", "hi" to "दोस्त", "en" to "Friend"),
        "dost" to mapOf("hi" to "दोस्त", "mr" to "मित्र", "en" to "Friend"),
        "aaj" to mapOf("hi" to "आज", "mr" to "आज", "en" to "Today"),
        "udya" to mapOf("mr" to "उद्या", "hi" to "कल", "en" to "Tomorrow"),
        "kal" to mapOf("hi" to "कल", "mr" to "काल", "en" to "Yesterday"),
        "raat" to mapOf("hi" to "रात", "mr" to "रात्र", "en" to "Night"),
        "aai" to mapOf("mr" to "आई", "hi" to "माँ", "en" to "Mother"),
        "maa" to mapOf("hi" to "माँ", "mr" to "आई", "en" to "Mother"),
        "baba" to mapOf("mr" to "बाबा", "hi" to "पिता", "en" to "Father"),
        "bhau" to mapOf("mr" to "भाऊ", "hi" to "भाई", "en" to "Brother"),
        "bhai" to mapOf("hi" to "भाई", "mr" to "भाऊ", "en" to "Brother"),
        "bahin" to mapOf("mr" to "बहीण", "hi" to "बहन", "en" to "Sister"),
        "behen" to mapOf("hi" to "बहन", "mr" to "बहीण", "en" to "Sister"),
        "khup" to mapOf("mr" to "खूप", "hi" to "बहुत", "en" to "Very"),
        "bohot" to mapOf("hi" to "बहुत", "mr" to "खूप", "en" to "Very"),
        "sundar" to mapOf("hi" to "सुंदर", "mr" to "सुंदर", "en" to "Beautiful"),
        "chan" to mapOf("mr" to "छान", "hi" to "अच्छा", "en" to "Nice"),
        "accha" to mapOf("hi" to "अच्छा", "mr" to "छान", "en" to "Good"),
        "ho" to mapOf("mr" to "हो", "hi" to "हाँ", "en" to "Yes"),
        "haa" to mapOf("hi" to "हाँ", "mr" to "हो", "en" to "Yes"),
        "nahi" to mapOf("hi" to "नहीं", "mr" to "नाही", "en" to "No"),
        "naahi" to mapOf("mr" to "नाही", "hi" to "नहीं", "en" to "No"),
        "madat" to mapOf("mr" to "मदत", "hi" to "मदद", "en" to "Help"),
        "madad" to mapOf("hi" to "मदद", "mr" to "मदत", "en" to "Help")
    )

    fun translate(text: String, sourceLang: String, targetLang: String): String {
        if (text.isBlank()) return text
        val clean = text.trim()
        val normalizedSource = if (sourceLang.equals("auto", ignoreCase = true)) detectLanguage(clean) else sourceLang.lowercase()
        val normalizedTarget = targetLang.lowercase()

        if (normalizedSource == normalizedTarget) return clean

        // 1. In-memory cache hit from real Google Translate
        val cached = GoogleTranslationEngine.getCached(clean, normalizedSource, normalizedTarget)
            ?: GoogleTranslationEngine.getCached(clean, "auto", normalizedTarget)
        if (cached != null && cached.isNotBlank()) {
            return cached
        }

        // 2. Offline dictionary lookup
        val offline = translateOffline(clean, normalizedSource, normalizedTarget)
        if (offline != clean) {
            GoogleTranslationEngine.cacheResult(clean, normalizedSource, normalizedTarget, offline)
            return offline
        }

        // 3. Prefetch via Google Translate in background
        GoogleTranslationEngine.prefetch(clean, normalizedSource, normalizedTarget)
        return clean
    }

    suspend fun translateOnline(text: String, sourceLang: String, targetLang: String): String {
        return GoogleTranslationEngine.translate(text, sourceLang, targetLang)
    }

    fun translateOffline(text: String, sourceLang: String, targetLang: String): String {
        if (text.isBlank()) return text
        val clean = text.trim()
        val normalizedSource = if (sourceLang.equals("auto", ignoreCase = true)) detectLanguage(clean) else sourceLang.lowercase()
        val normalizedTarget = targetLang.lowercase()

        if (normalizedSource == normalizedTarget) return clean

        val lowerText = clean.lowercase(Locale.ROOT).replace(Regex("[?.!,]"), "").trim()
        val normalizedWord = typoNormalizations[lowerText] ?: lowerText

        // 1. Direct match in phrase dictionary
        for ((phraseKey, translations) in phraseDictionary) {
            if (normalizedWord == phraseKey || translations[normalizedSource]?.lowercase() == normalizedWord || translations["en"]?.lowercase() == normalizedWord) {
                val targetText = translations[normalizedTarget]
                if (targetText != null) return targetText
            }
        }

        // 2. Direct match in single word dictionary
        if (singleWordDictionary.containsKey(normalizedWord)) {
            val res = singleWordDictionary[normalizedWord]?.get(normalizedTarget)
            if (res != null) return res
        }

        // 3. Match in Romanized / Indic dictionary
        if (romanizedIndicLookup.containsKey(normalizedWord)) {
            val res = romanizedIndicLookup[normalizedWord]?.get(normalizedTarget)
            if (res != null) return res
            if (normalizedTarget in listOf("hi", "mr", "bn", "gu", "pa", "ta", "te", "kn", "ml", "ur")) {
                val indicRes = romanizedIndicLookup[normalizedWord]?.get("hi") ?: romanizedIndicLookup[normalizedWord]?.get("mr")
                if (indicRes != null) return indicRes
            }
        }

        // 4. Reverse lookup from non-English sources (Strictly direct: match sourceLang only)
        for ((engKey, trans) in singleWordDictionary) {
            if (trans[normalizedSource]?.equals(clean, ignoreCase = true) == true) {
                return if (normalizedTarget == "en") engKey else (trans[normalizedTarget] ?: clean)
            }
        }
        for ((phraseKey, trans) in phraseDictionary) {
            if (trans[normalizedSource]?.equals(clean, ignoreCase = true) == true) {
                return if (normalizedTarget == "en") phraseKey else (trans[normalizedTarget] ?: clean)
            }
        }

        // 5. Compound sentence / Word-by-word translation
        val words = clean.split(" ")
        if (words.size in 2..10) {
            val translatedWords = words.map { rawToken ->
                val punct = rawToken.filter { it in ".,!?" }
                val token = rawToken.filter { it !in ".,!?" }.lowercase()
                val normToken = typoNormalizations[token] ?: token
                val match = singleWordDictionary[normToken]?.get(normalizedTarget)
                    ?: romanizedIndicLookup[normToken]?.get(normalizedTarget)
                    ?: phraseDictionary[normToken]?.get(normalizedTarget)
                    ?: rawToken
                match + punct
            }
            if (translatedWords.any { it != words[translatedWords.indexOf(it)] }) {
                return translatedWords.joinToString(" ")
            }
        }

        // 6. Phonetic Indian Script transliteration fallback for Devanagari (Hindi/Marathi), Bengali, Gujarati, etc.
        if (normalizedTarget in listOf("hi", "mr", "bn", "gu", "pa", "ta", "te", "kn", "ml", "ur")) {
            val transliterated = transliterateToIndic(normalizedWord, normalizedTarget)
            if (transliterated.isNotBlank() && transliterated != normalizedWord) {
                return transliterated
            }
        }

        return clean
    }

    fun detectLanguage(text: String): String {
        if (text.isBlank()) return "en"
        for (char in text) {
            val code = char.code
            when (code) {
                in 0x0900..0x097F -> return "hi" // Devanagari (Hindi / Marathi)
                in 0x0980..0x09FF -> return "bn" // Bengali
                in 0x0A80..0x0AFF -> return "gu" // Gujarati
                in 0x0B80..0x0BFF -> return "ta" // Tamil
                in 0x0C00..0x0C7F -> return "te" // Telugu
                in 0x0C80..0x0CFF -> return "kn" // Kannada
                in 0x0D00..0x0D7F -> return "ml" // Malayalam
                in 0x0A00..0x0A7F -> return "pa" // Gurmukhi (Punjabi)
                in 0x0600..0x06FF -> return "ur" // Arabic/Urdu
                in 0x3040..0x30FF, in 0x4E00..0x9FAF -> return "ja" // Japanese
            }
        }
        val lower = text.lowercase()
        if (lower.contains("hola") || lower.contains("gracias") || lower.contains("bueno") || lower.contains("amigo")) return "es"
        if (lower.contains("bonjour") || lower.contains("merci") || lower.contains("salut")) return "fr"
        if (lower.contains("danke") || lower.contains("guten") || lower.contains("bitte")) return "de"
        return "en"
    }

    // Phonetic Indic Transliteration for Roman inputs
    private fun transliterateToIndic(input: String, targetLang: String): String {
        if (input.isBlank() || !input.all { it.isLetter() }) return input
        // Common Indian names and places fast mapping
        val namesMap = mapOf(
            "rahul" to "राहुल",
            "pooja" to "पूजा",
            "puja" to "पूजा",
            "amit" to "अमित",
            "neha" to "नेहा",
            "rohit" to "रोहित",
            "priya" to "प्रिया",
            "sachin" to "सचिन",
            "raj" to "राज",
            "vikas" to "विकास",
            "anita" to "अनिता",
            "mumbai" to "मुंबई",
            "pune" to "पुणे",
            "delhi" to "दिल्ली",
            "india" to "भारत",
            "bharat" to "भारत",
            "maharashtra" to "महाराष्ट्र",
            "namaskar" to "नमस्कार",
            "namaste" to "नमस्ते",
            "dhanyavad" to "धन्यवाद",
            "kasa" to "कसा",
            "ahes" to "आहेस",
            "kay" to "काय",
            "ahe" to "आहे",
            "hai" to "है",
            "kaisa" to "कैसा",
            "kaise" to "कैसे"
        )
        val lower = input.lowercase()
        if (namesMap.containsKey(lower)) {
            return namesMap[lower] ?: input
        }
        return input
    }
}
