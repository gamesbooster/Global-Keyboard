package com.example.model

data class ExpressiveSticker(
    val id: String,
    val title: String,
    val textToInsert: String,
    val category: String,
    val iconBadge: String,
    val gradientColors: List<Long>,
    val subtext: String = "",
    val isUserCreated: Boolean = false
)

object StickerCatalog {
    val STICKER_CATEGORIES = listOf(
        "🔥 Viral Memes",
        "🪔 Festivals",
        "💬 Desi Slang",
        "❤️ Love & Care",
        "☕ Chai & Fun",
        "🎉 Celebrations",
        "📌 My Templates"
    )

    val ALL_STICKERS = listOf(
        // Viral Memes & Reactions
        ExpressiveSticker(
            id = "meme_paisa",
            title = "Paisa Hi Paisa!",
            textToInsert = "🤑 Paisa hi paisa hoga! 💰📈 25 din mein paisa double!",
            category = "🔥 Viral Memes",
            iconBadge = "🤑",
            gradientColors = listOf(0xFF10B981, 0xFF059669),
            subtext = "Scheme Boss"
        ),
        ExpressiveSticker(
            id = "meme_badhiya",
            title = "Yeh Badhiya Tha!",
            textToInsert = "😂 Yeh badhiya tha guru! Top tier humour! 👏🔥",
            category = "🔥 Viral Memes",
            iconBadge = "😂",
            gradientColors = listOf(0xFFF59E0B, 0xFFD97706),
            subtext = "Classic Joke"
        ),
        ExpressiveSticker(
            id = "meme_marjau",
            title = "Kya Karu Mar Jau?",
            textToInsert = "🤷‍♂️ Ab kya karu main, mar jau meri koi feelings nahi hai? 🥺💔",
            category = "🔥 Viral Memes",
            iconBadge = "🥺",
            gradientColors = listOf(0xFFEC4899, 0xFFBE185D),
            subtext = "Emotional Damage"
        ),
        ExpressiveSticker(
            id = "meme_pakad",
            title = "Pakad Mere Ko!",
            textToInsert = "🏃‍♂️💨 Bhai mujhe chakkar aa rahe hain, pakad mere ko! 😵‍💫⚡",
            category = "🔥 Viral Memes",
            iconBadge = "🏃‍♂️",
            gradientColors = listOf(0xFF6366F1, 0xFF4338CA),
            subtext = "Mind Blown"
        ),
        ExpressiveSticker(
            id = "meme_chotibachi",
            title = "Choti Bachi Ho Kya?",
            textToInsert = "🍼 Choti bachi ho kya? Samajh nahi aata? 👶😤",
            category = "🔥 Viral Memes",
            iconBadge = "🍼",
            gradientColors = listOf(0xFF8B5CF6, 0xFF6D28D9),
            subtext = "Epic Dialogue"
        ),
        ExpressiveSticker(
            id = "meme_gigachad",
            title = "Gigachad Mode",
            textToInsert = "🗿 Gigachad mindset activated! Unbothered, focused & winning. 💪🔥",
            category = "🔥 Viral Memes",
            iconBadge = "🗿",
            gradientColors = listOf(0xFF475569, 0xFF1E293B),
            subtext = "Sigma Rule"
        ),
        ExpressiveSticker(
            id = "meme_jalwa",
            title = "Jalwa Hai Hamara!",
            textToInsert = "😎 Jalwa hai hamara yahan! Entry aisi honi chahiye! 👑🔥",
            category = "🔥 Viral Memes",
            iconBadge = "😎",
            gradientColors = listOf(0xFFEF4444, 0xFFB91C1C),
            subtext = "Full Swag"
        ),
        ExpressiveSticker(
            id = "meme_smash",
            title = "Smash or Pass",
            textToInsert = "😏 Smash or Pass? 100% Smash with no hesitation! 💯✨",
            category = "🔥 Viral Memes",
            iconBadge = "😏",
            gradientColors = listOf(0xFFF43F5E, 0xFFE11D48),
            subtext = "Viral Trend"
        ),
        ExpressiveSticker(
            id = "meme_control",
            title = "Control Majnu!",
            textToInsert = "🧘‍♂️ Control Majnu control! Shant ho jao, lambi saans lo! 🕊️😅",
            category = "🔥 Viral Memes",
            iconBadge = "🧘‍♂️",
            gradientColors = listOf(0xFF14B8A6, 0xFF0F766E),
            subtext = "Stay Calm"
        ),
        ExpressiveSticker(
            id = "meme_women",
            title = "Women Haha ☕",
            textToInsert = "☕ Women... haha! Classic moment! 😂🤝",
            category = "🔥 Viral Memes",
            iconBadge = "☕",
            gradientColors = listOf(0xFFD97706, 0xFF92400E),
            subtext = "Sip Tea"
        ),
        ExpressiveSticker(
            id = "meme_chronology",
            title = "Chronology Samajhiye",
            textToInsert = "🧠 Aap chronology samajhiye, step by step sab clear hoga! 📊🤓",
            category = "🔥 Viral Memes",
            iconBadge = "🧠",
            gradientColors = listOf(0xFF3B82F6, 0xFF1D4ED8),
            subtext = "Masterplan"
        ),
        ExpressiveSticker(
            id = "meme_beizzati",
            title = "Gajab Beizzati!",
            textToInsert = "🤦‍♂️ Gajab beizzati hai yaar! I can't even look in the mirror! 💀😭",
            category = "🔥 Viral Memes",
            iconBadge = "🤦‍♂️",
            gradientColors = listOf(0xFFE11D48, 0xFF9F1239),
            subtext = "Dead Roast"
        ),

        // Festivals
        ExpressiveSticker(
            id = "fest_diwali",
            title = "Happy Diwali",
            textToInsert = "🪔 Happy Diwali! Wishing you and your family endless light, peace, and prosperity! ✨🎇",
            category = "🪔 Festivals",
            iconBadge = "🪔",
            gradientColors = listOf(0xFFF59E0B, 0xFFEF4444),
            subtext = "Shubh Deepavali"
        ),
        ExpressiveSticker(
            id = "fest_holi",
            title = "Happy Holi",
            textToInsert = "🎨 Bura Na Mano Holi Hai! Wishing you a vibrant and colorful Happy Holi! 🌈🥳",
            category = "🪔 Festivals",
            iconBadge = "🎨",
            gradientColors = listOf(0xFFEC4899, 0xFF8B5CF6),
            subtext = "Rang Barse!"
        ),
        ExpressiveSticker(
            id = "fest_eid",
            title = "Eid Mubarak",
            textToInsert = "🌙 Eid Mubarak! May peace, happiness, and prosperity bless you and your family! 🕌✨",
            category = "🪔 Festivals",
            iconBadge = "🌙",
            gradientColors = listOf(0xFF10B981, 0xFF047857),
            subtext = "Taqabbalallahu Minna"
        ),
        ExpressiveSticker(
            id = "fest_rakhi",
            title = "Happy Rakhi",
            textToInsert = "🧵 Happy Raksha Bandhan! Celebrating our special bond today and always! 👫🎁",
            category = "🪔 Festivals",
            iconBadge = "🧵",
            gradientColors = listOf(0xFFF43F5E, 0xFFFB7185),
            subtext = "Brother & Sister Love"
        ),
        ExpressiveSticker(
            id = "fest_india",
            title = "Jai Hind!",
            textToInsert = "🇮🇳 Vande Mataram! Proud to be Indian! Happy Independence / Republic Day! 🫡✨",
            category = "🪔 Festivals",
            iconBadge = "🇮🇳",
            gradientColors = listOf(0xFFFF9933, 0xFF138808),
            subtext = "Desh Bhakti"
        ),

        // Desi Slang & Reactions
        ExpressiveSticker(
            id = "desi_kyabaat",
            title = "Kya Baat Hai!",
            textToInsert = "🔥 Kya Baat Hai! Absolute perfection! 👏✨",
            category = "💬 Desi Slang",
            iconBadge = "🔥",
            gradientColors = listOf(0xFFF97316, 0xFFEA580C),
            subtext = "Next Level!"
        ),
        ExpressiveSticker(
            id = "desi_arrewah",
            title = "Arre Wah!",
            textToInsert = "🤩 Arre Wah! Dil khush ho gaya! Superb! 👏🌟",
            category = "💬 Desi Slang",
            iconBadge = "🤩",
            gradientColors = listOf(0xFF3B82F6, 0xFF1D4ED8),
            subtext = "Too Good!"
        ),
        ExpressiveSticker(
            id = "desi_bindaas",
            title = "Bindaas!",
            textToInsert = "😎 Bindaas raho, tension mat lo! Sab badhiya hoga! 🤙✨",
            category = "💬 Desi Slang",
            iconBadge = "😎",
            gradientColors = listOf(0xFF8B5CF6, 0xFF6D28D9),
            subtext = "No Worries"
        ),
        ExpressiveSticker(
            id = "desi_zabardast",
            title = "Zabardast!",
            textToInsert = "💥 Zabardast performance! You nailed it! 🚀🔥",
            category = "💬 Desi Slang",
            iconBadge = "💥",
            gradientColors = listOf(0xFFEF4444, 0xFFB91C1C),
            subtext = "Unstoppable"
        ),
        ExpressiveSticker(
            id = "desi_shabaash",
            title = "Shabaash!",
            textToInsert = "👏 Shabaash! Proud of you, keep shining! 🏆🌟",
            category = "💬 Desi Slang",
            iconBadge = "👏",
            gradientColors = listOf(0xFF10B981, 0xFF059669),
            subtext = "Well Done!"
        ),
        ExpressiveSticker(
            id = "desi_eknumber",
            title = "Ek Number!",
            textToInsert = "💯 Ek Number boss! Mazaa aa gaya! 👌🔥",
            category = "💬 Desi Slang",
            iconBadge = "💯",
            gradientColors = listOf(0xFF6366F1, 0xFF4338CA),
            subtext = "A1 Quality"
        ),

        // Love & Care
        ExpressiveSticker(
            id = "love_dilse",
            title = "Dil Se...",
            textToInsert = "❤️ Dil Se... Thinking of you and sending big warm hugs! 🥰✨",
            category = "❤️ Love & Care",
            iconBadge = "❤️",
            gradientColors = listOf(0xFFEC4899, 0xFFBE185D),
            subtext = "From The Heart"
        ),
        ExpressiveSticker(
            id = "love_missyou",
            title = "Miss You!",
            textToInsert = "🥺 Miss you so much! Can't wait to catch up soon! 💔❤️",
            category = "❤️ Love & Care",
            iconBadge = "🥺",
            gradientColors = listOf(0xFF8B5CF6, 0xFFA855F7),
            subtext = "Come Back Soon"
        ),
        ExpressiveSticker(
            id = "love_takecare",
            title = "Take Care!",
            textToInsert = "🌸 Take care of yourself! Rest well and stay safe! 💊🤗",
            category = "❤️ Love & Care",
            iconBadge = "🌸",
            gradientColors = listOf(0xFF14B8A6, 0xFF0F766E),
            subtext = "Stay Healthy"
        ),

        // Chai & Fun
        ExpressiveSticker(
            id = "fun_chai",
            title = "Chai Time! ☕",
            textToInsert = "☕ Hello friends, chai peelo! Garam chai break time! 🫖✨",
            category = "☕ Chai & Fun",
            iconBadge = "☕",
            gradientColors = listOf(0xFFD97706, 0xFFB45309),
            subtext = "Chai Lovers"
        ),
        ExpressiveSticker(
            id = "fun_party",
            title = "Party Time!",
            textToInsert = "🥳 Party time! Let's celebrate and have a blast tonight! 🍻🎉",
            category = "☕ Chai & Fun",
            iconBadge = "🥳",
            gradientColors = listOf(0xFFF43F5E, 0xFF9333EA),
            subtext = "Masti Unlimited"
        ),
        ExpressiveSticker(
            id = "fun_lol",
            title = "Hahaha / LOL",
            textToInsert = "😂 Hahaha! I literally can't stop laughing! Dead! 💀🤣",
            category = "☕ Chai & Fun",
            iconBadge = "😂",
            gradientColors = listOf(0xFFEAB308, 0xFFCA8A04),
            subtext = "Comedy Gold"
        ),

        // Celebrations
        ExpressiveSticker(
            id = "celeb_birthday",
            title = "Happy Birthday!",
            textToInsert = "🎂 Happy Birthday! May all your dreams come true this year! Have a fantastic day! 🎁🎉✨",
            category = "🎉 Celebrations",
            iconBadge = "🎂",
            gradientColors = listOf(0xFF3B82F6, 0xFFEC4899),
            subtext = "Janmadin Mubarak"
        ),
        ExpressiveSticker(
            id = "celeb_anniversary",
            title = "Happy Anniversary",
            textToInsert = "💐 Happy Anniversary to the wonderful couple! Wishing you a lifetime of love & joy! 🥂❤️",
            category = "🎉 Celebrations",
            iconBadge = "💐",
            gradientColors = listOf(0xFFA855F7, 0xFFE11D48),
            subtext = "Together Forever"
        ),
        ExpressiveSticker(
            id = "celeb_congrats",
            title = "Congratulations!",
            textToInsert = "🎉 Huge Congratulations on your great achievement! So proud of you! 🏆👏",
            category = "🎉 Celebrations",
            iconBadge = "🎉",
            gradientColors = listOf(0xFF10B981, 0xFF3B82F6),
            subtext = "Super Success"
        )
    )
}
