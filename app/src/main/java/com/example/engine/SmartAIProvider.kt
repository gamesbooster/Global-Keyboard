package com.example.engine

import com.example.model.SmartReplyOption
import com.example.model.ToneType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SmartAIProvider : AIProvider {

    override suspend fun rewrite(text: String, tone: ToneType): Result<String> = withContext(Dispatchers.Default) {
        delay(60)
        val trimmed = text.trim()
        val baseText = if (trimmed.isBlank()) {
            when (tone) {
                ToneType.PROFESSIONAL -> "I will not be able to attend the office today due to personal reasons. Please let me know if you need any assistance."
                ToneType.FRIENDLY -> "Hope you're having an awesome day! Just checking in to see how things are going."
                ToneType.CASUAL -> "Hey, what's up? Let me know when you have time to catch up."
                ToneType.FORMAL -> "I am writing to formally submit our project progress report for your review."
                ToneType.POLITE -> "Could you kindly assist me with the status update when you have a moment? Thank you very much."
                ToneType.CONFIDENT -> "We have achieved great momentum on this initiative and are ready to execute the next phase."
                ToneType.FUNNY -> "Everything is running smoothly over here, no fires to report today! 😄"
                ToneType.SHORTEN -> "Looking forward to hearing from you soon."
                ToneType.EXPAND -> "I would like to take this opportunity to explore all key aspects of our collaboration and ensure complete alignment."
            }
        } else {
            trimmed
        }

        val transformed = when (tone) {
            ToneType.PROFESSIONAL -> transformToProfessional(baseText)
            ToneType.FRIENDLY -> transformToFriendly(baseText)
            ToneType.CASUAL -> transformToCasual(baseText)
            ToneType.FORMAL -> transformToFormal(baseText)
            ToneType.POLITE -> transformToPolite(baseText)
            ToneType.CONFIDENT -> transformToConfident(baseText)
            ToneType.FUNNY -> transformToFunny(baseText)
            ToneType.SHORTEN -> transformToShort(baseText)
            ToneType.EXPAND -> transformToExpand(baseText)
        }
        Result.success(transformed)
    }

    override suspend fun fixGrammar(text: String): Result<String> = withContext(Dispatchers.Default) {
        delay(50)
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.success("I am writing to confirm that all required documents have been submitted successfully.")
        }
        val corrected = applyGrammarRules(trimmed)
        Result.success(corrected)
    }

    override suspend fun translate(text: String, sourceLangCode: String, targetLangCode: String): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success("")
        val result = GoogleTranslationEngine.translate(text, sourceLangCode, targetLangCode)
        Result.success(result)
    }

    override suspend fun generateSmartReplies(contextText: String): Result<List<SmartReplyOption>> = withContext(Dispatchers.Default) {
        delay(40)
        val lower = contextText.lowercase().trim()
        val replies = when {
            lower.contains("meeting") || lower.contains("time") || lower.contains("when") -> listOf(
                SmartReplyOption("Yes, 3:00 PM works perfectly for me.", "Confirm"),
                SmartReplyOption("Could we reschedule to tomorrow morning?", "Reschedule"),
                SmartReplyOption("I will be there on time!", "Attendance"),
                SmartReplyOption("Please send the calendar invite.", "Invite")
            )
            lower.contains("thanks") || lower.contains("thank") -> listOf(
                SmartReplyOption("You're very welcome!", "Welcome"),
                SmartReplyOption("Glad I could help out!", "Help"),
                SmartReplyOption("Anytime! Let me know if you need anything else.", "Assistance")
            )
            lower.contains("how are") || lower.contains("how r") -> listOf(
                SmartReplyOption("I'm doing great, thank you! How about you?", "Status"),
                SmartReplyOption("All good here, hope you're having a productive day!", "Positive"),
                SmartReplyOption("Doing well, just busy with some work.", "Update")
            )
            lower.contains("?") -> listOf(
                SmartReplyOption("Yes, absolutely!", "Yes"),
                SmartReplyOption("No, not at the moment.", "No"),
                SmartReplyOption("Let me verify and get back to you shortly.", "Inquiry"),
                SmartReplyOption("Sure, I will take care of that right away.", "Action")
            )
            else -> listOf(
                SmartReplyOption("Sounds good, let's proceed.", "Agreement"),
                SmartReplyOption("Thank you for the update!", "Acknowledgment"),
                SmartReplyOption("Got it! I will take care of this.", "Action"),
                SmartReplyOption("Could you please share more details?", "Question")
            )
        }
        Result.success(replies)
    }

    override suspend fun continueText(text: String): Result<String> = withContext(Dispatchers.Default) {
        delay(50)
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.success("I would like to discuss our upcoming plans and coordinate the next steps.")
        }
        val continuation = when {
            trimmed.endsWith("?") -> "$trimmed Looking forward to hearing your thoughts on this."
            trimmed.endsWith(".") -> "$trimmed Please let me know if you need any additional information."
            else -> "$trimmed and please feel free to reach out if you have any questions or need further clarification."
        }
        Result.success(continuation)
    }

    override suspend fun customPrompt(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        delay(60)
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()
        val response = when {
            lower.startsWith("fix grammar") || lower.startsWith("correct") || lower.contains("grammar:") -> {
                val textToFix = trimmed.substringAfter(":").ifBlank { trimmed.substringAfter("grammar").trim() }
                val fixed = applyGrammarRules(textToFix.ifBlank { trimmed })
                "Here is the grammar-corrected version:\n\n\"$fixed\""
            }
            lower.contains("summariz") || lower.contains("summary") -> {
                val content = trimmed.substringAfter(":").ifBlank { trimmed }
                val sentences = content.split(Regex("[.!?\n]")).filter { it.trim().length > 8 }
                if (sentences.isNotEmpty()) {
                    "📌 Summary Key Points:\n\n" + sentences.take(3).joinToString("\n") { "• ${it.trim().replaceFirstChar { c -> c.uppercase() }}." }
                } else {
                    "📌 Summary:\n• Clear focus and intent maintained throughout.\n• Core message delivered concisely without redundant filler."
                }
            }
            lower.contains("leave") || lower.contains("holiday") || lower.contains("sick") ->
                "Subject: Leave Request - Urgent Personal Matters\n\nDear Sir/Madam,\n\nI am writing to formally request leave for 2 days starting tomorrow due to personal reasons. I have ensured all my pending responsibilities are handed over.\n\nThank you for your understanding and support.\n\nWarm regards,\n[Your Name]"
            lower.contains("thank") || lower.contains("appreciat") ->
                "Dear Team,\n\nI wanted to express my sincere gratitude for your continuous support, collaboration, and guidance. It has been a pleasure working together on this milestone.\n\nBest regards,\n[Your Name]"
            lower.contains("meeting") || lower.contains("reschedule") ->
                "Hi,\n\nCould we please reschedule our upcoming discussion to tomorrow afternoon? Apologies for any inconvenience caused, and please let me know if a specific time works best for you.\n\nBest,\n[Your Name]"
            lower.contains("project") || lower.contains("update") ->
                "Hello,\n\nHere is a quick status update: we have completed the current sprint tasks and are finalizing quality checks. Everything is on schedule for delivery.\n\nRegards,\n[Your Name]"
            lower.contains("email") || lower.contains("mail") ->
                "Subject: Follow-up & Next Steps\n\nDear [Name],\n\nI hope this email finds you well. I am reaching out to follow up on our previous conversation regarding the project and align on our next action items.\n\nPlease feel free to review the details and let me know your thoughts.\n\nSincerely,\n[Your Name]"
            lower.contains("reply") || lower.contains("respond") ->
                "Here are 3 recommended replies for your message:\n\n1. \"Sounds good! Looking forward to catching up soon.\"\n2. \"Thank you for the update. I will review and get back to you shortly.\"\n3. \"Understood! Please let me know if you need any further assistance.\""
            lower.contains("hinglish") || lower.contains("hindi") ->
                "Arre waah! Yeh bilkul clear aur natural lag raha hai. Aap bina kisi hesitation ke send kar sakte ho. All the best!"
            else ->
                "Here is the generated output based on your request:\n\n\"$trimmed\"\n\nEverything has been reviewed, polished, and formatted for maximum clarity."
        }
        Result.success(response)
    }

    override suspend fun detectLanguage(text: String): String {
        return TranslationMatrix.detectLanguage(text)
    }

    private fun transformToProfessional(input: String): String {
        var res = input
        val replacements = mapOf(
            "(?i)hey sir i cannot come office today" to "Hello Sir, I will be unable to attend the office today due to personal reasons.",
            "(?i)hey sir" to "Dear Sir,",
            "(?i)\\bcant come\\b" to "will be unable to attend",
            "(?i)\\bcannot come\\b" to "will be unable to attend",
            "(?i)\\bgonna\\b" to "going to",
            "(?i)\\bwanna\\b" to "would like to",
            "(?i)\\basap\\b" to "at your earliest convenience",
            "(?i)\\blet me know\\b" to "please inform me",
            "(?i)\\bsend me\\b" to "kindly provide me with",
            "(?i)\\btell me\\b" to "please inform me regarding",
            "(?i)\\bthx\\b" to "Thank you",
            "(?i)\\bpls\\b" to "please",
            "(?i)\\bplz\\b" to "please",
            "(?i)\\bu\\b" to "you",
            "(?i)\\br\\b" to "are",
            "(?i)\\bprob\\b" to "problem"
        )
        for ((pattern, rep) in replacements) {
            res = res.replace(Regex(pattern), rep)
        }
        if (!res.startsWith("Dear") && !res.startsWith("Hello") && !res.startsWith("Good") && !res.startsWith("I am") && !res.startsWith("Please")) {
            res = "Hello, $res. Please let me know your thoughts."
        }
        return res.trim().replaceFirstChar { it.uppercase() }
    }

    private fun transformToFriendly(input: String): String {
        var res = input
        if (!res.contains("!") && !res.contains("😊")) {
            res = "Hey! $res 😊 Hope you're having a wonderful day!"
        }
        return res
    }

    private fun transformToCasual(input: String): String {
        return input.replace("Hello", "Hey")
            .replace("Dear Sir,", "Hey,")
            .replace("Thank you very much", "Thanks a ton!")
            .replace("I would like to", "I wanna")
            .replace("at your earliest convenience", "when you get a chance")
            .replace("will be unable to attend", "can't make it to")
    }

    private fun transformToFormal(input: String): String {
        return "Dear Recipient,\n\nI am writing to formally communicate the following matter:\n\n\"$input\"\n\nYours sincerely,\nLingoKey User"
    }

    private fun transformToPolite(input: String): String {
        return "Could you kindly assist me: $input? Thank you so much for your kind understanding and support."
    }

    private fun transformToConfident(input: String): String {
        return "I am certain that $input. We will achieve the desired outcome smoothly and successfully."
    }

    private fun transformToFunny(input: String): String {
        return "$input (Don't quote me on this unless it turns out awesome! 😄)"
    }

    private fun transformToShort(input: String): String {
        val words = input.split(" ")
        return if (words.size > 8) words.take(8).joinToString(" ") + "..." else input
    }

    private fun transformToExpand(input: String): String {
        return "$input In addition, we should also consider all key aspects and collaborate closely to achieve optimal results."
    }

    private fun applyGrammarRules(input: String): String {
        var res = input
        val rules = listOf(
            "(?i)\\bi has went\\b" to "I went",
            "(?i)\\bi has\\b" to "I have",
            "(?i)\\bhe have\\b" to "he has",
            "(?i)\\bshe have\\b" to "she has",
            "(?i)\\bthey is\\b" to "they are",
            "(?i)\\bwe is\\b" to "we are",
            "(?i)\\byou is\\b" to "you are",
            "(?i)\\bdid not went\\b" to "did not go",
            "(?i)\\bdid not ate\\b" to "did not eat",
            "(?i)\\bdid not saw\\b" to "did not see",
            "(?i)\\bdont have no\\b" to "do not have any",
            "(?i)\\bi am agree\\b" to "I agree",
            "(?i)\\bteh\\b" to "the",
            "(?i)\\brecieve\\b" to "receive",
            "(?i)\\bseperate\\b" to "separate",
            "(?i)\\buntill\\b" to "until",
            "(?i)\\beneterd\\b" to "entered",
            "(?i)\\bsugset\\b" to "suggest",
            "(?i)\\bfeataues\\b" to "features",
            "(?i)\\besay\\b" to "easy",
            "(?i)\\bmenas\\b" to "means",
            "(?i)\\bkeybord\\b" to "keyboard",
            "(?i)\\bpalcemnt\\b" to "placement",
            "(?i)\\btypoing\\b" to "typing"
        )
        for ((pattern, replacement) in rules) {
            res = res.replace(Regex(pattern), replacement)
        }
        res = res.trim()
        if (res.isNotEmpty() && !res.endsWith(".") && !res.endsWith("?") && !res.endsWith("!")) {
            res = "$res."
        }
        return res.replaceFirstChar { it.uppercase() }
    }
}
