package com.example.engine.smartreply

import com.example.model.*

/**
 * Local rule, intent, and style-based Smart Reply provider for offline use or network fallback.
 * Genuinely tailors suggested replies according to the user's selected Reply Style,
 * purpose, and length constraints.
 */
class OfflineSmartReplyProvider : SmartReplyProvider {

    override suspend fun analyzeAndGenerateReplies(request: SmartReplyRequest): Result<SmartReplyResponse> {
        val message = request.message.trim()
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }

        val detection = SmartReplyLanguageDetector.detect(message)
        val lower = message.lowercase()
        val style = request.style
        val langCode = if (request.requestedLanguage != "auto") request.requestedLanguage else detection.detectedLanguageCode
        val isCodeMixed = detection.isCodeMixed

        val replies = mutableListOf<SmartReplySuggestion>()
        var intent = "conversation"

        // 1. Check if user selected an explicit Response Purpose
        when (style.structuredPurpose) {
            "confirm" -> {
                intent = "confirm"
                when (langCode) {
                    "hi" -> {
                        if (isCodeMixed) {
                            replies.add(SmartReplySuggestion("Haan, confirmed! Main aa jaunga.", style.displayName, "hi", 0.95f))
                            replies.add(SmartReplySuggestion("Yes done, count me in.", style.displayName, "hi", 0.92f))
                            replies.add(SmartReplySuggestion("Haan bhai, pakka. Everything is set.", style.displayName, "hi", 0.90f))
                        } else {
                            replies.add(SmartReplySuggestion("हाँ, बिल्कुल पक्का है।", style.displayName, "hi", 0.95f))
                            replies.add(SmartReplySuggestion("हाँ, मेरी तरफ से कन्फर्म है। धन्यवाद।", style.displayName, "hi", 0.92f))
                            replies.add(SmartReplySuggestion("निश्चिंत रहें, सब तय है।", style.displayName, "hi", 0.90f))
                        }
                    }
                    "mr" -> {
                        replies.add(SmartReplySuggestion("हो नक्की, माझ्याकडून कन्फर्म आहे.", style.displayName, "mr", 0.95f))
                        replies.add(SmartReplySuggestion("होय, सर्व काही ठरल्याप्रमाणे होईल.", style.displayName, "mr", 0.92f))
                        replies.add(SmartReplySuggestion("नक्कीच, मी वेळेवर उपस्थित राहीन.", style.displayName, "mr", 0.90f))
                    }
                    else -> {
                        replies.add(SmartReplySuggestion("Yes, confirmed! Looking forward to it.", style.displayName, "en", 0.95f))
                        replies.add(SmartReplySuggestion("Confirmed on my end. All good to go.", style.displayName, "en", 0.92f))
                        replies.add(SmartReplySuggestion("Yes, absolutely. Count me in!", style.displayName, "en", 0.90f))
                        replies.add(SmartReplySuggestion("Sounds good, let's proceed with this plan.", style.displayName, "en", 0.88f))
                    }
                }
            }

            "decline" -> {
                intent = "decline"
                when (langCode) {
                    "hi" -> {
                        if (isCodeMixed) {
                            replies.add(SmartReplySuggestion("Sorry bhai, is baar nahi ho payega.", style.displayName, "hi", 0.95f))
                            replies.add(SmartReplySuggestion("I will have to pass on this. Thoda busy hoon.", style.displayName, "hi", 0.92f))
                            replies.add(SmartReplySuggestion("Unfortunately nahi jamega abhi. Thanks for asking though!", style.displayName, "hi", 0.88f))
                        } else {
                            replies.add(SmartReplySuggestion("माफ़ कीजिए, इस बार मेरे लिए संभव नहीं हो पाएगा।", style.displayName, "hi", 0.95f))
                            replies.add(SmartReplySuggestion("क्षमा करें, मैं इसमें शामिल नहीं हो सकूंगा।", style.displayName, "hi", 0.92f))
                            replies.add(SmartReplySuggestion("धन्यवाद, लेकिन फिलहाल मैं यह नहीं कर पाऊंगा।", style.displayName, "hi", 0.88f))
                        }
                    }
                    "mr" -> {
                        replies.add(SmartReplySuggestion("माफ करा, या वेळी मला जमणार नाही.", style.displayName, "mr", 0.95f))
                        replies.add(SmartReplySuggestion("क्षमस्व, सध्या मला वेळ देणे शक्य नाही.", style.displayName, "mr", 0.92f))
                    }
                    else -> {
                        replies.add(SmartReplySuggestion("Thank you for thinking of me, but I won't be able to make it.", style.displayName, "en", 0.95f))
                        replies.add(SmartReplySuggestion("Unfortunately, I have to decline this time due to prior commitments.", style.displayName, "en", 0.92f))
                        replies.add(SmartReplySuggestion("Sorry, I won't be able to participate. Appreciate you asking!", style.displayName, "en", 0.89f))
                    }
                }
            }

            "apologize" -> {
                intent = "apology"
                when (langCode) {
                    "hi" -> {
                        replies.add(SmartReplySuggestion("Really sorry for the inconvenience caused.", style.displayName, "hi", 0.95f))
                        replies.add(SmartReplySuggestion("Mujhse galti ho gayi, I apologize sincerely.", style.displayName, "hi", 0.92f))
                        replies.add(SmartReplySuggestion("Maaf karna bhai, next time dhyan rakhunga.", style.displayName, "hi", 0.89f))
                    }
                    else -> {
                        replies.add(SmartReplySuggestion("I sincerely apologize for the delay and any inconvenience.", style.displayName, "en", 0.95f))
                        replies.add(SmartReplySuggestion("My apologies. I take full responsibility for this.", style.displayName, "en", 0.92f))
                        replies.add(SmartReplySuggestion("So sorry about that! Let me make it right immediately.", style.displayName, "en", 0.88f))
                    }
                }
            }

            "clarify" -> {
                intent = "clarification"
                replies.add(SmartReplySuggestion("Could you please clarify what you mean by that?", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("Just to make sure we're on the same page, could you give more details?", style.displayName, "en", 0.92f))
                replies.add(SmartReplySuggestion("Quick question: what are the next steps on this?", style.displayName, "en", 0.89f))
            }

            "more_time" -> {
                intent = "extension"
                replies.add(SmartReplySuggestion("Could I get a little more time to review this? I'll get back to you soon.", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("Currently looking into this. May I have until tomorrow morning?", style.displayName, "en", 0.92f))
                replies.add(SmartReplySuggestion("Need some extra time to finish this properly. Thanks for your patience!", style.displayName, "en", 0.88f))
            }

            "schedule" -> {
                intent = "scheduling"
                replies.add(SmartReplySuggestion("Does tomorrow afternoon at 3 PM work for a quick call?", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("Let's schedule some time to discuss. What does your calendar look like?", style.displayName, "en", 0.92f))
                replies.add(SmartReplySuggestion("I'm free on Thursday or Friday. Let me know what suits you best.", style.displayName, "en", 0.88f))
            }

            "reschedule" -> {
                intent = "rescheduling"
                replies.add(SmartReplySuggestion("Something urgent came up. Could we please reschedule to tomorrow?", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("Apologies, I won't make our scheduled time. Can we move it to later this week?", style.displayName, "en", 0.92f))
            }

            "agree" -> {
                intent = "agreement"
                replies.add(SmartReplySuggestion("I completely agree with this approach.", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("100% aligned! Let's go ahead.", style.displayName, "en", 0.92f))
                replies.add(SmartReplySuggestion("Sounds like a great plan, I'm fully on board.", style.displayName, "en", 0.89f))
            }

            "disagree" -> {
                intent = "disagreement"
                replies.add(SmartReplySuggestion("I see your point, but I have a slightly different perspective on this.", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("I respectfully disagree. Here is what we might want to consider instead.", style.displayName, "en", 0.92f))
            }

            "suggest" -> {
                intent = "suggestion"
                replies.add(SmartReplySuggestion("What if we try this alternative instead?", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("I'd suggest we test it first and see how it performs.", style.displayName, "en", 0.92f))
            }

            "thank" -> {
                intent = "gratitude"
                replies.add(SmartReplySuggestion("Thank you so much! Really appreciate your help and support. 🙏", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("Thanks a lot, that was very kind of you!", style.displayName, "en", 0.92f))
                replies.add(SmartReplySuggestion("Much appreciated! Let me know if I can return the favor.", style.displayName, "en", 0.88f))
            }

            "congratulate" -> {
                intent = "congratulations"
                replies.add(SmartReplySuggestion("Huge congratulations! So proud of you and this milestone! 🥳🎉", style.displayName, "en", 0.95f))
                replies.add(SmartReplySuggestion("Congratulations! Well deserved success! 👏", style.displayName, "en", 0.92f))
            }
        }

        // 2. Length-specific styles
        if (replies.isEmpty() && style.structuredLength != null) {
            when (style.structuredLength) {
                "very_short" -> {
                    replies.add(SmartReplySuggestion("Got it!", style.displayName, langCode, 0.95f))
                    replies.add(SmartReplySuggestion("Sounds good.", style.displayName, langCode, 0.92f))
                    replies.add(SmartReplySuggestion("Will do.", style.displayName, langCode, 0.90f))
                    replies.add(SmartReplySuggestion("Yes, confirmed.", style.displayName, langCode, 0.88f))
                }
                "short" -> {
                    replies.add(SmartReplySuggestion("Thanks for letting me know, will follow up soon.", style.displayName, langCode, 0.95f))
                    replies.add(SmartReplySuggestion("Everything looks good from my side.", style.displayName, langCode, 0.92f))
                    replies.add(SmartReplySuggestion("I'll take care of this right away.", style.displayName, langCode, 0.88f))
                }
                "detailed", "elaborate" -> {
                    replies.add(SmartReplySuggestion("Thank you for sharing this update. I have reviewed the details and will prepare the required items by this afternoon so we stay on track.", style.displayName, langCode, 0.95f))
                    replies.add(SmartReplySuggestion("I understand the situation and agree with your recommendation. Let's touch base tomorrow morning to finalize the remaining action items.", style.displayName, langCode, 0.92f))
                }
            }
        }

        // 3. Category & Tone Specific Formats
        if (replies.isEmpty()) {
            when (style.category) {
                SmartReplyStyleCategory.PROFESSIONAL -> {
                    replies.add(SmartReplySuggestion("Thank you for the update. I will review and get back to you shortly.", style.displayName, "en", 0.95f))
                    replies.add(SmartReplySuggestion("Received with thanks. Let me know if you need any additional information.", style.displayName, "en", 0.92f))
                    replies.add(SmartReplySuggestion("I acknowledge receipt and will ensure this is handled promptly.", style.displayName, "en", 0.88f))
                }

                SmartReplyStyleCategory.POSITIVE_WARM -> {
                    replies.add(SmartReplySuggestion("Thank you so much! Wishing you a wonderful day ahead! ☀️😊", style.displayName, "en", 0.95f))
                    replies.add(SmartReplySuggestion("Aww that means so much! Really appreciate your kindness. 🥰", style.displayName, "en", 0.92f))
                    replies.add(SmartReplySuggestion("Always here for you! Sending lots of love and good vibes. 💕", style.displayName, "en", 0.88f))
                }

                SmartReplyStyleCategory.SOCIAL_FUN -> {
                    replies.add(SmartReplySuggestion("Haha that is hilarious! You made my day 😂", style.displayName, "en", 0.95f))
                    replies.add(SmartReplySuggestion("Totally on board, let's make it epic! 😎🔥", style.displayName, "en", 0.92f))
                    replies.add(SmartReplySuggestion("Oh 100%! Count me in for the fun. 🎉", style.displayName, "en", 0.88f))
                }

                SmartReplyStyleCategory.STRONG_COMMUNICATION -> {
                    replies.add(SmartReplySuggestion("I understand your position, but my decision on this is final.", style.displayName, "en", 0.95f))
                    replies.add(SmartReplySuggestion("Let's keep this strictly to the agreed facts and move forward.", style.displayName, "en", 0.92f))
                    replies.add(SmartReplySuggestion("I require this to be completed by end of day without exceptions.", style.displayName, "en", 0.88f))
                }

                SmartReplyStyleCategory.RELATIONSHIP -> {
                    replies.add(SmartReplySuggestion("I miss you so much! Can't wait to see you soon. ❤️", style.displayName, "en", 0.95f))
                    replies.add(SmartReplySuggestion("Thinking of you! Hope your day is as sweet as you are. 💕", style.displayName, "en", 0.92f))
                    replies.add(SmartReplySuggestion("You always know how to make me smile. Thank you! 🥰", style.displayName, "en", 0.88f))
                }

                else -> {
                    // Smart Match / General context
                    val isQuestion = lower.contains("?") || lower.startsWith("are") || lower.startsWith("can") || lower.startsWith("kya")
                    val isMeeting = lower.contains("meeting") || lower.contains("finalize") || lower.contains("discuss")
                    val isThanks = lower.contains("thank") || lower.contains("thx") || lower.contains("dhanyawad") || lower.contains("shukriya")

                    if (isMeeting) {
                        replies.add(SmartReplySuggestion("Yes, I'll be there. What time is the meeting?", "Smart Match", langCode, 0.95f))
                        replies.add(SmartReplySuggestion("Confirmed, looking forward to discussing the project.", "Smart Match", langCode, 0.92f))
                        replies.add(SmartReplySuggestion("I may be slightly delayed, but I'll join as soon as possible.", "Smart Match", langCode, 0.88f))
                    } else if (isThanks) {
                        replies.add(SmartReplySuggestion("You're very welcome! Happy to help 😊", "Smart Match", langCode, 0.95f))
                        replies.add(SmartReplySuggestion("Anytime! Let me know if you need anything else.", "Smart Match", langCode, 0.92f))
                    } else if (isQuestion) {
                        replies.add(SmartReplySuggestion("Yes, absolutely! Let's proceed.", "Smart Match", langCode, 0.95f))
                        replies.add(SmartReplySuggestion("Let me check and get back to you in a few minutes.", "Smart Match", langCode, 0.92f))
                        replies.add(SmartReplySuggestion("I don't think that will work right now, can we explore an alternative?", "Smart Match", langCode, 0.88f))
                    } else {
                        replies.add(SmartReplySuggestion("Sounds good, thanks for the update!", "Smart Match", langCode, 0.95f))
                        replies.add(SmartReplySuggestion("Understood, will keep you posted.", "Smart Match", langCode, 0.92f))
                        replies.add(SmartReplySuggestion("Great, talk soon!", "Smart Match", langCode, 0.88f))
                    }
                }
            }
        }

        val filtered = SmartReplyQualityFilter.filterAndValidate(replies, request.maxReplies)
        return Result.success(
            SmartReplyResponse(
                detectedLanguage = detection.displayName,
                dominantLanguage = langCode,
                isCodeMixed = isCodeMixed,
                replies = filtered,
                detectedIntent = intent,
                appliedStyle = style
            )
        )
    }
}
