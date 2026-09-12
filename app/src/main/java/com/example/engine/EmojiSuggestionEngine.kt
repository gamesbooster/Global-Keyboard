package com.example.engine

import java.util.Locale

/**
 * Fast contextual emoji suggestion engine.
 * Maps typed words (English, Hindi, Hinglish, Bengali) to expressive emojis.
 */
object EmojiSuggestionEngine {

    private val KEYWORD_TO_EMOJIS: Map<String, List<String>> = mapOf(
        // Love, Affection, Romance
        "love" to listOf("❤️", "🥰", "😍", "💕"),
        "pyar" to listOf("❤️", "🥰", "😍", "💖"),
        "pyaar" to listOf("❤️", "🥰", "😍", "💖"),
        "prem" to listOf("❤️", "💕", "🌹"),
        "bhalobasha" to listOf("❤️", "🥰", "💕"),
        "heart" to listOf("❤️", "💖", "💘"),
        "dil" to listOf("❤️", "💓", "💘"),
        "kiss" to listOf("😘", "💋", "😚"),
        "miss" to listOf("🥺", "💔", "❤️"),
        "crush" to listOf("🙈", "😍", "💓"),
        "romantic" to listOf("🌹", "🕯️", "❤️"),

        // Happiness, Smiles, Laughter
        "happy" to listOf("😊", "😄", "🎉"),
        "khushi" to listOf("😊", "😄", "✨"),
        "khush" to listOf("😊", "🥳", "✨"),
        "anand" to listOf("😊", "✨", "🌸"),
        "smile" to listOf("😊", "🙂", "😁"),
        "laugh" to listOf("😂", "🤣", "😆"),
        "lol" to listOf("😂", "🤣", "💀"),
        "haha" to listOf("😂", "😆", "🤣"),
        "haso" to listOf("😄", "😂", "😁"),
        "mast" to listOf("🤩", "🔥", "😎"),
        "masti" to listOf("🥳", "🎉", "🤪"),
        "bindaas" to listOf("😎", "🤙", "🔥"),
        "yay" to listOf("🎉", "🥳", "🙌"),

        // Indian Banter, Chai, Food
        "chai" to listOf("☕", "🫖", "☕️"),
        "tea" to listOf("☕", "🫖", "🧋"),
        "coffee" to listOf("☕", "🤎", "☕️"),
        "khana" to listOf("🍛", "🍲", "😋"),
        "food" to listOf("🍔", "🍕", "🍛"),
        "roti" to listOf("🫓", "🍛", "😋"),
        "biryani" to listOf("🍛", "🍗", "🤤"),
        "samosa" to listOf("🥟", "😋", "☕"),
        "mithai" to listOf("🍬", "🥮", "🧁"),
        "chawal" to listOf("🍚", "🍛"),
        "pani" to listOf("💧", "🚰", "🥤"),
        "paani" to listOf("💧", "🥤"),
        "hungry" to listOf("🤤", "🍔", "🍕"),
        "bhook" to listOf("🤤", "🍛", "🍽️"),
        "party" to listOf("🥳", "🎉", "🍻"),

        // Greetings, Respect, Prayers
        "namaste" to listOf("🙏", "✨", "🌸"),
        "namaskar" to listOf("🙏", "🌸", "💐"),
        "pranam" to listOf("🙏", "🙇‍♂️", "✨"),
        "thanks" to listOf("🙏", "❤️", "😊"),
        "thank" to listOf("🙏", "❤️"),
        "dhanyawad" to listOf("🙏", "🌸"),
        "shukriya" to listOf("🙏", "💐"),
        "welcome" to listOf("🤗", "💐", "✨"),
        "swagat" to listOf("🌸", "💐", "🙏"),
        "bye" to listOf("👋", "🙋‍♂️", "✨"),
        "alvida" to listOf("👋", "🥺", "🌸"),
        "good morning" to listOf("🌅", "☀️", "🌻"),
        "subah" to listOf("🌅", "☀️", "☕"),
        "gm" to listOf("☀️", "🌅", "☕"),
        "good night" to listOf("🌙", "😴", "✨"),
        "raat" to listOf("🌙", "🌌", "😴"),
        "gn" to listOf("🌙", "😴", "✨"),
        "congrats" to listOf("🎉", "💐", "👏"),
        "badhai" to listOf("💐", "🎉", "👏"),
        "shabaash" to listOf("👏", "🏆", "🌟"),

        // Festivals & Celebrations
        "diwali" to listOf("🪔", "✨", "🎆"),
        "deepavali" to listOf("🪔", "🎇", "🥮"),
        "holi" to listOf("🎨", "🌈", "🥳"),
        "eid" to listOf("🌙", "✨", "🕌"),
        "rakhi" to listOf("🧵", "🎁", "👫"),
        "puja" to listOf("🪔", "🌸", "🔔"),
        "independence" to listOf("🇮🇳", "🫡", "🎆"),
        "republic" to listOf("🇮🇳", "🫡", "🎖️"),
        "bharat" to listOf("🇮🇳", "🚩", "✨"),
        "india" to listOf("🇮🇳", "🏏", "✨"),

        // Emotions: Sad, Crying, Angry, Shocked
        "sad" to listOf("😢", "😔", "💔"),
        "dukh" to listOf("😢", "💔", "😞"),
        "cry" to listOf("😭", "😢", "🥺"),
        "rona" to listOf("😭", "😢", "🥺"),
        "tears" to listOf("😭", "😢"),
        "sorry" to listOf("🥺", "🙏", "😔"),
        "maaf" to listOf("🙏", "🥺", "😔"),
        "angry" to listOf("😡", "🤬", "😤"),
        "gussa" to listOf("😡", "😤", "💢"),
        "shock" to listOf("😱", "😳", "😲"),
        "omg" to listOf("😱", "🤯", "😮"),
        "wow" to listOf("🤩", "😮", "✨"),
        "kya baat" to listOf("👏", "🔥", "🤩"),
        "arre wah" to listOf("👏", "🤩", "✨"),

        // Cool, Fire, Money, Success
        "cool" to listOf("😎", "🤙", "❄️"),
        "fire" to listOf("🔥", "💥", "⚡"),
        "lit" to listOf("🔥", "✨", "🚀"),
        "aag" to listOf("🔥", "🧨", "💥"),
        "zabardast" to listOf("🔥", "⚡", "🌟"),
        "money" to listOf("💰", "💵", "🤑"),
        "paisa" to listOf("💰", "💸", "🤑"),
        "rupee" to listOf("₹", "💰", "💳"),
        "cash" to listOf("💵", "💰"),
        "rich" to listOf("🤑", "💎", "👑"),
        "work" to listOf("💼", "💻", "📊"),
        "kaam" to listOf("💻", "📝", "🏢"),
        "office" to listOf("🏢", "💼", "💻"),
        "study" to listOf("📚", "📖", "✍️"),
        "padhai" to listOf("📚", "✏️", "📖"),

        // Travel, Vehicles, Sports
        "car" to listOf("🚗", "🏎️", "🚙"),
        "gaadi" to listOf("🚗", "🚙", "🏍️"),
        "bike" to listOf("🏍️", "🛵", "🚲"),
        "flight" to listOf("✈️", "🛫", "🧳"),
        "plane" to listOf("✈️", "🛫"),
        "travel" to listOf("🧳", "✈️", "🌍"),
        "cricket" to listOf("🏏", "🏆", "⚾"),
        "match" to listOf("🏏", "⚽", "🏆"),
        "ipl" to listOf("🏏", "🏆", "🔥"),
        "football" to listOf("⚽", "🥅"),
        "game" to listOf("🎮", "🕹️", "🏆"),
        "gym" to listOf("💪", "🏋️‍♂️", "🔥"),
        "fitness" to listOf("💪", "🏃‍♂️", "🥗"),

        // Approvals & Quick Reactions
        "ok" to listOf("👍", "👌", "✅"),
        "okay" to listOf("👍", "👌", "✅"),
        "theek" to listOf("👍", "👌", "✅"),
        "thik" to listOf("👍", "👌", "✅"),
        "yes" to listOf("✅", "🙌", "👍"),
        "haan" to listOf("👍", "✅", "🙌"),
        "no" to listOf("❌", "🙅‍♂️", "🚫"),
        "nahi" to listOf("❌", "🙅‍♂️", "🚫"),
        "done" to listOf("✅", "🎉", "👍"),
        "sure" to listOf("👍", "👌", "💯"),
        "perfect" to listOf("👌", "✨", "💯"),
        "super" to listOf("🌟", "✨", "🚀"),
        "best" to listOf("🥇", "🏆", "🌟")
    )

    /**
     * Returns contextual emojis matching the typed prefix or word.
     */
    fun getMatchingEmojis(word: String): List<String> {
        val clean = word.trim().lowercase(Locale.ROOT)
        if (clean.isBlank()) return emptyList()

        // 1. Direct match
        val direct = KEYWORD_TO_EMOJIS[clean]
        if (direct != null) return direct

        // 2. Prefix match
        for ((k, emojis) in KEYWORD_TO_EMOJIS) {
            if (clean.length >= 3 && (k.startsWith(clean) || clean.startsWith(k))) {
                return emojis
            }
        }

        return emptyList()
    }
}
