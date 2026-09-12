package com.example.model

enum class ToneType(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val description: String,
    val example: String
) {
    PROFESSIONAL("professional", "Professional", "💼", "Refined, corporate-ready and polite tone", "Hello Sir, I will not be able to attend the office today."),
    FRIENDLY("friendly", "Friendly", "😊", "Warm, open and conversational tone", "Hey there! Hope you're doing great today."),
    CASUAL("casual", "Casual", "✌️", "Relaxed, easygoing vibe with friends", "Hey, caught up today. Let's catch up later!"),
    FORMAL("formal", "Formal", "📜", "Official, precise and traditional language", "Dear Mr. Smith, please find the document attached for your review."),
    POLITE("polite", "Polite", "🙏", "Courteous and deeply respectful phrasing", "Could you kindly assist me with this matter at your earliest convenience?"),
    CONFIDENT("confident", "Confident", "🚀", "Strong, decisive and persuasive statements", "I am confident this proposal will deliver exceptional value to our team."),
    FUNNY("funny", "Humorous", "😄", "Witty, playful and energetic message", "My coffee needs a coffee this morning!"),
    SHORTEN("shorten", "Shorten", "✂️", "Concise, punchy summary of text", "Cannot make it today. Will update soon."),
    EXPAND("expand", "Expand", "📖", "Elaborate in detail with rich phrasing", "I am writing to provide a detailed update regarding our ongoing project milestones.")
}
