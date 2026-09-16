package com.example.model

data class MessageTemplate(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val isUserCreated: Boolean = false
)

object TemplateCatalog {
    const val CAT_ALL = "⭐ All"
    const val CAT_BIRTHDAY = "🎂 Birthday & Wishes"
    const val CAT_FESTIVALS = "🪔 Festivals & New Year"
    const val CAT_BUSINESS = "💼 Business & Deals"
    const val CAT_LOVE = "❤️ Love & Romance"
    const val CAT_DAILY = "✨ Daily & Quick"
    const val CAT_CUSTOM = "📌 My Templates"

    val CATEGORIES = listOf(
        CAT_ALL,
        CAT_BIRTHDAY,
        CAT_FESTIVALS,
        CAT_BUSINESS,
        CAT_LOVE,
        CAT_DAILY,
        CAT_CUSTOM
    )

    val BUILT_IN_TEMPLATES = listOf(
        // Birthday & Wishes
        MessageTemplate(
            id = "bday_cheer",
            title = "Happy Birthday Cheer",
            content = "🎂 Happy Birthday! May your special day be loaded with joy, laughter, and all your favorite things! Have a wonderful year ahead! 🎉✨",
            category = CAT_BIRTHDAY
        ),
        MessageTemplate(
            id = "bday_belated",
            title = "Belated Birthday",
            content = "🎈 Happy Belated Birthday! Hope you had a fantastic celebration! Sending you lots of love and best wishes! 🎂🥳",
            category = CAT_BIRTHDAY
        ),
        MessageTemplate(
            id = "bday_milestone",
            title = "Milestone Birthday",
            content = "🌟 Happy Milestone Birthday! Cheers to another great chapter filled with great health, success, and prosperity! 🥂🎁",
            category = CAT_BIRTHDAY
        ),

        // Festivals & New Year
        MessageTemplate(
            id = "fest_new_year",
            title = "Happy New Year",
            content = "🎆 Happy New Year! May this year bring fresh opportunities, great happiness, and grand success to you and your family! 🥂✨",
            category = CAT_FESTIVALS
        ),
        MessageTemplate(
            id = "fest_diwali_tmpl",
            title = "Happy Diwali & Prosperity",
            content = "🪔 Wishing you and your family a very Happy and Prosperous Diwali! May the divine lights illuminate your path! ✨🎇",
            category = CAT_FESTIVALS
        ),
        MessageTemplate(
            id = "fest_eid_tmpl",
            title = "Eid Mubarak & Blessings",
            content = "🌙 Eid Mubarak! Wishing you and your loved ones peace, harmony, and blessings on this auspicious occasion! 🕌🤲",
            category = CAT_FESTIVALS
        ),
        MessageTemplate(
            id = "fest_xmas_tmpl",
            title = "Merry Christmas",
            content = "🎄 Merry Christmas! May your holiday season sparkle with moments of love, laughter, and goodwill! 🎅🎁",
            category = CAT_FESTIVALS
        ),
        MessageTemplate(
            id = "fest_holi_tmpl",
            title = "Joyous Happy Holi",
            content = "🎨 Wishing you a joyous and colorful Happy Holi! May your life be filled with vibrant moments! 🌈🥳",
            category = CAT_FESTIVALS
        ),
        MessageTemplate(
            id = "fest_rakhi_tmpl",
            title = "Raksha Bandhan Love",
            content = "🧵 Happy Raksha Bandhan! Grateful for our bond and sending warmest wishes and love! 👫🎁",
            category = CAT_FESTIVALS
        ),

        // Business & Deals
        MessageTemplate(
            id = "biz_quotation",
            title = "Quotation & Proposal",
            content = "📄 Dear Partner, We have shared the updated proposal and quotation for your review. Looking forward to your feedback! 💼🤝",
            category = CAT_BUSINESS
        ),
        MessageTemplate(
            id = "biz_followup",
            title = "Meeting Follow-up",
            content = "📅 Thank you for taking the time to connect today. As discussed, I am sharing the action items and next steps. 🤝📋",
            category = CAT_BUSINESS
        ),
        MessageTemplate(
            id = "biz_payment",
            title = "Payment Reminder",
            content = "💳 Polite reminder regarding invoice pending payment. Kindly process at your earliest convenience. Thank you! 🧾✨",
            category = CAT_BUSINESS
        ),
        MessageTemplate(
            id = "biz_deal_closed",
            title = "Deal Closed Thank You",
            content = "🎉 We are delighted to confirm our partnership! Thank you for placing your trust in us. Let's achieve great milestones together! 🚀🤝",
            category = CAT_BUSINESS
        ),
        MessageTemplate(
            id = "biz_status",
            title = "Project Status Update",
            content = "⚡ Quick update: Project milestones are progressing right on schedule. Detailed report will follow shortly. 📊👍",
            category = CAT_BUSINESS
        ),

        // Love & Romance
        MessageTemplate(
            id = "love_morning",
            title = "Good Morning Love",
            content = "🌅 Good morning sweetheart! Wishing you a day as wonderful and beautiful as you are! Love you! ❤️🥰",
            category = CAT_LOVE
        ),
        MessageTemplate(
            id = "love_missing",
            title = "Missing You Truly",
            content = "🥺 Thinking of you and missing your smile so much today. Counting hours until I see you! 💔❤️",
            category = CAT_LOVE
        ),
        MessageTemplate(
            id = "love_sweet_dreams",
            title = "Sweet Dreams",
            content = "🌙 Sleep tight and have sweet dreams, my love! Can't wait to wake up and talk to you tomorrow! 😴💕",
            category = CAT_LOVE
        ),
        MessageTemplate(
            id = "love_date_night",
            title = "Date Night Ready",
            content = "🍷 Can't wait for our date tonight! Getting ready to make beautiful memories together! 🌹✨",
            category = CAT_LOVE
        ),

        // Daily & Quick
        MessageTemplate(
            id = "daily_late",
            title = "Running 5 Mins Late",
            content = "⏳ Running about 5-10 minutes behind due to traffic. Extremely sorry for the delay, almost there! 🚗💨",
            category = CAT_DAILY
        ),
        MessageTemplate(
            id = "daily_callback",
            title = "Will Call Back Shortly",
            content = "📞 In a quick meeting right now. Will call you back as soon as I wrap up! Thanks! 🙏",
            category = CAT_DAILY
        ),
        MessageTemplate(
            id = "daily_ack",
            title = "Received with Thanks",
            content = "👍 Acknowledging receipt with thanks. Everything received in good order. Have a great day! ✨",
            category = CAT_DAILY
        ),
        MessageTemplate(
            id = "daily_coffee",
            title = "Let's Catch Up",
            content = "☕ Hey! It's been a while, let's catch up over coffee this week! Let me know when you're free! 🫖😊",
            category = CAT_DAILY
        )
    )
}
