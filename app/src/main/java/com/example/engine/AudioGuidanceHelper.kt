package com.example.engine

import com.example.model.Language

object AudioGuidanceHelper {

    fun getSplashAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "नमस्कार! ग्लोबल कीबोर्ड डायनॅमिक मध्ये आपले स्वागत आहे!"
            "hi" -> "नमस्ते! ग्लोबल कीबोर्ड डायनामिक में आपका स्वागत है!"
            "bn" -> "নমস্কার! গ্লোবাল কীবোর্ড ডায়নামিক এ আপনাকে স্বাগতম!"
            "ta" -> "வணக்கம்! குளோபல் விசைப்பலகை டைனமிக்கிற்கு வரவேற்கிறோம்!"
            "te" -> "నమస్కారం! గ్లోబల్ కీబోర్డ్ డైనమిక్ కి స్వాగతం!"
            else -> "Namaste! Welcome to Global Keyboard Dynamic!"
        }
    }

    fun getWelcomeAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "नमस्कार! ग्लोबल कीबोर्ड डायनॅमिक मध्ये आपले स्वागत आहे. येथे तुम्हाला सोपे फोनिटिक टायपिंग, व्हॉइस टायपिंग आणि एआय टूल्स मिळतील."
            "hi" -> "नमस्ते! ग्लोबल कीबोर्ड डायनामिक में आपका स्वागत है। यहां आपको आसान फोनिटिक टाइपिंग, बोलकर टाइपिंग और एआई टूल्स मिलेंगे।"
            else -> "Namaste! Welcome to Global Keyboard Dynamic. Enjoy fast typing, native transliteration, voice typing, and intelligent AI tools."
        }
    }

    fun getLanguageStepAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "आपली मुख्य भाषा निवडा. तुम्ही भारतीय आणि जागतिक भाषांमधून निवड करू शकता."
            "hi" -> "अपनी मुख्य भाषा चुनें। आप भारतीय और विश्व भाषाओं में से चुन सकते हैं।"
            else -> "Choose your preferred primary typing and transliteration language."
        }
    }

    fun getPermissionsAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "कीबोर्ड प्रायव्हसी: अँड्रॉइड सुरक्षा अलर्ट प्रत्येक कीबोर्डसाठी येतो. तुमचे सर्व टायपिंग सुरक्षित आहे आणि डिव्हाइसवरच राहते."
            "hi" -> "कीबोर्ड गोपनीयता: एंड्रॉइड सुरक्षा अलर्ट हर कीबोर्ड के लिए आता है। आपका सारा टाइपिंग सुरक्षित है और फोन पर ही रहता है।"
            else -> "Keyboard Privacy: Android shows a standard alert for all third-party keyboards. Your keystrokes are 100% private and processed locally on your device."
        }
    }

    fun getEnableStepAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "खालील इनेबल बटनावर क्लिक करा आणि ग्लोबल कीबोर्ड चालू करा."
            "hi" -> "नीचे दिए गए इनेबल बटन पर क्लिक करें और ग्लोबल कीबोर्ड चालू करें।"
            else -> "Tap the Enable Keyboard button below and turn on Global Keyboard Dynamic in settings."
        }
    }

    fun getSelectStepAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "खालील सिलेक्ट बटनावर क्लिक करा आणि ग्लोबल कीबोर्ड निवडा."
            "hi" -> "नीचे दिए गए सिलेक्ट बटन पर क्लिक करें और ग्लोबल कीबोर्ड चुनें।"
            else -> "Tap the Select Keyboard button below and choose Global Keyboard Dynamic."
        }
    }

    fun getReadyAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "कीबोर्ड तयार आहे! आता तुम्ही कोणत्याही ॲपमध्ये ग्लोबल कीबोर्ड वापरू शकता."
            "hi" -> "कीबोर्ड तैयार है! अब आप किसी भी ऐप में ग्लोबल कीबोर्ड इस्तेमाल कर सकते हैं।"
            else -> "Keyboard is ready! You can now use Global Keyboard Dynamic in any app."
        }
    }

    fun getTutorialAudioText(language: Language): String {
        return when (language.code) {
            "mr" -> "इंग्रजीत टाईप करा आणि मराठी शब्द मिळवा. कर्सर हलवण्यासाठी स्पेसबारवर उजवीकडे किंवा डावीकडे सरकवा."
            "hi" -> "अंग्रेजी में टाइप करें और हिंदी शब्द पाएं। कर्सर हिलाने के लिए स्पेसबार पर स्वाइप करें।"
            else -> "Type in English to get native transliteration, slide across the spacebar to move cursor, and tap mic for real-time voice typing."
        }
    }
}
