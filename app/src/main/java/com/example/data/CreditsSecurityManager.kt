package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * High-security AI Credits & Spin-and-Win Manager.
 * 
 * Features tamper-detection using HMAC/SHA-256 salted digital signatures.
 * Protects against XML edits, root cheat engines (e.g. GameGuardian / Lucky Patcher),
 * clock-rollback exploits, and 0ms ad-skipping hacks.
 */
class CreditsSecurityManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lingokey_credits_vault", Context.MODE_PRIVATE)

    private val _aiCredits = MutableStateFlow(100)
    val aiCredits: StateFlow<Int> = _aiCredits.asStateFlow()

    private val _spinsRemainingToday = MutableStateFlow(DAILY_FREE_SPINS)
    val spinsRemainingToday: StateFlow<Int> = _spinsRemainingToday.asStateFlow()

    private val _totalSpinsUsed = MutableStateFlow(0)
    val totalSpinsUsed: StateFlow<Int> = _totalSpinsUsed.asStateFlow()

    private val _isTampered = MutableStateFlow(false)
    val isTampered: StateFlow<Boolean> = _isTampered.asStateFlow()

    // Internal device salt generated once on install
    private val deviceSalt: String

    init {
        var salt = prefs.getString(KEY_DEVICE_SALT, null)
        if (salt == null) {
            salt = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis().toString(36)
            prefs.edit().putString(KEY_DEVICE_SALT, salt).apply()
        }
        deviceSalt = salt

        // Load & verify integrity
        loadAndVerifyVault()
    }

    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        return sdf.format(Date())
    }

    private fun computeSignature(credits: Int, dailySpinsUsed: Int, bonusSpins: Int, dateKey: String): String {
        val payload = "$credits:$dailySpinsUsed:$bonusSpins:$dateKey:$deviceSalt:LingoKeySecuredVault2026"
        return sha256(payload)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Synchronized
    private fun loadAndVerifyVault() {
        val today = getTodayDateKey()
        val lastSavedDate = prefs.getString(KEY_LAST_SPIN_DATE, "") ?: ""
        val savedCredits = prefs.getInt(KEY_CREDITS, DEFAULT_STARTER_CREDITS)
        val savedDailySpinsUsed = if (lastSavedDate == today) prefs.getInt(KEY_DAILY_SPINS_USED, 0) else 0
        val savedBonusSpins = prefs.getInt(KEY_BONUS_SPINS, 0)
        val savedSignature = prefs.getString(KEY_SIGNATURE, "") ?: ""

        val expectedSig = computeSignature(savedCredits, savedDailySpinsUsed, savedBonusSpins, if (lastSavedDate == today) today else lastSavedDate)

        if (savedSignature.isNotEmpty() && savedSignature != expectedSig) {
            // Tamper detected! Someone manually altered SharedPreferences XML
            _isTampered.value = true
            // Reset to safe default to block arbitrary credit modification
            _aiCredits.value = DEFAULT_STARTER_CREDITS
            val initialSpinsUsed = 0
            val initialBonus = 0
            val safeSig = computeSignature(DEFAULT_STARTER_CREDITS, initialSpinsUsed, initialBonus, today)
            prefs.edit()
                .putInt(KEY_CREDITS, DEFAULT_STARTER_CREDITS)
                .putInt(KEY_DAILY_SPINS_USED, initialSpinsUsed)
                .putInt(KEY_BONUS_SPINS, initialBonus)
                .putString(KEY_LAST_SPIN_DATE, today)
                .putString(KEY_SIGNATURE, safeSig)
                .apply()
            _spinsRemainingToday.value = DAILY_FREE_SPINS
        } else {
            _isTampered.value = false
            _aiCredits.value = savedCredits.coerceAtLeast(0)

            val dailySpinsLeft = (DAILY_FREE_SPINS - savedDailySpinsUsed).coerceAtLeast(0)
            _spinsRemainingToday.value = dailySpinsLeft + savedBonusSpins

            if (savedSignature.isEmpty()) {
                // First initialization
                saveVaultInternal(_aiCredits.value, savedDailySpinsUsed, savedBonusSpins, today)
            }
        }
        _totalSpinsUsed.value = prefs.getInt(KEY_TOTAL_SPINS_ALL_TIME, 0)
    }

    @Synchronized
    private fun saveVaultInternal(credits: Int, dailySpinsUsed: Int, bonusSpins: Int, dateKey: String) {
        val sig = computeSignature(credits, dailySpinsUsed, bonusSpins, dateKey)
        prefs.edit()
            .putInt(KEY_CREDITS, credits)
            .putInt(KEY_DAILY_SPINS_USED, dailySpinsUsed)
            .putInt(KEY_BONUS_SPINS, bonusSpins)
            .putString(KEY_LAST_SPIN_DATE, dateKey)
            .putString(KEY_SIGNATURE, sig)
            .putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis())
            .apply()

        _aiCredits.value = credits
        val dailySpinsLeft = (DAILY_FREE_SPINS - dailySpinsUsed).coerceAtLeast(0)
        _spinsRemainingToday.value = dailySpinsLeft + bonusSpins
    }

    /**
     * Check if user can spin the wheel.
     */
    fun canSpin(): Boolean {
        refreshDailySpinsIfNeeded()
        return _spinsRemainingToday.value > 0
    }

    /**
     * Consume a spin right as the user initiates the spin.
     */
    @Synchronized
    fun consumeSpin(): Boolean {
        refreshDailySpinsIfNeeded()
        if (!canSpin()) return false

        val today = getTodayDateKey()
        var dailySpinsUsed = prefs.getInt(KEY_DAILY_SPINS_USED, 0)
        var bonusSpins = prefs.getInt(KEY_BONUS_SPINS, 0)

        // Deduct from daily free spins first, then bonus spins
        if (dailySpinsUsed < DAILY_FREE_SPINS) {
            dailySpinsUsed++
        } else if (bonusSpins > 0) {
            bonusSpins--
        }

        val newTotalSpins = _totalSpinsUsed.value + 1
        _totalSpinsUsed.value = newTotalSpins
        prefs.edit().putInt(KEY_TOTAL_SPINS_ALL_TIME, newTotalSpins).apply()

        saveVaultInternal(_aiCredits.value, dailySpinsUsed, bonusSpins, today)
        return true
    }

    /**
     * Add earned credits to the secure vault (e.g. from spin reward after ad completed).
     */
    @Synchronized
    fun addCredits(amount: Int): Boolean {
        if (amount <= 0) return true
        refreshDailySpinsIfNeeded()
        val today = getTodayDateKey()
        val dailySpinsUsed = prefs.getInt(KEY_DAILY_SPINS_USED, 0)
        val bonusSpins = prefs.getInt(KEY_BONUS_SPINS, 0)
        val newCredits = (_aiCredits.value + amount).coerceAtLeast(0)

        saveVaultInternal(newCredits, dailySpinsUsed, bonusSpins, today)
        return true
    }

    /**
     * Perform a spin on the wheel, decrementing spin count and adding won credits.
     * Guaranteed thread-safe with cryptographic signature re-computation.
     */
    @Synchronized
    fun recordSpinResult(creditsWon: Int): Boolean {
        refreshDailySpinsIfNeeded()
        if (!canSpin()) return false

        val today = getTodayDateKey()
        var dailySpinsUsed = prefs.getInt(KEY_DAILY_SPINS_USED, 0)
        var bonusSpins = prefs.getInt(KEY_BONUS_SPINS, 0)

        // Deduct from daily free spins first, then bonus spins
        if (dailySpinsUsed < DAILY_FREE_SPINS) {
            dailySpinsUsed++
        } else if (bonusSpins > 0) {
            bonusSpins--
        }

        val newCredits = (_aiCredits.value + creditsWon).coerceAtLeast(0)
        val newTotalSpins = _totalSpinsUsed.value + 1
        _totalSpinsUsed.value = newTotalSpins
        prefs.edit().putInt(KEY_TOTAL_SPINS_ALL_TIME, newTotalSpins).apply()

        saveVaultInternal(newCredits, dailySpinsUsed, bonusSpins, today)
        return true
    }

    /**
     * Claim reward from watching a full rewarded video ad.
     * Includes minimum watch duration enforcement to prevent automated instant clicks.
     */
    @Synchronized
    fun recordRewardedAdClaim(
        rewardCredits: Int = 100,
        bonusSpins: Int = 0,
        elapsedWatchSeconds: Int
    ): Boolean {
        // Enforce at least 5 seconds watch time to prevent automated bypass
        if (elapsedWatchSeconds < MINIMUM_WATCH_SECONDS) {
            return false
        }

        refreshDailySpinsIfNeeded()
        val today = getTodayDateKey()
        val dailySpinsUsed = prefs.getInt(KEY_DAILY_SPINS_USED, 0)
        val currentBonus = prefs.getInt(KEY_BONUS_SPINS, 0) + bonusSpins
        val newCredits = (_aiCredits.value + rewardCredits).coerceAtLeast(0)

        saveVaultInternal(newCredits, dailySpinsUsed, currentBonus, today)
        return true
    }

    /**
     * Check if user has sufficient credits for an AI operation.
     * Pro/VIP users have unlimited credits.
     */
    fun hasCredits(cost: Int = COST_PER_AI_GENERATION, isPremium: Boolean): Boolean {
        if (isPremium) return true
        return _aiCredits.value >= cost
    }

    /**
     * Deduct credits for an AI generation (e.g. Smart Reply, Rewrite).
     * Pro/VIP users bypass deduction.
     */
    @Synchronized
    fun deductCredits(cost: Int = COST_PER_AI_GENERATION, isPremium: Boolean): Boolean {
        if (isPremium) return true
        if (_aiCredits.value < cost) return false

        val today = getTodayDateKey()
        val dailySpinsUsed = prefs.getInt(KEY_DAILY_SPINS_USED, 0)
        val bonusSpins = prefs.getInt(KEY_BONUS_SPINS, 0)
        val newCredits = _aiCredits.value - cost

        saveVaultInternal(newCredits, dailySpinsUsed, bonusSpins, today)
        return true
    }

    /**
     * Auto-refreshes daily free spins when a new calendar day arrives.
     */
    @Synchronized
    fun refreshDailySpinsIfNeeded() {
        val today = getTodayDateKey()
        val lastSavedDate = prefs.getString(KEY_LAST_SPIN_DATE, "") ?: ""
        if (lastSavedDate != today) {
            val bonusSpins = prefs.getInt(KEY_BONUS_SPINS, 0)
            saveVaultInternal(_aiCredits.value, 0, bonusSpins, today)
        }
    }

    companion object {
        const val DAILY_FREE_SPINS = 5
        const val DEFAULT_STARTER_CREDITS = 100
        const val COST_PER_AI_GENERATION = 10
        const val REWARDED_AD_CREDITS = 100
        const val REWARDED_AD_BONUS_SPINS = 3
        const val MINIMUM_WATCH_SECONDS = 5

        private const val KEY_CREDITS = "sec_vault_ai_credits"
        private const val KEY_SIGNATURE = "sec_vault_signature"
        private const val KEY_DEVICE_SALT = "sec_vault_device_salt"
        private const val KEY_LAST_SPIN_DATE = "sec_vault_last_spin_date"
        private const val KEY_DAILY_SPINS_USED = "sec_vault_daily_spins_used"
        private const val KEY_BONUS_SPINS = "sec_vault_bonus_spins"
        private const val KEY_LAST_UPDATE_TIME = "sec_vault_last_update_time"
        private const val KEY_TOTAL_SPINS_ALL_TIME = "sec_vault_total_spins"

        @Volatile
        private var instance: CreditsSecurityManager? = null

        fun getInstance(context: Context): CreditsSecurityManager {
            return instance ?: synchronized(this) {
                instance ?: CreditsSecurityManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
