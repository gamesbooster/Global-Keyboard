package com.example.engine.smartreply

import com.example.data.LingoKeyPreferences

/**
 * Usage repository interface and implementation for Smart Reply.
 * Manages daily free generation quota vs. VIP unlimited access.
 */
interface SmartReplyUsageRepository {
    fun getUsageCount(): Int
    fun canGenerate(): Boolean
    fun recordSuccessfulGeneration(): Boolean
    fun isPremium(): Boolean
    fun getRemainingGenerations(): Int
}

class DefaultSmartReplyUsageRepository(
    private val preferences: LingoKeyPreferences
) : SmartReplyUsageRepository {

    companion object {
        const val FREE_DAILY_QUOTA = 10
    }

    override fun getUsageCount(): Int {
        return preferences.smartReplyUsageCount.value
    }

    override fun isPremium(): Boolean {
        return preferences.isPremiumUser.value
    }

    override fun canGenerate(): Boolean {
        if (isPremium()) return true
        return preferences.hasRemainingAiCredits()
    }

    override fun recordSuccessfulGeneration(): Boolean {
        preferences.deductCreditsForSmartReply()
        return preferences.recordSmartReplyUsage()
    }

    override fun getRemainingGenerations(): Int {
        if (isPremium()) return Int.MAX_VALUE
        return preferences.aiCredits.value / 10
    }
}
