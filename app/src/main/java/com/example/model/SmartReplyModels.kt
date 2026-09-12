package com.example.model

import org.json.JSONObject

/**
 * Domain models for Smart Reply feature in Global MKeyboard Dynamic.
 * Includes a rich, expandable Reply Style & Tone library with categorized styles,
 * purpose-based intentions, and length constraints.
 */

enum class SmartReplyStyleCategory(val id: String, val displayName: String, val iconEmoji: String) {
    SMART_AUTO("smart_auto", "Smart & Auto", "🧠"),
    GENERAL("general", "General", "💬"),
    PROFESSIONAL("professional", "Professional", "💼"),
    POSITIVE_WARM("positive_warm", "Positive & Warm", "🥰"),
    SOCIAL_FUN("social_fun", "Social & Fun", "🎉"),
    RESPECT_COMMUNICATION("respect_comm", "Respect", "🙏"),
    STRONG_COMMUNICATION("strong_comm", "Strong", "💪"),
    RELATIONSHIP("relationship", "Relationship", "💕"),
    RESPONSE_PURPOSE("response_purpose", "Purpose", "🎯"),
    LENGTH("length", "Length", "📏")
}

data class SmartReplyStyle(
    val id: String,
    val displayName: String,
    val emoji: String,
    val category: SmartReplyStyleCategory,
    val promptDescription: String,
    val structuredTone: String? = null,
    val structuredPurpose: String? = null,
    val structuredLength: String? = null
) {
    val fullLabel: String
        get() = "$emoji $displayName"

    fun toStructuredJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("styleName", displayName)
        obj.put("category", category.id)
        structuredTone?.let { obj.put("tone", it) }
        structuredPurpose?.let { obj.put("purpose", it) }
        structuredLength?.let { obj.put("length", it) }
        return obj
    }
}

object SmartReplyLibrary {
    // SMART / AUTO (Default is Smart Match!)
    val SMART_MATCH = SmartReplyStyle(
        id = "smart_match",
        displayName = "Smart Match",
        emoji = "🧠",
        category = SmartReplyStyleCategory.SMART_AUTO,
        promptDescription = "Automatically analyze the message intent, relationship context, and tone to generate the most fitting, natural response.",
        structuredTone = "auto",
        structuredPurpose = "auto",
        structuredLength = "auto"
    )

    val AUTO = SmartReplyStyle(
        id = "auto",
        displayName = "Auto",
        emoji = "🤖",
        category = SmartReplyStyleCategory.SMART_AUTO,
        promptDescription = "Adaptive reply matching the sender's language, dialect, and emotional tone.",
        structuredTone = "auto"
    )

    val MATCH_MESSAGE_STYLE = SmartReplyStyle(
        id = "match_message_style",
        displayName = "Match Message Style",
        emoji = "🌐",
        category = SmartReplyStyleCategory.SMART_AUTO,
        promptDescription = "Closely mirror the exact style, formality, enthusiasm, and slang used in the incoming message.",
        structuredTone = "mirror"
    )

    // GENERAL
    val CASUAL = SmartReplyStyle(
        id = "casual",
        displayName = "Casual",
        emoji = "💬",
        category = SmartReplyStyleCategory.GENERAL,
        promptDescription = "Relaxed, informal, natural, and friendly conversational phrasing.",
        structuredTone = "casual"
    )

    val FRIENDLY = SmartReplyStyle(
        id = "friendly",
        displayName = "Friendly",
        emoji = "😊",
        category = SmartReplyStyleCategory.GENERAL,
        promptDescription = "Warm, welcoming, cheerful, kind, and pleasant.",
        structuredTone = "friendly"
    )

    val COOL = SmartReplyStyle(
        id = "cool",
        displayName = "Cool",
        emoji = "😎",
        category = SmartReplyStyleCategory.GENERAL,
        promptDescription = "Effortlessly smooth, chilled, calm, confident, and trendy.",
        structuredTone = "cool"
    )

    val NATURAL = SmartReplyStyle(
        id = "natural",
        displayName = "Natural",
        emoji = "✨",
        category = SmartReplyStyleCategory.GENERAL,
        promptDescription = "Authentic everyday language without sounding scripted or artificial.",
        structuredTone = "natural"
    )

    val SIMPLE = SmartReplyStyle(
        id = "simple",
        displayName = "Simple",
        emoji = "👋",
        category = SmartReplyStyleCategory.GENERAL,
        promptDescription = "Clear, uncomplicated, straightforward words with zero fluff.",
        structuredTone = "simple"
    )

    val HELPFUL = SmartReplyStyle(
        id = "helpful",
        displayName = "Helpful",
        emoji = "🤝",
        category = SmartReplyStyleCategory.GENERAL,
        promptDescription = "Constructive, supportive, cooperative, and offering practical assistance.",
        structuredTone = "helpful"
    )

    val RELAXED = SmartReplyStyle(
        id = "relaxed",
        displayName = "Relaxed",
        emoji = "🌿",
        category = SmartReplyStyleCategory.GENERAL,
        promptDescription = "Low pressure, chilled, easygoing, reassuring that there is no rush.",
        structuredTone = "relaxed"
    )

    // PROFESSIONAL
    val PROFESSIONAL = SmartReplyStyle(
        id = "professional",
        displayName = "Professional",
        emoji = "💼",
        category = SmartReplyStyleCategory.PROFESSIONAL,
        promptDescription = "Polite, structured, courteous, and appropriate for workplace and business interactions.",
        structuredTone = "professional"
    )

    val BUSINESS = SmartReplyStyle(
        id = "business",
        displayName = "Business",
        emoji = "🏢",
        category = SmartReplyStyleCategory.PROFESSIONAL,
        promptDescription = "Commercially minded, results-oriented, professional, and clear.",
        structuredTone = "business"
    )

    val FORMAL = SmartReplyStyle(
        id = "formal",
        displayName = "Formal",
        emoji = "📧",
        category = SmartReplyStyleCategory.PROFESSIONAL,
        promptDescription = "Traditional, highly polished, dignified syntax with proper etiquette.",
        structuredTone = "formal"
    )

    val DIRECT = SmartReplyStyle(
        id = "direct",
        displayName = "Direct",
        emoji = "🎯",
        category = SmartReplyStyleCategory.PROFESSIONAL,
        promptDescription = "Straight to the point with zero preamble or unnecessary pleasantries.",
        structuredTone = "direct"
    )

    val CORPORATE = SmartReplyStyle(
        id = "corporate",
        displayName = "Corporate",
        emoji = "🤝",
        category = SmartReplyStyleCategory.PROFESSIONAL,
        promptDescription = "Diplomatic, cross-functional team aligned, enterprise communication style.",
        structuredTone = "corporate"
    )

    val EXECUTIVE = SmartReplyStyle(
        id = "executive",
        displayName = "Executive",
        emoji = "👔",
        category = SmartReplyStyleCategory.PROFESSIONAL,
        promptDescription = "High-level, decisive, leadership-oriented, strategic, and authoritative.",
        structuredTone = "executive"
    )

    val WORK_APPROPRIATE = SmartReplyStyle(
        id = "work_appropriate",
        displayName = "Work Appropriate",
        emoji = "📋",
        category = SmartReplyStyleCategory.PROFESSIONAL,
        promptDescription = "Strictly safe, neutral, respectful, and standard office-appropriate phrasing.",
        structuredTone = "work_appropriate"
    )

    // POSITIVE & WARM
    val ROMANTIC = SmartReplyStyle(
        id = "romantic",
        displayName = "Romantic",
        emoji = "❤️",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Tender, romantic, emotionally intimate, sweet, and caring.",
        structuredTone = "romantic"
    )

    val LOVING = SmartReplyStyle(
        id = "loving",
        displayName = "Loving",
        emoji = "💕",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Deeply affectionate, warmhearted, devoted, and loving.",
        structuredTone = "loving"
    )

    val CARING = SmartReplyStyle(
        id = "caring",
        displayName = "Caring",
        emoji = "🥰",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Thoughtful, attentive, asking after recipient's comfort and well-being.",
        structuredTone = "caring"
    )

    val SWEET = SmartReplyStyle(
        id = "sweet",
        displayName = "Sweet",
        emoji = "🌸",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Gentle, endearing, cute, lovely, and heartwarming.",
        structuredTone = "sweet"
    )

    val CHEERFUL = SmartReplyStyle(
        id = "cheerful",
        displayName = "Cheerful",
        emoji = "😊",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Sunny, joyful, optimistic, bright, and smiling tone.",
        structuredTone = "cheerful"
    )

    val GRATEFUL = SmartReplyStyle(
        id = "grateful",
        displayName = "Grateful",
        emoji = "🙏",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Full of genuine gratitude, appreciative, acknowledging recipient's kindness.",
        structuredTone = "grateful",
        structuredPurpose = "thank"
    )

    val ENCOURAGING = SmartReplyStyle(
        id = "encouraging",
        displayName = "Encouraging",
        emoji = "🌟",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Inspiring, uplifting, motivating, boosting morale and confidence.",
        structuredTone = "encouraging"
    )

    val SUPPORTIVE = SmartReplyStyle(
        id = "supportive",
        displayName = "Supportive",
        emoji = "🤗",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Reassuring, standing in solidarity, offering emotional support.",
        structuredTone = "supportive"
    )

    val AFFECTIONATE = SmartReplyStyle(
        id = "affectionate",
        displayName = "Affectionate",
        emoji = "💖",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Fond, expressing heartfelt fondness, closeness, and warmth.",
        structuredTone = "affectionate"
    )

    val WARM = SmartReplyStyle(
        id = "warm",
        displayName = "Warm",
        emoji = "☀️",
        category = SmartReplyStyleCategory.POSITIVE_WARM,
        promptDescription = "Radiant, compassionate, inviting, gentle, and empathetic warmth.",
        structuredTone = "warm"
    )

    // SOCIAL & FUN
    val FUNNY = SmartReplyStyle(
        id = "funny",
        displayName = "Funny",
        emoji = "😂",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Humorous, witty, funny jokes, lighthearted comedic touch.",
        structuredTone = "funny"
    )

    val PLAYFUL = SmartReplyStyle(
        id = "playful",
        displayName = "Playful",
        emoji = "😜",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Light teasing in good fun, bouncy, lively, and energetic.",
        structuredTone = "playful"
    )

    val WITTY = SmartReplyStyle(
        id = "witty",
        displayName = "Witty",
        emoji = "🤪",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Sharp wit, clever phrasing, clever comeback, smart humor.",
        structuredTone = "witty"
    )

    val SARCASTIC = SmartReplyStyle(
        id = "sarcastic",
        displayName = "Sarcastic",
        emoji = "😏",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Dry humor, cheeky irony, gentle playful sarcasm without cruelty.",
        structuredTone = "sarcastic"
    )

    val BOLD = SmartReplyStyle(
        id = "bold",
        displayName = "Bold",
        emoji = "🔥",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Spirited, gutsy, direct, daring, and unapologetically confident.",
        structuredTone = "bold"
    )

    val CONFIDENT = SmartReplyStyle(
        id = "confident",
        displayName = "Confident",
        emoji = "😎",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Self-assured, poised, positive certainty with calm conviction.",
        structuredTone = "confident"
    )

    val EXCITED = SmartReplyStyle(
        id = "excited",
        displayName = "Excited",
        emoji = "🎉",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Thrilled, pumped up, high excitement and eager anticipation!",
        structuredTone = "excited"
    )

    val CELEBRATORY = SmartReplyStyle(
        id = "celebratory",
        displayName = "Celebratory",
        emoji = "🥳",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Festive, cheering on a milestone, celebratory congratulatory energy.",
        structuredTone = "celebratory",
        structuredPurpose = "congratulate"
    )

    val ENTHUSIASTIC = SmartReplyStyle(
        id = "enthusiastic",
        displayName = "Enthusiastic",
        emoji = "🤩",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Very eager, passionate, genuine enthusiasm and interest.",
        structuredTone = "enthusiastic"
    )

    val CHILL = SmartReplyStyle(
        id = "chill",
        displayName = "Chill",
        emoji = "🧊",
        category = SmartReplyStyleCategory.SOCIAL_FUN,
        promptDescription = "Completely relaxed, mellow, unbothered, easygoing vibe.",
        structuredTone = "chill"
    )

    // RESPECT & COMMUNICATION
    val POLITE = SmartReplyStyle(
        id = "polite",
        displayName = "Polite",
        emoji = "🙏",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Courteous, well-mannered, considerate, using polite expressions.",
        structuredTone = "polite"
    )

    val RESPECTFUL = SmartReplyStyle(
        id = "respectful",
        displayName = "Respectful",
        emoji = "🫡",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Deferential, acknowledging seniority, deeply civil and appreciative.",
        structuredTone = "respectful"
    )

    val CALM = SmartReplyStyle(
        id = "calm",
        displayName = "Calm",
        emoji = "🌿",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Steady, peaceful, de-escalating, serene, and level-headed.",
        structuredTone = "calm"
    )

    val DIPLOMATIC = SmartReplyStyle(
        id = "diplomatic",
        displayName = "Diplomatic",
        emoji = "🧘",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Tactful, balanced, bridge-building, resolving differences gracefully.",
        structuredTone = "diplomatic"
    )

    val UNDERSTANDING = SmartReplyStyle(
        id = "understanding",
        displayName = "Understanding",
        emoji = "🤝",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Validating the sender's viewpoint, showing tolerance and comprehension.",
        structuredTone = "understanding"
    )

    val EMPATHETIC = SmartReplyStyle(
        id = "empathetic",
        displayName = "Empathetic",
        emoji = "💙",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Emotionally compassionate, validating feelings with heartfelt care.",
        structuredTone = "empathetic"
    )

    val PEACEFUL = SmartReplyStyle(
        id = "peaceful",
        displayName = "Peaceful",
        emoji = "🕊️",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Harmonious, gentle, non-confrontational, quiet, and tranquil.",
        structuredTone = "peaceful"
    )

    val ASSERTIVE_COMM = SmartReplyStyle(
        id = "assertive_comm",
        displayName = "Assertive",
        emoji = "🗣️",
        category = SmartReplyStyleCategory.RESPECT_COMMUNICATION,
        promptDescription = "Open, honest self-expression with respect for self and other.",
        structuredTone = "assertive"
    )

    // STRONG COMMUNICATION
    val ASSERTIVE = SmartReplyStyle(
        id = "assertive",
        displayName = "Assertive",
        emoji = "💪",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "Confident, resolute stance with definitive clarity and conviction.",
        structuredTone = "assertive"
    )

    val FIRM = SmartReplyStyle(
        id = "firm",
        displayName = "Firm",
        emoji = "⚡",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "Uncompromising, decisive, unwavering clarity, leaving no ambiguity.",
        structuredTone = "firm"
    )

    val STRAIGHTFORWARD = SmartReplyStyle(
        id = "straightforward",
        displayName = "Straightforward",
        emoji = "🎯",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "Direct, transparent, candid, cutting straight to the essence.",
        structuredTone = "straightforward"
    )

    val BOUNDARY_SETTING = SmartReplyStyle(
        id = "boundary_setting",
        displayName = "Boundary Setting",
        emoji = "🛑",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "Clear personal or professional boundaries, politely non-negotiable limits.",
        structuredTone = "boundary_setting"
    )

    val AGGRESSIVE = SmartReplyStyle(
        id = "aggressive",
        displayName = "Aggressive",
        emoji = "🔥",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "High-urgency, unyielding, decisive, forceful boundary communication (strictly safe, no abusive language, threats, or harassment).",
        structuredTone = "aggressive"
    )

    val COLD = SmartReplyStyle(
        id = "cold",
        displayName = "Cold",
        emoji = "🧊",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "Emotionally distant, detached, clipped, minimal words, purely transactional.",
        structuredTone = "cold"
    )

    val SERIOUS = SmartReplyStyle(
        id = "serious",
        displayName = "Serious",
        emoji = "😐",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "Grave, urgent, solemn, completely no jokes or casual emojis.",
        structuredTone = "serious"
    )

    val WARNING = SmartReplyStyle(
        id = "warning",
        displayName = "Warning",
        emoji = "⚠️",
        category = SmartReplyStyleCategory.STRONG_COMMUNICATION,
        promptDescription = "Precautionary notice, stating consequences or risks clearly and decisively.",
        structuredTone = "warning"
    )

    // RELATIONSHIP
    val FLIRTY = SmartReplyStyle(
        id = "flirty",
        displayName = "Flirty",
        emoji = "💕",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Playfully romantic, light flirtation, charming spark, alluring banter.",
        structuredTone = "flirty"
    )

    val REL_ROMANTIC = SmartReplyStyle(
        id = "rel_romantic",
        displayName = "Romantic",
        emoji = "😘",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Deep romantic connection, affectionate, poetic, loving bond.",
        structuredTone = "romantic"
    )

    val REL_CARING = SmartReplyStyle(
        id = "rel_caring",
        displayName = "Caring",
        emoji = "🥰",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Nurturing, attentive to the other's emotions and comfort.",
        structuredTone = "caring"
    )

    val APOLOGETIC = SmartReplyStyle(
        id = "apologetic",
        displayName = "Apologetic",
        emoji = "💔",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Sincere remorse, taking accountability, asking for forgiveness with humility.",
        structuredTone = "apologetic",
        structuredPurpose = "apologize"
    )

    val REASSURING = SmartReplyStyle(
        id = "reassuring",
        displayName = "Reassuring",
        emoji = "🤗",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Soothing anxiety, providing steady comfort, reaffirming safety and loyalty.",
        structuredTone = "reassuring"
    )

    val DEEP = SmartReplyStyle(
        id = "deep",
        displayName = "Deep",
        emoji = "❤️",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Thoughtful, reflective, emotional depth, meaningful connection.",
        structuredTone = "deep"
    )

    val HEARTFELT = SmartReplyStyle(
        id = "heartfelt",
        displayName = "Heartfelt",
        emoji = "💌",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Raw emotional sincerity, vulnerable, spoken directly from the heart.",
        structuredTone = "heartfelt"
    )

    val CHARMING = SmartReplyStyle(
        id = "charming",
        displayName = "Charming",
        emoji = "🌹",
        category = SmartReplyStyleCategory.RELATIONSHIP,
        promptDescription = "Gracious, captivating, suave, complimentary, and delightful.",
        structuredTone = "charming"
    )

    // RESPONSE PURPOSE
    val CONFIRM = SmartReplyStyle(
        id = "confirm",
        displayName = "Confirm",
        emoji = "✅",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Clearly confirm attendance, acceptance, receipt, or agreement.",
        structuredPurpose = "confirm"
    )

    val DECLINE = SmartReplyStyle(
        id = "decline",
        displayName = "Decline",
        emoji = "❌",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Politely and unambiguously decline the request, invitation, or proposal.",
        structuredPurpose = "decline"
    )

    val APOLOGIZE_PURPOSE = SmartReplyStyle(
        id = "apologize_purpose",
        displayName = "Apologize",
        emoji = "🙏",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Express sincere apology, regret for delay or inconvenience, and offer resolution.",
        structuredPurpose = "apologize"
    )

    val CLARIFY = SmartReplyStyle(
        id = "clarify",
        displayName = "Ask for Clarification",
        emoji = "❓",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Ask concise clarifying questions to understand what is needed.",
        structuredPurpose = "clarify"
    )

    val MORE_TIME = SmartReplyStyle(
        id = "more_time",
        displayName = "Ask for More Time",
        emoji = "⏳",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Politely ask for an extension or additional time before responding.",
        structuredPurpose = "more_time"
    )

    val SCHEDULE = SmartReplyStyle(
        id = "schedule",
        displayName = "Schedule",
        emoji = "📅",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Propose specific dates, slots, or calendar availability for meeting.",
        structuredPurpose = "schedule"
    )

    val RESCHEDULE = SmartReplyStyle(
        id = "reschedule",
        displayName = "Reschedule",
        emoji = "🔄",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Request to move an existing appointment/meeting to a different time.",
        structuredPurpose = "reschedule"
    )

    val AGREE = SmartReplyStyle(
        id = "agree",
        displayName = "Agree",
        emoji = "👍",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Express explicit agreement, support, and consent for the proposed plan.",
        structuredPurpose = "agree"
    )

    val DISAGREE = SmartReplyStyle(
        id = "disagree",
        displayName = "Disagree",
        emoji = "👎",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Express polite, constructive disagreement or alternate viewpoint.",
        structuredPurpose = "disagree"
    )

    val SUGGEST = SmartReplyStyle(
        id = "suggest",
        displayName = "Suggest",
        emoji = "💡",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Offer a proactive alternative idea, recommendation, or creative suggestion.",
        structuredPurpose = "suggest"
    )

    val NEGOTIATE = SmartReplyStyle(
        id = "negotiate",
        displayName = "Negotiate",
        emoji = "🤝",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Counter-propose terms, suggest middle ground, or negotiate flexibility.",
        structuredPurpose = "negotiate"
    )

    val GET_TO_POINT = SmartReplyStyle(
        id = "get_to_point",
        displayName = "Get to the Point",
        emoji = "🎯",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Strip away pleasantries and deliver the core decision or answer immediately.",
        structuredPurpose = "get_to_point"
    )

    val EXPLAIN = SmartReplyStyle(
        id = "explain",
        displayName = "Explain",
        emoji = "📝",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Provide clear context, explanation, rationale, or instructions.",
        structuredPurpose = "explain"
    )

    val THANK = SmartReplyStyle(
        id = "thank",
        displayName = "Thank",
        emoji = "🙌",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Express heartfelt, warm gratitude for recipient's effort or gesture.",
        structuredPurpose = "thank"
    )

    val CONGRATULATE = SmartReplyStyle(
        id = "congratulate",
        displayName = "Congratulate",
        emoji = "🎊",
        category = SmartReplyStyleCategory.RESPONSE_PURPOSE,
        promptDescription = "Celebrate a milestone, success, good news, or promotion with praise.",
        structuredPurpose = "congratulate"
    )

    // LENGTH
    val VERY_SHORT = SmartReplyStyle(
        id = "very_short",
        displayName = "Very Short",
        emoji = "⚡",
        category = SmartReplyStyleCategory.LENGTH,
        promptDescription = "Ultra-brief, 1 to 4 words maximum (e.g. 'Got it, thanks!', 'Will do.', 'Confirmed.').",
        structuredLength = "very_short"
    )

    val SHORT = SmartReplyStyle(
        id = "short",
        displayName = "Short",
        emoji = "😎",
        category = SmartReplyStyleCategory.LENGTH,
        promptDescription = "Concise single sentence, around 5 to 10 words.",
        structuredLength = "short"
    )

    val MEDIUM = SmartReplyStyle(
        id = "medium",
        displayName = "Medium",
        emoji = "💬",
        category = SmartReplyStyleCategory.LENGTH,
        promptDescription = "Standard conversational length of 1 to 2 balanced sentences.",
        structuredLength = "medium"
    )

    val DETAILED = SmartReplyStyle(
        id = "detailed",
        displayName = "Detailed",
        emoji = "✨",
        category = SmartReplyStyleCategory.LENGTH,
        promptDescription = "Complete explanation of 2 to 3 sentences with context and next steps.",
        structuredLength = "detailed"
    )

    val ELABORATE = SmartReplyStyle(
        id = "elaborate",
        displayName = "Elaborate",
        emoji = "📖",
        category = SmartReplyStyleCategory.LENGTH,
        promptDescription = "Thorough, structured, multi-point response providing comprehensive clarity.",
        structuredLength = "elaborate"
    )

    val ALL_STYLES: List<SmartReplyStyle> = listOf(
        // SMART / AUTO
        SMART_MATCH, AUTO, MATCH_MESSAGE_STYLE,
        // GENERAL
        CASUAL, FRIENDLY, COOL, NATURAL, SIMPLE, HELPFUL, RELAXED,
        // PROFESSIONAL
        PROFESSIONAL, BUSINESS, FORMAL, DIRECT, CORPORATE, EXECUTIVE, WORK_APPROPRIATE,
        // POSITIVE & WARM
        ROMANTIC, LOVING, CARING, SWEET, CHEERFUL, GRATEFUL, ENCOURAGING, SUPPORTIVE, AFFECTIONATE, WARM,
        // SOCIAL & FUN
        FUNNY, PLAYFUL, WITTY, SARCASTIC, BOLD, CONFIDENT, EXCITED, CELEBRATORY, ENTHUSIASTIC, CHILL,
        // RESPECT & COMMUNICATION
        POLITE, RESPECTFUL, CALM, DIPLOMATIC, UNDERSTANDING, EMPATHETIC, PEACEFUL, ASSERTIVE_COMM,
        // STRONG COMMUNICATION
        ASSERTIVE, FIRM, STRAIGHTFORWARD, BOUNDARY_SETTING, AGGRESSIVE, COLD, SERIOUS, WARNING,
        // RELATIONSHIP
        FLIRTY, REL_ROMANTIC, REL_CARING, APOLOGETIC, REASSURING, DEEP, HEARTFELT, CHARMING,
        // RESPONSE PURPOSE
        CONFIRM, DECLINE, APOLOGIZE_PURPOSE, CLARIFY, MORE_TIME, SCHEDULE, RESCHEDULE, AGREE, DISAGREE, SUGGEST, NEGOTIATE, GET_TO_POINT, EXPLAIN, THANK, CONGRATULATE,
        // LENGTH
        VERY_SHORT, SHORT, MEDIUM, DETAILED, ELABORATE
    )

    fun getStylesForCategory(category: SmartReplyStyleCategory): List<SmartReplyStyle> {
        return ALL_STYLES.filter { it.category == category }
    }

    fun findById(id: String?): SmartReplyStyle {
        if (id.isNullOrBlank()) return SMART_MATCH
        return ALL_STYLES.firstOrNull { it.id.equals(id, ignoreCase = true) }
            ?: SMART_MATCH
    }

    fun findByNameOrId(query: String?): SmartReplyStyle {
        if (query.isNullOrBlank()) return SMART_MATCH
        val clean = query.trim().lowercase()
        return ALL_STYLES.firstOrNull {
            it.id.equals(clean, ignoreCase = true) ||
            it.displayName.equals(clean, ignoreCase = true)
        } ?: SMART_MATCH
    }
}

/**
 * Backwards compatibility enum matching original SmartReplyTone
 */
enum class SmartReplyTone(val displayName: String, val iconEmoji: String, val promptDescription: String) {
    AUTO("Smart Match", "🧠", "natural and matching the detected tone of the incoming message"),
    CASUAL("Casual", "💬", "relaxed, informal, and conversational"),
    FRIENDLY("Friendly", "😊", "warm, welcoming, and cheerful"),
    PROFESSIONAL("Professional", "💼", "courteous, formal, structured, and business-appropriate"),
    POLITE("Polite", "🙏", "gentle, respectful, courteous, and considerate"),
    SHORT("Short", "⚡", "very concise, quick, and to the point in 3-8 words"),
    DETAILED("Detailed", "📝", "thorough, complete, and providing clear context");

    fun toSmartReplyStyle(): SmartReplyStyle {
        return when (this) {
            AUTO -> SmartReplyLibrary.SMART_MATCH
            CASUAL -> SmartReplyLibrary.CASUAL
            FRIENDLY -> SmartReplyLibrary.FRIENDLY
            PROFESSIONAL -> SmartReplyLibrary.PROFESSIONAL
            POLITE -> SmartReplyLibrary.POLITE
            SHORT -> SmartReplyLibrary.SHORT
            DETAILED -> SmartReplyLibrary.DETAILED
        }
    }

    companion object {
        fun fromString(value: String?): SmartReplyTone {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: AUTO
        }
    }
}

data class SmartReplyRequest(
    val message: String,
    val requestedLanguage: String = "auto",
    val style: SmartReplyStyle = SmartReplyLibrary.SMART_MATCH,
    val tone: SmartReplyTone = SmartReplyTone.AUTO,
    val maxReplies: Int = 4
)

data class SmartReplySuggestion(
    val text: String,
    val tone: String = "Friendly",
    val language: String = "auto",
    val confidence: Float = 0.9f
)

data class SmartReplyResponse(
    val detectedLanguage: String,
    val dominantLanguage: String,
    val isCodeMixed: Boolean,
    val replies: List<SmartReplySuggestion>,
    val detectedIntent: String? = null,
    val appliedStyle: SmartReplyStyle = SmartReplyLibrary.SMART_MATCH
)

sealed class SmartReplyUiState {
    object Idle : SmartReplyUiState()
    data class InputEntered(val text: String) : SmartReplyUiState()
    data class Analyzing(val stepMessage: String) : SmartReplyUiState()
    data class Success(val response: SmartReplyResponse) : SmartReplyUiState()
    data class EmptyResult(val reason: String = "No replies could be generated for this message.") : SmartReplyUiState()
    data class NetworkError(val message: String = "No internet connection. Please check your network.") : SmartReplyUiState()
    data class AuthenticationError(val message: String = "AI authentication error. Please verify your credentials.") : SmartReplyUiState()
    data class RateLimited(val message: String = "Rate limit reached. Please wait a moment and try again.") : SmartReplyUiState()
    data class Timeout(val message: String = "Request timed out. Please try again.") : SmartReplyUiState()
    data class ProviderError(val message: String = "Smart Reply is temporarily unavailable.") : SmartReplyUiState()
    object SensitiveFieldBlocked : SmartReplyUiState()
}
